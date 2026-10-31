import { normalizar } from "@/lib/formato"
import { auditar, db, esperar, fallar, nombreCompleto, siguienteId } from "@/mocks/store"

const usuarioPorId = (id) => db.usuarios.find((item) => item.idUsuario === Number(id))
const rolPorCodigo = (codigo) => db.roles.find((item) => item.codigo === codigo)

export const listarUsuarios = () =>
  esperar(
    db.usuarios.map((usuario) => ({
      ...usuario,
      nombreCompleto: nombreCompleto(usuario),
      rolesDetalle: usuario.roles.map(rolPorCodigo).filter(Boolean),
    }))
  )

export const listarRoles = () =>
  esperar(
    db.roles.map((rol) => ({
      ...rol,
      usuarios: db.usuarios.filter((usuario) => usuario.roles.includes(rol.codigo)).length,
    }))
  )

export const listarPermisos = () => esperar(db.permisos)

export function guardarUsuario({ valores, actor }) {
  const username = valores.username.trim().toLowerCase()
  const email = valores.email.trim().toLowerCase()
  const id = valores.idUsuario ? Number(valores.idUsuario) : null
  if (db.usuarios.some((item) => item.username === username && item.idUsuario !== id)) {
    return fallar(`El usuario ${username} ya existe.`)
  }
  if (db.usuarios.some((item) => normalizar(item.email) === normalizar(email) && item.idUsuario !== id)) {
    return fallar(`El correo ${email} ya está registrado.`)
  }
  const datos = {
    username,
    nombres: valores.nombres.trim(),
    apellidos: valores.apellidos.trim(),
    email,
    roles: valores.roles,
  }
  if (id) {
    const usuario = usuarioPorId(id)
    const quitaAdmin = usuario.roles.includes("ADMINISTRADOR") && !datos.roles.includes("ADMINISTRADOR")
    const otrosAdmins = db.usuarios.filter(
      (item) => item.idUsuario !== id && item.estado === "ACTIVO" && item.roles.includes("ADMINISTRADOR")
    )
    if (quitaAdmin && otrosAdmins.length === 0) {
      return fallar("Debe quedar al menos un administrador activo.")
    }
    Object.assign(usuario, datos)
    auditar(actor, "MODIFICAR", "Usuario", username, `Actualizó datos y roles: ${datos.roles.map((codigo) => rolPorCodigo(codigo).nombre).join(", ")}`)
    return esperar(usuario)
  }
  const usuario = {
    idUsuario: siguienteId(db.usuarios, "idUsuario"),
    ...datos,
    estado: "ACTIVO",
    ultimoAcceso: null,
  }
  db.usuarios.push(usuario)
  auditar(actor, "CREAR", "Usuario", username, `Creó la cuenta con rol ${datos.roles.map((codigo) => rolPorCodigo(codigo).nombre).join(", ")}`)
  return esperar(usuario)
}

export function cambiarEstadoUsuario({ idUsuario, actor }) {
  const usuario = usuarioPorId(idUsuario)
  const activar = usuario.estado !== "ACTIVO"
  if (!activar && nombreCompleto(usuario) === actor) {
    return fallar("No puedes desactivar tu propia cuenta.")
  }
  if (!activar && usuario.roles.includes("ADMINISTRADOR")) {
    const otrosAdmins = db.usuarios.filter(
      (item) => item.idUsuario !== usuario.idUsuario && item.estado === "ACTIVO" && item.roles.includes("ADMINISTRADOR")
    )
    if (otrosAdmins.length === 0) return fallar("Debe quedar al menos un administrador activo.")
  }
  usuario.estado = activar ? "ACTIVO" : "INACTIVO"
  auditar(actor, activar ? "ACTIVAR" : "DESACTIVAR", "Usuario", usuario.username, `${activar ? "Activó" : "Desactivó"} la cuenta del usuario`)
  return esperar(usuario)
}

export function guardarPermisosRoles({ cambios, actor }) {
  const admin = cambios.ADMINISTRADOR
  if (admin && !admin.includes("USUARIO_GESTIONAR")) {
    return fallar("El rol Administrador debe conservar el permiso de gestionar usuarios.")
  }
  Object.entries(cambios).forEach(([codigo, permisos]) => {
    const rol = rolPorCodigo(codigo)
    const agregados = permisos.filter((item) => !rol.permisos.includes(item))
    const quitados = rol.permisos.filter((item) => !permisos.includes(item))
    rol.permisos = db.permisos.map((item) => item.codigo).filter((item) => permisos.includes(item))
    const resumen = [
      agregados.length && `agregó ${agregados.join(", ")}`,
      quitados.length && `quitó ${quitados.join(", ")}`,
    ]
      .filter(Boolean)
      .join("; ")
    auditar(actor, "MODIFICAR", "Rol", rol.nombre, `Permisos: ${resumen}`)
  })
  return esperar(db.roles)
}

import { useState } from "react"
import { useQuery } from "@tanstack/react-query"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"
import { Pencil, Plus, Power, PowerOff } from "lucide-react"
import { ConfirmAction } from "@/components/shared/ConfirmAction"
import { EmptyRow } from "@/components/shared/EmptyState"
import { FieldRow, FormDialog } from "@/components/shared/FormDialog"
import { CheckboxListField, TextField } from "@/components/shared/fields"
import { EstadoBadge, StatusBadge } from "@/components/shared/StatusBadge"
import { LoadingRows, TablePanel } from "@/components/shared/TablePanel"
import { FilterSelect, SearchInput, Toolbar } from "@/components/shared/Toolbar"
import { Button } from "@/components/ui/button"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { useActor } from "@/hooks/useActor"
import { useMockMutation } from "@/hooks/useMockMutation"
import { ESTADO_USUARIO, opciones } from "@/lib/estados"
import { TODOS, pasaFiltro } from "@/lib/filtros"
import { coincide, iniciales, relativo } from "@/lib/formato"
import { cambiarEstadoUsuario, guardarUsuario, listarRoles, listarUsuarios } from "./usuariosApi"

const esquema = z.object({
  nombres: z.string().trim().min(2, "Ingresa los nombres"),
  apellidos: z.string().trim().min(2, "Ingresa los apellidos"),
  username: z
    .string()
    .trim()
    .regex(/^[a-zA-Z][a-zA-Z0-9._]{2,29}$/, "Entre 3 y 30 caracteres: letras, números, punto o guion bajo"),
  email: z.string().trim().email("Ingresa un correo válido"),
  roles: z.array(z.string()).min(1, "Asigna al menos un rol"),
})

function UsuarioDialog({ usuario, roles, onClose }) {
  const actor = useActor()
  const form = useForm({
    resolver: zodResolver(esquema),
    defaultValues: {
      nombres: usuario.nombres ?? "",
      apellidos: usuario.apellidos ?? "",
      username: usuario.username ?? "",
      email: usuario.email ?? "",
      roles: usuario.roles ?? [],
    },
  })
  const guardar = useMockMutation(guardarUsuario, {
    success: usuario.idUsuario ? "Usuario actualizado" : "Usuario creado; recibirá una contraseña temporal por correo",
    onSuccess: onClose,
  })
  return (
    <FormDialog
      open
      onOpenChange={(abierto) => !abierto && onClose()}
      title={usuario.idUsuario ? `Editar ${usuario.username}` : "Nuevo usuario"}
      description="Los permisos del usuario dependen de los roles asignados."
      form={form}
      pending={guardar.isPending}
      onSubmit={(valores) => guardar.mutate({ valores: { ...valores, idUsuario: usuario.idUsuario }, actor })}
    >
      <FieldRow>
        <TextField control={form.control} name="nombres" label="Nombres" />
        <TextField control={form.control} name="apellidos" label="Apellidos" />
      </FieldRow>
      <FieldRow>
        <TextField control={form.control} name="username" label="Usuario" autoComplete="off" />
        <TextField control={form.control} name="email" label="Correo" type="email" />
      </FieldRow>
      <CheckboxListField
        control={form.control}
        name="roles"
        label="Roles"
        options={roles.map((rol) => ({ value: rol.codigo, label: rol.nombre, description: rol.descripcion }))}
      />
    </FormDialog>
  )
}

function CambiarEstado({ usuario }) {
  const actor = useActor()
  const activo = usuario.estado === "ACTIVO"
  const cambiar = useMockMutation(cambiarEstadoUsuario, {
    success: `${usuario.username} ${activo ? "desactivado" : "activado"}`,
  })
  return (
    <ConfirmAction
      trigger={
        <Button variant="ghost" size="sm" disabled={cambiar.isPending}>
          {activo ? <PowerOff /> : <Power />}
          {activo ? "Desactivar" : "Activar"}
        </Button>
      }
      title={`${activo ? "Desactivar" : "Activar"} a ${usuario.nombreCompleto}`}
      description={
        activo
          ? "El usuario no podrá iniciar sesión. Sus registros e historial se conservan."
          : "El usuario podrá volver a iniciar sesión con sus roles actuales."
      }
      confirmLabel={activo ? "Desactivar" : "Activar"}
      destructive={activo}
      onConfirm={() => cambiar.mutate({ idUsuario: usuario.idUsuario, actor })}
    />
  )
}

export function UsuariosTab() {
  const { data = [], isPending } = useQuery({ queryKey: ["usuarios"], queryFn: listarUsuarios })
  const { data: roles = [] } = useQuery({ queryKey: ["roles"], queryFn: listarRoles })
  const [busqueda, setBusqueda] = useState("")
  const [rol, setRol] = useState(TODOS)
  const [estado, setEstado] = useState(TODOS)
  const [editando, setEditando] = useState(null)

  const filtrados = data.filter(
    (item) =>
      (rol === TODOS || item.roles.includes(rol)) &&
      pasaFiltro(estado, item.estado) &&
      coincide(busqueda, item.nombreCompleto, item.username, item.email)
  )

  return (
    <div className="space-y-4">
      <Toolbar
        actions={
          <Button onClick={() => setEditando({})} disabled={roles.length === 0}>
            <Plus />
            Nuevo usuario
          </Button>
        }
      >
        <SearchInput value={busqueda} onChange={setBusqueda} placeholder="Nombre, usuario o correo" />
        <FilterSelect label="Rol" value={rol} onChange={setRol} options={roles.map((item) => ({ value: item.codigo, label: item.nombre }))} />
        <FilterSelect label="Estado" value={estado} onChange={setEstado} options={opciones(ESTADO_USUARIO)} />
      </Toolbar>

      <TablePanel>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Usuario</TableHead>
              <TableHead>Correo</TableHead>
              <TableHead>Roles</TableHead>
              <TableHead>Último acceso</TableHead>
              <TableHead>Estado</TableHead>
              <TableHead className="text-right">Acciones</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isPending && <LoadingRows colSpan={6} />}
            {!isPending && filtrados.length === 0 && (
              <EmptyRow colSpan={6}>Ningún usuario coincide con los filtros.</EmptyRow>
            )}
            {filtrados.map((usuario) => (
              <TableRow key={usuario.idUsuario}>
                <TableCell>
                  <div className="flex items-center gap-2.5">
                    <span className="flex size-8 shrink-0 items-center justify-center rounded-full bg-secondary text-xs font-semibold text-secondary-foreground">
                      {iniciales(usuario.nombreCompleto)}
                    </span>
                    <div>
                      <div className="font-medium">{usuario.nombreCompleto}</div>
                      <div className="text-xs text-muted-foreground">{usuario.username}</div>
                    </div>
                  </div>
                </TableCell>
                <TableCell className="text-muted-foreground">{usuario.email}</TableCell>
                <TableCell>
                  <div className="flex flex-wrap gap-1">
                    {usuario.rolesDetalle.map((item) => (
                      <StatusBadge key={item.codigo}>{item.nombre}</StatusBadge>
                    ))}
                  </div>
                </TableCell>
                <TableCell className="text-muted-foreground">
                  {usuario.ultimoAcceso ? relativo(usuario.ultimoAcceso) : "Nunca"}
                </TableCell>
                <TableCell>
                  <EstadoBadge mapa={ESTADO_USUARIO} valor={usuario.estado} />
                </TableCell>
                <TableCell className="text-right">
                  <div className="flex justify-end gap-1">
                    <Button variant="ghost" size="sm" onClick={() => setEditando(usuario)}>
                      <Pencil />
                      Editar
                    </Button>
                    <CambiarEstado usuario={usuario} />
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TablePanel>

      {editando && (
        <UsuarioDialog
          key={editando.idUsuario ?? "nuevo"}
          usuario={editando}
          roles={roles}
          onClose={() => setEditando(null)}
        />
      )}
    </div>
  )
}

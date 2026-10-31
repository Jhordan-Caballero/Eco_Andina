import { useState } from "react"
import { useQuery } from "@tanstack/react-query"
import { Info, RotateCcw, Save } from "lucide-react"
import { LoadingRows, TablePanel } from "@/components/shared/TablePanel"
import { Button } from "@/components/ui/button"
import { Checkbox } from "@/components/ui/checkbox"
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
import { guardarPermisosRoles, listarPermisos, listarRoles } from "./usuariosApi"

const ABREVIATURAS = {
  ADMINISTRADOR: "Admin.",
  GENERADOR: "Generador",
  SUPERVISOR_AMBIENTAL: "Supervisor",
  ALMACENERO: "Almacenero",
  TRANSPORTISTA: "Transp.",
  GESTOR_AUTORIZADO: "Gestor",
  AUDITOR_GERENCIA: "Auditor",
}

export function RolesTab() {
  const actor = useActor()
  const { data: roles = [], isPending } = useQuery({ queryKey: ["roles"], queryFn: listarRoles })
  const { data: permisos = [] } = useQuery({ queryKey: ["permisos"], queryFn: listarPermisos })
  const [borrador, setBorrador] = useState({})
  const guardar = useMockMutation(guardarPermisosRoles, {
    success: "Permisos actualizados. Se aplicarán en el próximo inicio de sesión de cada usuario.",
    onSuccess: () => setBorrador({}),
  })

  const permisosDe = (rol) => borrador[rol.codigo] ?? rol.permisos
  const alternar = (rol, codigo, marcado) => {
    const actuales = permisosDe(rol)
    const siguientes = marcado ? [...actuales, codigo] : actuales.filter((item) => item !== codigo)
    const original = roles.find((item) => item.codigo === rol.codigo).permisos
    const igual = siguientes.length === original.length && siguientes.every((item) => original.includes(item))
    setBorrador((previo) => {
      const copia = { ...previo }
      if (igual) delete copia[rol.codigo]
      else copia[rol.codigo] = siguientes
      return copia
    })
  }
  const cambios = Object.keys(borrador).length

  return (
    <div className="space-y-4">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <p className="flex items-start gap-2 text-sm text-muted-foreground">
          <Info className="mt-0.5 size-4 shrink-0" />
          Marca los permisos de cada rol. Cada permiso corresponde a un caso de uso del sistema.
        </p>
        <div className="flex shrink-0 gap-2">
          <Button variant="outline" onClick={() => setBorrador({})} disabled={cambios === 0}>
            <RotateCcw />
            Descartar
          </Button>
          <Button onClick={() => guardar.mutate({ cambios: borrador, actor })} disabled={cambios === 0 || guardar.isPending}>
            <Save />
            Guardar {cambios > 0 && `(${cambios} ${cambios === 1 ? "rol" : "roles"})`}
          </Button>
        </div>
      </div>

      <TablePanel>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="min-w-64">Permiso</TableHead>
              <TableHead>CU</TableHead>
              {roles.map((rol) => (
                <TableHead key={rol.codigo} className="text-center" title={rol.nombre}>
                  <div>{ABREVIATURAS[rol.codigo] ?? rol.nombre}</div>
                  <div className="text-[0.68rem] font-normal text-muted-foreground">
                    {rol.usuarios} {rol.usuarios === 1 ? "usuario" : "usuarios"}
                  </div>
                </TableHead>
              ))}
            </TableRow>
          </TableHeader>
          <TableBody>
            {isPending && <LoadingRows colSpan={9} rows={8} />}
            {permisos.map((permiso) => (
              <TableRow key={permiso.codigo}>
                <TableCell className="whitespace-normal">
                  <div className="font-medium">{permiso.descripcion}</div>
                  <div className="text-xs text-muted-foreground">{permiso.codigo}</div>
                </TableCell>
                <TableCell className="text-xs text-muted-foreground tabular-nums">{permiso.casoUso}</TableCell>
                {roles.map((rol) => {
                  const marcado = permisosDe(rol).includes(permiso.codigo)
                  const modificado = Boolean(borrador[rol.codigo]) && marcado !== rol.permisos.includes(permiso.codigo)
                  return (
                    <TableCell key={rol.codigo} className={modificado ? "bg-warning/10 text-center" : "text-center"}>
                      <Checkbox
                        checked={marcado}
                        onCheckedChange={(valor) => alternar(rol, permiso.codigo, valor === true)}
                        aria-label={`${permiso.descripcion} para ${rol.nombre}`}
                      />
                    </TableCell>
                  )
                })}
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TablePanel>
    </div>
  )
}

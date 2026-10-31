import { useState } from "react"
import { useQuery } from "@tanstack/react-query"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"
import { Pencil, Plus, Truck } from "lucide-react"
import { EmptyState } from "@/components/shared/EmptyState"
import { FieldRow, FormDialog } from "@/components/shared/FormDialog"
import { NumberField, SelectField, TextField } from "@/components/shared/fields"
import { EstadoBadge } from "@/components/shared/StatusBadge"
import { FilterSelect, SearchInput, Toolbar } from "@/components/shared/Toolbar"
import { Button } from "@/components/ui/button"
import { useAuth } from "@/features/auth/AuthContext"
import { useActor } from "@/hooks/useActor"
import { useMockMutation } from "@/hooks/useMockMutation"
import { ESTADO_VEHICULO, ESTADO_VIGENCIA, opciones } from "@/lib/estados"
import { TODOS, pasaFiltro } from "@/lib/filtros"
import { cantidad, coincide } from "@/lib/formato"
import { guardarTransportista, guardarVehiculo, listarTransportistas } from "./transportistasApi"
import { Vigencia } from "./Vigencia"

const esquemaTransportista = z.object({
  empresa: z.string().trim().min(3, "Ingresa la razón social"),
  autorizacion: z.string().trim().min(6, "Ingresa el número de registro EO-RS"),
  contacto: z.string().trim().min(5, "Indica el contacto y teléfono"),
  vigencia: z.string().min(1, "Indica la fecha de vencimiento"),
})

function TransportistaDialog({ transportista, onClose }) {
  const actor = useActor()
  const form = useForm({
    resolver: zodResolver(esquemaTransportista),
    defaultValues: {
      empresa: transportista.empresa ?? "",
      autorizacion: transportista.autorizacion ?? "",
      contacto: transportista.contacto ?? "",
      vigencia: transportista.vigencia ?? "",
    },
  })
  const guardar = useMockMutation(guardarTransportista, {
    success: transportista.idTransportista ? "Transportista actualizado" : "Transportista registrado",
    onSuccess: onClose,
  })
  return (
    <FormDialog
      open
      onOpenChange={(abierto) => !abierto && onClose()}
      title={transportista.idTransportista ? `Editar ${transportista.empresa}` : "Nuevo transportista"}
      description="Solo se podrán asignar recolecciones mientras la autorización esté vigente."
      form={form}
      pending={guardar.isPending}
      onSubmit={(valores) =>
        guardar.mutate({ valores: { ...valores, idTransportista: transportista.idTransportista }, actor })
      }
    >
      <TextField control={form.control} name="empresa" label="Razón social" />
      <FieldRow>
        <TextField control={form.control} name="autorizacion" label="Registro EO-RS" placeholder="EO-RS-00000-2026-MINAM" />
        <TextField control={form.control} name="vigencia" label="Vigencia hasta" type="date" />
      </FieldRow>
      <TextField control={form.control} name="contacto" label="Contacto" placeholder="Nombre · teléfono" />
    </FormDialog>
  )
}

const esquemaVehiculo = z.object({
  placa: z
    .string()
    .trim()
    .regex(/^[A-Za-z0-9]{3}-[A-Za-z0-9]{3}$/, "Usa el formato ABC-123"),
  tipo: z.string().trim().min(3, "Describe el tipo de vehículo"),
  capacidad: z.coerce.number({ error: "Ingresa la capacidad" }).positive("La capacidad debe ser mayor a 0"),
  estado: z.string().min(1, "Selecciona el estado"),
})

function VehiculoDialog({ transportista, vehiculo, onClose }) {
  const actor = useActor()
  const form = useForm({
    resolver: zodResolver(esquemaVehiculo),
    defaultValues: {
      placa: vehiculo.placa ?? "",
      tipo: vehiculo.tipo ?? "",
      capacidad: vehiculo.capacidad ?? "",
      estado: vehiculo.estado ?? "OPERATIVO",
    },
  })
  const guardar = useMockMutation(guardarVehiculo, {
    success: vehiculo.idVehiculo ? "Vehículo actualizado" : "Vehículo registrado",
    onSuccess: onClose,
  })
  return (
    <FormDialog
      open
      onOpenChange={(abierto) => !abierto && onClose()}
      title={vehiculo.idVehiculo ? `Editar ${vehiculo.placa}` : "Nuevo vehículo"}
      description={transportista.empresa}
      form={form}
      pending={guardar.isPending}
      onSubmit={(valores) =>
        guardar.mutate({
          idTransportista: transportista.idTransportista,
          valores: { ...valores, idVehiculo: vehiculo.idVehiculo },
          actor,
        })
      }
    >
      <FieldRow>
        <TextField control={form.control} name="placa" label="Placa" placeholder="ABC-123" />
        <SelectField control={form.control} name="estado" label="Estado" options={opciones(ESTADO_VEHICULO)} />
      </FieldRow>
      <FieldRow>
        <TextField control={form.control} name="tipo" label="Tipo" placeholder="Ej. Camión furgón" />
        <NumberField control={form.control} name="capacidad" label="Capacidad de carga" suffix="kg" />
      </FieldRow>
    </FormDialog>
  )
}

function TransportistaCard({ transportista, puedeGestionar, onEditar, onVehiculo }) {
  return (
    <article className="flex flex-col rounded-xl bg-card ring-1 ring-foreground/10">
      <div className="flex items-start justify-between gap-3 p-4">
        <div className="min-w-0 space-y-1">
          <h3 className="font-heading text-lg leading-tight font-semibold">{transportista.empresa}</h3>
          <p className="text-xs text-muted-foreground tabular-nums">{transportista.autorizacion}</p>
          <p className="text-xs text-muted-foreground">{transportista.contacto}</p>
        </div>
        {puedeGestionar && (
          <Button variant="ghost" size="icon-sm" aria-label={`Editar ${transportista.empresa}`} onClick={onEditar}>
            <Pencil />
          </Button>
        )}
      </div>
      <div className="flex flex-wrap items-center justify-between gap-2 border-y px-4 py-2.5">
        <Vigencia valor={transportista.vigencia} estado={transportista.estadoVigencia} />
        <span className="text-xs text-muted-foreground">
          {transportista.ordenesActivas} activas · {transportista.ordenesCerradas} cerradas
        </span>
      </div>
      <ul className="flex-1 divide-y">
        {transportista.vehiculos.map((vehiculo) => (
          <li key={vehiculo.idVehiculo} className="flex items-center gap-3 px-4 py-2.5 text-sm">
            <Truck className="size-4 shrink-0 text-muted-foreground" />
            <div className="min-w-0 flex-1">
              <p className="font-medium tabular-nums">{vehiculo.placa}</p>
              <p className="truncate text-xs text-muted-foreground">
                {vehiculo.tipo} · {cantidad(vehiculo.capacidad, "kg")}
              </p>
            </div>
            <EstadoBadge mapa={ESTADO_VEHICULO} valor={vehiculo.estado} />
            {puedeGestionar && (
              <Button
                variant="ghost"
                size="icon-xs"
                aria-label={`Editar ${vehiculo.placa}`}
                onClick={() => onVehiculo(vehiculo)}
              >
                <Pencil />
              </Button>
            )}
          </li>
        ))}
        {transportista.vehiculos.length === 0 && (
          <li className="px-4 py-3 text-sm text-muted-foreground">Sin vehículos registrados.</li>
        )}
      </ul>
      {puedeGestionar && (
        <div className="border-t p-2">
          <Button variant="ghost" size="sm" className="w-full" onClick={() => onVehiculo({})}>
            <Plus />
            Agregar vehículo
          </Button>
        </div>
      )}
    </article>
  )
}

export function TransportistasTab() {
  const { hasPermission } = useAuth()
  const puedeGestionar = hasPermission("TRANSPORTISTA_GESTIONAR")
  const { data = [], isPending } = useQuery({ queryKey: ["transportistas"], queryFn: listarTransportistas })
  const [busqueda, setBusqueda] = useState("")
  const [vigencia, setVigencia] = useState(TODOS)
  const [editando, setEditando] = useState(null)
  const [vehiculo, setVehiculo] = useState(null)

  const filtrados = data.filter(
    (item) =>
      pasaFiltro(vigencia, item.estadoVigencia) &&
      coincide(busqueda, item.empresa, item.autorizacion, ...item.vehiculos.map((v) => v.placa))
  )

  return (
    <div className="space-y-4">
      <Toolbar
        actions={
          puedeGestionar && (
            <Button onClick={() => setEditando({})}>
              <Plus />
              Nuevo transportista
            </Button>
          )
        }
      >
        <SearchInput value={busqueda} onChange={setBusqueda} placeholder="Empresa, EO-RS o placa" />
        <FilterSelect label="Vigencia" value={vigencia} onChange={setVigencia} options={opciones(ESTADO_VIGENCIA)} todos="Todas" />
      </Toolbar>

      {isPending ? (
        <div className="grid gap-3 lg:grid-cols-2 2xl:grid-cols-3">
          {[0, 1, 2].map((index) => (
            <div key={index} className="h-64 animate-pulse rounded-xl bg-muted" />
          ))}
        </div>
      ) : filtrados.length === 0 ? (
        <div className="rounded-xl bg-card ring-1 ring-foreground/10">
          <EmptyState icon={Truck}>Ningún transportista coincide con los filtros.</EmptyState>
        </div>
      ) : (
        <div className="grid items-start gap-3 lg:grid-cols-2 2xl:grid-cols-3">
          {filtrados.map((transportista) => (
            <TransportistaCard
              key={transportista.idTransportista}
              transportista={transportista}
              puedeGestionar={puedeGestionar}
              onEditar={() => setEditando(transportista)}
              onVehiculo={(item) => setVehiculo({ transportista, vehiculo: item })}
            />
          ))}
        </div>
      )}

      {editando && (
        <TransportistaDialog
          key={editando.idTransportista ?? "nuevo"}
          transportista={editando}
          onClose={() => setEditando(null)}
        />
      )}
      {vehiculo && (
        <VehiculoDialog
          transportista={vehiculo.transportista}
          vehiculo={vehiculo.vehiculo}
          onClose={() => setVehiculo(null)}
        />
      )}
    </div>
  )
}

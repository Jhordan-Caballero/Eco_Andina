import { Route, Routes } from 'react-router-dom'
import AppLayout from '@/components/layout/AppLayout'
import { NAV_ITEMS } from '@/components/layout/navigation'
import AlmacenamientoPage from '@/features/almacenamiento/AlmacenamientoPage'
import AuditoriaPage from '@/features/auditoria/AuditoriaPage'
import LoginPage from '@/features/auth/LoginPage'
import { ProtectedRoute, RequirePermission } from '@/features/auth/ProtectedRoute'
import CatalogosPage from '@/features/catalogos/CatalogosPage'
import DashboardPage from '@/features/dashboard/DashboardPage'
import DocumentosPage from '@/features/documentos/DocumentosPage'
import GeneracionPage from '@/features/generacion/GeneracionPage'
import IncidenciasPage from '@/features/incidencias/IncidenciasPage'
import NotFoundPage from '@/features/placeholders/NotFoundPage'
import RecoleccionesPage from '@/features/recolecciones/RecoleccionesPage'
import ReportesPage from '@/features/reportes/ReportesPage'
import TransportistasPage from '@/features/transportistas/TransportistasPage'
import UsuariosPage from '@/features/usuarios/UsuariosPage'

const PAGINAS = {
  '/generacion': GeneracionPage,
  '/almacenamiento': AlmacenamientoPage,
  '/recolecciones': RecoleccionesPage,
  '/incidencias': IncidenciasPage,
  '/documentos': DocumentosPage,
  '/catalogos': CatalogosPage,
  '/transportistas-gestores': TransportistasPage,
  '/reportes': ReportesPage,
  '/usuarios': UsuariosPage,
  '/auditoria': AuditoriaPage,
}

const MODULOS = NAV_ITEMS.filter((item) => item.permisos && PAGINAS[item.to])

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route index element={<DashboardPage />} />
          {MODULOS.map(({ to, permisos }) => {
            const Pagina = PAGINAS[to]
            return (
              <Route
                key={to}
                path={to}
                element={
                  <RequirePermission permisos={permisos}>
                    <Pagina />
                  </RequirePermission>
                }
              />
            )
          })}
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Route>
    </Routes>
  )
}

export default App

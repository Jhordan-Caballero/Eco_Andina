import { Route, Routes } from 'react-router-dom'
import AppLayout from '@/components/layout/AppLayout'
import { NAV_ITEMS } from '@/components/layout/navigation'
import LoginPage from '@/features/auth/LoginPage'
import { ProtectedRoute, RequirePermission } from '@/features/auth/ProtectedRoute'
import DashboardPage from '@/features/dashboard/DashboardPage'
import ModuloPendiente from '@/features/placeholders/ModuloPendiente'
import NotFoundPage from '@/features/placeholders/NotFoundPage'

const MODULOS = NAV_ITEMS.filter((item) => item.permission)

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route index element={<DashboardPage />} />
          {MODULOS.map(({ to, permission, label }) => (
            <Route
              key={to}
              path={to}
              element={
                <RequirePermission permission={permission}>
                  <ModuloPendiente nombre={label} />
                </RequirePermission>
              }
            />
          ))}
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Route>
    </Routes>
  )
}

export default App

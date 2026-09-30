import { Routes, Route } from 'react-router-dom'
import {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
} from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import LoginPage from '@/features/auth/LoginPage'
import { ProtectedRoute } from '@/features/auth/ProtectedRoute'

function Home() {
  return (
    <div className="flex min-h-svh items-center justify-center p-6">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle>EcoAndina</CardTitle>
          <CardDescription>
            Sistema de gestión de residuos industriales
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Button>Comenzar</Button>
        </CardContent>
      </Card>
    </div>
  )
}

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route path="/" element={<Home />} />
      </Route>
    </Routes>
  )
}

export default App

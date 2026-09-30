import { useState } from "react"
import { Navigate, useLocation } from "react-router-dom"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"
import { useMutation } from "@tanstack/react-query"
import { Eye, EyeOff, LoaderCircle, TriangleAlert } from "lucide-react"
import { BrandLockup } from "@/components/brand/Logo"
import { Button } from "@/components/ui/button"
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form"
import { Input } from "@/components/ui/input"
import { getErrorMessage } from "@/lib/api"
import { useAuth } from "./AuthContext"
import { AuroraPanel } from "./AuroraPanel"
import { FullScreenLoader } from "./ProtectedRoute"

const loginSchema = z.object({
  username: z.string().trim().min(1, "Ingresa tu usuario o correo"),
  password: z.string().min(1, "Ingresa tu contraseña"),
})

export default function LoginPage() {
  const { status, signIn } = useAuth()
  const location = useLocation()
  const [showPassword, setShowPassword] = useState(false)

  const form = useForm({
    resolver: zodResolver(loginSchema),
    defaultValues: { username: "", password: "" },
  })

  const login = useMutation({ mutationFn: signIn })

  if (status === "loading") return <FullScreenLoader />
  if (status === "authenticated") {
    return <Navigate to={location.state?.from?.pathname ?? "/"} replace />
  }

  return (
    <div className="grid min-h-svh lg:grid-cols-2">
      <div className="flex flex-col gap-8 px-6 py-8 sm:px-10 lg:px-16">
        <BrandLockup />

        <div className="flex flex-1 items-center">
          <div className="mx-auto w-full max-w-sm">
            <div className="mb-8 space-y-2">
              <h1 className="font-heading text-4xl font-semibold tracking-tight">
                Iniciar sesión
              </h1>
              <p className="text-sm text-muted-foreground">
                Ingresa con tu usuario o correo para acceder al seguimiento de
                residuos de la planta.
              </p>
            </div>

            <Form {...form}>
              <form
                noValidate
                className="space-y-5"
                onSubmit={form.handleSubmit((values) => login.mutate(values))}
              >
                {login.isError && (
                  <div
                    role="alert"
                    className="flex items-start gap-2 rounded-lg border border-destructive/30 bg-destructive/10 px-3 py-2.5 text-sm text-destructive"
                  >
                    <TriangleAlert className="mt-0.5 size-4 shrink-0" />
                    <span>{getErrorMessage(login.error)}</span>
                  </div>
                )}

                <FormField
                  control={form.control}
                  name="username"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Usuario o correo</FormLabel>
                      <FormControl>
                        <Input
                          autoFocus
                          autoComplete="username"
                          placeholder="usuario@ecoandina.pe"
                          className="h-10"
                          {...field}
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="password"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Contraseña</FormLabel>
                      <div className="relative">
                        <FormControl>
                          <Input
                            type={showPassword ? "text" : "password"}
                            autoComplete="current-password"
                            className="h-10 pr-10"
                            {...field}
                          />
                        </FormControl>
                        <Button
                          type="button"
                          variant="ghost"
                          size="icon-sm"
                          className="absolute top-1/2 right-1.5 -translate-y-1/2 text-muted-foreground"
                          aria-label={
                            showPassword ? "Ocultar contraseña" : "Mostrar contraseña"
                          }
                          aria-pressed={showPassword}
                          onClick={() => setShowPassword((visible) => !visible)}
                        >
                          {showPassword ? <EyeOff /> : <Eye />}
                        </Button>
                      </div>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <Button
                  type="submit"
                  size="lg"
                  className="h-10 w-full text-sm"
                  disabled={login.isPending}
                >
                  {login.isPending ? (
                    <>
                      <LoaderCircle className="animate-spin" />
                      Ingresando…
                    </>
                  ) : (
                    "Iniciar sesión"
                  )}
                </Button>
              </form>
            </Form>
          </div>
        </div>

        <p className="text-xs text-muted-foreground">
          Planta industrial EcoAndina · Acceso interno
        </p>
      </div>

      <AuroraPanel className="hidden lg:flex" />
    </div>
  )
}

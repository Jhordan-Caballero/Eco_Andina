# EcoAndina — Frontend

Interfaz web de la plataforma de trazabilidad de residuos industriales (React + Vite).

## Stack

- React 19, Vite, React Router
- Tailwind CSS v4 + shadcn/ui (base Radix, preset Nova) — componentes en `src/components/ui`
- React Hook Form + Zod (formularios y validación)
- TanStack Query (estado del servidor) + Axios (API REST)

## Ejecutar

```bash
npm install
npm run dev
```

Abre `http://localhost:5173`. Necesita el backend en `http://localhost:8082` (ver el README de la raíz).
Para apuntar a otra URL, copia `.env.example` como `.env.local` y ajusta `VITE_API_URL`.

## Estructura

```
src/
├── components/
│   ├── brand/      # Logo y lockup de EcoAndina
│   ├── layout/     # Shell del panel (sidebar, barra superior) y menú de navegación
│   └── ui/         # Componentes shadcn/ui
├── features/
│   ├── auth/       # Login, sesión (AuthContext), rutas protegidas y por permiso
│   ├── dashboard/  # Panel general (datos de ejemplo por ahora)
│   └── placeholders/
└── lib/            # Cliente Axios (token JWT) y helpers
```

## Sesión y permisos

`POST /api/auth/login` devuelve un JWT y el usuario con sus `roles` y `permisos`. El token se guarda en
`localStorage` y Axios lo envía como `Authorization: Bearer`. El menú lateral y las rutas de cada módulo se
filtran según `user.permisos` (ver `src/components/layout/navigation.js`).

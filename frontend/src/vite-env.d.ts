/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** Override the API base URL (defaults to /api, proxied to Spring Boot in dev). */
  readonly VITE_API_BASE_URL?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

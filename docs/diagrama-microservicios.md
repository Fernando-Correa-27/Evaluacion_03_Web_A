# Arquitectura del inventario

```mermaid
flowchart LR
  UI[Frontend React :5173] -->|HTTP /api| GW[API Gateway :8080]
  GW -->|Eureka lb://| PS[proveedor-service :8081]
  GW -->|Eureka lb://| PR[producto-service :8082]
  GW -->|Eureka lb://| MS[movimiento-service :8083]
  PR -->|REST + Feign| PS
  MS -->|REST + Feign| PR
  PS --> DB1[(MySQL bd_proveedor)]
  PR --> DB2[(MySQL bd_producto)]
  MS --> DB3[(MySQL bd_movimiento)]
  CFG[Config Server :8888] -. YAML por perfil .-> GW
  CFG -. YAML por perfil .-> PS
  CFG -. YAML por perfil .-> PR
  CFG -. YAML por perfil .-> MS
  ES[Eureka Server :8761] -. registro y descubrimiento .-> GW
  ES -. registro y descubrimiento .-> PS
  ES -. registro y descubrimiento .-> PR
  ES -. registro y descubrimiento .-> MS
```

| Servicio | Puerto | Responsabilidad | Datos propios |
|---|---:|---|---|
| config-server | 8888 | YAML común y perfiles `dev`, `test`, `prod` | Ninguno |
| eureka-server | 8761 | Registro y descubrimiento | Ninguno |
| api-gateway | 8080 | Punto de entrada, rutas y CORS | Ninguno |
| proveedor-service | 8081 | CRUD y baja lógica de proveedores | `bd_proveedor` |
| producto-service | 8082 | Catálogo y validación REST del proveedor | `bd_producto` |
| movimiento-service | 8083 | Entradas, salidas, historial y stock calculado | `bd_movimiento` |
| frontend | 5173 | Panel administrativo React | Ninguno |

Las referencias entre bases son identificadores lógicos. Solo el servicio dueño consulta su base. El stock se calcula como entradas menos salidas.

# Arquitectura del inventario

```mermaid
flowchart TB
  UI[Frontend React :5173] -->|HTTP /api| GW[API Gateway :8080]

  subgraph DOMAIN[Servicios de dominio]
    direction LR
    PS[proveedor-service :8081]
    PR[producto-service :8082]
    MS[movimiento-service :8083]
    PS --> DB1[(MySQL bd_proveedor)]
    PR --> DB2[(MySQL bd_producto)]
    MS --> DB3[(MySQL bd_movimiento)]
    PR -->|REST + Feign| PS
    MS -->|REST + Feign| PR
  end

  GW -->|Eureka lb://| PS
  GW -->|Eureka lb://| PR
  GW -->|Eureka lb://| MS

  subgraph PLATFORM[Infraestructura]
    direction LR
    CFG[Config Server :8888]
    ES[Eureka Server :8761]
  end

  CFG -. YAML por perfil .-> DOMAIN
  ES -. registro y descubrimiento .-> DOMAIN
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

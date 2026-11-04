# Inventario de almacén · Evaluación N.º 03

## 1. Datos

- Estudiante: Correa Huincho Fernando Luis
- Sección:
- Curso: Desarrollo de Aplicaciones Web Avanzado (Microservicios)
- Laboratorio: Laboratorio 07 — Gestión de inventario de almacén
- Docente: Ricardo Coello Palomino

## 2. Descripción y alcance

Sistema de inventario con catálogo de proveedores, catálogo de productos y registro inmutable de entradas y salidas. El alcance implementa CRUD de productos/proveedores, validación de referencias, filtro de listados, stock calculado y un panel web. No implementa citas médicas.

## 3. Arquitectura

El [diagrama Mermaid](docs/diagrama-microservicios.md) muestra siete procesos: tres servicios de dominio, Eureka, Config Server, Gateway y frontend. El navegador solo llama al Gateway. Producto consulta Proveedor por REST/Feign; Movimiento consulta Producto. Cada dominio accede a su única base, sin tablas compartidas ni claves foráneas entre bases. La separación mantiene el historial de movimientos independiente del catálogo.

## 4. Tecnologías y versiones

- Java 25 (el proyecto requiere Java 17 o superior).
- Spring Boot 4.1.2 y Spring Cloud 2025.1.4 (Oakwood), alineados según la [tabla oficial de compatibilidad de Spring Cloud](https://spring.io/projects/spring-cloud/).
- Maven Wrapper generado por Spring Initializr; Spring Data JPA, Hibernate, MySQL Connector/J, Jakarta Validation, Actuator, DevTools, Eureka, Config Server, OpenFeign y Gateway WebFlux.
- React 19, Vite 8, React Router y Axios.
- El identificador del starter Gateway reciente es `spring-cloud-starter-gateway-server-webflux`, según la [documentación oficial](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webflux/starter.html).

## 5. Requisitos previos

- JDK 17+ (`java -version`); se construyó con JDK 25.
- Node.js 20+ y npm (`node -v`, `npm -v`); se preparó con Node 24.
- MySQL 8.4 ejecutándose en `localhost:3306`.
- Puertos libres: 8080, 8081, 8082, 8083, 8761, 8888 y 5173.
- Git es recomendado.

## 6. Puertos y bases de datos

| Proceso | Puerto | Base |
|---|---:|---|
| api-gateway | 8080 | — |
| proveedor-service | 8081 | `bd_proveedor` |
| producto-service | 8082 | `bd_producto` |
| movimiento-service | 8083 | `bd_movimiento` |
| eureka-server | 8761 | — |
| config-server | 8888 | — |
| frontend | 5173 | — |

## 7. Variables de entorno

| Variable | Uso |
|---|---|
| `DB_USERNAME` | Usuario MySQL del entorno local |
| `DB_PASSWORD` | Contraseña MySQL del entorno local; se mantiene fuera de Git |
| `DB_URL` | URL JDBC opcional para prod; por defecto usa la base del servicio |
| `SPRING_PROFILES_ACTIVE` | Perfil de configuración (`dev`, `test` o `prod`) |
| `VITE_API_URL` | URL del Gateway en el frontend; valor predeterminado `http://localhost:8080` |

Copia `.env.example` como referencia. Spring Boot no carga `.env` automáticamente: define `DB_USERNAME` y `DB_PASSWORD` en la sesión de PowerShell que ejecutará los servicios. No agregues valores reales a archivos versionados.

## 8. Crear las bases

En MySQL Workbench o MySQL Command Line Client, ejecuta en orden `database/01-crear-bases.sql`, `02-esquema-proveedor.sql`, `03-esquema-producto.sql` y `04-esquema-movimiento.sql`. El script inicial crea las tres bases. El usuario `almacen_app` es opcional; antes de ejecutar ese bloque, reemplaza `CAMBIAR_ESTA_CLAVE` por una clave propia y guárdala como variable de entorno, no en Git. Si solo se crean las bases, configura las credenciales de un usuario MySQL que tenga permisos sobre ellas.

Las tablas SQL se escribieron para coincidir con las entidades y restricciones. **La comprobación con `SHOW CREATE TABLE` y `ddl-auto: validate` sigue pendiente**, porque MySQL no respondió en el equipo durante esta ejecución. Antes de entregar, verifica los tipos reales y deja la salida en `docs/evidencias/bases-de-datos.txt`.

## 9. Ejecución en Windows

Desde PowerShell en la raíz del proyecto, configura credenciales en la sesión (no las guardes en el repositorio):

```powershell
$env:DB_USERNAME = '<usuario MySQL>'
$env:DB_PASSWORD = '<contraseña MySQL>'
$env:SPRING_PROFILES_ACTIVE = 'dev'
```

Después crea bases y arranca todos los procesos:

```powershell
.\mvnw.cmd -DskipTests package
.\scripts\start-all.ps1 -SkipBuild
```

Abre `http://localhost:5173`; Eureka está en `http://localhost:8761`. Para cargar datos de muestra, ejecuta `.\scripts\seed-demo.ps1`. Para detener los procesos iniciados por el script, ejecuta `.\scripts\stop-all.ps1`. Los logs quedan en `logs/` (ignorados por Git).

Los procesos Java se levantan en segundo plano y cada uno debe responder `/actuator/health` con UP. El script comprueba los puertos antes de iniciar y no detiene procesos preexistentes.

## 10. Pruebas

```powershell
.\mvnw.cmd test
.\scripts\smoke-test.ps1
npx newman run docs/postman/Inventario-Almacen.postman_collection.json -e docs/postman/Inventario-Local.postman_environment.json
cd frontend
npm run build
```

Las pruebas unitarias de los servicios usan Mockito y no necesitan MySQL. La prueba de humo y Newman sí necesitan MySQL, Eureka y los servicios arriba. El build de frontend se ejecutó correctamente. En la última ejecución, `mvnw test` terminó en BUILD SUCCESS con 15 pruebas; el resumen queda en `docs/evidencias/mvn-test.txt`.

## 11. Perfiles `dev`, `test` y `prod` (A-69)

Los YAML se sirven desde Config Server en `config-server/src/main/resources/config-data/`.

| Propiedad | dev | test | prod |
|---|---|---|---|
| Persistencia | MySQL 8.4 local, base del servicio | Bases MySQL aisladas `*_test` | MySQL por `DB_URL` o URL predeterminada |
| Credenciales | `DB_USERNAME`, `DB_PASSWORD` | `DB_USERNAME`, `DB_PASSWORD` | Variables de entorno obligatorias |
| `ddl-auto` | `update` | `create-drop` | `validate` |
| Uso | Desarrollo y datos locales | Pruebas aisladas | Entorno preparado con esquema creado |

Gateway usa CORS para el origen `http://localhost:5173`. Los servicios no añaden CORS.

## 12. Endpoints

Los ejemplos usan `http://localhost:8080`. Las mismas rutas están disponibles directamente en los puertos del servicio.

| Servicio | Rutas |
|---|---|
| Proveedor | `GET/POST /api/proveedores`, `GET/PUT/DELETE /api/proveedores/{id}` |
| Producto | `GET/POST /api/productos`, `GET/PUT/DELETE /api/productos/{id}` |
| Movimiento | `POST /api/movimientos`, `GET /api/movimientos`, `GET /api/movimientos/{id}`, `GET /api/movimientos/producto/{productoId}`, `GET /api/movimientos/producto/{productoId}/stock`, `GET /api/movimientos/stock` |

Ejemplo de movimiento:

```json
{
  "productoId": 1,
  "tipo": "ENTRADA",
  "cantidad": 50,
  "fecha": "2026-11-04T10:30:00",
  "observacion": "Compra inicial"
}
```

El campo `fecha` es opcional; si se omite el servicio asigna la hora local. La cantidad debe ser mayor que cero.

| HTTP | Significado |
|---:|---|
| 201 | Creado; incluye cabecera `Location` |
| 200 | Consulta o actualización correcta |
| 204 | Baja lógica correcta |
| 400 | DTO, tipo, filtro o cantidad inválidos; incluye errores por campo cuando aplica |
| 404 | Recurso o referencia inexistente |
| 409 | Código/documento duplicado |
| 422 | Regla de negocio (inactivo o stock insuficiente) |
| 503 | Servicio remoto no disponible |

## 13. Decisiones de diseño

- Stock calculado como entradas menos salidas; no se almacena un contador adicional.
- Proveedor y producto se desactivan con baja lógica; listados muestran activos por defecto y aceptan `?activo=false`.
- Los movimientos son inmutables. Una corrección se registra con un movimiento contrario y observación.
- Las referencias remotas usan IDs lógicos y REST/Feign, sin relación JPA ni clave foránea entre servicios.
- La API devuelve 404 cuando la referencia consultada no existe; una referencia inactiva o saldo insuficiente devuelve 422.
- MySQL refuerza las reglas de cantidad y valores no negativos con `CHECK`.

## 14. Limitaciones y mejoras futuras

No se implementa autenticación. La lectura y escritura simultánea no usa bloqueo de saldo; dos salidas concurrentes pueden leer el mismo stock. No hay transacciones distribuidas entre servicios ni circuit breaker. La prueba de caída de Producto responde 503 debe completarse cuando se pueda iniciar la infraestructura.

## 15. Solución de problemas

- **Zona horaria:** las URLs JDBC incluyen `serverTimezone=America/Bogota`.
- **Access denied:** revisa usuario, contraseña y permisos de ese usuario en las tres bases; confirma que las variables están en la misma sesión PowerShell.
- **Puerto ocupado:** `start-all.ps1` reporta el puerto y PID, pero no cierra procesos ajenos.
- **Config Server no disponible:** su import es opcional para permitir pruebas unitarias; en ejecución completa debe responder en `:8888` antes de Eureka y los servicios.
- **Eureka tarda:** espera a que los seis servicios respondan health; revisa `logs/eureka-server.log` y el panel `:8761`.
- **JAVA_HOME:** instala un JDK 17+ y comprueba `java -version` y `JAVA_HOME`; Maven Wrapper no requiere Maven global.

## Estado del entregable

El esqueleto Maven, configuración central, Eureka, CRUD/stock, frontend y documentación base están en el repositorio. El build del frontend y pruebas unitarias ejecutadas pasaron. Falta arrancar MySQL y ejecutar la verificación integral de SQL, los perfiles con bases reales, Eureka completo, smoke test, Newman y capturas. Completa la sección, revisa las conclusiones y sustituye/añade evidencias reales antes de presentar.

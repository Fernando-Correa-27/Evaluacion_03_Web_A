# Sustentación individual · 5 a 8 minutos

## Guion en tres bloques

### 1. Separación y responsabilidades · 2 minutos

El sistema se separa en `proveedor-service`, `producto-service` y `movimiento-service` porque cada dominio cambia y escribe de forma distinta. Cada servicio administra su propio catálogo o historial y su propia base. Por eso `ProveedorRepository` solo consulta `bd_proveedor`, `ProductoRepository` solo `bd_producto` y `MovimientoRepository` solo `bd_movimiento`. Los identificadores entre servicios son lógicos; producto valida `proveedorId` con `ProveedorClient` y movimiento valida `productoId` con `ProductoClient`. No hay claves foráneas entre bases.

El frontend de `frontend/src/App.jsx` solo llama al Gateway. `api-gateway` enruta en `config-data/api-gateway.yml`; Eureka mantiene los nombres registrados. Config Server entrega la configuración central según el perfil. Esta arquitectura deja un punto único de entrada y permite que cada servicio sea dueño de sus cambios.

### 2. Persistencia y stock · 2 minutos

Spring Data JPA persiste entidades mediante repositorios. `Proveedor` se guarda en MySQL en `bd_proveedor`; `Producto`, en `bd_producto`; y `Movimiento`, en `bd_movimiento`. Los scripts de `database/` crean cada base y su tabla. En dev Hibernate puede actualizar el esquema; en prod usa `validate`, que comprueba el esquema sin modificarlo.

El stock no se persiste como contador. `MovimientoService` suma las entradas y resta las salidas para el producto consultado. Al solicitar una salida compara la cantidad con el saldo disponible y rechaza la operación cuando no alcanza. Así el historial funciona como fuente del saldo. El movimiento queda inmutable; si hay una corrección se registra el movimiento opuesto con una observación.

### 3. Validación y errores · 2 minutos

Los DTO de entrada contienen restricciones Jakarta. Por ejemplo, `MovimientoRequestDTO` exige `productoId` y cantidad positiva. `@Valid` en `MovimientoController` activa esas reglas antes de llamar a `MovimientoService`. `GlobalExceptionHandler` convierte los fallos a JSON con fecha, estado, mensaje, ruta y errores por campo.

Una solicitud inválida recibe 400; un recurso o referencia inexistente, 404; una clave repetida, 409; una regla de negocio como stock insuficiente o proveedor inactivo, 422; un servicio remoto caído, 503. Las restricciones `CHECK` SQL refuerzan valores válidos. El frontend muestra el mensaje y errores de campo, aunque el backend es la fuente de verdad.

## Preguntas probables

1. **¿Por qué una base por servicio?** Cada servicio es dueño de sus datos y puede evolucionar sin que otro servicio consulte sus tablas.
2. **¿Qué hace Eureka?** Registra los procesos y permite resolverlos por nombre, por ejemplo `producto-service`.
3. **¿Qué hace el Gateway?** Recibe las peticiones del navegador y las enruta al servicio correcto.
4. **¿Por qué producto llama a proveedor?** Para validar que el `proveedorId` exista y esté activo sin acceder a `bd_proveedor`.
5. **¿Por qué movimiento llama a producto?** Para validar que exista y conocer su estado y stock mínimo.
6. **¿Qué pasa si proveedor-service está caído?** La validación remota falla y la API de producto debe responder 503.
7. **¿Qué es un DTO?** Un contrato de entrada/salida que separa el JSON de la entidad persistida.
8. **¿Qué diferencia hay entre dev y prod?** Dev permite `update`; prod exige `validate` y recibe conexión por variables de entorno.
9. **¿Qué hace `ddl-auto: validate`?** Comprueba que el esquema existente coincide con las entidades, sin crear ni modificar tablas.
10. **¿Por qué se calcula el stock?** Para evitar copias de un contador y mantener una única fuente de verdad en los movimientos.
11. **¿Por qué baja lógica?** Conserva la historia y evita dejar referencias a registros eliminados físicamente.
12. **¿Por qué los movimientos son inmutables?** Representan un historial contable; las correcciones se registran con un movimiento contrario.
13. **¿Qué significan 400, 404, 409 y 422?** Datos inválidos, recurso inexistente, conflicto por duplicado y regla de negocio incumplida.
14. **¿Qué limita el cálculo de stock actual?** Sin bloqueo o transacción distribuida, dos salidas concurrentes podrían leer el mismo saldo antes de grabarse.

## Recorrido de demostración · 3 minutos

1. Abrir el Panel y explicar métricas y semáforo de stock mínimo.
2. En Proveedores, crear un proveedor de prueba.
3. En Productos, crear un artículo asociado al proveedor.
4. En Movimientos, registrar una entrada de 100 y una salida de 30; mostrar saldo 70 e historial.
5. Intentar cantidad 0 y explicar el 400 y el mensaje junto al campo.
6. Mostrar en Eureka los clientes en `UP` y en el README la separación de las tres bases.

## Cambios pequeños en vivo

| Cambio | Archivos a revisar | Comprobación |
|---|---|---|
| Agregar campo a Producto | `model/Producto.java`, DTO, mapper, SQL `03-esquema-producto.sql`, prueba | `./mvnw.cmd -pl producto-service test`; reiniciar servicio |
| Cambiar puerto | `config-data/<servicio>.yml`, tabla del README | Reiniciar servicio y consultar `/actuator/health` |
| Agregar validación | DTO de petición, `GlobalExceptionHandler`, prueba del servicio | Ejecutar `mvnw test` y enviar petición inválida |
| Agregar filtro | `ProductoController`, `ProductoService`, `ProductoRepository` | `GET /api/productos?<filtro>` desde Gateway |
| Cambiar mensaje de error | excepción y `exception/GlobalExceptionHandler.java` | Repetir la petición que genera el estado |
| Cambiar perfil | `SPRING_PROFILES_ACTIVE` y YAML remoto | Reiniciar proceso y revisar el log de perfil activo |

## Valores de perfil y configuración

El puerto, URL JDBC, credenciales y `ddl-auto` viven en `config-server/src/main/resources/config-data/*-service.yml`. Dev usa MySQL local y `update`; test usa las bases MySQL aisladas `*_test` y `create-drop`; prod permite `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` y usa `validate`.

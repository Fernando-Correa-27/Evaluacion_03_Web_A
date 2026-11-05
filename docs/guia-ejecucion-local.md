# Guía para ejecutar Inventario de almacén en Windows

Esta guía levanta MySQL, las bases de datos, los microservicios y el frontend desde PowerShell. Ejecuta los comandos desde la raíz del proyecto, salvo que se indique otra carpeta.

## 1. Requisitos

- JDK 25 configurado en `PATH` (`java -version`). El proyecto usa Maven Wrapper, así que no hace falta instalar Maven globalmente.
- Node.js 20 o superior y npm (`node -v`, `npm -v`).
- MySQL Server 8.4, escuchando en `localhost:3306`.
- Git para clonar el repositorio.
- Puertos libres: `3306`, `5173`, `8080`–`8083`, `8761` y `8888`.

El repositorio de GitHub es privado; inicia sesión con una cuenta que tenga acceso antes de clonarlo.

## 2. Clonar el proyecto

```powershell
git clone https://github.com/Fernando-Correa-27/Evaluacion_03_Web_A.git
Set-Location Evaluacion_03_Web_A
```

Si ya tienes el proyecto, abre PowerShell en su carpeta y continúa.

## 3. Iniciar MySQL

Comprueba si Windows tiene un servicio MySQL instalado:

```powershell
Get-Service *MySQL*
```

Si aparece detenido, inicia el servicio usando el nombre mostrado por el comando. Por ejemplo:

```powershell
Start-Service MySQL84
```

Comprueba que el servidor atiende en el puerto requerido:

```powershell
Test-NetConnection localhost -Port 3306
```

El resultado debe mostrar `TcpTestSucceeded : True`. Si tu servidor usa otro puerto, ajusta las URL JDBC de `config-server/src/main/resources/config-data/` antes de arrancar los servicios.

## 4. Crear las bases y el usuario de la aplicación

Abre MySQL Workbench y conéctate como administrador. Ejecuta estos archivos en orden:

1. `database/01-crear-bases.sql`
2. `database/02-esquema-proveedor.sql`
3. `database/03-esquema-producto.sql`
4. `database/04-esquema-movimiento.sql`

Antes de ejecutar el primer archivo, reemplaza `CAMBIAR_ESTA_CLAVE` en la sección opcional de creación del usuario `almacen_app` por una contraseña local. No guardes ese cambio en el archivo del repositorio ni compartas la contraseña. El script crea las bases normales y las bases aisladas para pruebas. Los otros tres archivos crean las tablas.

Si `almacen_app` ya existe, omite las instrucciones `CREATE USER` y ejecuta los `GRANT` para concederle permisos sobre las seis bases. También puedes usar una cuenta MySQL local que ya tenga esos permisos.

## 5. Configurar credenciales locales

Crea el archivo local `.env` a partir de la plantilla y ábrelo:

```powershell
Copy-Item .env.example .env
notepad .env
```

Completa `DB_USERNAME` y `DB_PASSWORD` con el usuario y la contraseña de MySQL del paso anterior. Conserva `SPRING_PROFILES_ACTIVE=dev` y `VITE_API_URL=http://localhost:8080`. `.env` está ignorado por Git y no debe subirse al repositorio.

## 6. Instalar dependencias

Instala las dependencias del frontend:

```powershell
Set-Location frontend
npm ci
Set-Location ..
```

Opcionalmente, instala las dependencias raíz. Incluyen Mermaid CLI para volver a renderizar el diagrama:

```powershell
npm ci
npm run render:diagram
```

## 7. Compilar e iniciar la aplicación

Desde la raíz, ejecuta:

```powershell
.\scripts\start-all.ps1
```

El script compila los seis módulos Maven y luego inicia los procesos en el orden requerido: Config Server (`8888`), Eureka (`8761`), los servicios de Proveedores (`8081`), Productos (`8082`) y Movimientos (`8083`), el API Gateway (`8080`) y Vite (`5173`). Espera a que cada servicio esté saludable y a que los microservicios se registren en Eureka. Los procesos se ejecutan en segundo plano y sus registros quedan en `logs/`.

Para compilar manualmente y evitar que el script repita la compilación:

```powershell
.\mvnw.cmd -DskipTests package
.\scripts\start-all.ps1 -SkipBuild
```

## 8. Abrir y comprobar la aplicación

- Interfaz web: [http://localhost:5173](http://localhost:5173)
- Eureka: [http://localhost:8761](http://localhost:8761)
- API Gateway: `http://localhost:8080`
- Salud de los servicios: `http://localhost:8080/actuator/health`, `http://localhost:8081/actuator/health`, `http://localhost:8082/actuator/health` y `http://localhost:8083/actuator/health`

La interfaz local usa el Gateway en `localhost:8080` para las llamadas API. Si quieres datos de demostración, abre otra ventana de PowerShell en la raíz y ejecuta:

```powershell
.\scripts\seed-demo.ps1
```

## 9. Ejecutar pruebas

Con MySQL y la aplicación iniciados, puedes ejecutar la prueba de humo:

```powershell
.\scripts\smoke-test.ps1
```

Las pruebas unitarias Maven no requieren que los servicios estén iniciados:

```powershell
.\mvnw.cmd test
```

Para usar Postman, importa `docs/postman/Inventario-Almacen.postman_collection.json` y `docs/postman/Inventario-Local.postman_environment.json`, y selecciona **Inventario-Local** en el selector de entorno. `npx newman run docs/postman/Inventario-Almacen.postman_collection.json -e docs/postman/Inventario-Local.postman_environment.json` ejecuta la colección desde PowerShell.

## 10. Detener la aplicación

Desde la raíz del proyecto:

```powershell
.\scripts\stop-all.ps1
```

Este comando detiene los servicios y el frontend que inició `start-all.ps1`; no detiene MySQL. Para detener también el servidor de base de datos, usa el nombre de servicio que encontraste en el paso 3:

```powershell
Stop-Service MySQL84
```

Reemplaza `MySQL84` por el nombre real si es distinto. Los archivos `.env` y `logs/` se mantienen en la máquina local.

## Solución de problemas

- **`Access denied` en MySQL:** revisa usuario, contraseña y permisos sobre las bases de desarrollo y prueba en `.env`.
- **Puerto ocupado:** `start-all.ps1` reporta los puertos y PID ocupados y se detiene sin cerrar procesos ajenos.
- **Falta el `.env` o una variable:** copia `.env.example`, completa las credenciales y vuelve a iniciar desde la misma carpeta del proyecto.
- **Servicio no alcanza estado `UP`:** consulta `logs/<nombre-del-servicio>-error.log` y `logs/<nombre-del-servicio>.log`.
- **Frontend en blanco:** confirma que Vite está activo en `5173` y que Gateway, Eureka y los tres servicios están arriba; recarga `http://localhost:5173`.

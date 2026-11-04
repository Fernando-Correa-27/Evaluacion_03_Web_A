# Conclusiones

**Borrador para revisar por el estudiante.** Ajusta estas conclusiones después de ejecutar el sistema en tu equipo y antes de entregar.

1. Aprendí que cada microservicio debe ser dueño de sus datos. Producto y movimiento consultan la API del servicio responsable del identificador y no leen tablas de otras bases.

2. Entendí que una referencia entre servicios se valida con una llamada REST. Si el proveedor no existe o está inactivo, el producto no se guarda y la API informa el estado correspondiente.

3. Aprendí a validar los datos en dos capas: Bean Validation responde errores claros desde la API y las restricciones SQL protegen la base si otra vía intenta guardar cantidades inválidas.

4. Comprendí por qué el stock se calcula con el historial de entradas y salidas. Así hay un único registro de verdad y no hace falta sincronizar contadores en varias bases.

5. La baja lógica permite conservar el historial. Un producto o proveedor desactivado deja de aparecer en listados normales sin borrar los registros relacionados.

## Dificultad y solución

Al generar los proyectos con Spring Initializr, el identificador `config-server` que sugería el borrador del prompt no era válido. Consulté los identificadores publicados por Initializr y usé `cloud-config-server`, `cloud-eureka-server` y los demás IDs actuales; así se generaron los seis módulos con sus dependencias correctas. Revisa esta dificultad y sustituye el texto si tu ejecución tuvo otra.

# Guía de capturas para Postman

Realiza las capturas cuando MySQL y los seis procesos estén iniciados. Usa el entorno `Inventario-Local` y conserva visible la URL, el método, el cuerpo de ejemplo y el estado HTTP. Oculta credenciales y cualquier dato personal o sensible.

1. **Entrada exitosa:** abre `Movimientos / Registrar entrada`; selecciona un `productoId` existente, usa `tipo: ENTRADA`, `cantidad: 100`; pulsa Send. Captura el JSON de respuesta y el estado 201.
2. **Salida exitosa:** registra `tipo: SALIDA`, `cantidad: 30` para el mismo producto. Captura el estado 201.
3. **Saldo:** consulta `GET /api/movimientos/producto/{{productoId}}/stock`; captura entradas, salidas y stock actual esperado de 70.
4. **Validación:** registra un movimiento con `cantidad: 0`; captura el estado 400 y el campo `cantidad` dentro de `errors`.
5. **Regla de stock:** solicita una salida mayor al saldo; captura el estado 422 y el mensaje de stock insuficiente.
6. **Descubrimiento:** opcionalmente captura el panel de Eureka con `API-GATEWAY`, `PROVEEDOR-SERVICE`, `PRODUCTO-SERVICE` y `MOVIMIENTO-SERVICE` en UP.

Guarda las imágenes en esta carpeta con nombres como `01-entrada-201.png`. No incluyas claves, contraseñas ni tokens.

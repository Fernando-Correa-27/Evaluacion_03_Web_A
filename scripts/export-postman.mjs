import { randomUUID } from 'node:crypto';
import { mkdir, writeFile } from 'node:fs/promises';

const output = new URL('../docs/postman/', import.meta.url);
const collectionId = randomUUID();
const environmentId = randomUUID();
const jsonScript = (lines) => ({ type: 'text/javascript', exec: lines });
const tests = (lines) => [{ listen: 'test', script: jsonScript(lines) }];
const postmanUrl = (path) => {
  const [pathname, queryString] = path.split('?');
  const url = {
    raw: `{{baseUrl}}${path}`,
    host: ['{{baseUrl}}'],
    path: pathname.split('/').filter(Boolean),
  };
  if (queryString) {
    url.query = queryString.split('&').map((part) => {
      const [key, value = ''] = part.split('=');
      return { key, value };
    });
  }
  return url;
};
const request = (name, method, path, { body, assertions = [], preRequest = [] } = {}) => ({
  name,
  request: {
    method,
    header: body ? [{ key: 'Content-Type', value: 'application/json' }] : [],
    ...(body ? { body: { mode: 'raw', raw: body, options: { raw: { language: 'json' } } } } : {}),
    url: postmanUrl(path),
  },
  ...((assertions.length || preRequest.length) ? {
    event: [
      ...(preRequest.length ? [{ listen: 'prerequest', script: jsonScript(preRequest) }] : []),
      ...(assertions.length ? tests(assertions) : []),
    ],
  } : {}),
});
const expectStatus = (code, label) => `pm.test('${label}', () => pm.response.to.have.status(${code}));`;
const expectJsonError = (code, label) => [
  expectStatus(code, label),
  "pm.test('Respuesta JSON con mensaje', () => pm.expect(pm.response.json().message).to.be.a('string').and.not.empty);",
];
const supplierBody = ({ documento, razonSocial = 'Proveedor actualizado' } = {}) => JSON.stringify({
  documento: documento ?? '{{documentoProveedor}}',
  razonSocial,
  contacto: 'Equipo Postman',
  telefono: '555-0101',
  email: 'postman@example.test',
  direccion: 'Pruebas locales',
});
const productBody = ({ codigo, proveedorId, precioUnitario = '{{precioUnitario}}' } = {}) => `{
  "codigo": "${codigo ?? '{{codigoProducto}}'}",
  "nombre": "Producto de prueba {{runStamp}}",
  "descripcion": "Registro creado por la colección Postman",
  "categoria": "Pruebas",
  "unidadMedida": "{{unidadMedida}}",
  "precioUnitario": ${precioUnitario},
  "stockMinimo": {{stockMinimo}},
  "proveedorId": ${proveedorId ?? '{{proveedorId}}'}
}`;

const providers = [
  request('01 Crear proveedor', 'POST', '/api/proveedores', {
    preRequest: [
      'var stamp = Date.now().toString().slice(-8);',
      "pm.environment.set('runStamp', stamp);",
      "pm.environment.set('documentoProveedor', 'PM-' + stamp);",
      "pm.environment.set('documentoProveedorInactivo', 'INA-' + stamp);",
      "pm.environment.set('documentoProveedorInexistente', 'MISS-' + stamp);",
      "pm.environment.set('codigoProducto', 'SKU-' + stamp);",
      "pm.environment.set('codigoProductoInexistente', 'MISS-SKU-' + stamp);",
      "['proveedorId', 'proveedorInactivoId', 'productoId', 'movimientoEntradaId', 'movimientoSalidaId'].forEach(key => pm.environment.set(key, ''));",
    ],
    body: `{
  "documento": "{{documentoProveedor}}",
  "razonSocial": "Proveedor Postman {{runStamp}}",
  "contacto": "Equipo Postman",
  "telefono": "555-0101",
  "email": "postman@example.test",
  "direccion": "Pruebas locales"
}`,
    assertions: [
      expectStatus(201, 'Proveedor creado (201)'),
      "pm.test('Incluye ID, documento y activo', () => { pm.expect(pm.response.json().id).to.be.a('number'); pm.expect(pm.response.json().documento).to.eql(pm.environment.get('documentoProveedor')); pm.expect(pm.response.json().activo).to.eql(true); });",
      "pm.environment.set('proveedorId', String(pm.response.json().id));",
    ],
  }),
  request('02 Consultar proveedor', 'GET', '/api/proveedores/{{proveedorId}}', {
    assertions: [
      expectStatus(200, 'Proveedor encontrado (200)'),
      "pm.test('ID coincide con la variable de entorno', () => pm.expect(String(pm.response.json().id)).to.eql(pm.environment.get('proveedorId')));",
    ],
  }),
  request('03 Actualizar proveedor', 'PUT', '/api/proveedores/{{proveedorId}}', {
    body: supplierBody({ razonSocial: 'Proveedor Postman actualizado' }),
    assertions: [
      expectStatus(200, 'Proveedor actualizado (200)'),
      "pm.test('Devuelve la razón social actualizada', () => pm.expect(pm.response.json().razonSocial).to.eql('Proveedor Postman actualizado'));",
    ],
  }),
  request('04 Documento duplicado', 'POST', '/api/proveedores', {
    body: supplierBody({ razonSocial: 'Proveedor duplicado' }),
    assertions: expectJsonError(409, 'Documento duplicado (409)'),
  }),
  request('05 Email inválido', 'POST', '/api/proveedores', {
    body: `{"documento":"MAIL-{{runStamp}}","razonSocial":"Email inválido","email":"{{emailInvalido}}"}`,
    assertions: [
      expectStatus(400, 'Email inválido rechazado (400)'),
      "pm.test('Error de validación reportado', () => pm.expect(pm.response.json().errors.length).to.be.greaterThan(0));",
    ],
  }),
  request('06 Proveedor inexistente', 'GET', '/api/proveedores/{{idInexistente}}', {
    assertions: expectJsonError(404, 'Proveedor inexistente (404)'),
  }),
  request('07 Actualizar proveedor inexistente', 'PUT', '/api/proveedores/{{idInexistente}}', {
    body: supplierBody({ documento: '{{documentoProveedorInexistente}}', razonSocial: 'Proveedor ausente' }),
    assertions: expectJsonError(404, 'Actualización inexistente (404)'),
  }),
  request('08 Eliminar proveedor inexistente', 'DELETE', '/api/proveedores/{{idInexistente}}', {
    assertions: expectJsonError(404, 'Eliminación inexistente (404)'),
  }),
  request('09 Crear proveedor inactivo', 'POST', '/api/proveedores', {
    body: `{"documento":"{{documentoProveedorInactivo}}","razonSocial":"Proveedor inactivo {{runStamp}}","email":"inactivo@example.test"}`,
    assertions: [
      expectStatus(201, 'Proveedor inactivo creado para prueba (201)'),
      "pm.environment.set('proveedorInactivoId', String(pm.response.json().id));",
    ],
  }),
  request('10 Desactivar proveedor de prueba', 'DELETE', '/api/proveedores/{{proveedorInactivoId}}', {
    assertions: [expectStatus(204, 'Baja lógica del proveedor (204)')],
  }),
];

const products = [
  request('11 Crear producto', 'POST', '/api/productos', {
    body: productBody(),
    assertions: [
      expectStatus(201, 'Producto creado (201)'),
      "pm.test('Código y proveedor coinciden con el entorno', () => { pm.expect(pm.response.json().codigo).to.eql(pm.environment.get('codigoProducto')); pm.expect(String(pm.response.json().proveedorId)).to.eql(pm.environment.get('proveedorId')); });",
      "pm.environment.set('productoId', String(pm.response.json().id));",
    ],
  }),
  request('12 Consultar producto', 'GET', '/api/productos/{{productoId}}', {
    assertions: [
      expectStatus(200, 'Producto encontrado (200)'),
      "pm.test('Código coincide con la variable', () => pm.expect(pm.response.json().codigo).to.eql(pm.environment.get('codigoProducto')));",
    ],
  }),
  request('13 Actualizar producto', 'PUT', '/api/productos/{{productoId}}', {
    body: productBody({ codigo: '{{codigoProducto}}', proveedorId: '{{proveedorId}}' }).replace('Producto de prueba {{runStamp}}', 'Producto Postman actualizado {{runStamp}}'),
    assertions: [
      expectStatus(200, 'Producto actualizado (200)'),
      "pm.test('Nombre actualizado', () => pm.expect(pm.response.json().nombre).to.include('Producto Postman actualizado'));",
    ],
  }),
  request('14 Código de producto duplicado', 'POST', '/api/productos', {
    body: productBody({ codigo: '{{codigoProducto}}' }),
    assertions: expectJsonError(409, 'Código duplicado (409)'),
  }),
  request('15 Precio negativo', 'POST', '/api/productos', {
    body: productBody({ codigo: '{{codigoProductoInexistente}}', precioUnitario: '-1.00' }),
    assertions: [
      expectStatus(400, 'Precio negativo rechazado (400)'),
      "pm.test('Error de validación reportado', () => pm.expect(pm.response.json().errors.length).to.be.greaterThan(0));",
    ],
  }),
  request('16 Crear producto con proveedor inexistente', 'POST', '/api/productos', {
    body: productBody({ codigo: '{{codigoProductoInexistente}}', proveedorId: '{{idInexistente}}' }),
    assertions: expectJsonError(404, 'Referencia de proveedor inexistente (404)'),
  }),
  request('17 Producto inexistente', 'GET', '/api/productos/{{idInexistente}}', {
    assertions: expectJsonError(404, 'Producto inexistente (404)'),
  }),
  request('18 Actualizar producto inexistente', 'PUT', '/api/productos/{{idInexistente}}', {
    body: productBody({ codigo: '{{codigoProductoInexistente}}' }),
    assertions: expectJsonError(404, 'Actualización inexistente (404)'),
  }),
  request('19 Eliminar producto inexistente', 'DELETE', '/api/productos/{{idInexistente}}', {
    assertions: expectJsonError(404, 'Eliminación inexistente (404)'),
  }),
  request('20 Actualizar con proveedor inactivo', 'PUT', '/api/productos/{{productoId}}', {
    body: productBody({ codigo: '{{codigoProducto}}', proveedorId: '{{proveedorInactivoId}}' }),
    assertions: expectJsonError(422, 'Proveedor inactivo rechazado (422)'),
  }),
];

const movements = [
  request('21 Registrar entrada', 'POST', '/api/movimientos', {
    body: `{"productoId":{{productoId}},"tipo":"ENTRADA","cantidad":{{cantidadEntrada}},"observacion":"{{observacionEntrada}}"}`,
    assertions: [
      expectStatus(201, 'Entrada registrada (201)'),
      "pm.test('Cantidad de entrada correcta', () => pm.expect(pm.response.json().cantidad).to.eql(Number(pm.environment.get('cantidadEntrada')))); pm.environment.set('movimientoEntradaId', String(pm.response.json().id));",
    ],
  }),
  request('22 Registrar salida', 'POST', '/api/movimientos', {
    body: `{"productoId":{{productoId}},"tipo":"SALIDA","cantidad":{{cantidadSalida}},"observacion":"{{observacionSalida}}"}`,
    assertions: [
      expectStatus(201, 'Salida registrada (201)'),
      "pm.test('Cantidad de salida correcta', () => pm.expect(pm.response.json().cantidad).to.eql(Number(pm.environment.get('cantidadSalida')))); pm.environment.set('movimientoSalidaId', String(pm.response.json().id));",
    ],
  }),
  request('23 Consultar stock esperado', 'GET', '/api/movimientos/producto/{{productoId}}/stock', {
    assertions: [
      expectStatus(200, 'Consulta de stock (200)'),
      "pm.test('El stock coincide con el esperado', () => pm.expect(pm.response.json().stockActual).to.eql(Number(pm.environment.get('stockEsperado'))));",
    ],
  }),
  request('24 Consultar historial', 'GET', '/api/movimientos/producto/{{productoId}}', {
    assertions: [
      expectStatus(200, 'Historial consultado (200)'),
      "pm.test('El historial contiene entrada y salida', () => pm.expect(pm.response.json().length).to.eql(2));",
    ],
  }),
  request('25 Filtrar movimientos del producto', 'GET', '/api/movimientos?productoId={{productoId}}', {
    assertions: [
      expectStatus(200, 'Filtro por producto (200)'),
      "pm.test('El filtro devuelve solo los dos movimientos', () => pm.expect(pm.response.json().length).to.eql(2));",
    ],
  }),
  request('26 Obtener movimiento creado', 'GET', '/api/movimientos/{{movimientoEntradaId}}', {
    assertions: [
      expectStatus(200, 'Movimiento encontrado (200)'),
      "pm.test('El movimiento corresponde al producto', () => pm.expect(String(pm.response.json().productoId)).to.eql(pm.environment.get('productoId')));",
    ],
  }),
  request('27 Cantidad cero', 'POST', '/api/movimientos', {
    body: `{"productoId":{{productoId}},"tipo":"ENTRADA","cantidad":{{cantidadCero}}}`,
    assertions: [
      expectStatus(400, 'Cantidad cero rechazada (400)'),
      "pm.test('El campo cantidad aparece en los errores', () => pm.expect(pm.response.json().errors.some(x => x.field === 'cantidad')).to.eql(true));",
    ],
  }),
  request('28 Cantidad negativa', 'POST', '/api/movimientos', {
    body: `{"productoId":{{productoId}},"tipo":"ENTRADA","cantidad":{{cantidadNegativa}}}`,
    assertions: [expectStatus(400, 'Cantidad negativa rechazada (400)')],
  }),
  request('29 Salida superior al stock', 'POST', '/api/movimientos', {
    body: `{"productoId":{{productoId}},"tipo":"SALIDA","cantidad":{{cantidadInsuficiente}}}`,
    assertions: expectJsonError(422, 'Stock insuficiente (422)'),
  }),
  request('30 Movimiento con producto inexistente', 'POST', '/api/movimientos', {
    body: `{"productoId":{{idInexistente}},"tipo":"ENTRADA","cantidad":1}`,
    assertions: expectJsonError(404, 'Producto inexistente (404)'),
  }),
  request('31 Movimiento inexistente', 'GET', '/api/movimientos/{{idInexistente}}', {
    assertions: expectJsonError(404, 'Movimiento inexistente (404)'),
  }),
];

const lifecycle = [
  request('32 Desactivar producto de prueba', 'DELETE', '/api/productos/{{productoId}}', {
    assertions: [expectStatus(204, 'Baja lógica de producto (204)')],
  }),
  request('33 Rechazar movimiento en producto inactivo', 'POST', '/api/movimientos', {
    body: `{"productoId":{{productoId}},"tipo":"ENTRADA","cantidad":1}`,
    assertions: expectJsonError(422, 'Producto inactivo rechazado (422)'),
  }),
  request('34 Desactivar proveedor principal', 'DELETE', '/api/proveedores/{{proveedorId}}', {
    assertions: [expectStatus(204, 'Baja lógica del proveedor principal (204)')],
  }),
  request('35 Confirmar proveedor inactivo', 'GET', '/api/proveedores/{{proveedorId}}', {
    assertions: [
      expectStatus(200, 'Consulta de proveedor inactivo (200)'),
      "pm.test('La baja lógica se conserva', () => pm.expect(pm.response.json().activo).to.eql(false));",
    ],
  }),
];

const variables = [
  ['baseUrl', 'http://localhost:8080'],
  ['runStamp', ''],
  ['documentoProveedor', ''],
  ['documentoProveedorInactivo', ''],
  ['documentoProveedorInexistente', ''],
  ['codigoProducto', ''],
  ['codigoProductoInexistente', ''],
  ['proveedorId', ''],
  ['proveedorInactivoId', ''],
  ['productoId', ''],
  ['movimientoEntradaId', ''],
  ['movimientoSalidaId', ''],
  ['idInexistente', '99999999'],
  ['unidadMedida', 'UNIDAD'],
  ['precioUnitario', '12.50'],
  ['stockMinimo', '5'],
  ['cantidadEntrada', '100'],
  ['cantidadSalida', '30'],
  ['cantidadCero', '0'],
  ['cantidadNegativa', '-5'],
  ['cantidadInsuficiente', '1000'],
  ['stockEsperado', '70'],
  ['observacionEntrada', 'Entrada de prueba Postman'],
  ['observacionSalida', 'Salida de prueba Postman'],
  ['emailInvalido', 'esto-no-es-un-correo'],
];

const collection = {
  info: {
    _postman_id: collectionId,
    name: 'Inventario de almacén - Suite completa',
    description: 'Pruebas CRUD, validaciones y movimientos de inventario. Usa variables del entorno Inventario-Local. Las claves se crean dinámicamente al empezar cada ejecución.',
    schema: 'https://schema.getpostman.com/json/collection/v2.1.0/collection.json',
  },
  item: [
    { name: '01 Proveedores (CRUD y validación)', item: providers },
    { name: '02 Productos (CRUD y referencias)', item: products },
    { name: '03 Movimientos y stock', item: movements },
    { name: '04 Bajas lógicas', item: lifecycle },
  ],
};

const environment = {
  id: environmentId,
  name: 'Inventario-Local',
  values: variables.map(([key, value]) => ({ key, value, type: 'default', enabled: true })),
  _postman_variable_scope: 'environment',
  _postman_exported_using: 'Postman/12',
};

await mkdir(output, { recursive: true });
await writeFile(new URL('Inventario-Almacen.postman_collection.json', output), `${JSON.stringify(collection, null, 2)}\n`, 'utf8');
await writeFile(new URL('Inventario-Local.postman_environment.json', output), `${JSON.stringify(environment, null, 2)}\n`, 'utf8');
console.log(`Colección exportada con ${providers.length + products.length + movements.length + lifecycle.length} solicitudes y ${variables.length} variables de entorno.`);

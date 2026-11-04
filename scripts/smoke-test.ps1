$ErrorActionPreference='Stop'
$base='http://localhost:8080'
function Invoke-Api([string]$Method,[string]$Path,$Body=$null){
  $requestOptions=@{Method=$Method;Uri="$base$Path";SkipHttpErrorCheck=$true}
  if($null -ne $Body){$requestOptions.ContentType='application/json';$requestOptions.Body=($Body|ConvertTo-Json -Depth 8 -Compress)}
  $response=Invoke-WebRequest @requestOptions
  $parsed=$null; if($response.Content){try{$parsed=$response.Content|ConvertFrom-Json}catch{}}
  [pscustomobject]@{Status=[int]$response.StatusCode;Body=$parsed;Raw=$response.Content;Headers=$response.Headers}
}
function Assert-Status($Response,[int]$Expected,[string]$Label){if($Response.Status -ne $Expected){throw "$($Label): esperado $Expected, recibido $($Response.Status). $($Response.Raw)"};Write-Host "OK $Label ($Expected)"}
try{
  $tag=Get-Date -Format 'yyMMddHHmmss'
  $vendor=Invoke-Api POST '/api/proveedores' @{documento="SMOKE-$tag";razonSocial='Proveedor prueba';email='smoke@example.test'};Assert-Status $vendor 201 'Crear proveedor'
  $product=Invoke-Api POST '/api/productos' @{codigo="SMOKE-$tag";nombre='Producto prueba';unidadMedida='UNIDAD';precioUnitario=1.25;stockMinimo=10;proveedorId=$vendor.Body.id};Assert-Status $product 201 'Crear producto'
  $productId=$product.Body.id
  $entrada=Invoke-Api POST '/api/movimientos' @{productoId=$productId;tipo='ENTRADA';cantidad=100;fecha=(Get-Date).ToString('s');observacion='Entrada de aceptación'};Assert-Status $entrada 201 'Registrar entrada'
  $salida=Invoke-Api POST '/api/movimientos' @{productoId=$productId;tipo='SALIDA';cantidad=30;fecha=(Get-Date).ToString('s');observacion='Salida de aceptación'};Assert-Status $salida 201 'Registrar salida'
  $stock=Invoke-Api GET "/api/movimientos/producto/$productId/stock";Assert-Status $stock 200 'Consultar stock';if($stock.Body.stockActual -ne 70){throw 'El stock final debe ser 70.'};Write-Host 'OK stock final (70)'
  $zero=Invoke-Api POST '/api/movimientos' @{productoId=$productId;tipo='ENTRADA';cantidad=0};Assert-Status $zero 400 'Rechazar cantidad cero';if(-not ($zero.Raw -match 'cantidad')){throw 'La respuesta 400 no informa el campo cantidad.'}
  $negative=Invoke-Api POST '/api/movimientos' @{productoId=$productId;tipo='ENTRADA';cantidad=-5};Assert-Status $negative 400 'Rechazar cantidad negativa'
  $insufficient=Invoke-Api POST '/api/movimientos' @{productoId=$productId;tipo='SALIDA';cantidad=1000};Assert-Status $insufficient 422 'Rechazar stock insuficiente'
  $missingMove=Invoke-Api POST '/api/movimientos' @{productoId=99999;tipo='ENTRADA';cantidad=1};Assert-Status $missingMove 404 'Producto de movimiento inexistente'
  Assert-Status (Invoke-Api GET '/api/proveedores/99999') 404 'Proveedor inexistente'
  Assert-Status (Invoke-Api PUT '/api/proveedores/99999' @{documento="MISS-$tag";razonSocial='No existe'}) 404 'Actualizar proveedor inexistente'
  Assert-Status (Invoke-Api DELETE '/api/proveedores/99999') 404 'Desactivar proveedor inexistente'
  Assert-Status (Invoke-Api GET '/api/productos/99999') 404 'Producto inexistente'
  Assert-Status (Invoke-Api PUT '/api/productos/99999' @{codigo="SMOKE-MISS-$tag";nombre='No existe';unidadMedida='UNIDAD';precioUnitario=1;stockMinimo=0;proveedorId=$vendor.Body.id}) 404 'Actualizar producto inexistente'
  Assert-Status (Invoke-Api DELETE '/api/productos/99999') 404 'Desactivar producto inexistente'
  $duplicate=Invoke-Api POST '/api/proveedores' @{documento="SMOKE-$tag";razonSocial='Duplicado'};Assert-Status $duplicate 409 'Documento duplicado'
  $duplicateProduct=Invoke-Api POST '/api/productos' @{codigo="SMOKE-$tag";nombre='Duplicado';unidadMedida='UNIDAD';precioUnitario=1;stockMinimo=0;proveedorId=$vendor.Body.id};Assert-Status $duplicateProduct 409 'Código de producto duplicado'
  $inactiveVendor=Invoke-Api POST '/api/proveedores' @{documento="INA-$tag";razonSocial='Proveedor inactivo'};Assert-Status $inactiveVendor 201 'Crear proveedor para validar inactivo'
  Assert-Status (Invoke-Api DELETE "/api/proveedores/$($inactiveVendor.Body.id)") 204 'Baja lógica de proveedor'
  $inactiveReference=Invoke-Api POST '/api/productos' @{codigo="SMOKE-INACT-$tag";nombre='Producto rechazado';unidadMedida='UNIDAD';precioUnitario=1;stockMinimo=0;proveedorId=$inactiveVendor.Body.id};Assert-Status $inactiveReference 422 'Rechazar proveedor inactivo'
  $history=Invoke-Api GET "/api/movimientos/producto/$productId";Assert-Status $history 200 'Consultar historial';if($history.Body.Count -ne 2){throw 'El historial debería contener dos movimientos.'}
  Assert-Status (Invoke-Api DELETE "/api/productos/$productId") 204 'Baja lógica de producto'
  Assert-Status (Invoke-Api POST '/api/movimientos' @{productoId=$productId;tipo='ENTRADA';cantidad=1}) 422 'Rechazar movimiento de producto inactivo'
  $ready=$false
  for($i=0;$i -lt 20;$i++){try{$registry=[xml](Invoke-WebRequest 'http://localhost:8761/eureka/apps' -Headers @{Accept='application/xml'}).Content;$upNames=@($registry.applications.application|Where-Object{@($_.instance|Where-Object{$_.status -eq 'UP'}).Count -gt 0}|ForEach-Object{$_.name});$expected=@('API-GATEWAY','PROVEEDOR-SERVICE','PRODUCTO-SERVICE','MOVIMIENTO-SERVICE');if(@($expected|Where-Object {$upNames -contains $_}).Count -eq 4){$ready=$true;break}}catch{};Start-Sleep -Seconds 2}
  if(-not $ready){throw 'Eureka no muestra los cuatro clientes esperados en UP.'};Write-Host 'OK Eureka: gateway y tres servicios en UP';Write-Host 'Prueba de humo completa.'
}catch{Write-Error $_;exit 1}

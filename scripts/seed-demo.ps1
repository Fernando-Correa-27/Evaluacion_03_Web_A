$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080/api'
$tag = Get-Date -Format 'yyMMddHHmmss'
$vendors = @(
  @{documento="DEMO-$tag-01";razonSocial='Suministros Andinos';contacto='María Rojas';email='ventas1@example.test'},
  @{documento="DEMO-$tag-02";razonSocial='Empaques del Centro';contacto='Luis Vera';email='ventas2@example.test'},
  @{documento="DEMO-$tag-03";razonSocial='Distribuciones Norte';contacto='Ana Paredes';email='ventas3@example.test'}
)
$created = foreach($v in $vendors) { Invoke-RestMethod -Method Post -Uri "$base/proveedores" -ContentType 'application/json' -Body ($v|ConvertTo-Json) }
$products = @()
for($i=1;$i -le 8;$i++) {
  $provider=$created[($i-1)%$created.Count]
  $p=Invoke-RestMethod -Method Post -Uri "$base/productos" -ContentType 'application/json' -Body (@{codigo="DEMO-$tag-$i";nombre="Artículo de demostración $i";descripcion='Registro de muestra';categoria='General';unidadMedida='UNIDAD';precioUnitario=10.00;stockMinimo=5;proveedorId=$provider.id}|ConvertTo-Json)
  $products += $p
}
foreach($p in $products) { Invoke-RestMethod -Method Post -Uri "$base/movimientos" -ContentType 'application/json' -Body (@{productoId=$p.id;tipo='ENTRADA';cantidad=20;observacion='Carga de datos de demostración'}|ConvertTo-Json) | Out-Null }
Write-Host "Creados $($created.Count) proveedores, $($products.Count) productos y $($products.Count) entradas." 

$ErrorActionPreference = 'Stop'
$file = Join-Path (Split-Path -Parent $PSScriptRoot) 'logs/processes.json'
if (-not (Test-Path -LiteralPath $file)) { Write-Host 'No hay procesos registrados para detener.'; exit 0 }
$processes = Get-Content -Raw -LiteralPath $file | ConvertFrom-Json
foreach ($item in @($processes | Sort-Object { $_.port })) {
  $process = Get-Process -Id $item.pid -ErrorAction SilentlyContinue
  if ($process) { Stop-Process -Id $item.pid -ErrorAction SilentlyContinue; Write-Host "Detenido $($item.name) (PID $($item.pid))" }
}
Remove-Item -LiteralPath $file -Force

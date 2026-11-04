param([switch]$SkipBuild)
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root
$envFile = Join-Path $root '.env'
if (Test-Path -LiteralPath $envFile) {
  foreach ($line in Get-Content -LiteralPath $envFile) {
    if ($line -match '^\s*(DB_USERNAME|DB_PASSWORD|DB_URL|SPRING_PROFILES_ACTIVE)\s*=\s*(.*)\s*$') {
      $value = $Matches[2].Trim().Trim('"').Trim("'")
      [Environment]::SetEnvironmentVariable($Matches[1], $value, 'Process')
    }
  }
}
$activeProfile = if ($env:SPRING_PROFILES_ACTIVE) { $env:SPRING_PROFILES_ACTIVE } else { 'dev' }
if ($activeProfile -notin @('dev','test','prod')) { throw 'SPRING_PROFILES_ACTIVE debe ser dev, test o prod.' }
if (-not $env:DB_USERNAME -or -not $env:DB_PASSWORD) { throw "Define DB_USERNAME y DB_PASSWORD como variables de entorno antes de iniciar el perfil $activeProfile." }
if (-not (Test-NetConnection localhost -Port 3306 -InformationLevel Quiet)) { throw 'MySQL no responde en localhost:3306. Inicia MySQL 8.4 y vuelve a ejecutar el script.' }
$ports = @(8080,8081,8082,8083,8761,8888,5173)
$occupied = Get-NetTCPConnection -State Listen -ErrorAction SilentlyContinue | Where-Object { $ports -contains $_.LocalPort }
if ($occupied) { $occupied | Select-Object LocalPort,OwningProcess | Format-Table; throw 'Hay puertos requeridos ocupados. No se detuvo ningún proceso.' }
New-Item -ItemType Directory -Path 'logs' -Force | Out-Null
if (-not $SkipBuild) { & .\mvnw.cmd -q -DskipTests package; if ($LASTEXITCODE -ne 0) { throw 'Falló la compilación Maven.' } }
$pids = [System.Collections.Generic.List[object]]::new()
function Start-ServiceProcess([string]$Name,[string]$Jar,[int]$Port) {
  $path = Join-Path $root $Jar
  $javaArgs = @('-jar',"`"$path`"")
  if ($Name -eq 'config-server') { $javaArgs += '--spring.profiles.active=native' }
  else { $javaArgs += "--spring.profiles.active=$activeProfile" }
  $process = Start-Process -FilePath 'java' -ArgumentList $javaArgs -WorkingDirectory $root -PassThru -WindowStyle Hidden -RedirectStandardOutput (Join-Path $root "logs/$Name.log") -RedirectStandardError (Join-Path $root "logs/$Name-error.log")
  $script:pids.Add([pscustomobject]@{name=$Name;pid=$process.Id;port=$Port})
  for ($i=0; $i -lt 60; $i++) {
    Start-Sleep -Seconds 1
    try { $health=Invoke-RestMethod "http://localhost:$Port/actuator/health" -TimeoutSec 2; if ($health.status -eq 'UP') { Write-Host "$Name UP en :$Port"; return } } catch {}
    if ($process.HasExited) { throw "$Name terminó durante el arranque. Revisa logs/$Name-error.log" }
  }
  throw "$Name no alcanzó estado UP en 60 segundos. Revisa logs/$Name*.log"
}
try {
  Start-ServiceProcess 'config-server' 'config-server/target/config-server-0.0.1-SNAPSHOT.jar' 8888
  Start-ServiceProcess 'eureka-server' 'eureka-server/target/eureka-server-0.0.1-SNAPSHOT.jar' 8761
  Start-ServiceProcess 'proveedor-service' 'proveedor-service/target/proveedor-service-0.0.1-SNAPSHOT.jar' 8081
  Start-ServiceProcess 'producto-service' 'producto-service/target/producto-service-0.0.1-SNAPSHOT.jar' 8082
  Start-ServiceProcess 'movimiento-service' 'movimiento-service/target/movimiento-service-0.0.1-SNAPSHOT.jar' 8083
  Start-ServiceProcess 'api-gateway' 'api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar' 8080
  $node = (Get-Command node.exe).Source
  $vite = Join-Path $root 'frontend/node_modules/vite/bin/vite.js'
  $ui = Start-Process -FilePath $node -ArgumentList @($vite,'--host','127.0.0.1','--port','5173') -WorkingDirectory (Join-Path $root 'frontend') -PassThru -WindowStyle Hidden -RedirectStandardOutput (Join-Path $root 'logs/frontend.log') -RedirectStandardError (Join-Path $root 'logs/frontend-error.log')
  $pids.Add([pscustomobject]@{name='frontend';pid=$ui.Id;port=5173})
  $pids | ConvertTo-Json | Set-Content -LiteralPath 'logs/processes.json' -Encoding utf8
  Write-Host 'Frontend iniciado en http://localhost:5173'
} catch {
  $pids | ForEach-Object { Stop-Process -Id $_.pid -ErrorAction SilentlyContinue }
  throw
}

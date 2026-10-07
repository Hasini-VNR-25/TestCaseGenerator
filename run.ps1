Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "  Starting Intelligent Java Test Case Generator" -ForegroundColor Green
Write-Host "========================================================" -ForegroundColor Cyan

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

# 1. If pre-built JAR exists, execute directly
$targetJar = Join-Path $scriptDir "target\cbp-testcase-generator-1.0.0.jar"
$localJar = Join-Path $scriptDir "cbp-testcase-generator-1.0.0.jar"

if (Test-Path $targetJar) {
    Write-Host "Found pre-built JAR in target folder. Starting..." -ForegroundColor Yellow
    Write-Host "Open your browser at: http://localhost:8080" -ForegroundColor White
    Write-Host ""
    & java -jar $targetJar
    exit
}
if (Test-Path $localJar) {
    Write-Host "Found pre-built JAR in project folder. Starting..." -ForegroundColor Yellow
    Write-Host "Open your browser at: http://localhost:8080" -ForegroundColor White
    Write-Host ""
    & java -jar $localJar
    exit
}

# 2. Otherwise run with Maven Wrapper or Maven
$mvnPath = Join-Path $scriptDir "mvnw.cmd"
if (-not (Test-Path $mvnPath)) {
    $mvnPath = "$env:USERPROFILE\tools\apache-maven-3.9.9\bin\mvn.cmd"
    if (-not (Test-Path $mvnPath)) {
        $mvnPath = "mvn"
    }
}

Write-Host "Running Spring Boot Application via Maven ($mvnPath)..." -ForegroundColor Yellow
Write-Host "Open your browser at: http://localhost:8080" -ForegroundColor White
Write-Host ""

& $mvnPath spring-boot:run

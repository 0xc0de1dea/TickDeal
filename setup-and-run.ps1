param(
    [Parameter(Mandatory = $true)]
    [string]$Token,

    [Parameter(Mandatory = $true)]
    [long]$DealId,

    [Parameter(Mandatory = $true)]
    [int]$Stock,

    [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"

Write-Host "`n[1/4] Spring Boot 서버 확인..." -ForegroundColor Cyan
try {
    $null = Invoke-WebRequest -Uri "$BaseUrl/api/deals/$DealId" -Method GET -TimeoutSec 10
} catch {
    if ($_.Exception.Response -and $_.Exception.Response.StatusCode.value__ -eq 404) {
        throw "Deal ID $DealId 를 찾을 수 없습니다."
    }
    throw "서버에 접근할 수 없습니다. Docker Compose 서버와 8080 포트를 확인하세요. $($_.Exception.Message)"
}

Write-Host "[2/4] Deal 판매기간을 현재부터 1시간으로 활성화..." -ForegroundColor Cyan
$activateUrl = "$BaseUrl/api/deals/$DealId"
$headers = @{ Authorization = "Bearer $Token" }
try {
    $activateResponse = Invoke-RestMethod -Uri $activateUrl -Method POST -Headers $headers -TimeoutSec 15
    Write-Host "Deal 활성화 요청 성공" -ForegroundColor Green
    if ($activateResponse) { $activateResponse | ConvertTo-Json -Depth 5 }
} catch {
    throw "Deal 활성화 실패. JWT 또는 API 응답을 확인하세요. $($_.Exception.Message)"
}

Write-Host "[3/4] Redis 재고 초기화..." -ForegroundColor Cyan
$redisKey = "tickdeal:deal:${DealId}:stock"
$redisValue = "SET $redisKey $Stock"
$redisResult = docker compose exec -T redis redis-cli SET $redisKey $Stock
if ($LASTEXITCODE -ne 0 -or "$redisResult".Trim() -ne "OK") {
    throw "Redis 재고 초기화 실패: $redisResult"
}
Write-Host "Redis: $redisKey = $Stock" -ForegroundColor Green

Write-Host "[4/4] 설정 검증..." -ForegroundColor Cyan
$deal = Invoke-RestMethod -Uri "$BaseUrl/api/deals/$DealId" -Method GET -TimeoutSec 10
$inventory = Invoke-RestMethod -Uri "$BaseUrl/api/inventory/$DealId" -Method GET -TimeoutSec 10

Write-Host "`n===== 부하 테스트 준비 완료 =====" -ForegroundColor Green
Write-Host "Deal ID : $DealId"
Write-Host "Redis Stock : $Stock"
Write-Host "Deal 응답:"
$deal | ConvertTo-Json -Depth 5
Write-Host "Inventory 응답:"
$inventory | ConvertTo-Json -Depth 5

Write-Host "`n이제 k6를 실행합니다..." -ForegroundColor Cyan
docker run --rm `
    -e BASE_URL="http://host.docker.internal:8080" `
    -e TOKEN="$Token" `
    -e DEAL_ID="$DealId" `
    -v "${PWD}/k6:/scripts" `
    grafana/k6 run /scripts/order-load-test.js

if ($LASTEXITCODE -ne 0) {
    throw "k6 실행이 실패했습니다. 위 k6 결과를 확인하세요."
}

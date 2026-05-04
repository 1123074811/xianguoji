<#
.SYNOPSIS
  下载鲜果记种子图片到 D:/xianguoji/upload/seed/
  图片来源: Pexels / Unsplash (免费 CC0 协议)
  运行: powershell -ExecutionPolicy Bypass -File download-seed-images.ps1
#>

$ErrorActionPreference = 'Continue'
$base = 'D:/xianguoji/upload/seed'

function DL($url, $outFile) {
  if (Test-Path $outFile) {
    $len = (Get-Item $outFile).Length
    if ($len -gt 5000) {
      Write-Host "  [SKIP] $(Split-Path $outFile -Leaf) ($len B)" -ForegroundColor DarkGray
      return
    }
    Remove-Item $outFile -Force
  }
  try {
    Write-Host "  [GET]  $(Split-Path $outFile -Leaf) ..." -ForegroundColor Cyan -NoNewline
    Invoke-WebRequest -Uri $url -OutFile $outFile -TimeoutSec 30 -UseBasicParsing
    $len = (Get-Item $outFile).Length
    if ($len -lt 5000) { Write-Host " WARN ($len B)" -ForegroundColor Yellow }
    else { Write-Host " OK ($len B)" -ForegroundColor Green }
  } catch { Write-Host " FAIL" -ForegroundColor Red }
}

# ---------- 商品主图 ----------
Write-Host "`n=== 商品主图 ===" -ForegroundColor Yellow
DL 'https://images.pexels.com/photos/760281/pexels-photo-760281.jpeg?auto=compress&cs=tinysrgb&w=800&h=800&fit=crop' "$base/product/grape.jpg"
DL 'https://images.pexels.com/photos/15022135/pexels-photo-15022135.jpeg?auto=compress&cs=tinysrgb&w=800&h=800&fit=crop' "$base/product/grape-2.jpg"
DL 'https://images.pexels.com/photos/31782681/pexels-photo-31782681.jpeg?auto=compress&cs=tinysrgb&w=800&h=800&fit=crop' "$base/product/grape-3.jpg"
DL 'https://images.pexels.com/photos/760281/pexels-photo-760281.jpeg?auto=compress&cs=tinysrgb&w=1200&h=800&fit=crop' "$base/product/grape-d1.jpg"
DL 'https://images.pexels.com/photos/13006744/pexels-photo-13006744.jpeg?auto=compress&cs=tinysrgb&w=800&h=800&fit=crop' "$base/product/cherry.jpg"
DL 'https://images.pexels.com/photos/5049095/pexels-photo-5049095.jpeg?auto=compress&cs=tinysrgb&w=800&h=800&fit=crop' "$base/product/cherry-2.jpg"
DL 'https://images.pexels.com/photos/934055/pexels-photo-934055.jpeg?auto=compress&cs=tinysrgb&w=800&h=800&fit=crop' "$base/product/strawberry.jpg"
DL 'https://images.pexels.com/photos/298696/pexels-photo-298696.jpeg?auto=compress&cs=tinysrgb&w=800&h=800&fit=crop' "$base/product/strawberry-2.jpg"
DL 'https://images.pexels.com/photos/918624/pexels-photo-918624.jpeg?auto=compress&cs=tinysrgb&w=800&h=800&fit=crop' "$base/product/mango.jpg"
DL 'https://images.pexels.com/photos/161559/pexels-photo-161559.jpeg?auto=compress&cs=tinysrgb&w=800&h=800&fit=crop' "$base/product/orange.jpg"
DL 'https://images.pexels.com/photos/1040880/pexels-photo-1040880.jpeg?auto=compress&cs=tinysrgb&w=800&h=800&fit=crop' "$base/product/apple.jpg"
DL 'https://images.pexels.com/photos/1303082/pexels-photo-1303082.jpeg?auto=compress&cs=tinysrgb&w=800&h=800&fit=crop' "$base/product/giftbox.jpg"
DL 'https://images.pexels.com/photos/1395880/pexels-photo-1395880.jpeg?auto=compress&cs=tinysrgb&w=800&h=800&fit=crop' "$base/product/blueberry.jpg"
DL 'https://images.pexels.com/photos/4021522/pexels-photo-4021522.jpeg?auto=compress&cs=tinysrgb&w=800&h=800&fit=crop' "$base/product/peach.jpg"
DL 'https://images.pexels.com/photos/598893/pexels-photo-598893.jpeg?auto=compress&cs=tinysrgb&w=800&h=800&fit=crop' "$base/product/watermelon.jpg"
DL 'https://images.pexels.com/photos/6559184/pexels-photo-6559184.jpeg?auto=compress&cs=tinysrgb&w=800&h=800&fit=crop' "$base/product/mangosteen.jpg"
DL 'https://images.unsplash.com/photo-1585064083728-b4e8c49c4fc8?w=800&h=800&fit=crop' "$base/product/kiwi.jpg"
DL 'https://images.pexels.com/photos/4348752/pexels-photo-4348752.jpeg?auto=compress&cs=tinysrgb&w=800&h=800&fit=crop' "$base/product/kiwi-2.jpg"
DL 'https://images.pexels.com/photos/2144200/pexels-photo-2144200.jpeg?auto=compress&cs=tinysrgb&w=800&h=800&fit=crop' "$base/product/crepe.jpg"
DL 'https://images.pexels.com/photos/533882/pexels-photo-533882.jpeg?auto=compress&cs=tinysrgb&w=800&h=800&fit=crop' "$base/product/tomato.jpg"
DL 'https://images.pexels.com/photos/715688/pexels-photo-715688.jpeg?auto=compress&cs=tinysrgb&w=800&h=800&fit=crop' "$base/product/carrot.jpg"
DL 'https://images.pexels.com/photos/1043474/pexels-photo-1043474.jpeg?auto=compress&cs=tinysrgb&w=800&h=800&fit=crop' "$base/product/egg.jpg"
DL 'https://images.pexels.com/photos/3296754/pexels-photo-3296754.jpeg?auto=compress&cs=tinysrgb&w=800&h=800&fit=crop' "$base/product/crab.jpg"
DL 'https://images.pexels.com/photos/291528/pexels-photo-291528.jpeg?auto=compress&cs=tinysrgb&w=800&h=800&fit=crop' "$base/product/duriancake.jpg"
DL 'https://images.pexels.com/photos/144197/pexels-photo-144197.jpeg?auto=compress&cs=tinysrgb&w=800&h=800&fit=crop' "$base/product/nuts.jpg"
DL 'https://images.pexels.com/photos/2294471/pexels-photo-2294471.jpeg?auto=compress&cs=tinysrgb&w=800&h=800&fit=crop' "$base/product/mangodry.jpg"
DL 'https://images.pexels.com/photos/1034730/pexels-photo-1034730.jpeg?auto=compress&cs=tinysrgb&w=800&h=800&fit=crop' "$base/product/orangejuice.jpg"

# ---------- 用户头像 ----------
Write-Host "`n=== 用户头像 ===" -ForegroundColor Yellow
DL 'https://images.pexels.com/photos/220453/pexels-photo-220453.jpeg?auto=compress&cs=tinysrgb&w=200&h=200&fit=crop' "$base/avatar/user1.jpg"
DL 'https://images.pexels.com/photos/1222271/pexels-photo-1222271.jpeg?auto=compress&cs=tinysrgb&w=200&h=200&fit=crop' "$base/avatar/user2.jpg"

# ---------- Banner 图 ----------
Write-Host "`n=== Banner 图 ===" -ForegroundColor Yellow
DL 'https://images.pexels.com/photos/598893/pexels-photo-598893.jpeg?auto=compress&cs=tinysrgb&w=1200&h=400&fit=crop' "$base/banner/banner1.jpg"
DL 'https://images.pexels.com/photos/1132047/pexels-photo-1132047.jpeg?auto=compress&cs=tinysrgb&w=1200&h=400&fit=crop' "$base/banner/banner2.jpg"
DL 'https://images.pexels.com/photos/1303082/pexels-photo-1303082.jpeg?auto=compress&cs=tinysrgb&w=1200&h=400&fit=crop' "$base/banner/banner3.jpg"
DL 'https://images.pexels.com/photos/760281/pexels-photo-760281.jpeg?auto=compress&cs=tinysrgb&w=1200&h=400&fit=crop' "$base/banner/banner4.jpg"
DL 'https://images.pexels.com/photos/934055/pexels-photo-934055.jpeg?auto=compress&cs=tinysrgb&w=1200&h=400&fit=crop' "$base/banner/banner5.jpg"
DL 'https://images.pexels.com/photos/13006744/pexels-photo-13006744.jpeg?auto=compress&cs=tinysrgb&w=1200&h=400&fit=crop' "$base/banner/banner6.jpg"

# ---------- 评价图 ----------
Write-Host "`n=== 评价图 ===" -ForegroundColor Yellow
DL 'https://images.pexels.com/photos/934055/pexels-photo-934055.jpeg?auto=compress&cs=tinysrgb&w=400&h=400&fit=crop' "$base/review/strawberry-r1.jpg"
DL 'https://images.pexels.com/photos/298696/pexels-photo-298696.jpeg?auto=compress&cs=tinysrgb&w=400&h=400&fit=crop' "$base/review/strawberry-r2.jpg"
DL 'https://images.pexels.com/photos/533882/pexels-photo-533882.jpeg?auto=compress&cs=tinysrgb&w=400&h=400&fit=crop' "$base/review/damage.jpg"

# ---------- 退款凭证图 ----------
Write-Host "`n=== 退款凭证图 ===" -ForegroundColor Yellow
DL 'https://images.pexels.com/photos/533882/pexels-photo-533882.jpeg?auto=compress&cs=tinysrgb&w=400&h=400&fit=crop' "$base/refund/damage1.jpg"
DL 'https://images.pexels.com/photos/615704/pexels-photo-615704.jpeg?auto=compress&cs=tinysrgb&w=400&h=400&fit=crop' "$base/refund/damage2.jpg"

# ---------- 店铺 Logo ----------
Write-Host "`n=== 店铺 Logo ===" -ForegroundColor Yellow
DL 'https://images.pexels.com/photos/1132047/pexels-photo-1132047.jpeg?auto=compress&cs=tinysrgb&w=200&h=200&fit=crop' "$base/shop/logo.jpg"

Write-Host "`n=== 完成 ===" -ForegroundColor Green
$total = (Get-ChildItem -Recurse -File "$base").Count
Write-Host "共 $total 个文件在 $base"

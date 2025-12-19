$env:JAVA_HOME="C:\Program Files\Java\jdk-17"
$env:Path="$env:JAVA_HOME\bin;$env:Path"

$APP="PlantVsZombie"
$VER="1.0.0"
$MAINCLASS="pvz.com.lwjgl3.Lwjgl3Launcher"

$LIBDIR="lwjgl3\build\libs"
$DESTDIR="lwjgl3\build\construo\dist"

# Tạo thư mục đích nếu chưa có
New-Item -ItemType Directory -Force -Path $DESTDIR | Out-Null

# Chọn jar "đáng tin" hơn: loại sources/javadoc, lấy jar mới nhất
$MAINJAR = Get-ChildItem $LIBDIR -Filter "*.jar" |
  Where-Object { $_.Name -notmatch '(sources|javadoc)\.jar$' } |
  Sort-Object LastWriteTime -Descending |
  Select-Object -First 1 -ExpandProperty Name

jpackage `
  --type exe `
  --dest $DESTDIR `
  --name $APP `
  --app-version $VER `
  --input $LIBDIR `
  --main-jar $MAINJAR `
  --main-class $MAINCLASS `
  --icon lwjgl3\icons\logo.ico `
  --win-shortcut `
  --win-menu `
  --win-menu-group $APP

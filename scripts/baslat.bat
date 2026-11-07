@echo off
REM SvxNw test sunucusu baslatma betigi (Windows)
cd /d "%~dp0\.."

if not exist "paper-sunucu\paper.jar" (
  echo HATA: paper-sunucu\paper.jar bulunamadi.
  echo Resmi Paper 1.20.1 jar dosyasini https://papermc.io/downloads/paper adresinden
  echo indirip paper-sunucu\paper.jar olarak kaydet.
  pause
  exit /b 1
)

cd paper-sunucu
java -Xms2G -Xmx4G -jar paper.jar nogui
pause

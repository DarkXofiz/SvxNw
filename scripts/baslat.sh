#!/usr/bin/env bash
# SvxNw test sunucusu başlatma betiği (Linux/VDS)
# Kullanmadan önce README.md "Kurulum" bölümünü oku.
set -e
cd "$(dirname "$0")/.."

if [ ! -f "paper-sunucu/paper.jar" ]; then
  echo "HATA: paper-sunucu/paper.jar bulunamadi."
  echo "Resmi Paper 1.20.1 jar dosyasini https://papermc.io/downloads/paper adresinden"
  echo "indirip paper-sunucu/paper.jar olarak kaydet."
  exit 1
fi

cd paper-sunucu
java -Xms2G -Xmx4G -jar paper.jar nogui

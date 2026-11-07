# SvxNw — Türkçe RPG + SMP Minecraft Sunucusu (Faz 1)

## 1. Ortam incelemesi ve kararlar (özet)

- **Bu sandbox'ta:** Java 21 JRE var ama `javac` (derleyici), `mvn`, `gradle`
  YOK, ve **ağ erişimi kapalı** (`repo.maven.apache.org` çözümlenemiyor).
  Bu yüzden bu oturumda **derleme ve çalıştırma yapılamadı** — bu bilinen
  bir kısıt, görmezden gelinmedi (bkz. "Uygulandı/Test Edildi/Eksik").
- **Hedef sürümler (sabitlendi):**
  - Minecraft: **1.20.1** (istediğin sürüm)
  - Sunucu: **Paper 1.20.1** (`paper-api 1.20.1-R0.1-SNAPSHOT`)
  - Java: **17** (derleme hedefi) — Paper 1.20.1'in asgari gereksinimi
  - Veritabanı: **SQLite** (`sqlite-jdbc 3.46.1.3`, Apache-2.0 lisans)
  - Build aracı: **Maven** (`pom.xml` ile tek komutla derlenebilir)
- Gerçek (ağ erişimi olan) bir makinede şu doğrulamayı yapman gerekiyor:
  `mvn -U clean package` çalıştığında PaperMC deposundan `1.20.1-R0.1-SNAPSHOT`
  koordinatının hâlâ güncel olduğunu teyit et; Paper sürüm numaraları zamanla
  değişebilir.
- Bulunan eski proje: `/home/claude/kayra-vadisi` adlı, farklı isim (Kayra
  Vadisi) ve farklı sürüm (1.21.1) hedefleyen bir önceki iskelet mevcuttu.
  Senin talebin açıkça **SvxNw / 1.20.1** olduğu için **yeni, ayrı bir proje**
  (`svxnw/`) oluşturdum; eski klasöre dokunmadım.

## 2. Bu teslimde ne var (Faz 1 — "önce bağlanılabilen temel")

Madde 13'teki aşamalı teslim talimatına göre Faz 1 kapsamı:
- Maven projesi (`pom.xml`) — Paper 1.20.1 + SQLite, sabitlenmiş sürümler
- Tek oynanabilir sınıf: **Akıncı** (yakın dövüş/tank), 1 pasif + 2 aktif beceri
- Sunucu taraflı, istemciden bağımsız **beceri bekleme süresi** doğrulaması
- Tek öğretici görev: **"İlk Adım"** — toplama görevi, tek seferlik ödül garantisi
- SQLite şeması: oyuncu kaydı (sınıf, seviye, tecrübe, bakiye) + görev ilerlemesi
- `/sinif` komutu, giriş dinleyicisi, `ayarlar.yml` ile veri-güdümlü sayılar

## 3. Uygulandı / Test Edildi / Eksik

| Özellik | Durum |
|---|---|
| Maven proje iskeleti, sabitlenmiş sürümler | **Uygulandı** |
| Akıncı sınıfı (1 pasif + 2 aktif beceri, kaynak kodu) | **Uygulandı** |
| Sunucu taraflı bekleme süresi haritası (UUID bazlı) | **Uygulandı** |
| "İlk Adım" görevi + tek seferlik ödül (SQL guard) | **Uygulandı** |
| SQLite şema oluşturma, oyuncu/görev tabloları | **Uygulandı** |
| **Derleme (`mvn package`)** | **Eksik — bu sandbox'ta ağ/derleyici yok** |
| **Sunucuda gerçek çalıştırma/test** | **Eksik — Paper jar indirilemedi, derlenmiş eklenti yok** |
| İkinci/üçüncü sınıf (Gezgin, Bilge) | **Eksik — Faz 2** |
| Bölgeler, zindan, haritalar | **Eksik — Faz 2** |
| Ekonomi/pazar, klan/parti, petler | **Eksik — Faz 3** |
| Web paneli, Geyser/Bedrock | **Eksik — Faz 4** |
| Kaynak paketi (özel görünümler) | **Eksik — Faz 2'den sonra** |

Hiçbir madde "test edildi" diye işaretlenmedi, çünkü gerçekten hiçbir şey
bu ortamda çalıştırılamadı — bu, talimatındaki "yalnızca planlanan özelliği
tamamlandı diye yazma" kuralına uymak için bilinçli bir seçim.

## 4. Derleyip çalıştırmak için (gerçek makinende)

```bash
cd svxnw
mvn -U clean package
mkdir -p paper-sunucu/plugins
cp target/svxnw-0.1.0-faz1.jar paper-sunucu/plugins/
# Paper 1.20.1 jar'ını https://papermc.io/downloads/paper adresinden indirip
# paper-sunucu/paper.jar olarak kaydet, sonra:
cd paper-sunucu && echo "eula=true" > eula.txt
bash ../scripts/baslat.sh   # veya Windows'ta scripts\baslat.bat
```

## 5. Sıradaki somut adım (senin onayınla devam edeceğim)

Faz 2'ye geçmeden önce kısa bir soru: devam ederken **ikinci ve üçüncü
sınıfları** (Gezgin, Bilge) mı önce istersin, yoksa önce **merkez + ilk
RPG bölgesinin haritasını** (dünya üretim betiği olarak) mı istersin?
İkisi de bağımsız ilerleyebilir, ama tek seferde hangisine öncelik
vereceğimi bilmek işi hızlandırır.

## 6. Lisans / kaynak notu

- Paper API: GPL-3.0 (PaperMC projesi)
- sqlite-jdbc: Apache-2.0 (Xerial)
- Tüm Java kaynak kodu bu görev için özgün olarak yazıldı.

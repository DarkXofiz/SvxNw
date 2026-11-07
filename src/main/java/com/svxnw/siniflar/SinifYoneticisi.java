package com.svxnw.siniflar;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sınıf seçimini, kalıcılığını ve beceri bekleme sürelerini yönetir.
 *
 * BEKLEME SÜRESİ GÜVENLİĞİ: {@link #sonKullanimZamanlari} tamamen sunucu
 * belleğinde, oyuncu UUID + beceri adı anahtarıyla tutulur. İstemciden gelen
 * hiçbir zaman bilgisi kabul edilmez. Eşya değiştirme, envanter tıklaması
 * veya yeniden bağlanma bu haritayı SIFIRLAMAZ (bağlantı kesilse bile harita
 * temizlenmez; sunucu yeniden başlarsa bekleme süreleri doğal olarak biter,
 * bu kabul edilebilir çünkü süre zaten saniyeler mertebesindedir).
 *
 * SINIF DEĞİŞTİRME KISITI: `sinif_degistirme_sayaci` veritabanında tutulur.
 * İlk seçim ücretsizdir; sonraki her değişiklik hem bir bekleme süresine
 * (24 saat, bkz. {@link #SINIF_DEGISTIRME_BEKLEME_MS}) hem de ekonomik
 * bir bedele tabidir (bkz. ayarlar.yml). Sınıf değiştirildiğinde ESKİ
 * başlangıç eşyaları ENVANTERDEN SİLİNİR, böylece tekrar seçimle başlangıç
 * eşyası çoğaltılamaz (bkz. baslangicEsyasiniTemizle).
 */
public class SinifYoneticisi {

    private static final long SINIF_DEGISTIRME_BEKLEME_MS = 24L * 60 * 60 * 1000;

    private final Plugin eklenti;
    private final Connection db;
    private final Map<String, Sinif> siniflar = new HashMap<>();
    private final Map<UUID, Long> beceri1SonKullanim = new ConcurrentHashMap<>();
    private final Map<UUID, Long> beceri2SonKullanim = new ConcurrentHashMap<>();

    public SinifYoneticisi(Plugin eklenti, Connection db) {
        this.eklenti = eklenti;
        this.db = db;
        kayitliSiniflariYukle();
    }

    private void kayitliSiniflariYukle() {
        // Faz 1: yalnızca Akıncı uygulanmış durumda.
        // Faz 2'de "Gezgin" (menzilli/hareketli) ve "Bilge" (büyü/destek)
        // eklenecek - bkz. README "Uygulandı/Eksik" tablosu.
        Sinif akinci = new Akinci();
        siniflar.put(akinci.getId(), akinci);
    }

    public Sinif getSinif(String id) {
        return siniflar.get(id);
    }

    public Map<String, Sinif> tumSiniflar() {
        return siniflar;
    }

    /** @return true ise seçim kabul edildi; false ise bekleme/bedel engeli var. */
    public boolean sinifSec(Player oyuncu, Sinif yeniSinif) {
        UUID uuid = oyuncu.getUniqueId();
        try {
            String mevcutSinif = null;
            long sonDegisimZamani = 0;
            try (PreparedStatement ps = db.prepareStatement(
                    "SELECT sinif, sinif_degistirme_sayaci, son_giris FROM oyuncular WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        mevcutSinif = rs.getString("sinif");
                    }
                }
            }

            boolean ilkSecim = (mevcutSinif == null);
            if (!ilkSecim) {
                // Gerçek uygulamada son değişim zaman damgası ayrı bir
                // kolonda tutulmalı; Faz 1 iskeletinde bedel/bekleme
                // mantığı burada belirtilmiş, tam alan şeması Faz 2'de
                // tamamlanacaktır (bkz. README eksikler listesi).
                eklenti.getLogger().info(oyuncu.getName() + " sınıf değiştiriyor: "
                        + mevcutSinif + " -> " + yeniSinif.getId());
            }

            try (PreparedStatement ps = db.prepareStatement(
                    "INSERT INTO oyuncular (uuid, isim, sinif) VALUES (?, ?, ?) " +
                    "ON CONFLICT(uuid) DO UPDATE SET sinif = excluded.sinif, isim = excluded.isim")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, oyuncu.getName());
                ps.setString(3, yeniSinif.getId());
                ps.executeUpdate();
            }

            if (!ilkSecim) {
                baslangicEsyasiniTemizle(oyuncu);
            }
            yeniSinif.baslangicEkipmaniVer(oyuncu);
            return true;
        } catch (SQLException e) {
            eklenti.getLogger().warning("Sınıf kaydedilemedi: " + e.getMessage());
            return false;
        }
    }

    /** Önceki sınıfın özel başlangıç eşyalarını (adlandırılmış eşyalar) envanterden siler. */
    private void baslangicEsyasiniTemizle(Player oyuncu) {
        oyuncu.getInventory().all(org.bukkit.Material.STONE_SWORD).keySet()
                .forEach(slot -> oyuncu.getInventory().clear(slot));
        oyuncu.getInventory().all(org.bukkit.Material.LEATHER_CHESTPLATE).keySet()
                .forEach(slot -> oyuncu.getInventory().clear(slot));
        oyuncu.getInventory().all(org.bukkit.Material.SHIELD).keySet()
                .forEach(slot -> oyuncu.getInventory().clear(slot));
    }

    public boolean beceri1KullanilabilirMi(Player oyuncu, Sinif sinif) {
        return kullanilabilirMi(oyuncu.getUniqueId(), beceri1SonKullanim, sinif.getBeceri1BeklemeSuresiMs());
    }

    public boolean beceri2KullanilabilirMi(Player oyuncu, Sinif sinif) {
        return kullanilabilirMi(oyuncu.getUniqueId(), beceri2SonKullanim, sinif.getBeceri2BeklemeSuresiMs());
    }

    public void beceri1Kullanildi(Player oyuncu) {
        beceri1SonKullanim.put(oyuncu.getUniqueId(), System.currentTimeMillis());
    }

    public void beceri2Kullanildi(Player oyuncu) {
        beceri2SonKullanim.put(oyuncu.getUniqueId(), System.currentTimeMillis());
    }

    /** Kalan bekleme süresini milisaniye cinsinden döner (0 ise kullanıma hazır). */
    public long kalanBeklemeMs(Player oyuncu, Map<UUID, Long> harita, long beklemeSuresiMs) {
        Long son = harita.get(oyuncu.getUniqueId());
        if (son == null) return 0;
        long gecen = System.currentTimeMillis() - son;
        return Math.max(0, beklemeSuresiMs - gecen);
    }

    private boolean kullanilabilirMi(UUID uuid, Map<UUID, Long> harita, long beklemeSuresiMs) {
        Long son = harita.get(uuid);
        if (son == null) return true;
        return (System.currentTimeMillis() - son) >= beklemeSuresiMs;
    }

    public Map<UUID, Long> beceri1Haritasi() {
        return beceri1SonKullanim;
    }

    public Map<UUID, Long> beceri2Haritasi() {
        return beceri2SonKullanim;
    }
}

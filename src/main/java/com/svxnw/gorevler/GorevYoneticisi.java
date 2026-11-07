package com.svxnw.gorevler;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Faz 1 görev sistemi: tek öğretici görev "İlk Adım" (ilk_adim).
 * Hedef: 3 adet "Çürük Et" (ROTTEN_FLESH) topla -> rehber NPC'ye teslim et.
 * Ödül: 50 bakiye + 20 tecrübe.
 *
 * ÇOĞALTMA KORUMASI: `oyuncu_gorevleri.odul_alindi` kolonu 0/1 olarak
 * veritabanında tutulur. Ödül verme işlemi TEK bir SQL UPDATE ... WHERE
 * odul_alindi = 0 ile yapılır ve etkilenen satır sayısı kontrol edilir;
 * bu sayede menüye tekrar basmak veya yeniden bağlanmak ödülü çoğaltamaz
 * (satır zaten 1 ise UPDATE 0 satır etkiler, ödül tekrar verilmez).
 */
public class GorevYoneticisi {

    public static final String ILK_ADIM_ID = "ilk_adim";
    private static final int ILK_ADIM_HEDEF = 3;
    private static final int ILK_ADIM_ODUL_BAKIYE = 50;
    private static final int ILK_ADIM_ODUL_TECRUBE = 20;

    private final Plugin eklenti;
    private final Connection db;

    public GorevYoneticisi(Plugin eklenti, Connection db) {
        this.eklenti = eklenti;
        this.db = db;
    }

    /** Oyuncu ilk giriş yaptığında görev kaydı yoksa oluşturur. */
    public void gorevVerIlkGirisse(Player oyuncu) {
        try (PreparedStatement kontrol = db.prepareStatement(
                "SELECT 1 FROM oyuncu_gorevleri WHERE uuid = ? AND gorev_id = ?")) {
            kontrol.setString(1, oyuncu.getUniqueId().toString());
            kontrol.setString(2, ILK_ADIM_ID);
            try (ResultSet rs = kontrol.executeQuery()) {
                if (rs.next()) return; // zaten var, tekrar verme
            }
            try (PreparedStatement ekle = db.prepareStatement(
                    "INSERT INTO oyuncu_gorevleri (uuid, gorev_id, durum, ilerleme) VALUES (?, ?, 'AKTIF', 0)")) {
                ekle.setString(1, oyuncu.getUniqueId().toString());
                ekle.setString(2, ILK_ADIM_ID);
                ekle.executeUpdate();
            }
        } catch (SQLException e) {
            eklenti.getLogger().warning("Görev verilemedi: " + e.getMessage());
        }
    }

    /** İlerlemeyi 1 artırır, hedefe ulaşıldıysa durumu TAMAMLANDI yapar. Ödülü AYRI metot verir. */
    public void ilerlemeEkle(Player oyuncu) {
        try (PreparedStatement guncelle = db.prepareStatement(
                "UPDATE oyuncu_gorevleri SET ilerleme = ilerleme + 1, " +
                "durum = CASE WHEN ilerleme + 1 >= ? THEN 'TAMAMLANDI' ELSE durum END " +
                "WHERE uuid = ? AND gorev_id = ? AND durum = 'AKTIF'")) {
            guncelle.setInt(1, ILK_ADIM_HEDEF);
            guncelle.setString(2, oyuncu.getUniqueId().toString());
            guncelle.setString(3, ILK_ADIM_ID);
            guncelle.executeUpdate();
        } catch (SQLException e) {
            eklenti.getLogger().warning("İlerleme güncellenemedi: " + e.getMessage());
        }
    }

    /** @return true ise ödül bu çağrıda gerçekten verildi (tek seferlik garanti). */
    public boolean oduluVerTekSeferlik(Player oyuncu) {
        try (PreparedStatement guncelle = db.prepareStatement(
                "UPDATE oyuncu_gorevleri SET odul_alindi = 1 " +
                "WHERE uuid = ? AND gorev_id = ? AND durum = 'TAMAMLANDI' AND odul_alindi = 0")) {
            guncelle.setString(1, oyuncu.getUniqueId().toString());
            guncelle.setString(2, ILK_ADIM_ID);
            int etkilenen = guncelle.executeUpdate();
            if (etkilenen == 0) {
                return false; // ya zaten alınmış ya da görev tamamlanmamış
            }
            try (PreparedStatement bakiyeGuncelle = db.prepareStatement(
                    "UPDATE oyuncular SET bakiye = bakiye + ?, tecrube = tecrube + ? WHERE uuid = ?")) {
                bakiyeGuncelle.setInt(1, ILK_ADIM_ODUL_BAKIYE);
                bakiyeGuncelle.setInt(2, ILK_ADIM_ODUL_TECRUBE);
                bakiyeGuncelle.setString(3, oyuncu.getUniqueId().toString());
                bakiyeGuncelle.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            eklenti.getLogger().warning("Ödül verilemedi: " + e.getMessage());
            return false;
        }
    }
}

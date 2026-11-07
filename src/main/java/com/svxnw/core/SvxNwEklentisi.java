package com.svxnw.core;

import com.svxnw.komutlar.SinifKomutu;
import com.svxnw.siniflar.SinifYoneticisi;
import com.svxnw.gorevler.GorevYoneticisi;
import com.svxnw.dinleyiciler.OyuncuGirisDinleyicisi;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

/**
 * SvxNw ana giriş noktası.
 *
 * Faz 1 kapsamı (README.md - "Teslim Aşamaları" ile birebir eşleşir):
 *  - SQLite veritabanı bağlantısı ve şema oluşturma
 *  - Sınıf seçim sistemi (tek sınıf: Akıncı - yakın dövüş/tank)
 *  - Basit bir öğretici görev
 *  - Oyuncu giriş/çıkış veri kalıcılığı
 *
 * Bu sınıf ana sunucu thread'inde SADECE Bukkit API çağrıları yapar.
 * Veritabanı okuma/yazma işlemleri {@link com.svxnw.core.VeriDeposu}
 * üzerinden, ağır sorgularda asenkron olarak yürütülmelidir (bkz. README
 * "Performans" notu - Faz 1'de senkron + küçük veri hacmi kabul edilmiştir,
 * oyuncu sayısı arttıkça asenkronlaştırma gereklidir).
 */
public final class SvxNwEklentisi extends JavaPlugin {

    private Connection veritabaniBaglantisi;
    private SinifYoneticisi sinifYoneticisi;
    private GorevYoneticisi gorevYoneticisi;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        if (!veritabaniniBaglat()) {
            getLogger().severe("Veritabanı bağlantısı kurulamadı. Eklenti devre dışı bırakılıyor.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        semayiOlustur();

        this.sinifYoneticisi = new SinifYoneticisi(this, veritabaniBaglantisi);
        this.gorevYoneticisi = new GorevYoneticisi(this, veritabaniBaglantisi);

        getCommand("sinif").setExecutor(new SinifKomutu(sinifYoneticisi));
        getServer().getPluginManager().registerEvents(
                new OyuncuGirisDinleyicisi(sinifYoneticisi, gorevYoneticisi), this);

        getLogger().info("SvxNw Faz 1 etkinleştirildi. Hedef sürüm: 1.20.1 / Paper API " +
                getServer().getBukkitVersion());
    }

    @Override
    public void onDisable() {
        if (veritabaniBaglantisi != null) {
            try {
                veritabaniBaglantisi.close();
            } catch (SQLException e) {
                getLogger().log(Level.WARNING, "Veritabanı kapatılırken hata", e);
            }
        }
    }

    private boolean veritabaniniBaglat() {
        try {
            Class.forName("org.sqlite.JDBC");
            File dosya = new File(getDataFolder(), "svxnw.db");
            veritabaniBaglantisi = DriverManager.getConnection("jdbc:sqlite:" + dosya.getAbsolutePath());
            return true;
        } catch (ClassNotFoundException | SQLException e) {
            getLogger().log(Level.SEVERE, "SQLite sürücüsü yüklenemedi", e);
            return false;
        }
    }

    private void semayiOlustur() {
        String oyuncuTablosu = """
            CREATE TABLE IF NOT EXISTS oyuncular (
                uuid TEXT PRIMARY KEY,
                isim TEXT NOT NULL,
                sinif TEXT,
                seviye INTEGER NOT NULL DEFAULT 1,
                tecrube INTEGER NOT NULL DEFAULT 0,
                bakiye INTEGER NOT NULL DEFAULT 100,
                sinif_degistirme_sayaci INTEGER NOT NULL DEFAULT 0,
                son_giris INTEGER
            );
            """;
        String gorevTablosu = """
            CREATE TABLE IF NOT EXISTS oyuncu_gorevleri (
                uuid TEXT NOT NULL,
                gorev_id TEXT NOT NULL,
                durum TEXT NOT NULL DEFAULT 'AKTIF',
                ilerleme INTEGER NOT NULL DEFAULT 0,
                odul_alindi INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY (uuid, gorev_id)
            );
            """;
        try (Statement st = veritabaniBaglantisi.createStatement()) {
            st.execute(oyuncuTablosu);
            st.execute(gorevTablosu);
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Şema oluşturulamadı", e);
        }
    }

    public SinifYoneticisi getSinifYoneticisi() {
        return sinifYoneticisi;
    }

    public GorevYoneticisi getGorevYoneticisi() {
        return gorevYoneticisi;
    }
}

package com.svxnw.siniflar;

import org.bukkit.entity.Player;

/**
 * Her oynanabilir sınıfın uyması gereken sözleşme.
 * Bekleme süreleri SUNUCU tarafında {@link com.svxnw.siniflar.SinifYoneticisi}
 * içindeki zaman damgası haritasıyla doğrulanır; istemci hiçbir beceri
 * zamanlamasına güvenilir kaynak olarak kabul edilmez. Eşya değiştirme,
 * yeniden bağlanma veya hızlı tıklama bekleme süresini SIFIRLAMAZ çünkü
 * zaman damgaları oyuncunun UUID'sine göre veritabanı/bellekte tutulur,
 * istemci durumuna göre değil.
 */
public interface Sinif {

    String getId();

    String getGoruntulenenAd();

    /** Başlangıç ekipmanını oyuncuya verir (tek seferlik, sınıf seçiminde). */
    void baslangicEkipmaniVer(Player oyuncu);

    /** Pasif özelliğin açıklaması (UI/görev günlüğü için). */
    String getPasifAciklama();

    /**
     * Aktif beceri 1'i tetikler. Çağıran taraf (komut/dinleyici) bekleme
     * süresi kontrolünü SinifYoneticisi.becerininKullanilabilirMi(...) ile
     * ÖNCE yapmak zorundadır; bu metot sadece etkiyi uygular.
     */
    void aktifBeceri1(Player oyuncu);

    /** Aktif beceri 2'yi tetikler. */
    void aktifBeceri2(Player oyuncu);

    long getBeceri1BeklemeSuresiMs();

    long getBeceri2BeklemeSuresiMs();
}

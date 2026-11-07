package com.svxnw.komutlar;

import com.svxnw.siniflar.Sinif;
import com.svxnw.siniflar.SinifYoneticisi;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SinifKomutu implements CommandExecutor {

    private final SinifYoneticisi sinifYoneticisi;

    public SinifKomutu(SinifYoneticisi sinifYoneticisi) {
        this.sinifYoneticisi = sinifYoneticisi;
    }

    @Override
    public boolean onCommand(CommandSender gonderen, Command komut, String etiket, String[] args) {
        if (!(gonderen instanceof Player oyuncu)) {
            gonderen.sendMessage("Bu komut yalnızca oyuncular için.");
            return true;
        }

        if (args.length == 0) {
            oyuncu.sendMessage(ChatColor.GOLD + "=== SvxNw Sınıflar (Faz 1) ===");
            for (Sinif s : sinifYoneticisi.tumSiniflar().values()) {
                oyuncu.sendMessage(ChatColor.AQUA + "/sinif " + s.getId() + ChatColor.WHITE
                        + " - " + s.getGoruntulenenAd() + ": " + s.getPasifAciklama());
            }
            oyuncu.sendMessage(ChatColor.GRAY + "Not: İlk seçim ücretsizdir. Sonraki değişimler "
                    + "bekleme süresi ve bedele tabidir.");
            return true;
        }

        Sinif secilen = sinifYoneticisi.getSinif(args[0].toLowerCase());
        if (secilen == null) {
            oyuncu.sendMessage(ChatColor.RED + "Böyle bir sınıf yok. /sinif yazarak listeyi gör.");
            return true;
        }

        boolean basarili = sinifYoneticisi.sinifSec(oyuncu, secilen);
        if (basarili) {
            oyuncu.sendMessage(ChatColor.GREEN + secilen.getGoruntulenenAd() + " sınıfını seçtin! "
                    + "Başlangıç ekipmanın envanterinde.");
        } else {
            oyuncu.sendMessage(ChatColor.RED + "Sınıf seçimi şu anda yapılamıyor, az sonra tekrar dene.");
        }
        return true;
    }
}

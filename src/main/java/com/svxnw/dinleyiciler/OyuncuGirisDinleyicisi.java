package com.svxnw.dinleyiciler;

import com.svxnw.gorevler.GorevYoneticisi;
import com.svxnw.siniflar.SinifYoneticisi;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class OyuncuGirisDinleyicisi implements Listener {

    private final SinifYoneticisi sinifYoneticisi;
    private final GorevYoneticisi gorevYoneticisi;

    public OyuncuGirisDinleyicisi(SinifYoneticisi sinifYoneticisi, GorevYoneticisi gorevYoneticisi) {
        this.sinifYoneticisi = sinifYoneticisi;
        this.gorevYoneticisi = gorevYoneticisi;
    }

    @EventHandler
    public void oyuncuGirdi(PlayerJoinEvent event) {
        var oyuncu = event.getPlayer();
        gorevYoneticisi.gorevVerIlkGirisse(oyuncu);
        oyuncu.sendMessage(ChatColor.GOLD + "SvxNw'e hoş geldin, " + oyuncu.getName() + "!");
        oyuncu.sendMessage(ChatColor.YELLOW + "Sınıf seçmek için /sinif yaz. Görev panosu meydanda seni bekliyor.");
    }
}

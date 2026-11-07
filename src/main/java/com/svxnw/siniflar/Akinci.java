package com.svxnw.siniflar;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * AKINCI - yakın dövüş / tank sınıfı.
 *
 * Pasif: "Çelik Deri" - uğradığı yakın dövüş hasarının %10'unu azaltır
 *        (gerçek uygulama EntityDamageEvent dinleyicisinde yapılmalıdır;
 *        bu sınıf sadece oranı tanımlar, hasar azaltma dinleyicisi Faz 2'de
 *        eklenecektir - bkz. README "Uygulandı/Test Edildi/Eksik" tablosu).
 *
 * Aktif Beceri 1: "Kalkan Hücumu" - 4 blok ileri atılır, önündeki hedeflere
 *        6 kalp (12 damage point) vurur ve 1 sn sersemletir (yavaşlatma
 *        efekti ile simüle edilir). Bekleme süresi: 8 saniye.
 *
 * Aktif Beceri 2: "Savaş Narası" - 6 blok yarıçapındaki tüm dost oyunculara
 *        5 saniye boyunca +1 güç (Strength I) verir. Bekleme süresi: 20 sn.
 */
public class Akinci implements Sinif {

    private static final long BECERI_1_BEKLEME_MS = 8_000L;
    private static final long BECERI_2_BEKLEME_MS = 20_000L;

    @Override
    public String getId() {
        return "akinci";
    }

    @Override
    public String getGoruntulenenAd() {
        return "Akıncı";
    }

    @Override
    public void baslangicEkipmaniVer(Player oyuncu) {
        oyuncu.getInventory().addItem(adlandirilmisEsya(Material.STONE_SWORD, "Akıncı Palası"));
        oyuncu.getInventory().addItem(adlandirilmisEsya(Material.LEATHER_CHESTPLATE, "Akıncı Zırhı"));
        oyuncu.getInventory().addItem(new ItemStack(Material.SHIELD));
        AttributeModifier canModifier = new AttributeModifier(
                "svxnw.akinci.can", 4.0, AttributeModifier.Operation.ADD_NUMBER);
        var canAttr = oyuncu.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (canAttr != null && canAttr.getModifier(canModifier.getUniqueId()) == null) {
            canAttr.addModifier(canModifier);
        }
        oyuncu.setHealth(oyuncu.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
    }

    @Override
    public String getPasifAciklama() {
        return "Çelik Deri: Gelen yakın dövüş hasarının %10'u azaltılır.";
    }

    @Override
    public void aktifBeceri1(Player oyuncu) {
        Location bakis = oyuncu.getLocation();
        Vector yon = bakis.getDirection().normalize().multiply(1.3);
        oyuncu.setVelocity(yon.setY(0.25));
        oyuncu.getWorld().spawnParticle(Particle.CRIT, bakis, 20, 0.5, 0.3, 0.5, 0.1);
        oyuncu.getWorld().playSound(bakis, Sound.ITEM_SHIELD_BLOCK, 1f, 0.8f);

        List<LivingEntity> hedefler = yakinHedefleriBul(oyuncu, 3.5);
        for (LivingEntity hedef : hedefler) {
            hedef.damage(12.0, oyuncu);
            hedef.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 3));
        }
    }

    @Override
    public void aktifBeceri2(Player oyuncu) {
        oyuncu.getWorld().playSound(oyuncu.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1f, 1f);
        oyuncu.getWorld().spawnParticle(Particle.SWEEP_ATTACK, oyuncu.getLocation(), 10);
        for (var yakindaki : oyuncu.getWorld().getNearbyEntities(oyuncu.getLocation(), 6, 3, 6)) {
            if (yakindaki instanceof Player dostOyuncu) {
                dostOyuncu.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20 * 5, 0));
            }
        }
    }

    @Override
    public long getBeceri1BeklemeSuresiMs() {
        return BECERI_1_BEKLEME_MS;
    }

    @Override
    public long getBeceri2BeklemeSuresiMs() {
        return BECERI_2_BEKLEME_MS;
    }

    private List<LivingEntity> yakinHedefleriBul(Player oyuncu, double menzil) {
        return oyuncu.getWorld().getNearbyEntities(oyuncu.getLocation(), menzil, menzil, menzil)
                .stream()
                .filter(e -> e instanceof LivingEntity && e != oyuncu && !(e instanceof Player p && p.getScoreboardTags().contains("svxnw.npc_dokunulmaz")))
                .map(e -> (LivingEntity) e)
                .toList();
    }

    private ItemStack adlandirilmisEsya(Material tur, String ad) {
        ItemStack esya = new ItemStack(tur);
        ItemMeta meta = esya.getItemMeta();
        meta.setDisplayName("§6" + ad);
        esya.setItemMeta(meta);
        return esya;
    }
}

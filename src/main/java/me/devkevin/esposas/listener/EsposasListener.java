package me.devkevin.esposas.listener;

import lombok.Getter;
import me.devkevin.esposas.Esposas;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by DevKevin on jul, 2019
 */
public class EsposasListener implements Listener {

    @Getter public Esposas plugin = Esposas.getInstance();

    private HashMap<String, String> prisionero = new HashMap<String, String>();
    private static ArrayList<String> dmg = new ArrayList<String>();

    @EventHandler(priority= EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        if (entity instanceof Player) {
            Player player1 = (Player)entity;
            if ((player.getItemInHand().getType() == Material.BLAZE_ROD) && ((entity instanceof Player)) &&
                    (player.getItemInHand().hasItemMeta()) &&  player.getItemInHand().getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Esposas")) {
                if (dmg.contains(entity.getName())) {
                    player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "(!) " + ChatColor.RED + "Has arrestado a " + player1.getName());
                }
                else if (dmg.contains(player.getName())) {
                    player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "(!) " + ChatColor.RED + "No puedes arrestarte a ti mismo!");
                }
                else {
                    entity.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "(!) Has sido arrestado por infringir la ley \n ha manos de " + player.getName());
                    player1.sendMessage(ChatColor.YELLOW + "Has arrestado ha " + entity.getName() + " por infringir la ley.");
                    final Pig effect = (Pig)player1.getWorld().spawnEntity(player1.getLocation(), EntityType.PIG);

                    //TODO: Efectos que se le asigna a los detenidos
                    effect.setBaby();
                    effect.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 180000000, 3));
                    effect.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 180000000, 6));
                    effect.setBreed(false);
                    effect.setLeashHolder((Entity)player);
                    effect.setMetadata(new StringBuilder().append(player1.getName()).toString(), (MetadataValue)new FixedMetadataValue((Plugin)this.plugin, (Object)true));
                    this.prisionero.clear();
                    dmg.add(player1.getName());
                    this.prisionero.put(String.valueOf(player.getName()) + "*", String.valueOf(entity.getName()) + "p*");
                    this.plugin.getServer().getScheduler().scheduleAsyncRepeatingTask((Plugin)this.plugin, (Runnable)new Runnable() {
                        @Override
                        public void run() {
                            if (!effect.isDead()) {
                                effect.teleport(player1.getLocation());
                            }
                            EsposasListener.this.plugin.getServer().getScheduler().cancelTask(0);
                        }
                    }, 20L, 1L);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        Entity victim = e.getEntity();
        if (victim instanceof Player) {
            Player player = (Player)victim;
            if (dmg.contains(player.getName()) && e.getCause() == EntityDamageEvent.DamageCause.FALL) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamagePrisionero(EntityDamageByEntityEvent event) {
        Entity entity = event.getDamager();
        Entity victima = event.getEntity();
        if (entity instanceof Player) {
            Player player = (Player)entity;
            if (victima instanceof Player) {
                Player player1 = (Player)victima;
                if (this.prisionero.containsKey(String.valueOf(player.getName()) + "*") && this.prisionero.values().contains(String.valueOf(player1.getName() + "p*"))) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "(!) " + ChatColor.RED + "No puedes golpear a un prisionero mientras estas esposado.");
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onGuardDeath(EntityDeathEvent event) {
        Entity victima = (Entity)event.getEntity();
        if (victima instanceof Player) {
            Player player = (Player)victima;
            if (this.prisionero.containsKey(String.valueOf(player.getName()) + "*")) {
                String s = new StringBuilder().append(this.prisionero.values()).toString();
                String t = s.replace("p*", "").replace("[", "").replace("]", "");
                Player pr = Bukkit.getServer().getPlayer(t);
                EsposasListener.dmg.remove(pr.getName());
                pr.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "(!) " + "Tu guardia ha muerto!");
                this.prisionero.clear();
                for (Entity entity : player.getNearbyEntities(3.0, 3.0, 3.0)) {
                    if (entity instanceof Pig && entity.hasMetadata(new StringBuilder().append(player.getName()).toString())) {
                        entity.remove();
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPrisionerDeath(EntityDeathEvent event) {
        Entity victima = (Entity)event.getEntity();
        if (victima instanceof Player) {
            Player player = (Player)victima;
            if (this.prisionero.values().contains(String.valueOf(player.getName()) + "p*")) {
                String s = new StringBuilder(String.valueOf(this.prisionero.toString())).toString();
                String t = s.replace("{", "")
                        .replace("}", "")
                        .replace(player.getName() + "p*", "")
                        .replace("=", "")
                        .replace("*", "");

                Bukkit.broadcastMessage(new StringBuilder().append(t).toString());
                Player player1 = Bukkit.getServer().getPlayer(t);
                dmg.remove(player.getName());
                player1.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "(!) " + "Tu prisionero ha muerto!");
                this.prisionero.clear();
                for (Entity entity : player.getNearbyEntities(3.0, 3.0, 3.0)) {
                    if (entity instanceof Pig && entity.hasMetadata(new StringBuilder().append(player.getName()).toString())) {
                        entity.remove();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPrisionero(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (this.prisionero.values().contains(String.valueOf(player.getName()) + "p*")) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 4));
            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 60, -8));
        }
    }

    @EventHandler
    public void onPrisioneroGuard(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (this.prisionero.containsKey(String.valueOf(player.getName()) + "*")) {
            String s = new StringBuilder().append(this.prisionero.values()).toString();
            String t = s.replace("p*", "").replace("[", "").replace("]", "");
            Player prisionero = Bukkit.getServer().getPlayer(t);
            if (player.getItemInHand().equals((Object)Material.BLAZE_ROD) && player.getItemInHand().hasItemMeta() && player.getItemInHand().getItemMeta().getDisplayName().equals(
                    ChatColor.GREEN + "Esposas") && (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getZ() != event.getTo().getZ())) {
                Vector direction = player.getLocation().toVector().subtract(prisionero.getLocation().toVector()).normalize();
                prisionero.setVelocity(direction);
            }
        }
    }

    @EventHandler
    public void onPrisioneroDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!prisionero.values().contains(String.valueOf(player.getName()) + "p*")) {
            String s = new StringBuilder(String.valueOf(this.prisionero.toString())).toString();
            String t = s.replace("{", "")
                    .replace("}", "")
                    .replace(player.getName() + "p*", "")
                    .replace("=", "")
                    .replace("*", "");
            Bukkit.broadcastMessage(new StringBuilder().append(t).toString());
            Player prisionero = Bukkit.getServer().getPlayer(t);
            dmg.remove(player.getName());
            prisionero.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "(!) " + ChatColor.RED + "Tu prisionero ha escapado Desconectandose \n " +
                    "Deberias banearlo o tal vez meterlo a la carcel");
            this.prisionero.clear();
            for (Entity entity : player.getNearbyEntities(3.0, 3.0, 3.0)) {
                if (entity instanceof Pig && entity.hasMetadata(new StringBuilder().append(player.getName()).toString())) {
                    entity.remove();
                }
            }
        }
    }

    @EventHandler
    public void onGuardiaDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (this.prisionero.containsKey(String.valueOf(player.getName()) + "*")) {
            String s = new StringBuilder().append(this.prisionero.values()).toString();
            String t = s.replace("p*", "").replace("[", "").replace("]", "");
            Player prisionero = Bukkit.getServer().getPlayer(t);
            dmg.remove(prisionero.getName());
            prisionero.sendMessage(ChatColor.GREEN + "Has escapado por que tu guardia se ha desconectado.");
            this.prisionero.clear();
            for (Entity entity : player.getNearbyEntities(3.0, 3.0, 3.0)) {
                if (entity instanceof Pig && entity.hasMetadata(new StringBuilder().append(player.getName()).toString())) {
                    entity.remove();
                }
            }
        }
    }

    @EventHandler
    public void onPlayerEsposasInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if ((player.getItemInHand().getType() == Material.BLAZE_ROD) && (event.getAction() == Action.LEFT_CLICK_AIR &&
                (player.getItemInHand().hasItemMeta()) &&
                (player.getItemInHand().getItemMeta().getDisplayName().equals(ChatColor.GOLD + "Esposas")))) {
            if (this.prisionero.containsKey(player.getName() + "*")) {
                String s = new StringBuilder().append(this.prisionero.values()).toString();
                String t = s.replace("p*", "").replace("[", "").replace("]", "");
                Player pr = Bukkit.getServer().getPlayer(t);
                this.prisionero.clear();
                pr.sendMessage(ChatColor.GREEN + "Ya no eres el prisionero de " + player.getName());
                dmg.remove(pr.getName());
                player.sendMessage("Has liberado al prisionero " + pr.getName());
                for (Entity entity : pr.getNearbyEntities(3.0D, 3.0D, 3.0D)) {
                    if (((entity instanceof Pig)) &&
                            (entity.hasMetadata(pr.getName()))) {
                        entity.remove();
                    }
                }
            }
        }
    }

    public void System(Player player, String item, int amount, String name) {
        ItemStack item1 = new ItemStack(Material.getMaterial(item.toUpperCase()), amount);
        ItemMeta itemMeta = item1.getItemMeta();
        ArrayList<String> itemMeta1 = new ArrayList<>();
        itemMeta.setDisplayName(name.replace("&", "§"));
        itemMeta.setLore((List)itemMeta1);
        item1.setItemMeta(itemMeta);
        player.getInventory().addItem(new ItemStack[] { item1});
    }
}

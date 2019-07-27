package me.devkevin.esposas.command;

import me.devkevin.esposas.Esposas;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
/**
 * Created by DevKevin on jul, 2019
 */
public class EsposasCommand extends Command {

    private Esposas plugin = Esposas.getInstance();

    public EsposasCommand() {
        super("esposas");
        this.setUsage(ChatColor.RED + "Usage: /esposas");
        this.setDescription("Esposas Command");
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {

        Player player = (Player)sender;

        if (sender.hasPermission("esposas.give")) {
            if (args.length == 0) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "(!) La consola no puede ejecutar este permiso, solo jugadores desde el juego.");
                }
                else {
                    Player player1 = (Player)sender;
                    this.plugin.getEsposasListener().System(player, "blaze_rod", 1, ChatColor.GREEN + "Esposas");
                    sender.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + "(!) " + ChatColor.GREEN + "Te haz dado las esposas.");
                }
            }
        }

        if (args.length == 1) {
            Player target = Bukkit.getServer().getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + args[0] + "no se encuentra conectado.");
            }
            else {
                this.plugin.getEsposasListener().System(target,"blaze_rod", 1, ChatColor.GREEN + "Esposas");
                sender.sendMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + "Haz dado unas esposas a " + target.getName());
                if (sender.getName() != target.getName()) {
                    target.sendMessage(ChatColor.YELLOW.toString() + ChatColor.BOLD + "(!) " + ChatColor.GREEN +
                            "Haz recibido unas esposas de parte de " + sender.getName());
                }
            }
        }
        return false;
    }
}

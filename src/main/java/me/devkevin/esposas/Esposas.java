package me.devkevin.esposas;

import lombok.Getter;
import me.devkevin.esposas.command.EsposasCommand;
import me.devkevin.esposas.listener.EsposasListener;
import net.minecraft.server.v1_13_R2.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

/**
 * Created by DevKevin on jul, 2019
 */
@Getter
public class Esposas extends JavaPlugin {

    @Getter private static Esposas instance;

    private EsposasListener esposasListener;

    @Override
    public void onEnable() {
        Esposas.instance = this;

        this.registerCommands();
        this.registerListeners();

        this.esposasListener = new EsposasListener();

        Bukkit.getPluginManager().registerEvents(new EsposasListener(), this);

        getLogger().info("El plugin de Esosas ha sido cargado correctamente.");
    }


    @Override
    public void onDisable() {
        for (Player player : getServer().getOnlinePlayers()) {
            player.kickPlayer(ChatColor.RED + "The Server is restarting.");
        }

        this.saveDefaultConfig();
        this.saveConfig();
        this.reloadConfig();
    }

    private void registerListeners() {
        Arrays.asList(
                new EsposasListener()

        ).forEach(listener -> this.getServer().getPluginManager().registerEvents(listener, this));
    }

    private void registerCommands() {
        Arrays.asList(
                new EsposasCommand()

        ).forEach(command -> this.registerCommand(command, getName()));
    }

    public void registerCommand(Command command, String fallbackPrefix) {
        MinecraftServer.getServer().server.getCommandMap().register(command.getName(), fallbackPrefix, command);
    }

    public void registerCommand(Command command) {
        this.registerCommand(command, this.getName());
    }
}

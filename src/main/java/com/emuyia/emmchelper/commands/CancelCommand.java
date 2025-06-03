package com.emuyia.emmchelper.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.emuyia.emmchelper.MCHelperPlugin;

public class CancelCommand implements CommandExecutor {
    private final MCHelperPlugin plugin;

    public CancelCommand(MCHelperPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MCHelperPlugin.MSG_PLAYER_ONLY);
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("emmc_resetorigin.cancel")) {
            player.sendMessage(MCHelperPlugin.MSG_NO_PERMISSION);
            return true;
        }

        if (plugin.hasPendingReset(player)) {
            plugin.removePendingReset(player);
            player.sendMessage(MCHelperPlugin.MSG_PREFIX + ChatColor.YELLOW + "Origin reset request cancelled.");
        } else {
            player.sendMessage(MCHelperPlugin.MSG_PREFIX + ChatColor.RED + "You don't have a pending origin reset to cancel.");
        }
        return true;
    }
}

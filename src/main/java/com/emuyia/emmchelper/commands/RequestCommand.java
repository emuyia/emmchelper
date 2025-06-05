package com.emuyia.emmchelper.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.emuyia.emmchelper.MCHelperPlugin;
import com.emuyia.emmchelper.utils.TimeFormatter;

public class RequestCommand implements CommandExecutor {
    private final MCHelperPlugin plugin;

    public RequestCommand(MCHelperPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MCHelperPlugin.MSG_PLAYER_ONLY);
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("emmc_resetorigin.request")) {
            player.sendMessage(MCHelperPlugin.MSG_NO_PERMISSION);
            return true;
        }

        if (plugin.isOnCooldown(player)) {
            long remainingMillis = plugin.getRemainingCooldown(player);
            player.sendMessage(MCHelperPlugin.ORIGIN_RESET_MSG_PREFIX + ChatColor.RED + "You are on cooldown. Time remaining: " +
                               TimeFormatter.formatMillis(remainingMillis));
            return true;
        }

        if (!plugin.hasEnoughXP(player)) {
            player.sendMessage(MCHelperPlugin.ORIGIN_RESET_MSG_PREFIX + ChatColor.RED + "You need " + plugin.xpCost +
                               " XP levels. You have " + player.getLevel() + ".");
            return true;
        }

        plugin.addPendingReset(player);
        player.sendMessage(MCHelperPlugin.ORIGIN_RESET_MSG_PREFIX + ChatColor.YELLOW + "Are you sure you want to reset your origin for " +
                           plugin.xpCost + " XP levels?");
        player.sendMessage(MCHelperPlugin.ORIGIN_RESET_MSG_PREFIX + ChatColor.GREEN + "Type /confirmoriginreset to confirm or /canceloriginreset to cancel.");
        return true;
    }
}

package com.emuyia.emmchelper.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.emuyia.emmchelper.MCHelperPlugin;
import com.emuyia.emmchelper.utils.TimeFormatter;

public class ConfirmCommand implements CommandExecutor {
    private final MCHelperPlugin plugin;

    public ConfirmCommand(MCHelperPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MCHelperPlugin.MSG_PLAYER_ONLY);
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("emmc_resetorigin.confirm")) {
            player.sendMessage(MCHelperPlugin.MSG_NO_PERMISSION);
            return true;
        }

        if (!plugin.hasPendingReset(player)) {
            player.sendMessage(MCHelperPlugin.ORIGIN_RESET_MSG_PREFIX + ChatColor.RED + "You don't have a pending origin reset request.");
            return true;
        }

        // Double-check cooldown and XP in case something changed
        if (plugin.isOnCooldown(player)) {
            long remainingMillis = plugin.getRemainingCooldown(player);
            player.sendMessage(MCHelperPlugin.ORIGIN_RESET_MSG_PREFIX + ChatColor.RED + "You went on cooldown. Time remaining: " +
                               TimeFormatter.formatMillis(remainingMillis));
            plugin.removePendingReset(player);
            return true;
        }

        if (!plugin.hasEnoughXP(player)) {
            player.sendMessage(MCHelperPlugin.ORIGIN_RESET_MSG_PREFIX + ChatColor.RED + "You no longer have enough XP (" + plugin.xpCost +
                               " levels). You have " + player.getLevel() + ".");
            plugin.removePendingReset(player);
            return true;
        }

        plugin.deductXP(player);
        plugin.executeOriginClearCommand(player);
        plugin.setCooldown(player);
        plugin.removePendingReset(player);

        player.sendMessage(MCHelperPlugin.ORIGIN_RESET_MSG_PREFIX + ChatColor.GREEN + "Your origin has been reset for " +
                           plugin.xpCost + " XP levels!");
        long nextAvailable = plugin.getRemainingCooldown(player);
        if (nextAvailable > 0) {
             player.sendMessage(MCHelperPlugin.ORIGIN_RESET_MSG_PREFIX + ChatColor.YELLOW + "You can reset again in " + TimeFormatter.formatMillis(nextAvailable) + ".");
        }
        return true;
    }
}

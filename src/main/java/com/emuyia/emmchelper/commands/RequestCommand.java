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

        if (plugin.hasPendingReset(player)) {
            player.sendMessage(MCHelperPlugin.ORIGIN_RESET_MSG_PREFIX + ChatColor.YELLOW + "You already have a pending reset request. Type /confirmoriginreset or /canceloriginreset.");
            return true;
        }

        if (plugin.isOnCooldown(player)) {
            if (!plugin.hasEnoughXP(player)) {
                player.sendMessage(MCHelperPlugin.ORIGIN_RESET_MSG_PREFIX + ChatColor.RED + "You are on cooldown and do not have enough XP (" + plugin.xpCost +
                                   " levels) to reset early.");
                long remainingMillis = plugin.getRemainingCooldown(player);
                player.sendMessage(MCHelperPlugin.ORIGIN_RESET_MSG_PREFIX + ChatColor.RED + "Time remaining on cooldown: " +
                                   TimeFormatter.formatMillis(remainingMillis));
                return true;
            }
            plugin.addPendingReset(player, MCHelperPlugin.PendingResetType.PAID_RESET_ON_COOLDOWN);
            player.sendMessage(MCHelperPlugin.ORIGIN_RESET_MSG_PREFIX + ChatColor.YELLOW +
                               "You are currently on cooldown. Are you sure you want to reset your origin for " +
                               plugin.xpCost + " XP levels?");
            player.sendMessage(MCHelperPlugin.ORIGIN_RESET_MSG_PREFIX + ChatColor.YELLOW +
                               "This will reset your cooldown timer, starting a new " + TimeFormatter.formatMillis(plugin.resetCooldownMillis) + " cooldown.");
            long remainingMillis = plugin.getRemainingCooldown(player);
            player.sendMessage(MCHelperPlugin.ORIGIN_RESET_MSG_PREFIX + ChatColor.GRAY + "Current cooldown remaining: " +
                               TimeFormatter.formatMillis(remainingMillis));

        } else { // Not on cooldown, so it's a free reset that will start the cooldown
            plugin.addPendingReset(player, MCHelperPlugin.PendingResetType.FREE_RESET);
            player.sendMessage(MCHelperPlugin.ORIGIN_RESET_MSG_PREFIX + ChatColor.YELLOW +
                               "Are you sure you want to reset your origin for free?");
            player.sendMessage(MCHelperPlugin.ORIGIN_RESET_MSG_PREFIX + ChatColor.YELLOW +
                               "This will start your " + TimeFormatter.formatMillis(plugin.resetCooldownMillis) + " cooldown.");
        }

        player.sendMessage(MCHelperPlugin.ORIGIN_RESET_MSG_PREFIX + ChatColor.GREEN + "Type /confirmoriginreset to confirm or /canceloriginreset to cancel.");
        return true;
    }
}

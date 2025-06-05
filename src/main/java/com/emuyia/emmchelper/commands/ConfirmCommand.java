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

        MCHelperPlugin.PendingResetType resetType = plugin.getPendingResetType(player);
        plugin.removePendingReset(player); // Remove pending state immediately

        if (resetType == MCHelperPlugin.PendingResetType.PAID_RESET_ON_COOLDOWN) {
            // This is a paid reset while on cooldown. Cooldown WILL be reset.
            if (!plugin.isOnCooldown(player)) { 
                player.sendMessage(MCHelperPlugin.ORIGIN_RESET_MSG_PREFIX + ChatColor.RED + "Your cooldown seems to have expired. Please try requesting again for a free reset.");
                return true;
            }
            if (!plugin.hasEnoughXP(player)) {
                player.sendMessage(MCHelperPlugin.ORIGIN_RESET_MSG_PREFIX + ChatColor.RED + "You no longer have enough XP (" + plugin.xpCost +
                                   " levels). You have " + player.getLevel() + ".");
                return true;
            }

            plugin.deductXP(player);
            plugin.executeOriginClearCommand(player);
            plugin.setCooldown(player); // Reset the cooldown

            player.sendMessage(MCHelperPlugin.ORIGIN_RESET_MSG_PREFIX + ChatColor.GREEN + "Your origin has been reset for " +
                               plugin.xpCost + " XP levels!");
            long newCooldownDuration = plugin.getRemainingCooldown(player); // This will be the full new cooldown
            player.sendMessage(MCHelperPlugin.ORIGIN_RESET_MSG_PREFIX + ChatColor.YELLOW + "Your cooldown has been reset. New cooldown: " + TimeFormatter.formatMillis(newCooldownDuration) + ".");


        } else if (resetType == MCHelperPlugin.PendingResetType.FREE_RESET) {
            // This is a free reset, cooldown should now start.
            if (plugin.isOnCooldown(player)) {
                // This case should ideally not happen if request logic is correct,
                // but as a safeguard:
                player.sendMessage(MCHelperPlugin.ORIGIN_RESET_MSG_PREFIX + ChatColor.RED + "You seem to have gone on cooldown. Please try requesting again.");
                return true;
            }

            plugin.executeOriginClearCommand(player);
            plugin.setCooldown(player); // Start the cooldown

            player.sendMessage(MCHelperPlugin.ORIGIN_RESET_MSG_PREFIX + ChatColor.GREEN + "Your origin has been reset for free!");
            long nextAvailable = plugin.getRemainingCooldown(player); // This will be the full cooldown duration
            player.sendMessage(MCHelperPlugin.ORIGIN_RESET_MSG_PREFIX + ChatColor.YELLOW + "Your cooldown has started. You can reset again (for XP) or wait " + TimeFormatter.formatMillis(nextAvailable) + " for a free reset.");

        } else {
            // Should not happen
            player.sendMessage(MCHelperPlugin.ORIGIN_RESET_MSG_PREFIX + ChatColor.RED + "Error determining reset type. Please cancel and try again.");
            plugin.getLogger().warning("Unknown PendingResetType for player " + player.getName());
        }

        return true;
    }
}

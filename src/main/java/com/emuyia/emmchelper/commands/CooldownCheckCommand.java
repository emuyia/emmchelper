package com.emuyia.emmchelper.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.emuyia.emmchelper.MCHelperPlugin;
import com.emuyia.emmchelper.utils.TimeFormatter;

public class CooldownCheckCommand implements CommandExecutor {
    private final MCHelperPlugin plugin;

    public CooldownCheckCommand(MCHelperPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(MCHelperPlugin.MSG_PLAYER_ONLY);
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("emmc_resetorigin.cooldowncheck")) {
            player.sendMessage(MCHelperPlugin.MSG_NO_PERMISSION);
            return true;
        }

        if (plugin.isOnCooldown(player)) {
            long remainingMillis = plugin.getRemainingCooldown(player);
            player.sendMessage(MCHelperPlugin.MSG_PREFIX + ChatColor.YELLOW + "You can reset your origin again in: " +
                               TimeFormatter.formatMillis(remainingMillis));
        } else {
            player.sendMessage(MCHelperPlugin.MSG_PREFIX + ChatColor.GREEN + "Your origin reset cooldown is over. You can request a reset now.");
        }
        return true;
    }
}

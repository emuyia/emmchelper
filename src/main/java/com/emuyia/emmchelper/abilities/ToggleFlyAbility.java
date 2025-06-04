package com.emuyia.emmchelper.abilities;

import com.emuyia.emmchelper.MCHelperPlugin;
import com.starshootercity.abilities.Ability;
import com.starshootercity.abilities.Key;
import com.starshootercity.abilities.VisibleAbility;
import com.starshootercity.abilities.TriggerableAbility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;


public class ToggleFlyAbility implements Ability, VisibleAbility, TriggerableAbility {
    private final MCHelperPlugin plugin;

    public ToggleFlyAbility(MCHelperPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key(plugin.getNamespace(), "toggle_fly");
    }

    @Override
    public @NotNull Component getName() {
        return Component.text("Toggle Fly").color(NamedTextColor.AQUA);
    }

    @Override
    public @NotNull List<Component> getDescription() {
        return List.of(
                Component.text("Press your ability key to toggle flight.").color(NamedTextColor.GRAY),
                Component.text("Requires EssentialsX /fly command.").color(NamedTextColor.DARK_GRAY)
        );
    }

    @Override
    public @NotNull ItemStack getIcon() {
        return new ItemStack(Material.FEATHER);
    }

    @Override
    public void onTrigger(@NotNull Player player, @NotNull TriggeringReason reason, @Nullable Location location, @Nullable Block block, @Nullable Entity entity, @Nullable ItemStack itemStack) {
        // Execute the /fly command. Player needs essentials.fly permission.
        boolean currentFlyState = player.getAllowFlight();
        Bukkit.dispatchCommand(player, "fly"); // EssentialsX /fly toggles based on current state

        // Send feedback based on the state *after* the command might have changed it.
        // It's hard to know the exact outcome of /fly without more EssentialsX integration,
        // so we assume it toggled successfully.
        if (!currentFlyState) { // If they couldn't fly before, now they (presumably) can
            player.sendMessage(MCHelperPlugin.MSG_PREFIX + Component.text("Flight enabled.").color(NamedTextColor.GREEN));
        } else { // If they could fly before, now they (presumably) can't
            player.sendMessage(MCHelperPlugin.MSG_PREFIX + Component.text("Flight disabled.").color(NamedTextColor.YELLOW));
        }
    }
}

package com.emuyia.emmchelper.abilities;

import com.emuyia.emmchelper.MCHelperPlugin;
import com.starshootercity.abilities.Ability;
import com.starshootercity.abilities.Key;
import com.starshootercity.abilities.VisibleAbility;
import com.starshootercity.abilities.TriggerableAbility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;


public class ToggleInvisibilityAbility implements Ability, VisibleAbility, TriggerableAbility {
    private final MCHelperPlugin plugin;

    public ToggleInvisibilityAbility(MCHelperPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key(plugin.getNamespace(), "toggle_invisibility");
    }

    @Override
    public @NotNull Component getName() {
        return Component.text("Toggle Invisibility").color(NamedTextColor.GRAY);
    }

    @Override
    public @NotNull List<Component> getDescription() {
        return List.of(
                Component.text("Press your ability key to toggle invisibility.").color(NamedTextColor.GRAY)
        );
    }

    @Override
    public @NotNull ItemStack getIcon() {
        // You could also create a Potion ItemStack with an invisibility effect
        return new ItemStack(Material.GLASS_PANE);
    }

    @Override
    public void onTrigger(@NotNull Player player, @NotNull TriggeringReason reason, @Nullable Location location, @Nullable Block block, @Nullable Entity entity, @Nullable ItemStack itemStack) {
        if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
            player.sendMessage(MCHelperPlugin.MSG_PREFIX + Component.text("You are now visible.").color(NamedTextColor.YELLOW));
        } else {
            // Apply invisibility indefinitely, without particles, with an icon
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 0, false, false, true));
            player.sendMessage(MCHelperPlugin.MSG_PREFIX + Component.text("You are now invisible.").color(NamedTextColor.GREEN));
        }
    }
}

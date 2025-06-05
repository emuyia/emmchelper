package com.emuyia.emmchelper.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import com.emuyia.emmchelper.MCHelperPlugin;
import com.starshootercity.abilities.types.Ability;
import com.starshootercity.abilities.types.TriggerableAbility;
import com.starshootercity.abilities.types.VisibleAbility;
import com.starshootercity.util.TriggerManager;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

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
    public @NotNull String title() {
        return "Toggle Invisibility";
    }

    public @NotNull Component getName() {
        return Component.text("Toggle Invisibility").color(NamedTextColor.GRAY);
    }

    @Override
    public @NotNull String description() {
        return "Trigger to toggle invisibility.";
    }

    public @NotNull ItemStack getIcon() {
        return new ItemStack(Material.GLASS_PANE);
    }

    @Override
    public char getDefaultKeybind() {
        return 'H';
    }

    @Override
    public @NotNull Trigger getTrigger() {
        TriggerType defaultTriggerType = TriggerType.DOUBLE_TAP_SNEAK;

        TriggerRunner runner = (TriggerManager.TriggerEvent event) -> {
            Player player = event.player();
            if (player == null) return;

            if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
            } else {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 0, false, false, true));
            }
        };
        return Trigger.builder(defaultTriggerType, this)
                .addConditions(Condition.DUMMY)
                .build(runner);
    }
}

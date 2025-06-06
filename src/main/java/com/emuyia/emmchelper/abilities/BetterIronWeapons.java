package com.emuyia.emmchelper.abilities;

import com.emuyia.emmchelper.MCHelperPlugin;
import com.starshootercity.abilities.types.VisibleAbility;
import com.starshootercity.util.config.ConfigManager.SettingType;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class BetterIronWeapons implements VisibleAbility, Listener {
    private final MCHelperPlugin plugin;
    private final String damageMultiplierConfigKey = "damage_multiplier";
    private final Key abilityKey = Key.key("emmchelper", "better_iron_weapons");

    public BetterIronWeapons(MCHelperPlugin plugin) {
        this.plugin = plugin;
        // Initialize config option with MCHelperPlugin's method
        this.plugin.registerAbilityConfigOption(
                this.abilityKey,
                damageMultiplierConfigKey,
                Collections.singletonList("Damage multiplier for iron weapons."),
                SettingType.FLOAT,
                1.5F // Default multiplier for iron weapons
        );
    }

    @Override
    public @NotNull String description() {
        return "Your affinity for iron makes iron weapons unbreakable and stronger.";
    }

    @Override
    public @NotNull String title() {
        return "Iron Edge";
    }

    @Override
    public @NotNull Key getKey() {
        return this.abilityKey;
    }

    @EventHandler
    public void onPlayerItemDamage(PlayerItemDamageEvent event) {
        Material itemType = event.getItem().getType();
        if (itemType == Material.IRON_SWORD || itemType == Material.IRON_AXE) {
            this.runForAbility(event.getPlayer(), (player) -> {
                event.setCancelled(true);
            });
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            ItemStack item = player.getInventory().getItemInMainHand();
            Material itemType = item.getType();
            if (itemType == Material.IRON_SWORD || itemType == Material.IRON_AXE) {
                this.runForAbility(player, (p) -> {
                    float multiplier = plugin.getAbilityConfigOption(
                            this.abilityKey,
                            damageMultiplierConfigKey,
                            SettingType.FLOAT,
                            1.5F
                    );
                    event.setDamage(event.getDamage() * multiplier);
                });
            }
        }
    }
}
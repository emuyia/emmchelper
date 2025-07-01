package com.emuyia.emmchelper.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import com.emuyia.emmchelper.MCHelperPlugin;
import com.starshootercity.abilities.types.DependantAbility;
import com.starshootercity.abilities.types.VisibleAbility;

import net.kyori.adventure.key.Key;

public class NoInventoryWhileFlyingAbility implements DependantAbility, VisibleAbility, Listener {

    private final MCHelperPlugin plugin;
    private final ToggleFlyAbility toggleFlyAbilityInstance;

    public NoInventoryWhileFlyingAbility(MCHelperPlugin plugin, ToggleFlyAbility toggleFlyAbilityInstance) {
        this.plugin = plugin;
        this.toggleFlyAbilityInstance = toggleFlyAbilityInstance;
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("emmchelper", "no_inventory_while_flying");
    }

    @Override
    public @NotNull Key getDependencyKey() {
        return this.toggleFlyAbilityInstance.getKey();
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player player) {
            if (this.hasAbility(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            if (this.hasAbility(player)) {
                event.setCancelled(true);
            }
        }
    }

    // Implement the required String methods
    @Override
    public String title() {
        return "Flight Restriction";
    }

    @Override
    public String description() {
        return "You cannot use your inventory while flying.";
    }

    // This is a custom method, not from the interface, so it has no @Override
    public @NotNull ItemStack getIcon() {
        return new ItemStack(Material.BARRIER);
    }
}

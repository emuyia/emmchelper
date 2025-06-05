package com.emuyia.emmchelper.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import com.emuyia.emmchelper.MCHelperPlugin;
import com.starshootercity.Origin;
import com.starshootercity.OriginSwapper;
import com.starshootercity.abilities.types.Ability;
import com.starshootercity.abilities.types.VisibleAbility;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class NoInventoryWhileFlyingAbility implements Ability, VisibleAbility, Listener {
    private final MCHelperPlugin plugin;
    private static final String DEFAULT_ORIGIN_LAYER = "origin";

    public NoInventoryWhileFlyingAbility(MCHelperPlugin plugin) {
        this.plugin = plugin;
        // Constructor logging can be kept if desired for initial load, or removed.
        // For this request, I'll remove the specific [NoInvFlyDEBUG] ones.
        // this.plugin.getLogger().info("[NoInvFlyDEBUG] Constructor for NoInventoryWhileFlyingAbility called.");
        try {
            this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
            // this.plugin.getLogger().info("[NoInvFlyDEBUG] Listener registration for NoInventoryWhileFlyingAbility SUCCEEDED in constructor.");
        } catch (Exception e) {
            this.plugin.getLogger().log(java.util.logging.Level.SEVERE, "Listener registration for NoInventoryWhileFlyingAbility FAILED in constructor:", e);
        }
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key(plugin.getNamespace(), "no_inventory_while_flying");
    }

    @Override
    public @NotNull String title() {
        return "Flight Restriction";
    }

    public @NotNull Component getName() {
        return Component.text("Flight Restriction").color(NamedTextColor.RED);
    }

    @Override
    public @NotNull String description() {
        return "Prevents opening your inventory or interacting with it while flying.";
    }

    public @NotNull ItemStack getIcon() {
        return new ItemStack(Material.BARRIER);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getPlayer();
        Key abilityKeyToCheck = getKey();

        if (event.getInventory().getType() == InventoryType.PLAYER) {
            return;
        }

        Origin playerOrigin = null;
        try {
            playerOrigin = OriginSwapper.getOrigin(player, DEFAULT_ORIGIN_LAYER);
        } catch (NoClassDefFoundError | NoSuchMethodError e) {
            plugin.getLogger().severe("Critical API mismatch for OriginSwapper.getOrigin: " + e.getMessage());
            return;
        } catch (Exception e) {
            // Silently fail or log minimally for non-critical errors during event
        }

        if (playerOrigin != null) {
            if (playerOrigin.hasAbility(abilityKeyToCheck)) {
                if (player.isFlying()) {
                    event.setCancelled(true);
                    // player.sendMessage(MCHelperPlugin.ABILITY_MSG_PREFIX.append(Component.text("You cannot open this while flying.").color(NamedTextColor.RED))); // Removed message
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        Key abilityKeyToCheck = getKey();

        if (event.getView().getType() == InventoryType.CRAFTING || event.getView().getType() == InventoryType.PLAYER) {
            if (event.getClickedInventory() == null) {
                return;
            }

            Origin playerOrigin = null;
            try {
                playerOrigin = OriginSwapper.getOrigin(player, DEFAULT_ORIGIN_LAYER);
            } catch (Exception e) {
                // Silently fail or log minimally
            }

            if (playerOrigin != null && playerOrigin.hasAbility(abilityKeyToCheck)) {
                if (player.isFlying()) {
                    event.setCancelled(true);
                    // player.sendMessage(MCHelperPlugin.ABILITY_MSG_PREFIX.append(Component.text("You cannot interact with your inventory while flying.").color(NamedTextColor.RED))); // Removed message
                }
            }
        }
    }
}

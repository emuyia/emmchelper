package com.emuyia.emmchelper.abilities;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent; // + Import
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType; // + Import
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
        if (this.plugin == null || this.plugin.getLogger() == null) {
            System.out.println("[NoInvFlyDEBUG] CRITICAL: Plugin or Logger is NULL in NoInventoryWhileFlyingAbility constructor!");
            return;
        }
        this.plugin.getLogger().info("[NoInvFlyDEBUG] Constructor for NoInventoryWhileFlyingAbility called.");
        try {
            this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
            this.plugin.getLogger().info("[NoInvFlyDEBUG] Listener registration for NoInventoryWhileFlyingAbility SUCCEEDED in constructor.");
        } catch (Exception e) {
            this.plugin.getLogger().log(java.util.logging.Level.SEVERE, "[NoInvFlyDEBUG] Listener registration for NoInventoryWhileFlyingAbility FAILED in constructor:", e);
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
        return "Prevents opening your inventory or interacting with it while flying."; // Updated description
    }

    public @NotNull ItemStack getIcon() {
        return new ItemStack(Material.BARRIER);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        // This handler is for external inventories like chests, crafting tables, etc.
        // It will NOT fire for the player's own inventory screen opened by 'E'.
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getPlayer();
        Key abilityKeyToCheck = getKey();

        // Prevent console spam for player inventory type if it ever gets here by mistake
        if (event.getInventory().getType() == InventoryType.PLAYER) {
            return;
        }

        plugin.getLogger().info("[NoInvFlyDEBUG] InventoryOpenEvent triggered for player: " + player.getName() + ", Inventory type: " + event.getInventory().getType());

        Origin playerOrigin = null;
        try {
            // plugin.getLogger().info("[NoInvFlyDEBUG] Attempting to get origin for " + player.getName() + " on layer '" + DEFAULT_ORIGIN_LAYER + "'");
            playerOrigin = OriginSwapper.getOrigin(player, DEFAULT_ORIGIN_LAYER);
            // if (playerOrigin != null) {
            //     plugin.getLogger().info("[NoInvFlyDEBUG] Successfully retrieved origin for " + player.getName() + ": " + playerOrigin.getName());
            // } else {
            //     plugin.getLogger().warning("[NoInvFlyDEBUG] OriginSwapper.getOrigin returned null for " + player.getName() + " on layer '" + DEFAULT_ORIGIN_LAYER + "'");
            // }
        } catch (NoClassDefFoundError | NoSuchMethodError e) {
            plugin.getLogger().severe("[NoInvFlyDEBUG] Critical API mismatch for OriginSwapper.getOrigin: " + e.getMessage());
            return;
        } catch (Exception e) {
            plugin.getLogger().warning("[NoInvFlyDEBUG] Error retrieving origin for " + player.getName() + ": " + e.getMessage());
        }

        if (playerOrigin != null) {
            // plugin.getLogger().info("[NoInvFlyDEBUG] Checking if origin '" + playerOrigin.getName() + "' has ability '" + abilityKeyToCheck + "' for player " + player.getName());
            if (playerOrigin.hasAbility(abilityKeyToCheck)) {
                plugin.getLogger().info("[NoInvFlyDEBUG] Player " + player.getName() + " (Origin: " + playerOrigin.getName() + ") HAS NoInventoryWhileFlyingAbility for InventoryOpenEvent.");
                // plugin.getLogger().info("[NoInvFlyDEBUG] Checking if player " + player.getName() + " is flying. Current state: " + player.isFlying());
                if (player.isFlying()) {
                    plugin.getLogger().info("[NoInvFlyDEBUG] Player " + player.getName() + " is flying. Cancelling " + event.getInventory().getType() + " open.");
                    event.setCancelled(true);
                    player.sendMessage(MCHelperPlugin.ABILITY_MSG_PREFIX.append(Component.text("You cannot open this while flying.").color(NamedTextColor.RED)));
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

        // Check if the click is within the player's own inventory screen
        // (when 'E' is pressed, the view type is CRAFTING for the 2x2 grid)
        if (event.getView().getType() == InventoryType.CRAFTING || event.getView().getType() == InventoryType.PLAYER) {
            // Further check: ensure a clicked inventory exists (not clicking outside GUI)
            if (event.getClickedInventory() == null) {
                return;
            }

            // plugin.getLogger().info("[NoInvFlyDEBUG] InventoryClickEvent in player's own inventory screen for " + player.getName());

            Origin playerOrigin = null;
            try {
                playerOrigin = OriginSwapper.getOrigin(player, DEFAULT_ORIGIN_LAYER);
            } catch (Exception e) {
                // Minor logging for click event, as it can be spammy
                // plugin.getLogger().warning("[NoInvFlyDEBUG] Error retrieving origin on click for " + player.getName() + ": " + e.getMessage());
            }

            if (playerOrigin != null && playerOrigin.hasAbility(abilityKeyToCheck)) {
                // plugin.getLogger().info("[NoInvFlyDEBUG] Player " + player.getName() + " HAS NoInventoryWhileFlyingAbility (for click).");
                if (player.isFlying()) {
                    plugin.getLogger().info("[NoInvFlyDEBUG] Player " + player.getName() + " is flying. Cancelling click in own inventory (type: " + event.getClickedInventory().getType() + ", slot: " + event.getSlot() + ").");
                    event.setCancelled(true);
                    // Sending a message on every click can be spammy. Consider if needed.
                    // player.sendMessage(MCHelperPlugin.ABILITY_MSG_PREFIX.append(Component.text("You cannot interact with your inventory while flying.").color(NamedTextColor.RED)));
                }
            }
        }
    }
}

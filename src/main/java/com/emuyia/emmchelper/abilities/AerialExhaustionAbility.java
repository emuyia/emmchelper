package com.emuyia.emmchelper.abilities;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import com.emuyia.emmchelper.MCHelperPlugin;
import com.emuyia.emmchelper.abilities.ToggleFlyAbility; // + Import ToggleFlyAbility
import com.starshootercity.Origin;
import com.starshootercity.OriginSwapper;
import com.starshootercity.abilities.types.Ability;
import com.starshootercity.abilities.types.VisibleAbility;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class AerialExhaustionAbility implements Ability, VisibleAbility, Listener {
    private final MCHelperPlugin plugin;
    private final ToggleFlyAbility toggleFlyAbilityInstance; // + Store ToggleFlyAbility instance
    private static final String DEFAULT_ORIGIN_LAYER = "origin";

    private static final int CHECK_INTERVAL_TICKS = 100;
    private static final float SATURATION_DEPLETION_PER_INTERVAL = 1.0f; // Adjusted back from 6.0f for example
    private static final int FOOD_DEPLETION_PER_INTERVAL = 1;

    private BukkitTask hungerTask;

    public AerialExhaustionAbility(MCHelperPlugin plugin, ToggleFlyAbility toggleFlyAbilityInstance) { // + Modify constructor
        this.plugin = plugin;
        this.toggleFlyAbilityInstance = toggleFlyAbilityInstance; // + Assign instance
        startHungerTask();
    }

    private void startHungerTask() {
        if (hungerTask != null && !hungerTask.isCancelled()) {
            hungerTask.cancel();
        }
        // plugin.getLogger().info("[AerialExhaustionDEBUG] Starting hunger task."); // Debug log
        hungerTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.isOnline() || player.isDead()) continue;

                    Origin playerOrigin = null;
                    boolean hasAbility = false;
                    try {
                        playerOrigin = OriginSwapper.getOrigin(player, DEFAULT_ORIGIN_LAYER);
                        if (playerOrigin != null && playerOrigin.hasAbility(getKey())) {
                            hasAbility = true;
                        }
                    } catch (Exception e) {
                        // plugin.getLogger().warning("[AerialExhaustionDEBUG] Error getting origin for " + player.getName() + ": " + e.getMessage());
                    }

                    if (hasAbility && player.isFlying()) {
                        // plugin.getLogger().info("[AerialExhaustionDEBUG] Player " + player.getName() + " has ability and is flying. Processing hunger.");
                        float currentSaturation = player.getSaturation();
                        int currentFoodLevel = player.getFoodLevel();
                        // plugin.getLogger().info("[AerialExhaustionDEBUG] " + player.getName() + " - Before: Food=" + currentFoodLevel + ", Sat=" + currentSaturation);

                        if (currentFoodLevel <= 0) {
                            // plugin.getLogger().info("[AerialExhaustionDEBUG] " + player.getName() + " food is <= 0. Forcing stop flying.");
                            forceStopFlying(player, "You are too hungry to fly!");
                            continue;
                        }

                        if (currentSaturation > 0) {
                            float newSaturation = Math.max(0, currentSaturation - SATURATION_DEPLETION_PER_INTERVAL);
                            player.setSaturation(newSaturation);
                            // plugin.getLogger().info("[AerialExhaustionDEBUG] " + player.getName() + " depleted saturation. New Sat=" + newSaturation);
                        } else {
                            int newFoodLevel = Math.max(0, currentFoodLevel - FOOD_DEPLETION_PER_INTERVAL);
                            player.setFoodLevel(newFoodLevel);
                            // plugin.getLogger().info("[AerialExhaustionDEBUG] " + player.getName() + " depleted food. New Food=" + newFoodLevel);
                            if (newFoodLevel <= 0) {
                                // plugin.getLogger().info("[AerialExhaustionDEBUG] " + player.getName() + " new food is <= 0 after depletion. Forcing stop flying.");
                                forceStopFlying(player, "You ran out of energy and can no longer fly!");
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, CHECK_INTERVAL_TICKS);
    }

    private void forceStopFlying(Player player, String message) {
        // plugin.getLogger().info("[AerialExhaustionDEBUG] Forcing " + player.getName() + " to stop flying. Reason: " + message);
        if (this.toggleFlyAbilityInstance != null) {
            this.toggleFlyAbilityInstance.forceSetPlayerFlightState(player, false);
        } else {
            // Fallback if toggleFlyAbilityInstance is somehow null, though it shouldn't be with proper instantiation
            plugin.getLogger().warning("[AerialExhaustionDEBUG] ToggleFlyAbility instance was null. Using direct Bukkit calls to stop flight for " + player.getName());
            if (player.isFlying()) player.setFlying(false);
            if (player.getAllowFlight()) player.setAllowFlight(false);
        }
        // player.sendMessage(MCHelperPlugin.ABILITY_MSG_PREFIX.append(Component.text(message).color(NamedTextColor.RED)));
    }

    public void cancelHungerTask() {
        if (hungerTask != null && !hungerTask.isCancelled()) {
            // plugin.getLogger().info("[AerialExhaustionDEBUG] Cancelling hunger task.");
            hungerTask.cancel();
            hungerTask = null;
        }
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key(plugin.getNamespace(), "aerial_exhaustion");
    }

    @Override
    public @NotNull String title() {
        return "Aerial Exhaustion";
    }

    public @NotNull Component getName() {
        return Component.text("Aerial Exhaustion").color(NamedTextColor.DARK_GREEN);
    }

    @Override
    public @NotNull String description() {
        return "Flying rapidly depletes your hunger. You will stop flying if you run out of food.";
    }

    public @NotNull ItemStack getIcon() {
        return new ItemStack(Material.ROTTEN_FLESH);
    }
}
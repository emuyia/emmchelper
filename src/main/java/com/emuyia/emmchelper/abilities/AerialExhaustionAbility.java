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
import com.emuyia.emmchelper.abilities.ToggleFlyAbility;
import com.starshootercity.Origin;
import com.starshootercity.OriginSwapper;
import com.starshootercity.abilities.types.Ability;
import com.starshootercity.abilities.types.VisibleAbility;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class AerialExhaustionAbility implements Ability, VisibleAbility, Listener {
    private final MCHelperPlugin plugin;
    private final ToggleFlyAbility toggleFlyAbilityInstance;
    private static final String DEFAULT_ORIGIN_LAYER = "origin";

    private static final int CHECK_INTERVAL_TICKS = 100; // 5 seconds, as per your current setting

    // Normal flying depletion rates per interval
    private static final float FLYING_SATURATION_DEPLETION = 1.0f;
    private static final int FLYING_FOOD_DEPLETION = 1;

    // Sprinting while flying depletion rates per interval (e.g., 3x faster)
    private static final float SPRINT_FLYING_SATURATION_DEPLETION = 3.0f;
    private static final int SPRINT_FLYING_FOOD_DEPLETION = 3;

    private BukkitTask hungerTask;

    public AerialExhaustionAbility(MCHelperPlugin plugin, ToggleFlyAbility toggleFlyAbilityInstance) {
        this.plugin = plugin;
        this.toggleFlyAbilityInstance = toggleFlyAbilityInstance;
        startHungerTask();
    }

    private void startHungerTask() {
        if (hungerTask != null && !hungerTask.isCancelled()) {
            hungerTask.cancel();
        }
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
                        // Silently ignore
                    }

                    if (hasAbility && player.isFlying()) {
                        float saturationToDepleteThisTick = FLYING_SATURATION_DEPLETION;
                        int foodToDepleteThisTick = FLYING_FOOD_DEPLETION;
                        String debugReason = "flying";

                        if (player.isSprinting()) {
                            saturationToDepleteThisTick = SPRINT_FLYING_SATURATION_DEPLETION;
                            foodToDepleteThisTick = SPRINT_FLYING_FOOD_DEPLETION;
                            debugReason = "sprint-flying";
                        }
                        
                        // plugin.getLogger().info(String.format("[AerialExhaustionDEBUG] %s: %s. SatDeplete: %.1f, FoodDeplete: %d", player.getName(), debugReason, saturationToDepleteThisTick, foodToDepleteThisTick));


                        float currentSaturation = player.getSaturation();
                        int currentFoodLevel = player.getFoodLevel();

                        if (currentFoodLevel <= 0) {
                            forceStopFlying(player, "You are too hungry to fly!");
                            continue;
                        }

                        if (currentSaturation > 0) {
                            player.setSaturation(Math.max(0, currentSaturation - saturationToDepleteThisTick));
                        } else {
                            int newFoodLevel = Math.max(0, currentFoodLevel - foodToDepleteThisTick);
                            player.setFoodLevel(newFoodLevel);
                            if (newFoodLevel <= 0) {
                                forceStopFlying(player, "You ran out of energy and can no longer fly!");
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, CHECK_INTERVAL_TICKS);
    }

    private void forceStopFlying(Player player, String message) {
        if (this.toggleFlyAbilityInstance != null) {
            this.toggleFlyAbilityInstance.forceSetPlayerFlightState(player, false);
        } else {
            if (player.isFlying()) player.setFlying(false);
            if (player.getAllowFlight()) player.setAllowFlight(false);
        }
        // Message sending is commented out in your provided file, keeping it that way
        // player.sendMessage(MCHelperPlugin.ABILITY_MSG_PREFIX.append(Component.text(message).color(NamedTextColor.RED)));
    }

    public void cancelHungerTask() {
        if (hungerTask != null && !hungerTask.isCancelled()) {
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
        return "Flying depletes hunger. Sprinting while flying depletes it much faster. You stop flying if you run out of food.";
    }

    public @NotNull ItemStack getIcon() {
        return new ItemStack(Material.ROTTEN_FLESH);
    }
}
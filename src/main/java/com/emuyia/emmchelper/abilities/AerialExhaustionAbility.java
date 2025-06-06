package com.emuyia.emmchelper.abilities;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler; // + Import EventHandler
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent; // + Import PlayerQuitEvent
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

import java.util.Map; // + Import Map
import java.util.UUID; // + Import UUID
import java.util.concurrent.ConcurrentHashMap; // + Import ConcurrentHashMap

public class AerialExhaustionAbility implements Ability, VisibleAbility, Listener {
    private final MCHelperPlugin plugin;
    private final ToggleFlyAbility toggleFlyAbilityInstance;
    private static final String DEFAULT_ORIGIN_LAYER = "origin";

    // Updated for more frequent checks (e.g., every 1 second)
    private static final int CHECK_INTERVAL_TICKS = 20; // Check every 1 second

    // Adjusted depletion rates for the new CHECK_INTERVAL_TICKS (rates per 1 second)
    // Old rates were per 5 seconds (100 ticks)
    // Flying: 1.0 sat / 5s = 0.2 sat / 1s.  1 food / 5s = 0.2 food_exhaustion / 1s
    // Sprinting: 3.0 sat / 5s = 0.6 sat / 1s. 3 food / 5s = 0.6 food_exhaustion / 1s

    private static final float FLYING_SATURATION_DEPLETION_PER_INTERVAL = 0.2f;
    private static final float FLYING_FOOD_EXHAUSTION_PER_INTERVAL = 0.2f;

    private static final float SPRINT_FLYING_SATURATION_DEPLETION_PER_INTERVAL = 0.6f;
    private static final float SPRINT_FLYING_FOOD_EXHAUSTION_PER_INTERVAL = 0.6f;

    private BukkitTask hungerTask;
    private final Map<UUID, Float> playerFoodExhaustion = new ConcurrentHashMap<>(); // + Accumulator for food

    public AerialExhaustionAbility(MCHelperPlugin plugin, ToggleFlyAbility toggleFlyAbilityInstance) {
        this.plugin = plugin;
        this.toggleFlyAbilityInstance = toggleFlyAbilityInstance;
        // Register listener for PlayerQuitEvent if not already done by MCHelperPlugin for all listeners
        // Bukkit.getPluginManager().registerEvents(this, plugin); // Only if MCHelperPlugin doesn't register its listeners globally
        startHungerTask();
    }

    @EventHandler // + Handle player quitting to clear their exhaustion data
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerFoodExhaustion.remove(event.getPlayer().getUniqueId());
    }

    private void startHungerTask() {
        if (hungerTask != null && !hungerTask.isCancelled()) {
            hungerTask.cancel();
        }
        hungerTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.isOnline() || player.isDead()) {
                        playerFoodExhaustion.remove(player.getUniqueId()); // Clean up if somehow missed
                        continue;
                    }

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
                        float saturationToDeplete = FLYING_SATURATION_DEPLETION_PER_INTERVAL;
                        float foodExhaustionToAdd = FLYING_FOOD_EXHAUSTION_PER_INTERVAL;

                        if (player.isSprinting()) {
                            saturationToDeplete = SPRINT_FLYING_SATURATION_DEPLETION_PER_INTERVAL;
                            foodExhaustionToAdd = SPRINT_FLYING_FOOD_EXHAUSTION_PER_INTERVAL;
                        }
                        
                        float currentSaturation = player.getSaturation();
                        int currentFoodLevel = player.getFoodLevel();

                        if (currentFoodLevel <= 0 && currentSaturation <=0) { // Check both if saturation is also a factor for stopping
                            forceStopFlying(player, "You are too hungry to fly!");
                            continue;
                        }

                        // Deplete saturation first
                        if (currentSaturation > 0) {
                            player.setSaturation(Math.max(0, currentSaturation - saturationToDeplete));
                        } else {
                            // If no saturation, apply exhaustion to food
                            float currentExhaustion = playerFoodExhaustion.getOrDefault(player.getUniqueId(), 0.0f);
                            currentExhaustion += foodExhaustionToAdd;

                            if (currentExhaustion >= 1.0f) {
                                int foodToActuallyDeplete = (int) Math.floor(currentExhaustion);
                                int newFoodLevel = Math.max(0, currentFoodLevel - foodToActuallyDeplete);
                                player.setFoodLevel(newFoodLevel);
                                currentExhaustion -= foodToActuallyDeplete;
                                
                                if (newFoodLevel <= 0) {
                                    forceStopFlying(player, "You ran out of energy and can no longer fly!");
                                    playerFoodExhaustion.remove(player.getUniqueId()); // Clear exhaustion as they can't fly
                                    continue; 
                                }
                            }
                            playerFoodExhaustion.put(player.getUniqueId(), currentExhaustion);
                        }
                    } else {
                        // If player is not flying or doesn't have the ability, ensure their exhaustion is cleared
                        // or slowly decays if you prefer, but clearing is simpler.
                        // playerFoodExhaustion.remove(player.getUniqueId()); 
                        // Optional: Only remove if they had an entry and are no longer flying with ability.
                        // This prevents removing exhaustion if they briefly stop flying for other reasons.
                        // A more robust way is to only clear if they *were* flying with ability and now are not.
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
        playerFoodExhaustion.remove(player.getUniqueId()); // Clear exhaustion when forced to stop
        // player.sendMessage(MCHelperPlugin.ABILITY_MSG_PREFIX.append(Component.text(message).color(NamedTextColor.RED)));
    }

    public void cancelHungerTask() {
        if (hungerTask != null && !hungerTask.isCancelled()) {
            hungerTask.cancel();
            hungerTask = null;
        }
        playerFoodExhaustion.clear(); // Clear all exhaustion data when task is cancelled (e.g. plugin disable)
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
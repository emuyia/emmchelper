package com.emuyia.emmchelper.abilities;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import com.emuyia.emmchelper.MCHelperPlugin;
import com.starshootercity.abilities.types.DependantAbility;
import com.starshootercity.abilities.types.VisibleAbility;

import net.kyori.adventure.key.Key;

public class AerialExhaustionAbility implements DependantAbility, VisibleAbility, Listener {
    private final MCHelperPlugin plugin;
    private final ToggleFlyAbility toggleFlyAbilityInstance;
    private static final int CHECK_INTERVAL_TICKS = 20;
    private static final float FLYING_FOOD_EXHAUSTION_PER_INTERVAL = 0.2f;
    private static final float SPRINT_FLYING_FOOD_EXHAUSTION_PER_INTERVAL = 0.6f;

    private BukkitTask hungerTask;
    private final Map<UUID, Float> playerFoodExhaustion = new ConcurrentHashMap<>();

    public AerialExhaustionAbility(MCHelperPlugin plugin, ToggleFlyAbility toggleFlyAbilityInstance) {
        this.plugin = plugin;
        this.toggleFlyAbilityInstance = toggleFlyAbilityInstance;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
        startHungerTask();
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("emmchelper", "aerial_exhaustion");
    }

    @Override
    public @NotNull Key getDependencyKey() {
        return this.toggleFlyAbilityInstance.getKey();
    }

    @EventHandler
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
                        playerFoodExhaustion.remove(player.getUniqueId());
                        continue;
                    }

                    if (hasAbility(player)) {
                        float foodExhaustionToAdd = player.isSprinting() ? SPRINT_FLYING_FOOD_EXHAUSTION_PER_INTERVAL : FLYING_FOOD_EXHAUSTION_PER_INTERVAL;

                        if (player.getFoodLevel() <= 0 && player.getSaturation() <= 0) {
                            forceStopFlying(player, "You are too hungry to fly!");
                            continue;
                        }

                        if (player.getSaturation() > 0) {
                            player.setSaturation(Math.max(0, player.getSaturation() - foodExhaustionToAdd));
                        } else {
                            float currentExhaustion = playerFoodExhaustion.getOrDefault(player.getUniqueId(), 0.0f);
                            currentExhaustion += foodExhaustionToAdd;

                            if (currentExhaustion >= 1.0f) {
                                int foodToDeplete = (int) Math.floor(currentExhaustion);
                                int newFoodLevel = Math.max(0, player.getFoodLevel() - foodToDeplete);
                                player.setFoodLevel(newFoodLevel);
                                currentExhaustion -= foodToDeplete;

                                if (newFoodLevel <= 0) {
                                    forceStopFlying(player, "You ran out of energy and can no longer fly!");
                                }
                            }
                            playerFoodExhaustion.put(player.getUniqueId(), currentExhaustion);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, CHECK_INTERVAL_TICKS);
    }

    private void forceStopFlying(Player player, String message) {
        if (this.toggleFlyAbilityInstance != null) {
            this.toggleFlyAbilityInstance.forceSetPlayerFlightState(player, false);
        }
        playerFoodExhaustion.remove(player.getUniqueId());
    }

    public void cancelTask() {
        if (hungerTask != null && !hungerTask.isCancelled()) {
            hungerTask.cancel();
        }
    }

    @Override
    public String title() {
        return "Aerial Exhaustion";
    }

    @Override
    public String description() {
        return "Flying depletes hunger. Sprinting while flying depletes it much faster.";
    }

    public @NotNull ItemStack getIcon() {
        return new ItemStack(Material.ROTTEN_FLESH);
    }
}
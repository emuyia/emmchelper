package com.emuyia.emmchelper.abilities;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent; // Added for cleanup
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import com.emuyia.emmchelper.MCHelperPlugin;
import com.starshootercity.Origin;
import com.starshootercity.OriginSwapper;
import com.starshootercity.abilities.types.Ability;
import com.starshootercity.abilities.types.VisibleAbility;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AerialAffinityAbility implements Ability, VisibleAbility, Listener {
    private final MCHelperPlugin plugin;
    private static final String DEFAULT_ORIGIN_LAYER = "origin";
    private static final PotionEffectType HASTE_EFFECT_TYPE = PotionEffectType.HASTE;
    private static final int HASTE_AMPLIFIER = 19; // Haste XX
    // Using a slightly shorter duration that's refreshed, instead of true infinite,
    // can sometimes be more robust with server restarts or plugin reloads.
    // 200 ticks = 10 seconds. The task runs every 0.5s, so it will be refreshed.
    private static final int HASTE_DURATION_TICKS = 200;

    private BukkitTask effectTask;
    // Optional: to track who we've given haste to, to be slightly more efficient on removal
    private final ConcurrentHashMap<UUID, Boolean> hasHasteFromThisAbility = new ConcurrentHashMap<>();


    public AerialAffinityAbility(MCHelperPlugin plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin); // For PlayerQuitEvent
        startEffectTask();
    }

    private void startEffectTask() {
        if (effectTask != null && !effectTask.isCancelled()) {
            effectTask.cancel();
        }
        effectTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.isOnline()) continue; // Should not happen with Bukkit.getOnlinePlayers() but good check

                    Origin playerOrigin = null;
                    boolean hasAbility = false;
                    try {
                        playerOrigin = OriginSwapper.getOrigin(player, DEFAULT_ORIGIN_LAYER);
                        if (playerOrigin != null && playerOrigin.hasAbility(getKey())) {
                            hasAbility = true;
                        }
                    } catch (Exception e) {
                        // Silently ignore, don't spam console from a repeating task
                    }

                    boolean isFlying = player.isFlying();

                    if (hasAbility && isFlying) {
                        // Apply/Refresh Haste
                        // Check if they already have the exact effect to avoid re-applying constantly if not needed
                        PotionEffect currentHaste = player.getPotionEffect(HASTE_EFFECT_TYPE);
                        if (currentHaste == null || currentHaste.getAmplifier() != HASTE_AMPLIFIER || currentHaste.getDuration() < (HASTE_DURATION_TICKS / 2)) {
                             player.addPotionEffect(new PotionEffect(HASTE_EFFECT_TYPE, HASTE_DURATION_TICKS, HASTE_AMPLIFIER, true, false, true));
                        }
                        hasHasteFromThisAbility.put(player.getUniqueId(), true);
                    } else {
                        // Remove Haste if they have it from this ability
                        if (hasHasteFromThisAbility.containsKey(player.getUniqueId())) {
                            PotionEffect existingEffect = player.getPotionEffect(HASTE_EFFECT_TYPE);
                            if (existingEffect != null && existingEffect.getAmplifier() == HASTE_AMPLIFIER) {
                                player.removePotionEffect(HASTE_EFFECT_TYPE);
                            }
                            hasHasteFromThisAbility.remove(player.getUniqueId());
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L); // Run every 10 ticks (0.5 seconds)
    }

    public void cancelEffectTask() {
        if (effectTask != null && !effectTask.isCancelled()) {
            effectTask.cancel();
            effectTask = null;
        }
        // Clean up any remaining haste effects from players when plugin disables
        for (UUID playerId : hasHasteFromThisAbility.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                PotionEffect existingEffect = player.getPotionEffect(HASTE_EFFECT_TYPE);
                if (existingEffect != null && existingEffect.getAmplifier() == HASTE_AMPLIFIER) {
                    player.removePotionEffect(HASTE_EFFECT_TYPE);
                }
            }
        }
        hasHasteFromThisAbility.clear();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clean up map when player quits
        hasHasteFromThisAbility.remove(event.getPlayer().getUniqueId());
    }


    @Override
    public @NotNull Key getKey() {
        return Key.key(plugin.getNamespace(), "aerial_affinity");
    }

    @Override
    public @NotNull String title() {
        return "Aerial Affinity";
    }

    public @NotNull Component getName() {
        return Component.text("Aerial Affinity").color(NamedTextColor.GOLD);
    }

    @Override
    public @NotNull String description() {
        return "You break blocks at normal speed while flying.";
    }

    public @NotNull ItemStack getIcon() {
        return new ItemStack(Material.DIAMOND_PICKAXE);
    }
}
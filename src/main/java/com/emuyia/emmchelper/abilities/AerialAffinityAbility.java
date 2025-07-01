package com.emuyia.emmchelper.abilities;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import com.emuyia.emmchelper.MCHelperPlugin;
import com.starshootercity.abilities.types.DependantAbility;
import com.starshootercity.abilities.types.VisibleAbility;

import net.kyori.adventure.key.Key;

public class AerialAffinityAbility implements DependantAbility, VisibleAbility, Listener {
    private final MCHelperPlugin plugin;
    private final ToggleFlyAbility toggleFlyAbilityInstance;
    private static final PotionEffectType HASTE_EFFECT_TYPE = PotionEffectType.HASTE;
    private static final int HASTE_AMPLIFIER = 19;
    private static final int HASTE_DURATION_TICKS = 200;

    private BukkitTask effectTask;
    private final ConcurrentHashMap<UUID, Boolean> hasHasteFromThisAbility = new ConcurrentHashMap<>();

    public AerialAffinityAbility(MCHelperPlugin plugin, ToggleFlyAbility toggleFlyAbilityInstance) {
        this.plugin = plugin;
        this.toggleFlyAbilityInstance = toggleFlyAbilityInstance;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
        startEffectTask();
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("emmchelper", "aerial_affinity");
    }

    @Override
    public @NotNull Key getDependencyKey() {
        return this.toggleFlyAbilityInstance.getKey();
    }

    private void startEffectTask() {
        if (effectTask != null && !effectTask.isCancelled()) {
            effectTask.cancel();
        }
        effectTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.isOnline()) continue;

                    if (hasAbility(player)) {
                        PotionEffect currentHaste = player.getPotionEffect(HASTE_EFFECT_TYPE);
                        if (currentHaste == null || currentHaste.getAmplifier() != HASTE_AMPLIFIER || currentHaste.getDuration() < (HASTE_DURATION_TICKS / 2)) {
                             player.addPotionEffect(new PotionEffect(HASTE_EFFECT_TYPE, HASTE_DURATION_TICKS, HASTE_AMPLIFIER, true, false, true));
                        }
                        hasHasteFromThisAbility.put(player.getUniqueId(), true);
                    } else {
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
        }.runTaskTimer(plugin, 0L, 10L);
    }

    public void cancelTask() {
        if (effectTask != null && !effectTask.isCancelled()) {
            effectTask.cancel();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        hasHasteFromThisAbility.remove(event.getPlayer().getUniqueId());
    }

    @Override
    public String title() {
        return "Aerial Affinity";
    }

    @Override
    public String description() {
        return "You break blocks at normal speed while flying.";
    }

    public @NotNull ItemStack getIcon() {
        return new ItemStack(Material.DIAMOND_PICKAXE);
    }
}
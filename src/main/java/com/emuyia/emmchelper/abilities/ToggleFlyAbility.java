package com.emuyia.emmchelper.abilities;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import com.emuyia.emmchelper.MCHelperPlugin;
import com.starshootercity.abilities.types.Ability;
import com.starshootercity.abilities.types.FlightAllowingAbility;
import com.starshootercity.abilities.types.TriggerableAbility;
import com.starshootercity.abilities.types.VisibleAbility;
import com.starshootercity.util.TriggerManager;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.TriState;

public class ToggleFlyAbility implements Ability, VisibleAbility, TriggerableAbility, FlightAllowingAbility {
    private final MCHelperPlugin plugin;
    private final Map<UUID, Boolean> playerFlightToggleState = new ConcurrentHashMap<>();
    private static final float DEFAULT_FLY_SPEED = 0.1f;

    public ToggleFlyAbility(MCHelperPlugin plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPlayerQuit(PlayerQuitEvent event) {
                playerFlightToggleState.remove(event.getPlayer().getUniqueId());
            }
        }, plugin);
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key(plugin.getNamespace(), "toggle_fly");
    }

    @Override
    public @NotNull String title() {
        return "Toggle Fly";
    }

    public @NotNull Component getName() {
        return Component.text("Toggle Fly").color(NamedTextColor.AQUA);
    }

    @Override
    public @NotNull String description() {
        return "Trigger to toggle flight.";
    }

    public @NotNull ItemStack getIcon() {
        return new ItemStack(Material.FEATHER);
    }

    @Override
    public char getDefaultKeybind() {
        return 'G';
    }

    @Override
    public boolean canFly(Player player) {
        return playerFlightToggleState.getOrDefault(player.getUniqueId(), false);
    }

    @Override
    public float getFlightSpeed(Player player) {
        return DEFAULT_FLY_SPEED;
    }

    @Override
    public boolean forceFly(Player player) {
        return canFly(player);
    }

    @Override
    public @NotNull TriState getFlyingFallDamage(Player player) {
        if (canFly(player)) {
            return TriState.FALSE;
        }
        return TriState.NOT_SET;
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public @NotNull Trigger getTrigger() {
        TriggerType defaultTriggerType = TriggerType.LEFT_CLICK;

        TriggerRunner runner = (TriggerManager.TriggerEvent event) -> {
            Player player = event.player();
            if (player == null) return;

            if (!plugin.isEnabled()) {
                return;
            }

            boolean currentToggleState = playerFlightToggleState.getOrDefault(player.getUniqueId(), false);
            forceSetPlayerFlightState(player, !currentToggleState);
        };
        return Trigger.builder(defaultTriggerType, this)
                .addConditions(Condition.EMPTY_HAND, Condition.NO_BLOCK)
                .build(runner);
    }

    /**
     * Sets the flight state for a player, updating both Bukkit API and internal tracking.
     * This method can be called internally by the trigger or externally by other abilities.
     * @param player The player whose flight state is to be changed.
     * @param enableFlight True to enable flight, false to disable.
     */
    public void forceSetPlayerFlightState(Player player, boolean enableFlight) {
        if (!player.isOnline()) {
            playerFlightToggleState.remove(player.getUniqueId());
            return;
        }

        playerFlightToggleState.put(player.getUniqueId(), enableFlight);

        if (enableFlight) {
            player.setAllowFlight(true);
            player.setFlySpeed(DEFAULT_FLY_SPEED);
            player.setFlying(true);
        } else {
            player.setFlying(false);
            player.setAllowFlight(false);
            player.setFlySpeed(DEFAULT_FLY_SPEED);
        }
    }
}

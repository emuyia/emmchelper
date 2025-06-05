package com.emuyia.emmchelper.abilities;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler; // For PlayerQuitEvent listener
import org.bukkit.event.Listener;  // For PlayerQuitEvent listener
import org.bukkit.event.player.PlayerQuitEvent; // For PlayerQuitEvent listener
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
    private static final float DEFAULT_FLY_SPEED = 0.1f; // Bukkit's default

    public ToggleFlyAbility(MCHelperPlugin plugin) {
        this.plugin = plugin;
        // Listener to clean up the map on player quit
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

    // --- FlightAllowingAbility Implementation ---

    @Override
    public boolean canFly(Player player) {
        boolean canFly = playerFlightToggleState.getOrDefault(player.getUniqueId(), false);
        // plugin.getLogger().info("[ToggleFlyAbility][FlightAllowingAbility] canFly for " + player.getName() + ": " + canFly);
        return canFly;
    }

    @Override
    public float getFlightSpeed(Player player) {
        // If this ability allows flight, return our defined speed.
        // Otherwise, let other abilities or defaults handle it (though Origins might just use the player's current speed).
        // plugin.getLogger().info("[ToggleFlyAbility][FlightAllowingAbility] getFlightSpeed for " + player.getName() + " returning " + DEFAULT_FLY_SPEED);
        return DEFAULT_FLY_SPEED;
    }

    @Override
    public boolean forceFly(Player player) {
        // If this ability intends for the player to fly, signal that flight should be forced.
        // Origins-Reborn might use this to ensure setFlying(true) is called or maintained.
        boolean shouldForce = canFly(player);
        // plugin.getLogger().info("[ToggleFlyAbility][FlightAllowingAbility] forceFly for " + player.getName() + " called. Returning: " + shouldForce);
        return shouldForce;
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

    // --- TriggerableAbility Implementation ---
    @Override
    public @NotNull Trigger getTrigger() {
        TriggerType defaultTriggerType = TriggerType.DOUBLE_TAP_SNEAK;

        TriggerRunner runner = (TriggerManager.TriggerEvent event) -> {
            Player player = event.player();
            if (player == null) return;

            if (!plugin.isEnabled()) {
                plugin.getLogger().warning("[ToggleFlyAbility] Triggered for player " + player.getName() +
                                           " but the plugin instance is disabled. Aborting.");
                return;
            }

            boolean currentToggleState = playerFlightToggleState.getOrDefault(player.getUniqueId(), false);
            boolean newToggleState = !currentToggleState;
            playerFlightToggleState.put(player.getUniqueId(), newToggleState);

            plugin.getLogger().info("[ToggleFlyAbility] Triggered for player: " + player.getName() +
                                   ". New toggle state: " + (newToggleState ? "ON" : "OFF") +
                                   ". (Was isFlying(): " + player.isFlying() + ", allowFlight(): " + player.getAllowFlight() + ")");


            if (newToggleState) { // Intending to ENABLE flight
                plugin.getLogger().info("[ToggleFlyAbility] Enabling flight for " + player.getName());
                if (!player.isOnline()) {
                    plugin.getLogger().warning("[ToggleFlyAbility] Action for " + player.getName() + " aborted as player offline.");
                    playerFlightToggleState.remove(player.getUniqueId());
                    return;
                }

                player.setAllowFlight(true);
                player.setFlySpeed(DEFAULT_FLY_SPEED); // Explicitly set fly speed
                plugin.getLogger().info("[ToggleFlyAbility] After setAllowFlight(true) and setFlySpeed(" + DEFAULT_FLY_SPEED + "): player.getAllowFlight() = " + player.getAllowFlight() + ", player.getFlySpeed() = " + player.getFlySpeed());

                if (player.isOnGround()) {
                    player.setVelocity(player.getVelocity().add(new Vector(0, 0.1, 0)));
                    plugin.getLogger().info("[ToggleFlyAbility] Nudged player upwards slightly.");
                }

                player.setFlying(true);
                plugin.getLogger().info("[ToggleFlyAbility] After setFlying(true): player.isFlying() = " + player.isFlying());

                if (player.getAllowFlight() && player.isFlying()) {
                    player.sendMessage(MCHelperPlugin.ABILITY_MSG_PREFIX.append(Component.text("Flight enabled by ability.").color(NamedTextColor.GREEN)));
                    plugin.getLogger().info("[ToggleFlyAbility] Flight successfully enabled by ability.");
                } else {
                    player.sendMessage(MCHelperPlugin.ABILITY_MSG_PREFIX.append(Component.text("Attempted to enable flight by ability.").color(NamedTextColor.GOLD)));
                    plugin.getLogger().warning("[ToggleFlyAbility] Problem enabling flight. AllowFlight: " + player.getAllowFlight() + ", IsFlying: " + player.isFlying());
                }

            } else { // Intending to DISABLE flight
                plugin.getLogger().info("[ToggleFlyAbility] Disabling flight for " + player.getName());
                player.setFlying(false);
                player.setAllowFlight(false);
                player.setFlySpeed(DEFAULT_FLY_SPEED); // Reset fly speed to default
                playerFlightToggleState.put(player.getUniqueId(), false);
                player.sendMessage(MCHelperPlugin.ABILITY_MSG_PREFIX.append(Component.text("Flight disabled by ability.").color(NamedTextColor.YELLOW)));
                plugin.getLogger().info("[ToggleFlyAbility] After disabling: player.getAllowFlight() = " + player.getAllowFlight() + ", player.isFlying() = " + player.isFlying());
            }
            // Consider if Origins-Reborn needs an explicit update call after ability state changes
            // e.g., if (plugin.getOriginsAPI() != null) plugin.getOriginsAPI().getAbilityManager().updateAbilities(player);
        };
        return Trigger.builder(defaultTriggerType, this)
                .addConditions(Condition.DUMMY)
                .build(runner);
    }
}

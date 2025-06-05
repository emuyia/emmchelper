package com.emuyia.emmchelper.abilities;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull; // Import the new interface

import com.emuyia.emmchelper.MCHelperPlugin;
import com.starshootercity.abilities.types.Ability;
import com.starshootercity.abilities.types.FlightAllowingAbility;
import com.starshootercity.abilities.types.TriggerableAbility;
import com.starshootercity.abilities.types.VisibleAbility;
import com.starshootercity.util.TriggerManager;

import net.kyori.adventure.key.Key; // Import TriState
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.TriState;

// Implement FlightAllowingAbility
public class ToggleFlyAbility implements Ability, VisibleAbility, TriggerableAbility, FlightAllowingAbility {
    private final MCHelperPlugin plugin;
    // Map to store the flight state for each player, managed by this ability
    private final Map<UUID, Boolean> playerFlightToggleState = new ConcurrentHashMap<>();

    public ToggleFlyAbility(MCHelperPlugin plugin) {
        this.plugin = plugin;
        // Consider adding a PlayerQuitEvent listener here to clean up the map
        // For example: plugin.getServer().getPluginManager().registerEvents(new Listener() {
        //     @EventHandler
        //     public void onPlayerQuit(PlayerQuitEvent event) {
        //         playerFlightToggleState.remove(event.getPlayer().getUniqueId());
        //     }
        // }, plugin);
        // This is important for long-term server stability to prevent memory leaks.
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
        // If this ability has toggled flight ON for the player, they can fly.
        // Otherwise, this ability doesn't grant flight permission via this method.
        boolean canFly = playerFlightToggleState.getOrDefault(player.getUniqueId(), false);
        // plugin.getLogger().info("[ToggleFlyAbility][FlightAllowingAbility] canFly for " + player.getName() + ": " + canFly);
        return canFly;
    }

    @Override
    public float getFlightSpeed(Player player) {
        // Return a default flight speed. You could make this configurable.
        // player.getFlySpeed() returns the player's current fly speed, which is a good default.
        return player.getFlySpeed(); // Bukkit's default is 0.1f
    }

    @Override
    public boolean forceFly(Player player) {
        // This method's role in Origins-Reborn needs to be fully understood.
        // If Origins calls this to *make* a player fly when canFly() is true,
        // then player.setFlying(true) could go here.
        // However, our trigger already handles player.setFlying().
        // Let's assume for now that Origins uses canFly() to manage player.setAllowFlight(),
        // and our trigger handles the direct player.setFlying().
        // If returning true here makes Origins call setFlying(true), it might be beneficial.
        // For now, returning false to avoid potential double-calls or conflicts with the trigger.
        // plugin.getLogger().info("[ToggleFlyAbility][FlightAllowingAbility] forceFly for " + player.getName() + " called.");
        return false;
    }

    @Override
    public @NotNull TriState getFlyingFallDamage(Player player) {
        // If this ability grants flight, it should probably negate fall damage while it's active.
        // However, to keep it simple and avoid conflicts, TriState.NOT_SET lets Origins or other systems decide.
        // If flight is active due to this ability, negating fall damage makes sense.
        if (canFly(player)) {
            return TriState.FALSE; // No fall damage if this ability allows flight
        }
        return TriState.NOT_SET; // Let other abilities/systems decide
    }

    @Override
    public int getPriority() {
        // Set a priority. Higher numbers are often higher priority.
        // If this ability should strongly dictate flight, give it a higher priority.
        // If it's a supplementary flight, a lower one. Defaulting to 1 as in the interface.
        return 10; // Example: Give it a moderate to high priority
    }

    // --- TriggerableAbility Implementation ---
    @Override
    public @NotNull Trigger getTrigger() {
        TriggerType defaultTriggerType = TriggerType.LEFT_CLICK;

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
                    playerFlightToggleState.remove(player.getUniqueId()); // Clean up state
                    return;
                }

                player.setAllowFlight(true); // Explicitly set, Origins should now respect this due to canFly()
                plugin.getLogger().info("[ToggleFlyAbility] After setAllowFlight(true): player.getAllowFlight() = " + player.getAllowFlight());

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
                player.setAllowFlight(false); // Revoke permission
                playerFlightToggleState.put(player.getUniqueId(), false); // Ensure state is false
                player.sendMessage(MCHelperPlugin.ABILITY_MSG_PREFIX.append(Component.text("Flight disabled by ability.").color(NamedTextColor.YELLOW)));
                plugin.getLogger().info("[ToggleFlyAbility] After disabling: player.getAllowFlight() = " + player.getAllowFlight() + ", player.isFlying() = " + player.isFlying());
            }
            // It might be necessary to tell Origins to re-evaluate flight abilities for the player here,
            // if it doesn't do so automatically after an ability state changes.
            // E.g., if Origins has an API like: OriginsRebornAPI.getAbilityManager().updateAbilities(player);
        };
        return Trigger.builder(defaultTriggerType, this)
                .addConditions(Condition.DUMMY)
                .build(runner);
    }
}

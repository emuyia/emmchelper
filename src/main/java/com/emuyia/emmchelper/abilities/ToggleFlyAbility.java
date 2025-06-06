package com.emuyia.emmchelper.abilities;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
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

public class ToggleFlyAbility implements Ability, VisibleAbility, TriggerableAbility, FlightAllowingAbility, Listener {
    private final MCHelperPlugin plugin;
    private final Map<UUID, Boolean> playerFlightToggleState = new ConcurrentHashMap<>();
    private static final float BASE_ABILITY_FLY_SPEED = 0.1f;

    private static final UUID FLIGHT_GROUND_SPEED_MODIFIER_UUID = UUID.fromString("a1b9b5a8-5b9f-4b3a-8b3e-1b9b5a8b3e1b");
    private static final String FLIGHT_GROUND_SPEED_MODIFIER_NAME = "FlightGroundSpeedReduction";

    private static final double MOVEMENT_SPEED_MULTIPLIER_WHILE_FLYING = 0.5;
    private static final double GROUND_SPEED_MODIFIER_AMOUNT = MOVEMENT_SPEED_MULTIPLIER_WHILE_FLYING - 1.0;

    public ToggleFlyAbility(MCHelperPlugin plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (playerFlightToggleState.remove(player.getUniqueId()) != null) {
            removeSpeedModifier(player);
            player.setFlySpeed(0.05f); // Minecraft's default fly speed
        }
    }

    private void applySpeedModifier(Player player) {
        AttributeInstance speedAttribute = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speedAttribute != null) {
            removeSpeedModifier(player, false); 
            
            AttributeModifier modifier = new AttributeModifier(
                    FLIGHT_GROUND_SPEED_MODIFIER_UUID,
                    FLIGHT_GROUND_SPEED_MODIFIER_NAME,
                    GROUND_SPEED_MODIFIER_AMOUNT,
                    AttributeModifier.Operation.MULTIPLY_SCALAR_1
            );
            speedAttribute.addModifier(modifier);
        }
    }

    private void removeSpeedModifier(Player player) {
        removeSpeedModifier(player, true); 
    }

    private void removeSpeedModifier(Player player, boolean logCurrentSpeed) {
        AttributeInstance speedAttribute = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speedAttribute != null) {
            for (AttributeModifier modifier : speedAttribute.getModifiers()) {
                if (modifier.getUniqueId().equals(FLIGHT_GROUND_SPEED_MODIFIER_UUID)) {
                    speedAttribute.removeModifier(modifier);
                    break;
                }
            }
        }
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
        return "Trigger to toggle flight. Movement speed is reduced while flying.";
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
        if (playerFlightToggleState.getOrDefault(player.getUniqueId(), false)) {
            return (float) (BASE_ABILITY_FLY_SPEED * MOVEMENT_SPEED_MULTIPLIER_WHILE_FLYING);
        }
        return BASE_ABILITY_FLY_SPEED;
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
            if (!plugin.isEnabled()) return;
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
            if (playerFlightToggleState.remove(player.getUniqueId()) != null) {
                 removeSpeedModifier(player);
                 player.setFlySpeed(0.05f); 
            }
            return;
        }

        playerFlightToggleState.put(player.getUniqueId(), enableFlight);

        if (enableFlight) {
            player.setAllowFlight(true);
            float newFlySpeed = (float) (BASE_ABILITY_FLY_SPEED * MOVEMENT_SPEED_MULTIPLIER_WHILE_FLYING);
            player.setFlySpeed(newFlySpeed);
            player.setFlying(true);
            applySpeedModifier(player); 
        } else {
            player.setFlying(false);
            player.setAllowFlight(false);
            player.setFlySpeed(BASE_ABILITY_FLY_SPEED); 
            removeSpeedModifier(player);
        }
    }
}

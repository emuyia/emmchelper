package com.emuyia.emmchelper.abilities;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import com.emuyia.emmchelper.MCHelperPlugin;
import com.starshootercity.abilities.types.Ability;
import com.starshootercity.abilities.types.TriggerableAbility;
import com.starshootercity.abilities.types.VisibleAbility;
import com.starshootercity.util.TriggerManager;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ToggleFlyAbility implements Ability, VisibleAbility, TriggerableAbility {
    private final MCHelperPlugin plugin;

    public ToggleFlyAbility(MCHelperPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key(plugin.getNamespace(), "toggle_fly");
    }

    // --- VisibleAbility Implementation ---
    @Override
    public @NotNull String title() {
        return "Toggle Fly";
    }

    // This method is not overriding anything from Ability or VisibleAbility
    public @NotNull Component getName() {
        return Component.text("Toggle Fly").color(NamedTextColor.AQUA);
    }

    @Override
    public @NotNull String description() { // Changed return type to String
        // Combine lines into a single string, use \n for new lines
        return "Trigger to toggle flight.\nRequires EssentialsX /fly command.";
    }

    // This method is not overriding anything from Ability or VisibleAbility
    public @NotNull ItemStack getIcon() {
        return new ItemStack(Material.FEATHER);
    }

    // --- TriggerableAbility Implementation ---
    @Override
    public char getDefaultKeybind() {
        return 'G';
    }

    @Override
    public @NotNull Trigger getTrigger() {
        TriggerType defaultTriggerType = TriggerType.SNEAK_TOGGLE;

        TriggerRunner runner = (TriggerManager.TriggerEvent event) -> {
            Player player = event.player();
            if (player == null) return;

            boolean currentFlyState = player.getAllowFlight();
            Bukkit.dispatchCommand(player, "fly");

            if (!currentFlyState) {
                player.sendMessage(MCHelperPlugin.ABILITY_MSG_PREFIX.append(Component.text("Flight enabled.").color(NamedTextColor.GREEN)));
            } else {
                player.sendMessage(MCHelperPlugin.ABILITY_MSG_PREFIX.append(Component.text("Flight disabled.").color(NamedTextColor.YELLOW)));
            }
        };
        return Trigger.builder(defaultTriggerType, this)
                .addConditions(Condition.DUMMY)
                .build(runner);
    }
}

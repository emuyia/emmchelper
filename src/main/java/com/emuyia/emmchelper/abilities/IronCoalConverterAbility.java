package com.emuyia.emmchelper.abilities;

import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class IronCoalConverterAbility extends AbstractBlockConverterAbility {

    private static final String ABILITY_NAMESPACE = "emmchelper"; // Your plugin's namespace
    private static final String ABILITY_PATH = "iron_coal_converter";
    private static final String COOLDOWN_KEY = "iron_coal_converter_cooldown";

    public IronCoalConverterAbility() {
        super(
                Material.IRON_BLOCK,                               // material1
                Material.COAL_BLOCK,                            // material2
                Sound.BLOCK_ENCHANTMENT_TABLE_USE,                  // sound1To2 (Iron -> Coal)
                Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE,                 // sound2To1 (Coal -> Iron, example sound)
                "Transmutation: Iron/Coal",                    // abilityTitle
                "Right-click Iron or Coal Blocks to convert them.", // abilityDescription
                Key.key(ABILITY_NAMESPACE, ABILITY_PATH),           // abilityKey
                100,                                                // cooldownTicks (5 seconds)
                COOLDOWN_KEY                                        // cooldownInternalKey
        );
    }

    @Override
    public @NotNull ItemStack getIcon() {
        return new ItemStack(Material.IRON_BLOCK);
    }
}
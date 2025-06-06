package com.emuyia.emmchelper.abilities;

import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GoldCopperConverterAbility extends AbstractBlockConverterAbility {

    private static final String ABILITY_NAMESPACE = "emmchelper"; // Your plugin's namespace
    private static final String ABILITY_PATH = "gold_copper_converter";
    private static final String COOLDOWN_KEY = "gold_copper_converter_cooldown";

    public GoldCopperConverterAbility() {
        super(
                Material.GOLD_BLOCK,                               // material1
                Material.COPPER_BLOCK,                            // material2
                Sound.BLOCK_ENCHANTMENT_TABLE_USE,                  // sound1To2 (Gold -> Copper)
                Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE,                 // sound2To1 (Copper -> Gold, example sound)
                "Transmutation: Gold/Copper",                    // abilityTitle
                "Right-click Gold or Copper Blocks to convert them.", // abilityDescription
                Key.key(ABILITY_NAMESPACE, ABILITY_PATH),           // abilityKey
                100,                                                // cooldownTicks (5 seconds)
                COOLDOWN_KEY                                        // cooldownInternalKey
        );
    }

    @Override
    public @NotNull ItemStack getIcon() {
        return new ItemStack(Material.GOLD_BLOCK);
    }
}
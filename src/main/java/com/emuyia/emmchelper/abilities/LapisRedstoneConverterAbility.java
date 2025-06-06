package com.emuyia.emmchelper.abilities;

import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class LapisRedstoneConverterAbility extends AbstractBlockConverterAbility {

    private static final String ABILITY_NAMESPACE = "emmchelper"; // Your plugin's namespace
    private static final String ABILITY_PATH = "lapis_redstone_converter";
    private static final String COOLDOWN_KEY = "lapis_redstone_converter_cooldown";

    public LapisRedstoneConverterAbility() {
        super(
                Material.LAPIS_BLOCK,                               // material1
                Material.REDSTONE_BLOCK,                            // material2
                Sound.BLOCK_ENCHANTMENT_TABLE_USE,                  // sound1To2 (Lapis -> Redstone)
                Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE,                 // sound2To1 (Redstone -> Lapis, example sound)
                "Transmutation: Lapis/Redstone",                    // abilityTitle
                "Right-click Lapis or Redstone Blocks to convert them.", // abilityDescription
                Key.key(ABILITY_NAMESPACE, ABILITY_PATH),           // abilityKey
                100,                                                // cooldownTicks (5 seconds)
                COOLDOWN_KEY                                        // cooldownInternalKey
        );
    }

    @Override
    public @NotNull ItemStack getIcon() {
        return new ItemStack(Material.LAPIS_BLOCK);
    }
}
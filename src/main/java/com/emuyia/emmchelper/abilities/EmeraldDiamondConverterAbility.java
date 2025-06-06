package com.emuyia.emmchelper.abilities;

import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class EmeraldDiamondConverterAbility extends AbstractBlockConverterAbility {

    private static final String ABILITY_NAMESPACE = "emmchelper"; // Your plugin's namespace
    private static final String ABILITY_PATH = "emerald_diamond_converter"; // Updated path
    private static final String COOLDOWN_KEY = "emerald_diamond_converter_cooldown"; // Updated cooldown key

    public EmeraldDiamondConverterAbility() {
        super(
                Material.EMERALD_BLOCK,                             // material1
                Material.DIAMOND_BLOCK,                             // material2 (Changed from IRON_BLOCK)
                Sound.BLOCK_BEACON_ACTIVATE,                        // sound1To2 (Emerald -> Diamond) - Kept sound
                Sound.BLOCK_BEACON_DEACTIVATE,                      // sound2To1 (Diamond -> Emerald) - Kept sound
                "Transmutation: Emerald/Diamond",                   // abilityTitle (Updated)
                "Right-click Emerald or Diamond Blocks to convert them.", // abilityDescription (Updated)
                Key.key(ABILITY_NAMESPACE, ABILITY_PATH),           // abilityKey (Using updated path)
                100,                                                // cooldownTicks (5 seconds)
                COOLDOWN_KEY                                        // cooldownInternalKey (Using updated key)
        );
    }

    @Override
    public @NotNull ItemStack getIcon() {
        // Show Emerald Block as icon, or alternate based on some logic if desired
        return new ItemStack(Material.EMERALD_BLOCK);
    }
}
package com.emuyia.emmchelper.abilities;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.SculkVein; // Changed from GlowLichen
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import com.emuyia.emmchelper.MCHelperPlugin;
import com.starshootercity.abilities.types.VisibleAbility;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class PlaceSculkVeinAbility implements VisibleAbility, Listener {
    private final MCHelperPlugin plugin;

    public PlaceSculkVeinAbility(MCHelperPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key(plugin.getNamespace(), "place_sculk_vein");
    }

    @Override
    public @NotNull String title() {
        return "Sculk Touch";
    }

    public @NotNull Component getName() {
        return Component.text(this.title()).color(NamedTextColor.DARK_AQUA); // Changed color for distinction
    }

    @Override
    public @NotNull String description() {
        return "Right-click blocks with an empty hand while sneaking to spread Sculk Veins onto them.";
    }

    public @NotNull ItemStack getIcon() {
        return new ItemStack(Material.SCULK_VEIN); // Changed icon
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();

        if (!player.isSneaking()) return;

        runForAbility(player, (p) -> {
            if (event.hasItem() && event.getItem() != null && event.getItem().getType() != Material.AIR) {
                return;
            }

            Block clickedBlock = event.getClickedBlock();
            BlockFace clickedFace = event.getBlockFace();

            if (clickedBlock.getType().isAir() || !clickedBlock.getType().isSolid() || clickedBlock.getType().isInteractable()) {
                return;
            }

            Block blockToPlaceVeinIn = clickedBlock.getRelative(clickedFace);

            if (blockToPlaceVeinIn.isReplaceable() || blockToPlaceVeinIn.getType() == Material.AIR) {
                BlockPlaceEvent placeEvent = new BlockPlaceEvent(
                        blockToPlaceVeinIn,
                        blockToPlaceVeinIn.getState(),
                        clickedBlock,
                        new ItemStack(Material.SCULK_VEIN), // Item being "placed"
                        p,
                        true,
                        EquipmentSlot.HAND
                );

                if (!placeEvent.callEvent() || !placeEvent.canBuild()) {
                    return;
                }

                org.bukkit.block.data.BlockData originalBlockData = blockToPlaceVeinIn.getBlockData();
                blockToPlaceVeinIn.setType(Material.SCULK_VEIN, false); // Set to SCULK_VEIN

                if (blockToPlaceVeinIn.getBlockData() instanceof SculkVein sculkVeinData) { // Check for SculkVein
                    BlockFace faceToActivate = clickedFace.getOppositeFace();

                    // SculkVein uses similar face logic to GlowLichen
                    if (sculkVeinData.getAllowedFaces().contains(faceToActivate)) {
                        sculkVeinData.setFace(faceToActivate, true);
                        blockToPlaceVeinIn.setBlockData(sculkVeinData, true);

                        if (blockToPlaceVeinIn.getType() == Material.SCULK_VEIN) { // Check for SCULK_VEIN
                            p.getWorld().playSound(
                                    p.getLocation(),
                                    Sound.BLOCK_SCULK_VEIN_PLACE, // Changed to sculk vein sound
                                    SoundCategory.BLOCKS,
                                    1.0f,
                                    1.0f
                            );
                            p.swingMainHand();
                        } else {
                            blockToPlaceVeinIn.setBlockData(originalBlockData, false);
                        }
                    } else {
                        blockToPlaceVeinIn.setBlockData(originalBlockData, false);
                    }
                } else {
                    blockToPlaceVeinIn.setBlockData(originalBlockData, false);
                }
            }
        });
    }
}
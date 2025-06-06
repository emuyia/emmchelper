package com.emuyia.emmchelper.abilities;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.GlowLichen;
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

// The class implements VisibleAbility and Listener.
// If runForAbility is a default method in VisibleAbility or a common parent (e.g., Ability),
// it will be available here.
public class PlaceGlowLichenAbility implements VisibleAbility, Listener {
    private final MCHelperPlugin plugin;

    public PlaceGlowLichenAbility(MCHelperPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key(plugin.getNamespace(), "place_glow_lichen");
    }

    @Override
    public @NotNull String title() {
        return "Lichen Touch";
    }

    // This method is not part of VisibleAbility or Listener by default in Bukkit.
    // It's often part of the Ability interface from Origins-Reborn.
    // Assuming VisibleAbility provides it or extends an interface that does.
    public @NotNull Component getName() {
        return Component.text(this.title()).color(NamedTextColor.GREEN);
    }

    @Override
    public @NotNull String description() {
        return "Right-click blocks with an empty hand to spread Glow Lichen onto them.";
    }

    // This method is not part of VisibleAbility or Listener by default in Bukkit.
    // It's often part of the Ability interface from Origins-Reborn.
    // Assuming VisibleAbility provides it or extends an interface that does.
    public @NotNull ItemStack getIcon() {
        return new ItemStack(Material.GLOW_LICHEN);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getHand() != EquipmentSlot.HAND) return; // Only main hand

        Player player = event.getPlayer(); // Player from the event

        if (!player.isSneaking()) return; // Ensure the player is sneaking

        // Ensure the player has this ability
        // The lambda now accepts a 'p' argument of type Player to match AbilityRunner's expected signature
        runForAbility(player, (p) -> { // <-- Changed from () to (p)
            // 'p' is the player instance passed by runForAbility, usually the same as 'player' from outer scope.
            // You can use 'p' or the outer 'player' (if effectively final). For consistency, using 'p' is fine.

            if (event.hasItem() && event.getItem() != null && event.getItem().getType() != Material.AIR) {
                return;
            }

            Block clickedBlock = event.getClickedBlock();
            BlockFace clickedFace = event.getBlockFace();

            if (clickedBlock.getType().isAir() || !clickedBlock.getType().isSolid() || clickedBlock.getType().isInteractable()) {
                return;
            }

            Block blockToPlaceLichenIn = clickedBlock.getRelative(clickedFace);

            if (blockToPlaceLichenIn.isReplaceable() || blockToPlaceLichenIn.getType() == Material.AIR) {
                BlockPlaceEvent placeEvent = new BlockPlaceEvent(
                        blockToPlaceLichenIn,
                        blockToPlaceLichenIn.getState(),
                        clickedBlock,
                        new ItemStack(Material.GLOW_LICHEN),
                        p, // Use the player from the lambda 'p' here
                        true,
                        EquipmentSlot.HAND
                );

                if (!placeEvent.callEvent() || !placeEvent.canBuild()) {
                    return;
                }

                org.bukkit.block.data.BlockData originalBlockData = blockToPlaceLichenIn.getBlockData();
                blockToPlaceLichenIn.setType(Material.GLOW_LICHEN, false);

                if (blockToPlaceLichenIn.getBlockData() instanceof GlowLichen glowLichenData) {
                    BlockFace faceToActivate = clickedFace.getOppositeFace();

                    if (glowLichenData.getAllowedFaces().contains(faceToActivate)) {
                        glowLichenData.setFace(faceToActivate, true);
                        blockToPlaceLichenIn.setBlockData(glowLichenData, true);

                        if (blockToPlaceLichenIn.getType() == Material.GLOW_LICHEN) {
                            // Keeping sound as per your request
                            p.getWorld().playSound( // Use 'p' here
                                    p.getLocation(), // Sound at the player
                                    Sound.BLOCK_GRASS_PLACE, // Grass sound
                                    SoundCategory.BLOCKS,
                                    1.0f,
                                    1.0f
                            );
                            p.swingMainHand(); // Use 'p' here
                        } else {
                            blockToPlaceLichenIn.setBlockData(originalBlockData, false);
                        }
                    } else {
                        blockToPlaceLichenIn.setBlockData(originalBlockData, false);
                    }
                } else {
                    blockToPlaceLichenIn.setBlockData(originalBlockData, false);
                }
            }
        });
    }
}
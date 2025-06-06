package com.emuyia.emmchelper.abilities;

import com.emuyia.emmchelper.MCHelperPlugin;
import com.starshootercity.abilities.types.VisibleAbility;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class PlacePoppyAbility implements VisibleAbility, Listener {

    public static Set<Material> getSUITABLE_GROUND_MATERIALS() {
        return SUITABLE_GROUND_MATERIALS;
    }
    private final MCHelperPlugin plugin;
    private static final Set<Material> SUITABLE_GROUND_MATERIALS = Set.of(
            Material.GRASS_BLOCK,
            Material.DIRT,
            Material.PODZOL,
            Material.COARSE_DIRT,
            Material.FARMLAND,
            Material.MOSS_BLOCK,
            Material.ROOTED_DIRT
            // Add Material.MUD if targeting 1.19+ and desired
    );

    public PlacePoppyAbility(MCHelperPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key(plugin.getNamespace(), "place_poppy");
    }

    @Override
    public @NotNull String title() {
        return "Flower Touch";
    }

    public @NotNull Component getName() {
        return Component.text(this.title()).color(NamedTextColor.RED);
    }

    @Override
    public @NotNull String description() {
        return "Right-click suitable ground with an empty hand while sneaking to plant a Poppy.";
    }

    public @NotNull ItemStack getIcon() {
        return new ItemStack(Material.POPPY);
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

            // Check if the clicked block is suitable ground for a poppy
            if (!SUITABLE_GROUND_MATERIALS.contains(clickedBlock.getType())) {
                return;
            }

            // Poppy should be placed on top of the clicked block
            Block blockToPlacePoppyIn = clickedBlock.getRelative(BlockFace.UP);

            if (blockToPlacePoppyIn.getType() == Material.AIR) { // Poppies need air to be placed in
                BlockPlaceEvent placeEvent = new BlockPlaceEvent(
                        blockToPlacePoppyIn,
                        blockToPlacePoppyIn.getState(),
                        clickedBlock, // Block against which the poppy is placed
                        new ItemStack(Material.POPPY),
                        p,
                        true,
                        EquipmentSlot.HAND
                );

                if (!placeEvent.callEvent() || !placeEvent.canBuild()) {
                    return;
                }

                blockToPlacePoppyIn.setType(Material.POPPY);

                // Check if placement was successful (e.g. not immediately broken by physics)
                if (blockToPlacePoppyIn.getType() == Material.POPPY) {
                    p.getWorld().playSound(
                            blockToPlacePoppyIn.getLocation(), // Sound at the poppy's location
                            Sound.BLOCK_GRASS_PLACE,
                            SoundCategory.BLOCKS,
                            1.0f,
                            1.0f
                    );
                    p.swingMainHand();
                }
                // No complex BlockData for poppies, so no revert logic needed like for lichen
            }
        });
    }
}
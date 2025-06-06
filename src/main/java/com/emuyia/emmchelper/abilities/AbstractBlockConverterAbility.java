package com.emuyia.emmchelper.abilities;

import com.starshootercity.abilities.types.CooldownAbility;
import com.starshootercity.abilities.types.VisibleAbility;
import com.starshootercity.cooldowns.Cooldowns;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractBlockConverterAbility implements VisibleAbility, Listener, CooldownAbility {

    private final Material material1;
    private final Material material2;
    private final Sound sound1To2;
    private final Sound sound2To1;
    private final String abilityTitle;
    private final String abilityDescription;
    private final Key abilityKey;
    private final Cooldowns.CooldownInfo cooldownInfo;

    public AbstractBlockConverterAbility(
            Material material1, Material material2,
            Sound sound1To2, Sound sound2To1,
            String abilityTitle, String abilityDescription,
            Key abilityKey,
            int cooldownTicks, String cooldownInternalKey) {
        this.material1 = material1;
        this.material2 = material2;
        this.sound1To2 = sound1To2;
        this.sound2To1 = sound2To1;
        this.abilityTitle = abilityTitle;
        this.abilityDescription = abilityDescription;
        this.abilityKey = abilityKey;
        this.cooldownInfo = new Cooldowns.CooldownInfo(cooldownTicks, cooldownInternalKey);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.hasItem() && event.getItem() != null && event.getItem().getType() != Material.AIR) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();

        this.runForAbility(player, (p) -> {
            boolean isMaterial1 = event.getClickedBlock().getType().equals(this.material1);
            boolean isMaterial2 = event.getClickedBlock().getType().equals(this.material2);

            if (isMaterial1 || isMaterial2) {
                if (this.hasCooldown(p)) {
                    return;
                }

                BlockPlaceEvent e = new BlockPlaceEvent(
                        event.getClickedBlock(),
                        event.getClickedBlock().getState(),
                        event.getClickedBlock(),
                        new ItemStack(Material.AIR),
                        p,
                        true,
                        EquipmentSlot.HAND);

                if (!e.callEvent() || !e.canBuild()) {
                    return;
                }

                this.setCooldown(p);
                Material targetMaterial = isMaterial1 ? this.material2 : this.material1;
                Sound soundToPlay = isMaterial1 ? this.sound1To2 : this.sound2To1;

                event.getClickedBlock().setType(targetMaterial);
                p.swingMainHand();
                event.getClickedBlock().getWorld().playSound(
                        event.getClickedBlock().getLocation(),
                        soundToPlay,
                        SoundCategory.BLOCKS,
                        1.0F,
                        1.0F);
                event.getClickedBlock().getWorld().spawnParticle(
                        Particle.GLOW,
                        event.getClickedBlock().getLocation().add(0.5, 0.5, 0.5),
                        30, 0.25, 0.25, 0.25, 0.0);
            }
        });
    }

    @Override
    public @NotNull String description() {
        return this.abilityDescription;
    }

    @Override
    public @NotNull String title() {
        return this.abilityTitle;
    }

    @Override
    public @NotNull Cooldowns.CooldownInfo getCooldownInfo() {
        if (this.cooldownInfo == null) {
            System.err.println("[MCHelper CRITICAL] this.cooldownInfo is NULL for ability: " + this.abilityKey.value());
        }
        return this.cooldownInfo;
    }

    @Override
    public @NotNull Key getKey() {
        return this.abilityKey;
    }

    public abstract @NotNull ItemStack getIcon();
}
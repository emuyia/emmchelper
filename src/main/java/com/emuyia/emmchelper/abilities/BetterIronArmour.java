package com.emuyia.emmchelper.abilities;

import com.destroystokyo.paper.MaterialTags;
import com.starshootercity.abilities.types.AttributeModifierAbility;
import com.starshootercity.abilities.types.VisibleAbility;
import com.starshootercity.version.MVAttribute;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BetterIronArmour implements VisibleAbility, AttributeModifierAbility, Listener {

    public BetterIronArmour() {
    }

    @Override
    public @NotNull Attribute getAttribute() {
        return MVAttribute.ARMOR.get();
    }

    @Override
    public double getAmount(Player player) {
        double amount = 0;
        ItemStack[] armorContents = player.getEquipment().getArmorContents();
        for (ItemStack item : armorContents) {
            if (item != null) {
                if (item.getType() == Material.IRON_HELMET) {
                    amount += 1; // Iron Helmet (2) + 1 = 3 (Netherite Helmet)
                } else if (item.getType() == Material.IRON_CHESTPLATE) {
                    amount += 2; // Iron Chestplate (6) + 2 = 8 (Netherite Chestplate)
                } else if (item.getType() == Material.IRON_LEGGINGS) {
                    amount += 1; // Iron Leggings (5) + 1 = 6 (Netherite Leggings)
                } else if (item.getType() == Material.IRON_BOOTS) {
                    amount += 1; // Iron Boots (2) + 1 = 3 (Netherite Boots)
                }
            }
        }
        return amount;
    }

    @Override
    public AttributeModifier.@NotNull Operation getOperation() {
        return Operation.ADD_NUMBER;
    }

    @Override
    public @NotNull String description() {
        return "Your mastery of iron makes iron armor unbreakable and as strong as Netherite.";
    }

    @Override
    public @NotNull String title() {
        return "Iron Will";
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key("emmchelper", "better_iron_armour");
    }

    @EventHandler
    public void onPlayerItemDamage(PlayerItemDamageEvent event) {
        // Check if the item is armor and made of iron
        if (MaterialTags.ARMOR.isTagged(event.getItem())) {
            Material type = event.getItem().getType();
            if (type == Material.IRON_HELMET || type == Material.IRON_CHESTPLATE ||
                type == Material.IRON_LEGGINGS || type == Material.IRON_BOOTS) {
                this.runForAbility(event.getPlayer(), (player) -> {
                    event.setCancelled(true);
                });
            }
        }
    }
}
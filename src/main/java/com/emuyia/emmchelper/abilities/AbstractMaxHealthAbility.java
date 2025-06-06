package com.emuyia.emmchelper.abilities;

import com.emuyia.emmchelper.MCHelperPlugin;
import com.starshootercity.abilities.types.AttributeModifierAbility;
import com.starshootercity.abilities.types.VisibleAbility;
import com.starshootercity.version.MVAttribute;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractMaxHealthAbility implements AttributeModifierAbility, VisibleAbility {

    protected final MCHelperPlugin plugin;
    private final String keyName;
    private final String title;
    private final String description;
    private final double targetHealthPoints; // Total health points (e.g., 2.0 for 1 heart)
    private final Material iconMaterial;

    // Standard base max health for calculation (Minecraft default is 20 points = 10 hearts)
    private static final double BASE_MAX_HEALTH_POINTS = 20.0;

    public AbstractMaxHealthAbility(MCHelperPlugin plugin, String keyName, String title, String description, double targetHealthPoints, Material iconMaterial) {
        this.plugin = plugin;
        this.keyName = keyName;
        this.title = title;
        this.description = description;
        this.targetHealthPoints = targetHealthPoints;
        this.iconMaterial = iconMaterial;
    }

    @Override
    public @NotNull Key getKey() {
        return Key.key(plugin.getNamespace(), this.keyName);
    }

    @Override
    public @NotNull String title() {
        return this.title;
    }

    @Override
    public @NotNull String description() {
        return this.description;
    }

    public @NotNull ItemStack getIcon() {
        return new ItemStack(this.iconMaterial);
    }

    @Override
    public @NotNull Attribute getAttribute() {
        return MVAttribute.MAX_HEALTH.get();
    }

    @Override
    public double getAmount(Player player) {
        // Calculate the difference from the standard base max health
        // Example: For 1 heart (2.0 points), amount = 2.0 - 20.0 = -18.0
        // Example: For 5 hearts (10.0 points), amount = 10.0 - 20.0 = -10.0
        return this.targetHealthPoints - BASE_MAX_HEALTH_POINTS;
    }

    @Override
    public AttributeModifier.@NotNull Operation getOperation() {
        return AttributeModifier.Operation.ADD_NUMBER;
    }
}
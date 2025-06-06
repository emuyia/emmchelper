package com.emuyia.emmchelper.abilities;

import com.emuyia.emmchelper.MCHelperPlugin;
import org.bukkit.Material;

public class MaxHealthOneHeartAbility extends AbstractMaxHealthAbility {

    public MaxHealthOneHeartAbility(MCHelperPlugin plugin) {
        super(
            plugin,
            "one_heart_max_health",      // keyName
            "One Heart",                 // title
            "You have a maximum of one heart.", // description
            2.0,                         // targetHealthPoints (1 heart * 2 points/heart)
            Material.APPLE               // iconMaterial (or your preferred icon)
        );
    }
    // All other methods are inherited from AbstractMaxHealthAbility
}
package com.emuyia.emmchelper.abilities;

import com.emuyia.emmchelper.MCHelperPlugin;
import org.bukkit.Material;

public class MaxHealthTwoHeartsAbility extends AbstractMaxHealthAbility {

    public MaxHealthTwoHeartsAbility(MCHelperPlugin plugin) {
        super(
            plugin,
            "two_hearts_max_health",     // keyName
            "Two Hearts",                // title
            "You have a maximum of two hearts.", // description
            4.0,                         // targetHealthPoints (2 hearts * 2 points/heart)
            Material.APPLE               // iconMaterial
        );
    }
}
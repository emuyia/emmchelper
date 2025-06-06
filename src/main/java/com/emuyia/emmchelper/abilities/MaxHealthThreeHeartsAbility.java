package com.emuyia.emmchelper.abilities;

import com.emuyia.emmchelper.MCHelperPlugin;
import org.bukkit.Material;

public class MaxHealthThreeHeartsAbility extends AbstractMaxHealthAbility {

    public MaxHealthThreeHeartsAbility(MCHelperPlugin plugin) {
        super(
            plugin,
            "three_hearts_max_health",   // keyName
            "Three Hearts",              // title
            "You have a maximum of three hearts.", // description
            6.0,                         // targetHealthPoints (3 hearts * 2 points/heart)
            Material.APPLE               // iconMaterial
        );
    }
}
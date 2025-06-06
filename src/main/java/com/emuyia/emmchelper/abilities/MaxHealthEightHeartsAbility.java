package com.emuyia.emmchelper.abilities;

import com.emuyia.emmchelper.MCHelperPlugin;
import org.bukkit.Material;

public class MaxHealthEightHeartsAbility extends AbstractMaxHealthAbility {

    public MaxHealthEightHeartsAbility(MCHelperPlugin plugin) {
        super(
            plugin,
            "eight_hearts_max_health",   // keyName
            "Eight Hearts",              // title
            "You have a maximum of eight hearts.", // description
            16.0,                         // targetHealthPoints (8 hearts * 2 points/heart)
            Material.APPLE               // iconMaterial
        );
    }
}
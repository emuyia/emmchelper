package com.emuyia.emmchelper.abilities;

import com.emuyia.emmchelper.MCHelperPlugin;
import org.bukkit.Material;

public class MaxHealthNineHeartsAbility extends AbstractMaxHealthAbility {

    public MaxHealthNineHeartsAbility(MCHelperPlugin plugin) {
        super(
            plugin,
            "nine_hearts_max_health",   // keyName
            "Nine Hearts",              // title
            "You have a maximum of nine hearts.", // description
            18.0,                         // targetHealthPoints (9 hearts * 2 points/heart)
            Material.APPLE               // iconMaterial
        );
    }
}
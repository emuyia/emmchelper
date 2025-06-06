package com.emuyia.emmchelper.abilities;

import com.emuyia.emmchelper.MCHelperPlugin;
import org.bukkit.Material;

public class MaxHealthFiveHeartsAbility extends AbstractMaxHealthAbility {

    public MaxHealthFiveHeartsAbility(MCHelperPlugin plugin) {
        super(
            plugin,
            "five_hearts_max_health",   // keyName
            "Five Hearts",              // title
            "You have a maximum of five hearts.", // description
            10.0,                         // targetHealthPoints (5 hearts * 2 points/heart)
            Material.APPLE               // iconMaterial
        );
    }
}
package com.emuyia.emmchelper.abilities;

import com.emuyia.emmchelper.MCHelperPlugin;
import org.bukkit.Material;

public class MaxHealthSixHeartsAbility extends AbstractMaxHealthAbility {

    public MaxHealthSixHeartsAbility(MCHelperPlugin plugin) {
        super(
            plugin,
            "six_hearts_max_health",   // keyName
            "Six Hearts",              // title
            "You have a maximum of six hearts.", // description
            12.0,                         // targetHealthPoints (6 hearts * 2 points/heart)
            Material.APPLE               // iconMaterial
        );
    }
}
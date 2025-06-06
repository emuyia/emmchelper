package com.emuyia.emmchelper.abilities;

import com.emuyia.emmchelper.MCHelperPlugin;
import org.bukkit.Material;

public class MaxHealthSevenHeartsAbility extends AbstractMaxHealthAbility {

    public MaxHealthSevenHeartsAbility(MCHelperPlugin plugin) {
        super(
            plugin,
            "seven_hearts_max_health",   // keyName
            "Seven Hearts",              // title
            "You have a maximum of seven hearts.", // description
            14.0,                         // targetHealthPoints (7 hearts * 2 points/heart)
            Material.APPLE               // iconMaterial
        );
    }
}
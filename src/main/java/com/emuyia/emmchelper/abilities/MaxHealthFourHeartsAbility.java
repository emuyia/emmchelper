package com.emuyia.emmchelper.abilities;

import com.emuyia.emmchelper.MCHelperPlugin;
import org.bukkit.Material;

public class MaxHealthFourHeartsAbility extends AbstractMaxHealthAbility {

    public MaxHealthFourHeartsAbility(MCHelperPlugin plugin) {
        super(
            plugin,
            "four_hearts_max_health",   // keyName
            "Four Hearts",              // title
            "You have a maximum of four hearts.", // description
            8.0,                         // targetHealthPoints (4 hearts * 2 points/heart)
            Material.APPLE               // iconMaterial
        );
    }
}
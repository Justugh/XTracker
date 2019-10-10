package net.justugh.xt.ore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

@Getter
@AllArgsConstructor
public enum OreType {

    STONE("Stone", Material.STONE),
    COAL("Coal Ore", Material.COAL_ORE),
    IRON("Iron Ore", Material.IRON_ORE),
    GOLD("Gold Ore", Material.GOLD_ORE),
    LAPIS("Lapis Lazuli Ore", Material.LAPIS_ORE),
    REDSTONE("Redstone Ore", Material.REDSTONE_ORE),
    EMERALD("Emerald Ore", Material.EMERALD_ORE),
    DIAMOND("Diamond Ore", Material.DIAMOND_ORE),
    QUARTZ("Quartz Ore", Material.QUARTZ_ORE);

    String friendlyName;
    Material material;

    public static OreType match(Material material) {
        for (OreType value : values()) {
            if(value.getMaterial().equals(material)) {
                return value;
            }
        }

        return null;
    }

}

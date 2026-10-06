/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.material.Material
 *  net.minecraft.world.level.block.Blocks
 */
package ic2.core.item.tool;

import ic2.core.item.tool.IToolClass;
import ic2.core.ref.IC2Material;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.world.level.block.Blocks;

public enum ToolClass implements IToolClass
{
    Axe("axe"),
    Pickaxe("pickaxe"),
    Shears("shears", Blocks.COBWEB, Blocks.REDSTONE_WIRE, Blocks.OAK_LEAVES),
    Shovel("shovel", Blocks.SNOW, Blocks.SNOW_BLOCK),
    Sword("sword", Blocks.COBWEB, Blocks.OAK_LEAVES, Blocks.MELON, Blocks.PUMPKIN),
    Hoe(null, Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.MYCELIUM),
    Wrench("wrench", new Object[]{IC2Material.MACHINE, IC2Material.PIPE}),
    WireCutter("wire_cutter", new Object[]{IC2Material.CABLE}),
    Crowbar("crowbar", Blocks.RAIL, Blocks.ACTIVATOR_RAIL, Blocks.DETECTOR_RAIL, Blocks.POWERED_RAIL);

    public final String name;
    public final Set<Object> whitelist;
    public final Set<Object> blacklist;

    private ToolClass(String name, Object ... whitelist) {
        this(name, whitelist, new Object[0]);
    }

    private ToolClass(String name, Object[] whitelist, Object[] blacklist) {
        this.name = name;
        this.whitelist = new HashSet<Object>(Arrays.asList(whitelist));
        this.blacklist = new HashSet<Object>(Arrays.asList(blacklist));
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Set<Object> getWhitelist() {
        return this.whitelist;
    }

    @Override
    public Set<Object> getBlacklist() {
        return this.blacklist;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item$ToolMaterial
 */
package ic2.core.item.tool;

import net.minecraft.world.item.Tiers;

public enum HarvestLevel {
    Wood(0, Tiers.WOOD),
    Stone(1, Tiers.STONE),
    Iron(2, Tiers.IRON),
    Diamond(3, Tiers.DIAMOND),
    Iridium(100, Tiers.NETHERITE);

    public final int level;
    public final Tiers toolMaterial;

    private HarvestLevel(int level, Tiers toolMaterial) {
        this.level = level;
        this.toolMaterial = toolMaterial;
    }
}


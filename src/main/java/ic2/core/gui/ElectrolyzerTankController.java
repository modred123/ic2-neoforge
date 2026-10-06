/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  gnu.trove.impl.unmodifiable.TUnmodifiableIntSet
 *  gnu.trove.set.Set<Integer>
 *  gnu.trove.set.hash.TIntHashSet
 */
package ic2.core.gui;

import ic2.api.recipe.IElectrolyzerRecipeManager;
import ic2.core.block.machine.container.ContainerElectrolyzer;
import ic2.core.block.machine.gui.GuiElectrolyzer;
import ic2.core.block.machine.tileentity.TileEntityElectrolyzer;
import ic2.core.gui.ElectrolyzerTank;
import ic2.core.gui.GuiElement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ElectrolyzerTankController
extends GuiElement<ElectrolyzerTankController> {
    private int lastRecipeLength = 0;
    private final TileEntityElectrolyzer electrolyzer;
    private final ElectrolyzerTank[] tanks;
    public static final Set<Integer> ONE_THREE_FIVE = Collections.unmodifiableSet(new HashSet<>(java.util.Arrays.asList(1, 3, 5)));
    public static final Set<Integer> TWO_TO_FIVE = Collections.unmodifiableSet(new HashSet<>(java.util.Arrays.asList(2, 3, 4, 5)));
    public static final Set<Integer> FOUR_FIVE = Collections.unmodifiableSet(new HashSet<>(java.util.Arrays.asList(4, 5)));

    public ElectrolyzerTankController(GuiElectrolyzer gui, int x, int y, ElectrolyzerTank ... tanks) {
        super(gui, x, y, 0, 0);
        this.electrolyzer = (TileEntityElectrolyzer)((ContainerElectrolyzer)((Object)gui.getContainer())).base;
        this.tanks = tanks;
    }

    @Override
    public boolean contains(int x, int y) {
        return false;
    }

    public int getLastRecipeLength() {
        return this.lastRecipeLength;
    }

    @Override
    public void tick() {
        for (ElectrolyzerTank tank : this.tanks) {
            tank.tick();
        }
        if (!this.electrolyzer.hasRecipe()) {
            this.lastRecipeLength = 0;
            return;
        }
        IElectrolyzerRecipeManager.ElectrolyzerOutput[] outputs = this.electrolyzer.getCurrentRecipe().outputs;
        this.lastRecipeLength = outputs.length;
    }
}


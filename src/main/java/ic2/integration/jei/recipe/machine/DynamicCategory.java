package ic2.integration.jei.recipe.machine;

import ic2.core.block.invslot.InvSlot;
import ic2.core.block.machine.tileentity.TileEntityStandardMachine;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.block.tileentity.TileEntityInventory;
import ic2.core.gui.Gauge;
import ic2.core.gui.GuiElement;
import ic2.core.gui.dynamic.GuiEnvironment;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.util.Tuple;
import ic2.integration.jeirei.SlotPosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;

public class DynamicCategory extends IORecipeCategory {
    protected final int xOffset;
    protected final int yOffset;
    protected final List<Tuple.T2<IDrawable, SlotPosition>> elements = new ArrayList<>();
    private final List<SlotPosition> inputSlots = new ArrayList<>();
    private final List<SlotPosition> outputSlots = new ArrayList<>();
    private final IDrawable background;
    private final RecipeType<IORecipeWrapper> recipeType;
    private final IGuiHelper guiHelper;

    public DynamicCategory(Ic2TileEntityBlock block, RecipeType<IORecipeWrapper> recipeType, IGuiHelper guiHelper) {
        super(block);
        this.recipeType = recipeType;
        this.guiHelper = guiHelper;
        this.initializeWidgets(guiHelper, GuiParser.parse(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block), block.getDummyTe().getClass()));
        int minX = 1000;
        int minY = 1000;
        int maxX = -1000;
        int maxY = -1000;
        for (Tuple.T2<IDrawable, SlotPosition> element : this.elements) {
            minX = Math.min(minX, element.b.getX());
            minY = Math.min(minY, element.b.getY());
            maxX = Math.max(maxX, element.b.getX() + element.a.getWidth());
            maxY = Math.max(maxY, element.b.getY() + element.a.getHeight());
        }
        int width = maxX - minX;
        int height = maxY - minY;
        this.xOffset = -minX;
        this.yOffset = -minY;
        this.background = guiHelper.createBlankDrawable(width, height);
    }

    private void initializeWidgets(IGuiHelper guiHelper, GuiParser.ParentNode parentNode) {
        for (GuiParser.Node node : parentNode.getNodes()) {
            switch (node.getType()) {
                case energygauge: {
                    GuiParser.EnergyGaugeNode gaugeNode = (GuiParser.EnergyGaugeNode)node;
                    Gauge.GaugeProperties props = gaugeNode.style.properties;
                    SlotPosition bgPos = new SlotPosition(gaugeNode.x + props.bgXOffset, gaugeNode.y + props.bgYOffset);
                    IDrawable bg = guiHelper.createDrawable(props.texture, props.uBgInactive, props.vBgInactive, props.bgWidth, props.bgHeight);
                    this.elements.add(new Tuple.T2<>(bg, bgPos));
                    IDrawableStatic inner = guiHelper.createDrawable(props.texture, props.uInner, props.vInner, props.innerWidth, props.innerHeight);
                    IDrawableAnimated animated = guiHelper.createAnimatedDrawable(inner, 300, props.reverse ? (props.vertical ? IDrawableAnimated.StartDirection.TOP : IDrawableAnimated.StartDirection.RIGHT) : (props.vertical ? IDrawableAnimated.StartDirection.BOTTOM : IDrawableAnimated.StartDirection.LEFT), true);
                    this.elements.add(new Tuple.T2<>(animated, new SlotPosition(gaugeNode.x, gaugeNode.y)));
                    break;
                }
                case gauge: {
                    GuiParser.GaugeNode gaugeNode = (GuiParser.GaugeNode)node;
                    Gauge.GaugeProperties props = gaugeNode.style.getProperties();
                    SlotPosition bgPos = new SlotPosition(gaugeNode.x + props.bgXOffset, gaugeNode.y + props.bgYOffset);
                    IDrawableStatic bg = guiHelper.createDrawable(props.texture, props.uBgActive, props.vBgActive, props.bgWidth, props.bgHeight);
                    this.elements.add(new Tuple.T2<>(bg, bgPos));
                    IDrawableStatic inner = guiHelper.createDrawable(props.texture, props.uInner, props.vInner, props.innerWidth, props.innerHeight);
                    IDrawable value = gaugeNode.style == Gauge.GaugeStyle.HeatCentrifuge ? inner : guiHelper.createAnimatedDrawable(inner, this.getProcessSpeed(gaugeNode.name), props.reverse ? (props.vertical ? IDrawableAnimated.StartDirection.BOTTOM : IDrawableAnimated.StartDirection.RIGHT) : (props.vertical ? IDrawableAnimated.StartDirection.TOP : IDrawableAnimated.StartDirection.LEFT), false);
                    this.elements.add(new Tuple.T2<>(value, new SlotPosition(gaugeNode.x, gaugeNode.y)));
                    break;
                }
                case image: {
                    GuiParser.ImageNode imageNode = (GuiParser.ImageNode)node;
                    SlotPosition pos = new SlotPosition(imageNode.x, imageNode.y);
                    IDrawable drawable = guiHelper.drawableBuilder(imageNode.src, imageNode.u1, imageNode.v1, imageNode.width, imageNode.height).setTextureSize(imageNode.baseWidth, imageNode.baseHeight).build();
                    this.elements.add(new Tuple.T2<>(drawable, pos));
                    break;
                }
                case slot: {
                    GuiParser.SlotNode slotNode = (GuiParser.SlotNode)node;
                    SlotPosition pos = new SlotPosition(slotNode.x, slotNode.y, slotNode.style);
                    IDrawable drawable = guiHelper.createDrawable(GuiElement.commonTexture, pos.getStyle().u, pos.getStyle().v, pos.getStyle().width, pos.getStyle().height);
                    this.elements.add(new Tuple.T2<>(drawable, pos));
                    int innerX = (slotNode.style.width - 16) / 2;
                    int innerY = (slotNode.style.height - 16) / 2;
                    String name = slotNode.name.toLowerCase(Locale.ENGLISH);
                    if (name.contains("input") || name.equals("cutterInputSlot")) {
                        this.inputSlots.add(new SlotPosition(pos, innerX, innerY));
                    } else if (name.contains("output")) {
                        this.outputSlots.add(new SlotPosition(pos, innerX, innerY));
                    }
                    break;
                }
                case slotgrid: {
                    GuiParser.SlotGridNode gridNode = (GuiParser.SlotGridNode)node;
                    TileEntityInventory inventory = (TileEntityInventory)this.block.getDummyTe();
                    if (inventory == null) {
                        throw new NullPointerException("Received null dummy for " + this.block + " in the JeiPlugin.");
                    }
                    InvSlot invSlot = inventory.getInventorySlot(gridNode.name);
                    if (invSlot == null) {
                        throw new RuntimeException("invalid invslot name " + gridNode.name + " for base " + inventory);
                    }
                    int count = invSlot.size();
                    if (count <= gridNode.offset) {
                        break;
                    }
                    GuiParser.SlotGridNode.SlotGridDimension dimension = gridNode.getDimension(count);
                    IDrawable slotBg = guiHelper.createDrawable(GuiElement.commonTexture, gridNode.style.u, gridNode.style.v, gridNode.style.width, gridNode.style.height);
                    boolean isInput = gridNode.name.toLowerCase().contains("input");
                    boolean isOutput = gridNode.name.toLowerCase().contains("output");
                    int innerX = (gridNode.style.width - 16) / 2;
                    int innerY = (gridNode.style.height - 16) / 2;
                    for (int i = 0; i < dimension.cols; i++) {
                        for (int j = 0; j < dimension.rows; j++) {
                            if (i * dimension.rows + j > count) {
                                return;
                            }
                            SlotPosition pos = new SlotPosition(gridNode.x + i * gridNode.style.width, gridNode.y + j * gridNode.style.height, gridNode.style);
                            this.elements.add(new Tuple.T2<>(slotBg, pos));
                            if (isInput) {
                                this.inputSlots.add(new SlotPosition(pos, innerX, innerY));
                            } else if (isOutput) {
                                this.outputSlots.add(new SlotPosition(pos, innerX, innerY));
                            }
                        }
                    }
                    break;
                }
                case environment: {
                    GuiParser.EnvironmentNode envNode = (GuiParser.EnvironmentNode)node;
                    if (envNode.environment == GuiEnvironment.JEI) {
                        this.initializeWidgets(guiHelper, envNode);
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, IORecipeWrapper recipe, IFocusGroup focusGroup) {
        this.addRecipeSlots(builder, recipe, focusGroup, this.xOffset, this.yOffset);
    }

    @Override
    public void draw(IORecipeWrapper recipe, IRecipeSlotsView slotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        for (Tuple.T2<IDrawable, SlotPosition> element : this.elements) {
            element.a.draw(guiGraphics, element.b.getX() + this.xOffset, element.b.getY() + this.yOffset);
        }
    }

    @Override
    public RecipeType<IORecipeWrapper> getRecipeType() {
        return this.recipeType;
    }

    @Override
    public IDrawable getIcon() {
        return this.guiHelper.createDrawableItemStack(this.getBlockStack());
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    protected List<SlotPosition> getInputSlotPos() {
        return this.inputSlots;
    }

    @Override
    protected List<SlotPosition> getOutputSlotPos() {
        return this.outputSlots;
    }

    protected int getProcessSpeed(String name) {
        Ic2TileEntity tileEntity;
        if ("progress".equals(name) && (tileEntity = this.block.getDummyTe()) != null && tileEntity instanceof TileEntityStandardMachine) {
            return ((TileEntityStandardMachine)tileEntity).defaultOperationLength / 3;
        }
        return 200;
    }
}

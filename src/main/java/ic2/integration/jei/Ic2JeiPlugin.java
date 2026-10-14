package ic2.integration.jei;

import ic2.core.IC2;
import ic2.api.item.IBlockCuttingBlade;
import ic2.core.item.type.BlockCuttingBladeType;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.ItemName;
import ic2.core.ref.Ic2RecipeTypes;
import ic2.core.recipe.v2.RecipeManagerGetter;
import ic2.integration.jei.recipe.canner.CannerBottleCategory;
import ic2.integration.jei.recipe.canner.CannerBottleRecipeWrapper;
import ic2.integration.jei.recipe.machine.FluidMachineCategory;
import ic2.integration.jei.recipe.machine.FluidRecipeWrapper;
import ic2.integration.jei.recipe.machine.DynamicCategory;
import ic2.integration.jei.recipe.machine.IORecipeWrapper;
import ic2.integration.jei.recipe.machine.MetalFormerCategory;
import ic2.integration.jei.recipe.machine.RecyclerRecipeCategory;
import ic2.integration.jei.recipe.machine.RecyclerRecipeWrapper;
import ic2.integration.jei.recipe.misc.ScrapboxRecipeCategory;
import ic2.integration.jei.recipe.misc.ScrapboxRecipeWrapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;

@JeiPlugin
public class Ic2JeiPlugin implements IModPlugin {
    private final RecipeType<IORecipeWrapper> BLAST_FURNACE = RecipeType.create("ic2", "blast_furnace", IORecipeWrapper.class);
    private final RecipeType<IORecipeWrapper> BLOCK_CUTTER = RecipeType.create("ic2", "block_cutter", IORecipeWrapper.class);
    private final RecipeType<IORecipeWrapper> CENTRIFUGE = RecipeType.create("ic2", "centrifuge", IORecipeWrapper.class);
    private final RecipeType<IORecipeWrapper> COMPRESSOR = RecipeType.create("ic2", "compressor", IORecipeWrapper.class);
    private final RecipeType<IORecipeWrapper> EXTRACTOR = RecipeType.create("ic2", "extractor", IORecipeWrapper.class);
    private final RecipeType<IORecipeWrapper> MACERATOR = RecipeType.create("ic2", "macerator", IORecipeWrapper.class);
    private final RecipeType<IORecipeWrapper> METAL_FORMER_EXTRUDING = RecipeType.create("ic2", "metal_former_extruding", IORecipeWrapper.class);
    private final RecipeType<IORecipeWrapper> METAL_FORMER_ROLLING = RecipeType.create("ic2", "metal_former_rolling", IORecipeWrapper.class);
    private final RecipeType<IORecipeWrapper> METAL_FORMER_CUTTING = RecipeType.create("ic2", "metal_former_cutting", IORecipeWrapper.class);
    private final RecipeType<IORecipeWrapper> ORE_WASHER = RecipeType.create("ic2", "ore_washer", IORecipeWrapper.class);
    /** 第四十九轮新增：装罐机「装瓶/装罐」—— "把核燃料压进空燃料棒"就是这条配方。 */
    private final RecipeType<CannerBottleRecipeWrapper> CANNER_BOTTLE = RecipeType.create("ic2", "canner_bottle", CannerBottleRecipeWrapper.class);
    private final RecipeType<FluidRecipeWrapper> CANNER_ENRICH = RecipeType.create("ic2", "canner_enrich", FluidRecipeWrapper.class);
    private final RecipeType<FluidRecipeWrapper> ELECTROLYZER = RecipeType.create("ic2", "electrolyzer", FluidRecipeWrapper.class);
    private final RecipeType<FluidRecipeWrapper> FERMENTER = RecipeType.create("ic2", "fermenter", FluidRecipeWrapper.class);
    private final RecipeType<RecyclerRecipeWrapper> RECYCLER = RecipeType.create("ic2", "recycler", RecyclerRecipeWrapper.class);
    private final RecipeType<ScrapboxRecipeWrapper> SCRAPBOX = RecipeType.create("ic2", "scrapbox", ScrapboxRecipeWrapper.class);

    @Override
    public ResourceLocation getPluginUid() {
        return IC2.getIdentifier("plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new IRecipeCategory[]{
            new DynamicCategory(Ic2Blocks.BLAST_FURNACE, this.BLAST_FURNACE, guiHelper),
            new DynamicCategory(Ic2Blocks.BLOCK_CUTTER, this.BLOCK_CUTTER, guiHelper),
            new DynamicCategory(Ic2Blocks.CENTRIFUGE, this.CENTRIFUGE, guiHelper),
            new DynamicCategory(Ic2Blocks.COMPRESSOR, this.COMPRESSOR, guiHelper),
            new DynamicCategory(Ic2Blocks.EXTRACTOR, this.EXTRACTOR, guiHelper),
            new DynamicCategory(Ic2Blocks.MACERATOR, this.MACERATOR, guiHelper),
            new MetalFormerCategory(this.METAL_FORMER_EXTRUDING, 0, guiHelper),
            new MetalFormerCategory(this.METAL_FORMER_ROLLING, 1, guiHelper),
            new MetalFormerCategory(this.METAL_FORMER_CUTTING, 2, guiHelper),
            new DynamicCategory(Ic2Blocks.ORE_WASHING_PLANT, this.ORE_WASHER, guiHelper),
            new CannerBottleCategory(this.CANNER_BOTTLE, guiHelper),
            new FluidMachineCategory(this.CANNER_ENRICH,
                    guiHelper.createDrawable(ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guicanner.png"), 0, 0, 140, 97),
                    guiHelper.createDrawableItemStack(new ItemStack(Ic2Blocks.CANNER)),
                    net.minecraft.network.chat.Component.translatable("jei.ic2.category.canner_enrich"),
                    new int[]{43, 46}, new int[]{80, 44}, new int[][]{{121, 46}}, 12, 47, false),
            new FluidMachineCategory(this.ELECTROLYZER,
                    guiHelper.createDrawable(ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guielectrolyzer.png"), 0, 0, 166, 80),
                    guiHelper.createDrawableItemStack(new ItemStack(Ic2Blocks.ELECTROLYZER)),
                    new ItemStack(Ic2Blocks.ELECTROLYZER).getHoverName(),
                    new int[]{79, 17}, null, new int[][]{{37, 62}, {58, 62}, {79, 62}, {100, 62}, {121, 62}}, 16, 16, true),
            new FluidMachineCategory(this.FERMENTER,
                    guiHelper.createDrawable(ResourceLocation.fromNamespaceAndPath("ic2", "textures/gui/guifermenter.png"), 0, 0, 184, 100),
                    guiHelper.createDrawableItemStack(new ItemStack(Ic2Blocks.FERMENTER)),
                    new ItemStack(Ic2Blocks.FERMENTER).getHoverName(),
                    new int[]{38, 49}, null, new int[][]{{129, 26}}, 48, 30, 12, 47, false),
            new RecyclerRecipeCategory(Ic2Blocks.RECYCLER, this.RECYCLER, guiHelper),
            new ScrapboxRecipeCategory(this.SCRAPBOX, guiHelper)
        });
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.BLAST_FURNACE), this.BLAST_FURNACE);
        registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.BLOCK_CUTTER), this.BLOCK_CUTTER);
        registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.CENTRIFUGE), this.CENTRIFUGE);
        registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.COMPRESSOR), this.COMPRESSOR);
        registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.EXTRACTOR), this.EXTRACTOR);
        registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.MACERATOR), this.MACERATOR);
        registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.METAL_FORMER), this.METAL_FORMER_CUTTING, this.METAL_FORMER_EXTRUDING, this.METAL_FORMER_ROLLING);
        registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.ORE_WASHING_PLANT), this.ORE_WASHER);
        registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.CANNER), this.CANNER_BOTTLE);
        registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.CANNER), this.CANNER_ENRICH);
        registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.ELECTROLYZER), this.ELECTROLYZER);
        registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.CLASSIC_ELECTROLYZER), this.ELECTROLYZER);
        registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.FERMENTER), this.FERMENTER);
        registration.addRecipeCatalyst(new ItemStack(Ic2Blocks.RECYCLER), this.RECYCLER);
        registration.addRecipeCatalyst(new ItemStack(ic2.core.ref.Ic2Items.SCRAP_BOX), this.SCRAPBOX);
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Level level = Minecraft.getInstance().level;
        RecipeManager recipeManager;
        try {
            recipeManager = IC2.sideProxy.getRecipeManager();
        } catch (Throwable t) {
            org.apache.logging.log4j.LogManager.getLogger("ic2-jei").warn("JEI recipe manager unavailable: {}", t.toString());
            return;
        }
        if (recipeManager == null) {
            org.apache.logging.log4j.LogManager.getLogger("ic2-jei").warn("JEI registerRecipes skipped: recipe manager is null");
            return;
        }
        // 诊断（2026-09-23）：区分两种情形——①客户端同步的 RecipeManager 整体为空（同步时机问题）；
        // ②只有 IC2 配方缺失（注册/序列化问题）。serverRm 为 -1 表示不在单机环境。
        try {
            RecipeManager clientManager = level == null ? null : level.getRecipeManager();
            net.minecraft.server.MinecraftServer server = level == null ? null : level.getServer();
            RecipeManager serverManager = server == null ? null : server.getRecipeManager();
            org.apache.logging.log4j.LogManager.getLogger("ic2-jei").info(
                "JEI diag: clientRm total={} macerator={} | serverRm total={} macerator={} | level={}",
                clientManager == null ? -1 : clientManager.getRecipes().size(),
                clientManager == null ? -1 : clientManager.getAllRecipesFor((net.minecraft.world.item.crafting.RecipeType) Ic2RecipeTypes.MACERATOR).size(),
                serverManager == null ? -1 : serverManager.getRecipes().size(),
                serverManager == null ? -1 : serverManager.getAllRecipesFor((net.minecraft.world.item.crafting.RecipeType) Ic2RecipeTypes.MACERATOR).size(),
                level == null ? "none" : level.getClass().getSimpleName());
        } catch (Throwable t) {
            org.apache.logging.log4j.LogManager.getLogger("ic2-jei").warn("JEI diag failed: {}", t.toString());
        }
        // Build IC2 recipe managers from the active server/client RecipeManager so JEI does not depend on Level timing.
        this.registerRecipes(registration, recipeManager, this.BLAST_FURNACE, Ic2RecipeTypes.BLAST_FURNACE, ic2.api.recipe.Recipes.blastfurnace);
        this.registerRecipes(registration, recipeManager, this.BLOCK_CUTTER, Ic2RecipeTypes.BLOCK_CUTTER, ic2.api.recipe.Recipes.blockcutter);
        this.registerRecipes(registration, recipeManager, this.CENTRIFUGE, Ic2RecipeTypes.CENTRIFUGE, ic2.api.recipe.Recipes.centrifuge);
        this.registerRecipes(registration, recipeManager, this.COMPRESSOR, Ic2RecipeTypes.COMPRESSOR, ic2.api.recipe.Recipes.compressor);
        this.registerRecipes(registration, recipeManager, this.EXTRACTOR, Ic2RecipeTypes.EXTRACTOR, ic2.api.recipe.Recipes.extractor);
        this.registerRecipes(registration, recipeManager, this.MACERATOR, Ic2RecipeTypes.MACERATOR, ic2.api.recipe.Recipes.macerator);
        this.registerRecipes(registration, recipeManager, this.METAL_FORMER_CUTTING, Ic2RecipeTypes.METAL_FORMER_CUTTING, ic2.api.recipe.Recipes.metalformerCutting);
        this.registerRecipes(registration, recipeManager, this.METAL_FORMER_EXTRUDING, Ic2RecipeTypes.METAL_FORMER_EXTRUDING, ic2.api.recipe.Recipes.metalformerExtruding);
        this.registerRecipes(registration, recipeManager, this.METAL_FORMER_ROLLING, Ic2RecipeTypes.METAL_FORMER_ROLLING, ic2.api.recipe.Recipes.metalformerRolling);
        this.registerRecipes(registration, recipeManager, this.ORE_WASHER, Ic2RecipeTypes.ORE_WASHER, ic2.api.recipe.Recipes.oreWashing);
        this.registerCannerBottleRecipes(registration, recipeManager, level);
        this.registerCannerEnrichRecipes(registration, recipeManager, level);
        this.registerElectrolyzerRecipes(registration);
        this.registerFermenterRecipes(registration);
        this.registerRecyclerRecipes(registration);
        this.registerScrapboxRecipes(registration);
    }

    private void registerCannerEnrichRecipes(IRecipeRegistration registration, RecipeManager recipeManager, Level level) {
        List<FluidRecipeWrapper> wrappers = new ArrayList<>();
        try {
            ic2.api.recipe.ICannerEnrichRecipeManager manager =
                    ic2.api.recipe.Recipes.cannerEnrich == null ? null
                            : this.getRecipeManager(ic2.api.recipe.Recipes.cannerEnrich, recipeManager, level);
            if (manager != null) {
                for (ic2.api.recipe.MachineRecipe<ic2.api.recipe.ICannerEnrichRecipeManager.Input, ic2.core.fluid.Ic2FluidStack> recipe : manager.getRecipes()) {
                    ic2.api.recipe.ICannerEnrichRecipeManager.Input input = recipe.getInput();
                    ic2.core.fluid.Ic2FluidStack output = recipe.getOutput();
                    if (input.fluid == null || input.additive == null || output == null || input.fluid.isEmpty() || output.isEmpty()) {
                        continue;
                    }
                    wrappers.add(new FluidRecipeWrapper(input.fluid.getFluid(), input.fluid.getAmountMb(),
                            input.additive.getInputs(), List.of(new FluidRecipeWrapper.FluidOutput(output.getFluid(), output.getAmountMb()))));
                }
            }
        } catch (Throwable t) {
            org.apache.logging.log4j.LogManager.getLogger("ic2-jei").warn("JEI canner_enrich read failed: {}", t.toString());
        }
        org.apache.logging.log4j.LogManager.getLogger("ic2-jei").info("JEI registerRecipes: canner_enrich wrappers={}", wrappers.size());
        registration.addRecipes(this.CANNER_ENRICH, wrappers);
    }

    private void registerElectrolyzerRecipes(IRecipeRegistration registration) {
        List<FluidRecipeWrapper> wrappers = new ArrayList<>();
        try {
            ic2.api.recipe.IElectrolyzerRecipeManager manager = ic2.api.recipe.Recipes.electrolyzer;
            if (manager != null) {
                for (Map.Entry<net.minecraft.world.level.material.Fluid, ic2.api.recipe.IElectrolyzerRecipeManager.ElectrolyzerRecipe> entry : manager.getRecipeMap().entrySet()) {
                    List<FluidRecipeWrapper.FluidOutput> outputs = new ArrayList<>();
                    for (ic2.api.recipe.IElectrolyzerRecipeManager.ElectrolyzerOutput output : entry.getValue().outputs) {
                        if (output.fluid != null && output.fluidAmount > 0) {
                            outputs.add(new FluidRecipeWrapper.FluidOutput(output.fluid, output.fluidAmount));
                        }
                    }
                    if (!outputs.isEmpty() && entry.getValue().inputAmount > 0) {
                        wrappers.add(new FluidRecipeWrapper(entry.getKey(), entry.getValue().inputAmount, List.of(), outputs));
                    }
                }
            }
        } catch (Throwable t) {
            org.apache.logging.log4j.LogManager.getLogger("ic2-jei").warn("JEI electrolyzer read failed: {}", t.toString());
        }
        org.apache.logging.log4j.LogManager.getLogger("ic2-jei").info("JEI registerRecipes: electrolyzer wrappers={}", wrappers.size());
        registration.addRecipes(this.ELECTROLYZER, wrappers);
    }

    private void registerFermenterRecipes(IRecipeRegistration registration) {
        List<FluidRecipeWrapper> wrappers = new ArrayList<>();
        try {
            ic2.api.recipe.IFermenterRecipeManager manager = ic2.api.recipe.Recipes.fermenter;
            if (manager != null) {
                for (Map.Entry<net.minecraft.world.level.material.Fluid, ic2.api.recipe.IFermenterRecipeManager.FermentationProperty> entry : manager.getRecipeMap().entrySet()) {
                    ic2.api.recipe.IFermenterRecipeManager.FermentationProperty recipe = entry.getValue();
                    if (recipe.output != null && recipe.inputAmount > 0 && recipe.outputAmount > 0) {
                        wrappers.add(new FluidRecipeWrapper(entry.getKey(), recipe.inputAmount, List.of(),
                                List.of(new FluidRecipeWrapper.FluidOutput(recipe.output, recipe.outputAmount))));
                    }
                }
            }
        } catch (Throwable t) {
            org.apache.logging.log4j.LogManager.getLogger("ic2-jei").warn("JEI fermenter read failed: {}", t.toString());
        }
        org.apache.logging.log4j.LogManager.getLogger("ic2-jei").info("JEI registerRecipes: fermenter wrappers={}", wrappers.size());
        registration.addRecipes(this.FERMENTER, wrappers);
    }

    private void registerRecyclerRecipes(IRecipeRegistration registration) {
        List<ItemStack> inputs = new ArrayList<>();
        try {
            for (Item item : BuiltInRegistries.ITEM) {
                ItemStack stack = new ItemStack(item);
                if (!stack.isEmpty() && !ic2.core.block.machine.tileentity.TileEntityRecycler.getIsItemBlacklisted(stack)) {
                    inputs.add(stack);
                }
            }
        } catch (Throwable t) {
            org.apache.logging.log4j.LogManager.getLogger("ic2-jei").warn("JEI recycler inputs read failed: {}", t.toString());
        }
        List<RecyclerRecipeWrapper> wrappers = inputs.isEmpty() ? List.of()
                : List.of(new RecyclerRecipeWrapper(inputs, new ItemStack(ic2.core.ref.Ic2Items.SCRAP),
                        1.0 / ic2.core.block.machine.tileentity.TileEntityRecycler.recycleChance()));
        org.apache.logging.log4j.LogManager.getLogger("ic2-jei").info("JEI registerRecipes: recycler acceptedInputs={}", inputs.size());
        registration.addRecipes(this.RECYCLER, wrappers);
    }

    private void registerScrapboxRecipes(IRecipeRegistration registration) {
        List<ScrapboxRecipeWrapper> wrappers = new ArrayList<>();
        try {
            if (ic2.api.recipe.Recipes.scrapboxDrops != null) {
                for (Map.Entry<ItemStack, Float> drop : ic2.api.recipe.Recipes.scrapboxDrops.getDrops().entrySet()) {
                    if (!drop.getKey().isEmpty() && drop.getValue() != null && drop.getValue() > 0.0f) {
                        wrappers.add(new ScrapboxRecipeWrapper(drop.getKey(), drop.getValue()));
                    }
                }
            }
        } catch (Throwable t) {
            org.apache.logging.log4j.LogManager.getLogger("ic2-jei").warn("JEI scrapbox read failed: {}", t.toString());
        }
        org.apache.logging.log4j.LogManager.getLogger("ic2-jei").info("JEI registerRecipes: scrapbox wrappers={}", wrappers.size());
        registration.addRecipes(this.SCRAPBOX, wrappers);
    }

    /**
     * 第四十九轮新增：装罐机「装瓶/装罐」配方的 JEI 注册。
     * 该类型的管理器是 {@code ICannerBottleRecipeManager}（不是 {@code IBasicMachineRecipeManager}），
     * 因此无法复用 {@link #registerRecipes} —— 直接遍历其 {@code getRecipes()} 即可。
     */
    private void registerCannerBottleRecipes(IRecipeRegistration registration, RecipeManager recipeManager, Level level) {
        List<CannerBottleRecipeWrapper> wrappers = new ArrayList<>();
        try {
            ic2.api.recipe.ICannerBottleRecipeManager manager =
                    ic2.api.recipe.Recipes.cannerBottle == null ? null
                            : this.getRecipeManager(ic2.api.recipe.Recipes.cannerBottle, recipeManager, level);
            if (manager != null) {
                for (ic2.api.recipe.MachineRecipe<ic2.api.recipe.ICannerBottleRecipeManager.Input, ItemStack> recipe : manager.getRecipes()) {
                    ic2.api.recipe.ICannerBottleRecipeManager.Input input = recipe.getInput();
                    wrappers.add(new CannerBottleRecipeWrapper(input.container, input.fill, recipe.getOutput()));
                }
            }
        } catch (Throwable t) {
            org.apache.logging.log4j.LogManager.getLogger("ic2-jei").warn("JEI canner_bottle read failed: {}", t.toString());
        }
        org.apache.logging.log4j.LogManager.getLogger("ic2-jei").info("JEI registerRecipes: canner_bottle wrappers={}", wrappers.size());
        registration.addRecipes(this.CANNER_BOTTLE, wrappers);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void registerRecipes(IRecipeRegistration registration, RecipeManager recipeManager, RecipeType<IORecipeWrapper> jeiType,
                                 net.minecraft.world.item.crafting.RecipeType<?> ic2Type,
                                 ic2.api.recipe.Recipes.IGetter<ic2.api.recipe.IBasicMachineRecipeManager> managerGetter) {
        List<IORecipeWrapper> wrappers = new ArrayList<>();
        // 方法1（原）：getAllRecipesFor —— 依赖 byType 的 key 与 ic2Type 实例相等，可能因客户端同步重建实例而不匹配
        int byKeyTotal = recipeManager.getAllRecipesFor((net.minecraft.world.item.crafting.RecipeType)ic2Type).size();
        // 方法3（新）：直接从 IC2 配方系统读取（Rezepte.registerRecipes 代码注册的配方，不进 Minecraft RecipeManager）
        int total = 0;
        int matched = 0;
        if (managerGetter != null) {
            try {
                net.minecraft.world.level.Level level = Minecraft.getInstance().level;
                ic2.api.recipe.IBasicMachineRecipeManager manager = this.getRecipeManager(managerGetter, recipeManager, level);
                Iterable<ic2.api.recipe.MachineRecipe<ic2.api.recipe.IRecipeInput, Collection<ItemStack>>> iterable =
                    (Iterable)manager.getRecipes();
                for (ic2.api.recipe.MachineRecipe<ic2.api.recipe.IRecipeInput, Collection<ItemStack>> machineRecipe : iterable) {
                    total++;
                    matched++;
                    if (jeiType == this.BLOCK_CUTTER) {
                        int hardness = machineRecipe.getMetaData() == null ? Integer.MAX_VALUE : machineRecipe.getMetaData().getInt("hardness");
                        List<ItemStack> blades = new ArrayList<>();
                        for (BlockCuttingBladeType bladeType : BlockCuttingBladeType.values()) {
                            ItemStack bladeStack = ItemName.block_cutting_blade.getItemStack(bladeType);
                            if (bladeStack.getItem() instanceof IBlockCuttingBlade blade && blade.getHardness(bladeStack) >= hardness) {
                                blades.add(bladeStack);
                            }
                        }
                        wrappers.add(new IORecipeWrapper(machineRecipe.getInput(), machineRecipe.getOutput(), List.of(blades)));
                    } else {
                        wrappers.add(new IORecipeWrapper(machineRecipe.getInput(), machineRecipe.getOutput()));
                    }
                }
            } catch (Throwable t) {
                org.apache.logging.log4j.LogManager.getLogger("ic2-jei").warn(
                    "JEI direct recipe read failed for {}: {}", ic2Type, t.toString());
            }
        }
        org.apache.logging.log4j.LogManager.getLogger("ic2-jei").info(
            "JEI registerRecipes: {} allTotal={} ic2Matched={} wrappers={} (byKey={})",
            ic2Type, total, matched, wrappers.size(), byKeyTotal);
        registration.addRecipes(jeiType, wrappers);
    }

    @SuppressWarnings("unchecked")
    private <T> T getRecipeManager(ic2.api.recipe.Recipes.IGetter<T> getter, RecipeManager recipeManager, Level level) {
        if (getter instanceof RecipeManagerGetter<?> managerGetter) {
            return (T)managerGetter.get(recipeManager);
        }
        return level == null ? null : getter.get(level);
    }
}

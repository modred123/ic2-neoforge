package ic2.integration.jei;

import ic2.core.IC2;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2RecipeTypes;
import ic2.integration.jei.recipe.canner.CannerBottleCategory;
import ic2.integration.jei.recipe.canner.CannerBottleRecipeWrapper;
import ic2.integration.jei.recipe.machine.DynamicCategory;
import ic2.integration.jei.recipe.machine.IORecipeWrapper;
import ic2.integration.jei.recipe.machine.MetalFormerCategory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

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
            new CannerBottleCategory(this.CANNER_BOTTLE, guiHelper)
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
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (Minecraft.getInstance().level == null) {
            org.apache.logging.log4j.LogManager.getLogger("ic2-jei").warn("JEI registerRecipes skipped: client level is null");
            return;
        }
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        // 诊断（2026-09-23）：区分两种情形——①客户端同步的 RecipeManager 整体为空（同步时机问题）；
        // ②只有 IC2 配方缺失（注册/序列化问题）。serverRm 为 -1 表示不在单机环境。
        try {
            net.minecraft.server.MinecraftServer server = Minecraft.getInstance().level.getServer();
            RecipeManager serverManager = server == null ? null : server.getRecipeManager();
            org.apache.logging.log4j.LogManager.getLogger("ic2-jei").info(
                "JEI diag: clientRm total={} macerator={} | serverRm total={} macerator={} | level={}",
                recipeManager.getRecipes().size(),
                recipeManager.getAllRecipesFor((net.minecraft.world.item.crafting.RecipeType) Ic2RecipeTypes.MACERATOR).size(),
                serverManager == null ? -1 : serverManager.getRecipes().size(),
                serverManager == null ? -1 : serverManager.getAllRecipesFor((net.minecraft.world.item.crafting.RecipeType) Ic2RecipeTypes.MACERATOR).size(),
                Minecraft.getInstance().level.getClass().getSimpleName());
        } catch (Throwable t) {
            org.apache.logging.log4j.LogManager.getLogger("ic2-jei").warn("JEI diag failed: {}", t.toString());
        }
        // 直接从 IC2 自研配方系统（ic2.api.recipe.Recipes）读取，不再依赖 Minecraft RecipeManager——
        // ic2 配方由 Rezepte.registerRecipes() 代码注册，不进入数据包配方表。
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
        this.registerCannerBottleRecipes(registration);
    }

    /**
     * 第四十九轮新增：装罐机「装瓶/装罐」配方的 JEI 注册。
     * 该类型的管理器是 {@code ICannerBottleRecipeManager}（不是 {@code IBasicMachineRecipeManager}），
     * 因此无法复用 {@link #registerRecipes} —— 直接遍历其 {@code getRecipes()} 即可。
     */
    private void registerCannerBottleRecipes(IRecipeRegistration registration) {
        List<CannerBottleRecipeWrapper> wrappers = new ArrayList<>();
        try {
            net.minecraft.world.level.Level level = Minecraft.getInstance().level;
            ic2.api.recipe.ICannerBottleRecipeManager manager =
                    level == null ? null : ic2.api.recipe.Recipes.cannerBottle.get(level);
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
                ic2.api.recipe.IBasicMachineRecipeManager manager = level == null ? null : managerGetter.get(level);
                Iterable<ic2.api.recipe.MachineRecipe<ic2.api.recipe.IRecipeInput, Collection<ItemStack>>> iterable =
                    (Iterable)manager.getRecipes();
                for (ic2.api.recipe.MachineRecipe<ic2.api.recipe.IRecipeInput, Collection<ItemStack>> machineRecipe : iterable) {
                    total++;
                    matched++;
                    wrappers.add(new IORecipeWrapper(machineRecipe.getInput(), machineRecipe.getOutput()));
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
}

/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.crafting.RecipeManager
 *  net.minecraft.world.level.Level
 */
package ic2.core.recipe.v2;

import ic2.api.recipe.Recipes;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;

public class RecipeManagerGetter<T>
implements Recipes.IGetter<T> {
    private final Function<RecipeManager, T> factory;
    private final Map<RecipeManager, T> cache = new WeakHashMap<RecipeManager, T>();

    public RecipeManagerGetter(Function<RecipeManager, T> function) {
        this.factory = function;
    }

    @Override
    public T get(Level level) {
        if (level.isClientSide()) {
            // 1.21 迁移修复（2026-09-23）：客户端 level 的 RecipeManager 来自 ClientPacketListener
            // （ClientLevel.getRecipeManager() 转发到 connection.getRecipeManager()），内容由
            // ClientboundUpdateRecipesPacket 同步填充，时机上晚于/可能缺失于 JEI 的注册期
            // —— 实测症状：JEI registerRecipes 对 10 台机器全部 allTotal=0（ic2 配方读不到）。
            // 单机（integrated server）下 level.getServer() 非空，直接取服务端权威 RecipeManager；
            // 多人游戏时 getServer() 为 null（集成服务端不存在），退回客户端同步的 RecipeManager。
            net.minecraft.server.MinecraftServer server = level.getServer();
            return this.factory.apply(server != null ? server.getRecipeManager() : level.getRecipeManager());
        }
        return this.cache.computeIfAbsent(level.getRecipeManager(), this.factory);
    }
}


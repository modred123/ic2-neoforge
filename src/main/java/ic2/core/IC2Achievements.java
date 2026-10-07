/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 */
package ic2.core;

import ic2.core.util.LogCategory;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class IC2Achievements {
    /**
     * 第三十八轮补全：可由代码发放的成就 id → advancement 映射。
     *
     * 背景：1.12.2 的 `IC2Achievements.issueAchievement` **本身就是空方法**（成就系统在 ex112 只剩空壳 +
     * `achievement.*` 语言键，所以那些调用点从来没有生效过），迁移版照搬了空实现，于是 1.21 侧只剩
     * 上游那 21 个「获得物品」类 advancement，1.12.2 语言键里另外 21 个成就没有定义。
     *
     * 本轮把 1.12.2 的成就补齐为 `data/ic2/advancement/ic2/**`：
     *  - 「建造 / 获得 / 采掘」类（buildElecFurnace、mineOre、acquireMatter…）：criteria 为
     *    `minecraft:inventory_changed`，**数据驱动自动解锁**，不经过本方法；
     *  - 「行为」类（下表 10 个）：criteria 为 `minecraft:impossible`（criterion 名统一为 `code`），
     *    只能由本方法 award —— 与 1.12.2 的全部 issueAchievement 调用点一一对应。
     */
    private static final Map<String, ResourceLocation> CODE_TRIGGERED = new HashMap<String, ResourceLocation>();

    static {
        CODE_TRIGGERED.put("explodeMachine", IC2.getIdentifier("ic2/explode_machine"));
        // 第五十二轮：原先该成就的 criterion 是 inventory_changed（拿个反应堆舱就解锁），
        // 与"让反应堆熔毁"的语义不符、也永远不会真的触发。现改为真正爆炸时由代码授予。
        CODE_TRIGGERED.put("makeNuclearReactorExplode", IC2.getIdentifier("ic2/build_generator/build_compressor/make_nuclear_reactor_explode"));
        CODE_TRIGGERED.put("dieFromOwnNuke", IC2.getIdentifier("ic2/die_from_own_nuke"));
        CODE_TRIGGERED.put("replicateObject", IC2.getIdentifier("ic2/build_generator/build_compressor/replicate_object"));
        CODE_TRIGGERED.put("teleportFarAway", IC2.getIdentifier("ic2/teleport_far_away"));
        CODE_TRIGGERED.put("terraformEndCultivation", IC2.getIdentifier("ic2/terraform_end_cultivation"));
        CODE_TRIGGERED.put("starveWithQHelmet", IC2.getIdentifier("ic2/starve_with_q_helmet"));
        CODE_TRIGGERED.put("killCreeperChainsaw", IC2.getIdentifier("ic2/kill_creeper_chainsaw"));
        CODE_TRIGGERED.put("killDragonMiningLaser", IC2.getIdentifier("ic2/kill_dragon_mining_laser"));
        CODE_TRIGGERED.put("getZapped", IC2.getIdentifier("ic2/get_zapped"));
        CODE_TRIGGERED.put("fallWithJetpack", IC2.getIdentifier("ic2/fall_with_jetpack"));
    }

    /**
     * 发放成就（1.21 的等价物是 Advancement）。
     * 未在表中的 id（例如「建造」类）是数据驱动的，这里只记一条 warn，便于排查写错的 id。
     */
    public void issueAchievement(Player player, String textId) {
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ResourceLocation resourceLocation = CODE_TRIGGERED.get(textId);
        if (resourceLocation == null) {
            IC2.log.warn(LogCategory.General, "Unknown code-triggered achievement id: %s", textId);
            return;
        }
        ServerPlayer serverPlayer = (ServerPlayer)player;
        MinecraftServer minecraftServer = serverPlayer.getServer();
        if (minecraftServer == null) {
            return;
        }
        AdvancementHolder advancementHolder = minecraftServer.getAdvancements().get(resourceLocation);
        if (advancementHolder == null) {
            IC2.log.warn(LogCategory.General, "Missing advancement %s for achievement %s", resourceLocation, textId);
            return;
        }
        serverPlayer.getAdvancements().award(advancementHolder, "code");
    }
}

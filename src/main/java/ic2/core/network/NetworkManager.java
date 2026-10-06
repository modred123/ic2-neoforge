/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.Connection
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.server.network.ServerGamePacketListenerImpl
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ContainerListener
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.phys.Vec3
 */
package ic2.core.network;

import ic2.api.network.ClientModifiable;
import ic2.api.network.IGrowingBuffer;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.network.INetworkDataProvider;
import ic2.api.network.INetworkItemEventListener;
import ic2.api.network.INetworkManager;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.Ic2Explosion;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.event.WorldData;
import ic2.core.item.IHandHeldInventory;
import ic2.core.item.IHandHeldSubInventory;
import ic2.core.item.tool.HandHeldInventory;
import ic2.core.network.DataEncoder;
import ic2.core.network.GrowingBuffer;
import ic2.core.network.IPlayerItemDataListener;
import ic2.core.network.IRpcProvider;
import ic2.core.network.RpcHandler;
import ic2.core.network.SubPacketType;
import ic2.core.network.TeUpdate;
import ic2.core.network.TeUpdateDataServer;
import ic2.core.util.LogCategory;
import ic2.core.util.ReflectionUtil;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.zip.DeflaterOutputStream;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class NetworkManager
implements INetworkManager {
    private static final int maxPacketDataLength = 32766;
    public static final ResourceLocation channelId = IC2.getIdentifier("m");

    protected boolean isClient() {
        return false;
    }

    public void onTickEnd(WorldData worldData) {
        try {
            TeUpdate.send(worldData, this);
        }
        catch (IOException iOException) {
            throw new RuntimeException(iOException);
        }
    }

    public final void sendPlayerItemData(Player player, int n, Object ... objectArray) {
        GrowingBuffer growingBuffer = new GrowingBuffer(256);
        try {
            SubPacketType.PlayerItemData.writeTo(growingBuffer);
            growingBuffer.writeByte(n);
            DataEncoder.encode(growingBuffer, ((ItemStack)player.getInventory().items.get(n)).getItem(), false);
            growingBuffer.writeVarInt(objectArray.length);
            for (Object object : objectArray) {
                DataEncoder.encode(growingBuffer, object);
            }
        }
        catch (IOException iOException) {
            throw new RuntimeException(iOException);
        }
        growingBuffer.flip();
        if (!this.isClient()) {
            this.sendS2CPacket((ServerPlayer)player, growingBuffer, true);
        } else {
            this.sendC2SPacket(growingBuffer);
        }
    }

    @Override
    public final void updateTileEntityField(BlockEntity blockEntity, String string) {
        if (!this.isClient()) {
            NetworkManager.getTeUpdateData(blockEntity).addGlobalField(string);
        } else if (this.getClientModifiableField(blockEntity.getClass(), string) == null) {
            IC2.log.warn(LogCategory.Network, "Field update for %s failed.", blockEntity);
        } else {
            GrowingBuffer growingBuffer = new GrowingBuffer(64);
            try {
                SubPacketType.TileEntityData.writeTo(growingBuffer);
                DataEncoder.encode(growingBuffer, blockEntity, false);
                NetworkManager.writeFieldData(blockEntity, string, growingBuffer);
            }
            catch (IOException iOException) {
                throw new RuntimeException(iOException);
            }
            growingBuffer.flip();
            this.sendC2SPacket(growingBuffer);
        }
    }

    private Field getClientModifiableField(Class<?> clazz, String string) {
        Field field = ReflectionUtil.getFieldRecursive(clazz, string);
        if (field == null) {
            IC2.log.warn(LogCategory.Network, "Can't find field %s in %s.", string, clazz.getName());
            return null;
        }
        if (field.getAnnotation(ClientModifiable.class) == null) {
            IC2.log.warn(LogCategory.Network, "The field %s in %s is not modifiable.", string, clazz.getName());
            return null;
        }
        return field;
    }

    private static TeUpdateDataServer getTeUpdateData(BlockEntity blockEntity) {
        assert (IC2.sideProxy.isSimulating());
        if (blockEntity == null) {
            throw new NullPointerException();
        }
        WorldData worldData = WorldData.get(blockEntity.getLevel());
        TeUpdateDataServer teUpdateDataServer = worldData.tesToUpdate.get(blockEntity);
        if (teUpdateDataServer == null) {
            teUpdateDataServer = new TeUpdateDataServer();
            worldData.tesToUpdate.put(blockEntity, teUpdateDataServer);
        }
        return teUpdateDataServer;
    }

    public final void updateTileEntityFieldTo(BlockEntity blockEntity, String string, ServerPlayer serverPlayer) {
        assert (!this.isClient());
        NetworkManager.getTeUpdateData(blockEntity).addPlayerField(string, serverPlayer);
    }

    public final void sendComponentUpdate(Ic2TileEntity ic2TileEntity, String string, ServerPlayer serverPlayer, GrowingBuffer growingBuffer) {
        assert (!this.isClient());
        if (serverPlayer.getCommandSenderWorld() != ic2TileEntity.getLevel()) {
            throw new IllegalArgumentException("mismatched world (te " + ic2TileEntity.getLevel() + ", player " + serverPlayer.getCommandSenderWorld() + ")");
        }
        GrowingBuffer growingBuffer2 = new GrowingBuffer(64);
        try {
            SubPacketType.TileEntityBlockComponent.writeTo(growingBuffer2);
            DataEncoder.encode(growingBuffer2, ic2TileEntity, false);
            growingBuffer2.writeString(string);
            growingBuffer2.writeVarInt(growingBuffer.available());
            growingBuffer.writeTo(growingBuffer2);
        }
        catch (IOException iOException) {
            throw new RuntimeException(iOException);
        }
        growingBuffer2.flip();
        this.sendS2CPacket(serverPlayer, growingBuffer2, true);
    }

    @Override
    public final void initiateTileEntityEvent(BlockEntity blockEntity, int n, boolean bl) {
        assert (!this.isClient());
        if (blockEntity.getLevel().players().isEmpty()) {
            return;
        }
        GrowingBuffer growingBuffer = new GrowingBuffer(32);
        try {
            SubPacketType.TileEntityEvent.writeTo(growingBuffer);
            DataEncoder.encode(growingBuffer, blockEntity, false);
            growingBuffer.writeInt(n);
        }
        catch (IOException iOException) {
            throw new RuntimeException(iOException);
        }
        growingBuffer.flip();
        for (ServerPlayer serverPlayer : NetworkManager.getPlayersInRange(blockEntity.getLevel(), blockEntity.getBlockPos(), new ArrayList<ServerPlayer>())) {
            int n2;
            int n3;
            if (bl && (n3 = (int)((double)blockEntity.getBlockPos().getX() + 0.5 - serverPlayer.getX())) * n3 + (n2 = (int)((double)blockEntity.getBlockPos().getZ() + 0.5 - serverPlayer.getZ())) * n2 > 400) continue;
            this.sendS2CPacket(serverPlayer, growingBuffer, false);
        }
    }

    @Override
    public final void initiateItemEvent(Player player, ItemStack itemStack, int n, boolean bl) {
        if (StackUtil.isEmpty(itemStack)) {
            throw new NullPointerException("invalid stack: " + StackUtil.toStringSafe(itemStack));
        }
        assert (!this.isClient());
        GrowingBuffer growingBuffer = new GrowingBuffer(256);
        try {
            SubPacketType.ItemEvent.writeTo(growingBuffer);
            DataEncoder.encode(growingBuffer, player.getGameProfile(), false);
            DataEncoder.encode(growingBuffer, itemStack, false);
            growingBuffer.writeInt(n);
        }
        catch (Exception exception) {
            throw new RuntimeException(exception);
        }
        growingBuffer.flip();
        for (ServerPlayer serverPlayer : NetworkManager.getPlayersInRange(player.getCommandSenderWorld(), player.blockPosition(), new ArrayList<ServerPlayer>())) {
            int n2;
            int n3;
            if (bl && (n3 = (int)(player.getX() - serverPlayer.getX())) * n3 + (n2 = (int)(player.getZ() - serverPlayer.getZ())) * n2 > 400) continue;
            this.sendS2CPacket(serverPlayer, growingBuffer, false);
        }
    }

    @Override
    public void initiateClientItemEvent(ItemStack itemStack, int n) {
        assert (false);
    }

    @Override
    public void initiateClientTileEntityEvent(BlockEntity blockEntity, int n) {
        assert (false);
    }

    public void initiateRpc(int n, Class<? extends IRpcProvider<?>> clazz, Object[] objectArray) {
        assert (false);
    }

    public void requestGUI(IHasGui iHasGui) {
        assert (false);
    }

    private final void handleSubData(GrowingBuffer growingBuffer, ItemStack itemStack, Integer n) {
        boolean bl = n != null && itemStack.getItem() instanceof IHandHeldSubInventory;
        growingBuffer.writeBoolean(bl);
        if (bl) {
            growingBuffer.writeShort(n);
        }
    }

    public final void sendInitialData(BlockEntity blockEntity, ServerPlayer serverPlayer) {
        assert (!this.isClient());
        if (blockEntity instanceof INetworkDataProvider) {
            TeUpdateDataServer teUpdateDataServer = NetworkManager.getTeUpdateData(blockEntity);
            for (String string : ((INetworkDataProvider)blockEntity).getNetworkedFields()) {
                teUpdateDataServer.addPlayerField(string, serverPlayer);
            }
        }
    }

    @Override
    public final void sendInitialData(BlockEntity blockEntity) {
        assert (!this.isClient());
        if (blockEntity instanceof INetworkDataProvider) {
            TeUpdateDataServer teUpdateDataServer = NetworkManager.getTeUpdateData(blockEntity);
            List<String> list = ((INetworkDataProvider)blockEntity).getNetworkedFields();
            for (String string : list) {
                teUpdateDataServer.addGlobalField(string);
            }
            if (TeUpdate.debug) {
                IC2.log.info(LogCategory.Network, "Sending initial TE data for %s (%s).", Util.formatPosition(blockEntity), list);
            }
        }
    }

    public final void sendChat(ServerPlayer serverPlayer, String string) {
        assert (!this.isClient());
        GrowingBuffer growingBuffer = new GrowingBuffer(string.length() * 2);
        growingBuffer.writeString(string);
        growingBuffer.flip();
        this.sendLargePacket(serverPlayer, 1, growingBuffer);
    }

    public final void sendConsole(ServerPlayer serverPlayer, String string) {
        assert (!this.isClient());
        GrowingBuffer growingBuffer = new GrowingBuffer(string.length() * 2);
        growingBuffer.writeString(string);
        growingBuffer.flip();
        this.sendLargePacket(serverPlayer, 2, growingBuffer);
    }

    public final void sendContainerFields(ContainerBase<?> containerBase, String ... stringArray) {
        for (String string : stringArray) {
            this.sendContainerField(containerBase, string);
        }
    }

    /**
     * 第四十二轮修复：把「字段更新 / 容器事件」包发给该容器对应的服务端玩家。
     *
     * 实测证据（latest.log）：
     *   [NetDiag] send field=scanResults containerId=1 listeners=1 sentTo=0
     * 即 `containerBase.getListeners()` 里那唯一的 listener **不是** `ServerPlayer`（1.20.5+ 起容器的同步机制
     * 改成了 `ContainerSynchronizer`：`ServerPlayer.initMenu` 虽仍调用 `addSlotListener(this)`，但监听者列表
     * 已不能再当作"在线玩家列表"用）→ 一个包都没发出去 → **所有 ContainerData 字段更新都到不了客户端**
     * （扫描器结果恒为空、电表读数恒为 0、升级模块配置不回显……都是这一个原因）。
     *
     * 现在的策略与 1.12.2 语义一致：**优先发给容器所属玩家**（`ContainerBase.getPlayer()`），
     * 再遍历 listeners 去重补发（覆盖多人/旁观场景），非 ServerPlayer 的监听者只记一条日志。
     */
    /** 已报告过的非玩家监听者类型（避免每条字段更新都刷日志）。 */
    private static final java.util.Set<String> REPORTED_LISTENER_TYPES = java.util.concurrent.ConcurrentHashMap.newKeySet();

    private void sendToContainerPlayer(ContainerBase<?> containerBase, GrowingBuffer growingBuffer, String string) {
        int n = 0;
        Player player = containerBase.getPlayer();
        if (player instanceof ServerPlayer) {
            this.sendS2CPacket((ServerPlayer)player, growingBuffer, false);
            ++n;
        }
        for (ContainerListener containerListener : containerBase.getListeners()) {
            if (containerListener == player) continue;
            if (!(containerListener instanceof ServerPlayer)) {
                // 实测：1.21.1 的 `ServerPlayer.initMenu` 加进 containerListeners 的是匿名内部类
                // `net.minecraft.server.level.ServerPlayer$2`（不是 ServerPlayer 本身）——
                // 这就是"listeners=1 却 sentTo=0"的根因。每类只报一次，免得刷屏。
                String string2 = containerListener.getClass().getName();
                if (REPORTED_LISTENER_TYPES.add(string2)) {
                    IC2.log.info(LogCategory.Network, "[NetDiag] skipping non-player listener %s (handled by owner routing)", string2);
                }
                continue;
            }
            this.sendS2CPacket((ServerPlayer)containerListener, growingBuffer, false);
            ++n;
        }
    }

    public final void sendContainerField(ContainerBase<?> containerBase, String string) {
        if (this.isClient() && this.getClientModifiableField(((Object)containerBase).getClass(), string) == null) {
            IC2.log.warn(LogCategory.Network, "Field update for %s failed.", new Object[]{containerBase});
            return;
        }
        GrowingBuffer growingBuffer = new GrowingBuffer(256);
        try {
            SubPacketType.ContainerData.writeTo(growingBuffer);
            growingBuffer.writeInt(containerBase.containerId);
            NetworkManager.writeFieldData(containerBase, string, growingBuffer);
        }
        catch (IOException iOException) {
            throw new RuntimeException(iOException);
        }
        growingBuffer.flip();
        if (!this.isClient()) {
            this.sendToContainerPlayer(containerBase, growingBuffer, string);
        } else {
            this.sendC2SPacket(growingBuffer);
        }
    }

    public final void sendContainerEvent(ContainerBase<?> containerBase, String string) {
        GrowingBuffer growingBuffer = new GrowingBuffer(64);
        SubPacketType.ContainerEvent.writeTo(growingBuffer);
        growingBuffer.writeInt(containerBase.containerId);
        growingBuffer.writeString(string);
        growingBuffer.flip();
        if (!this.isClient()) {
            this.sendToContainerPlayer(containerBase, growingBuffer, string);
        } else {
            this.sendC2SPacket(growingBuffer);
        }
    }

    public final void sendHandHeldInvField(ContainerBase<?> containerBase, String string) {
        if (!(containerBase.base instanceof HandHeldInventory)) {
            IC2.log.warn(LogCategory.Network, "Invalid container (%s) sent for field update.", new Object[]{containerBase});
            return;
        }
        if (this.isClient() && this.getClientModifiableField(containerBase.base.getClass(), string) == null) {
            IC2.log.warn(LogCategory.Network, "Field update for %s failed.", new Object[]{containerBase});
            return;
        }
        GrowingBuffer growingBuffer = new GrowingBuffer(256);
        try {
            SubPacketType.HandHeldInvData.writeTo(growingBuffer);
            growingBuffer.writeInt(containerBase.containerId);
            NetworkManager.writeFieldData(containerBase.base, string, growingBuffer);
        }
        catch (IOException iOException) {
            throw new RuntimeException(iOException);
        }
        growingBuffer.flip();
        if (!this.isClient()) {
            this.sendToContainerPlayer(containerBase, growingBuffer, string);
        } else {
            this.sendC2SPacket(growingBuffer);
        }
    }

    final void sendLargePacket(ServerPlayer serverPlayer, int n, GrowingBuffer growingBuffer) {
        boolean bl;
        GrowingBuffer growingBuffer2 = new GrowingBuffer(16384);
        growingBuffer2.writeShort(0);
        try {
            DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(growingBuffer2);
            growingBuffer.writeTo(deflaterOutputStream);
            deflaterOutputStream.close();
        }
        catch (IOException iOException) {
            throw new RuntimeException(iOException);
        }
        growingBuffer2.flip();
        boolean bl2 = true;
        do {
            boolean bl3 = bl = growingBuffer2.available() <= 32766;
            if (!bl2) {
                growingBuffer2.skipBytes(-2);
            }
            SubPacketType.LargePacket.writeTo(growingBuffer2);
            int n2 = 0;
            if (bl2) {
                n2 |= 1;
            }
            if (bl) {
                n2 |= 2;
            }
            growingBuffer2.write(n2 |= n << 2);
            growingBuffer2.skipBytes(-2);
            if (bl) {
                this.sendS2CPacket(serverPlayer, growingBuffer2, true);
                assert (!growingBuffer2.hasAvailable());
            } else {
                this.sendS2CPacket(serverPlayer, growingBuffer2.copy(32766), true);
            }
            bl2 = false;
        } while (!bl);
    }

    public void onPacket(ByteBuf byteBuf, Player player) {
        assert (!player.level().isClientSide);
        try {
            this.onPacketData(GrowingBuffer.wrap(byteBuf), player);
        }
        catch (Throwable throwable) {
            IC2.log.warn(LogCategory.Network, throwable, "Network read failed");
            throw new RuntimeException(throwable);
        }
    }

    private void onPacketData(GrowingBuffer growingBuffer, final Player player) throws IOException {
        if (!growingBuffer.hasAvailable()) {
            return;
        }
        SubPacketType subPacketType = SubPacketType.read(growingBuffer, true);
        if (subPacketType == null) {
            return;
        }
        switch (subPacketType) {
            case ItemEvent: {
                final ItemStack itemStack = DataEncoder.decode((IGrowingBuffer)growingBuffer, ItemStack.class);
                final int n = growingBuffer.readInt();
                if (!(itemStack.getItem() instanceof INetworkItemEventListener)) break;
                IC2.sideProxy.requestTick(true, new Runnable(){

                    @Override
                    public void run() {
                        ((INetworkItemEventListener)itemStack.getItem()).onNetworkEvent(itemStack, player, n);
                    }
                });
                break;
            }
            case KeyUpdate: {
                final int n = growingBuffer.readInt();
                IC2.sideProxy.requestTick(true, new Runnable(){

                    @Override
                    public void run() {
                        IC2.keyboard.processKeyUpdate(player, n);
                    }
                });
                break;
            }
            case TileEntityEvent: {
                final Object object = DataEncoder.decodeDeferred(growingBuffer, BlockEntity.class);
                final int n = growingBuffer.readInt();
                IC2.sideProxy.requestTick(true, new Runnable(){

                    @Override
                    public void run() {
                        BlockEntity blockEntity = (BlockEntity)DataEncoder.getValue(object, player.getServer());
                        if (blockEntity instanceof INetworkClientTileEntityEventListener) {
                            ((INetworkClientTileEntityEventListener)blockEntity).onNetworkEvent(player, n);
                        }
                    }
                });
                break;
            }
            case RequestGUI: {
                final boolean bl = growingBuffer.readBoolean();
                Object object = bl ? null : DataEncoder.decodeDeferred(growingBuffer, BlockEntity.class);
                IC2.sideProxy.requestTick(true, new Runnable(){

                    @Override
                    public void run() {
                        if (bl) {
                            for (InteractionHand interactionHand : Util.HANDS) {
                                ItemStack itemStack = player.getItemInHand(interactionHand);
                                if (itemStack == null || !(itemStack.getItem() instanceof IHandHeldInventory)) continue;
                                IHasGui iHasGui = ((IHandHeldInventory)itemStack.getItem()).getInventory(player, interactionHand, itemStack);
                                iHasGui.openManagedItem(player, interactionHand, null);
                                break;
                            }
                        }
                    }
                });
                break;
            }
            case Rpc: {
                RpcHandler.processRpcRequest(growingBuffer, (ServerPlayer)player);
                break;
            }
            default: {
                this.onCommonPacketData(subPacketType, true, growingBuffer, player);
            }
        }
    }

    protected void onCommonPacketData(SubPacketType subPacketType, boolean bl, GrowingBuffer growingBuffer, final Player player) throws IOException {
        switch (subPacketType) {
            case PlayerItemData: {
                final byte by = growingBuffer.readByte();
                final Item item = DataEncoder.decode((IGrowingBuffer)growingBuffer, Item.class);
                int n = growingBuffer.readVarInt();
                final Object[] objectArray = new Object[n];
                for (int i = 0; i < n; ++i) {
                    objectArray[i] = DataEncoder.decode(growingBuffer);
                }
                if (by < 0 || by >= 9) break;
                IC2.sideProxy.requestTick(bl, new Runnable(){

                    @Override
                    public void run() {
                        for (int i = 0; i < objectArray.length; ++i) {
                            objectArray[i] = DataEncoder.getValue(objectArray[i], player.getServer());
                        }
                        ItemStack itemStack = (ItemStack)player.getInventory().items.get(by);
                        if (!StackUtil.isEmpty(itemStack) && itemStack.getItem() == item && item instanceof IPlayerItemDataListener) {
                            ((IPlayerItemDataListener)item).onPlayerItemNetworkData(player, by, objectArray);
                        }
                    }
                });
                break;
            }
            case ContainerData: {
                final int n = growingBuffer.readInt();
                final String string = growingBuffer.readString();
                final Object object = DataEncoder.decode(growingBuffer);
                IC2.sideProxy.requestTick(bl, new Runnable(){

                    @Override
                    public void run() {
                        if (player.containerMenu instanceof ContainerBase && player.containerMenu.containerId == n && (NetworkManager.this.isClient() || NetworkManager.this.getClientModifiableField(player.containerMenu.getClass(), string) != null)) {
                            ReflectionUtil.setValueRecursive(player.containerMenu, string, DataEncoder.getValue(object, player.getServer()));
                        }
                    }
                });
                break;
            }
            case ContainerEvent: {
                final int n = growingBuffer.readInt();
                final String string = growingBuffer.readString();
                // 第四十四轮诊断：客户端点击（如电表"重置"）是否真的送达服务端
                IC2.log.info(LogCategory.Network, "[NetDiag] ContainerEvent recv event=%s containerId=%d", string, n);
                IC2.sideProxy.requestTick(bl, new Runnable(){

                    @Override
                    public void run() {
                        boolean bl2 = player.containerMenu instanceof ContainerBase && player.containerMenu.containerId == n;
                        IC2.log.info(LogCategory.Network, "[NetDiag] ContainerEvent apply event=%s match=%b menu=%s menuId=%s", string, bl2, player.containerMenu == null ? "null" : player.containerMenu.getClass().getSimpleName(), player.containerMenu == null ? "-" : Integer.valueOf(player.containerMenu.containerId));
                        if (bl2) {
                            ((ContainerBase)player.containerMenu).onContainerEvent(string);
                        }
                    }
                });
                break;
            }
            case HandHeldInvData: {
                final int n = growingBuffer.readInt();
                final String string = growingBuffer.readString();
                final Object object = DataEncoder.decode(growingBuffer);
                IC2.sideProxy.requestTick(bl, new Runnable(){

                    @Override
                    public void run() {
                        if (player.containerMenu instanceof ContainerBase && player.containerMenu.containerId == n) {
                            ContainerBase containerBase = (ContainerBase)player.containerMenu;
                            if (containerBase.base instanceof HandHeldInventory && (NetworkManager.this.isClient() || NetworkManager.this.getClientModifiableField(containerBase.base.getClass(), string) != null)) {
                                ReflectionUtil.setValueRecursive(containerBase.base, string, DataEncoder.getValue(object, player.getServer()));
                            }
                        }
                    }
                });
                break;
            }
            case TileEntityData: {
                final Object object = DataEncoder.decodeDeferred(growingBuffer, BlockEntity.class);
                final String string = growingBuffer.readString();
                final Object object2 = DataEncoder.decode(growingBuffer);
                IC2.sideProxy.requestTick(bl, new Runnable(){

                    @Override
                    public void run() {
                        BlockEntity blockEntity = (BlockEntity)DataEncoder.getValue(object, player.getServer());
                        if (blockEntity != null && (NetworkManager.this.isClient() || NetworkManager.this.getClientModifiableField(blockEntity.getClass(), string) != null)) {
                            ReflectionUtil.setValueRecursive(blockEntity, string, DataEncoder.getValue(object2, player.getServer()));
                        }
                    }
                });
                break;
            }
            default: {
                IC2.log.warn(LogCategory.Network, "Unhandled packet type: %s", subPacketType.name());
            }
        }
    }

    public void initiateKeyUpdate(int n) {
    }

    public void sendLoginData() {
    }

    public final void initiateExplosionEffect(Level level, Vec3 vec3, Ic2Explosion.Type type) {
        assert (!this.isClient());
        try {
            GrowingBuffer growingBuffer = new GrowingBuffer(32);
            SubPacketType.ExplosionEffect.writeTo(growingBuffer);
            DataEncoder.encode(growingBuffer, level, false);
            DataEncoder.encode(growingBuffer, vec3, false);
            DataEncoder.encode(growingBuffer, (Object)type, false);
            growingBuffer.flip();
            for (Player player : level.players()) {
                if (!(player instanceof ServerPlayer) || !(player.distanceToSqr(vec3.x, vec3.y, vec3.z) < 128.0)) continue;
                this.sendS2CPacket((ServerPlayer)player, growingBuffer, false);
            }
        }
        catch (IOException iOException) {
            throw new RuntimeException(iOException);
        }
    }

    protected void sendC2SPacket(GrowingBuffer growingBuffer) {
        throw new UnsupportedOperationException("can't send c2s packet serverside");
    }

    protected final void sendS2CPacket(ServerPlayer serverPlayer, GrowingBuffer growingBuffer, boolean bl) {
        assert (!this.isClient());
        ByteBuf byteBuf = NetworkManager.makePacket(growingBuffer, bl);
        ServerGamePacketListenerImpl serverGamePacketListenerImpl = serverPlayer.connection;
        if (serverGamePacketListenerImpl == null) {
            return;
        }
        Connection connection = serverGamePacketListenerImpl.getConnection();
        if (connection == null || !connection.isConnected()) {
            return;
        }
        // 1.21.1 修复（第二十二轮）：必须把 makePacket 的 ByteBuf **立即复制成 byte[]** 再交给 payload。
        // 原因见 Ic2Payload 注释：NeoForge 的 GenericPacketSplitter 会对每个出站包先试编码一次测大小，
        // 若仍以 ByteBuf 承载并让 codec 用 writeBytes(ByteBuf)（有副作用、消耗 readerIndex），
        // 第二次真正编码时就会写出 0 字节。
        byte[] bytes = io.netty.buffer.ByteBufUtil.getBytes(byteBuf);
        net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket clientboundCustomPayloadPacket = new net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket(new Ic2Payload(bytes));
        connection.send(clientboundCustomPayloadPacket);
    }

    static <T extends Collection<ServerPlayer>> T getPlayersInRange(Level level, BlockPos blockPos, T t) {
        if (!(level instanceof ServerLevel)) {
            return t;
        }
        List list = ((ServerLevel)level).getChunkSource().chunkMap.getPlayers(new ChunkPos(blockPos), false);
        T t2 = t;
        Objects.requireNonNull(t2);
        list.forEach((Consumer<ServerPlayer>)t2::add);
        return t;
    }

    static void writeFieldData(Object object, String string, GrowingBuffer growingBuffer) throws IOException {
        int n = string.indexOf(61);
        if (n != -1) {
            growingBuffer.writeString(string.substring(0, n));
            DataEncoder.encode(growingBuffer, string.substring(n + 1));
        } else {
            growingBuffer.writeString(string);
            try {
                DataEncoder.encode(growingBuffer, ReflectionUtil.getValueRecursive(object, string));
            }
            catch (NoSuchFieldException noSuchFieldException) {
                throw new RuntimeException("Can't find field " + string + " in " + object.getClass().getName(), noSuchFieldException);
            }
        }
    }

    protected static ByteBuf makePacket(GrowingBuffer growingBuffer, boolean bl) {
        return growingBuffer.toByteBuf(bl);
    }
}


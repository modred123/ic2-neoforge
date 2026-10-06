/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Registry
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 */
package ic2.core.network;

import ic2.api.network.IGrowingBuffer;
import ic2.api.network.INetworkUpdateListener;
import ic2.core.IC2;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.event.WorldData;
import ic2.core.network.DataEncoder;
import ic2.core.network.GrowingBuffer;
import ic2.core.network.NetworkManager;
import ic2.core.network.TeUpdateDataClient;
import ic2.core.network.TeUpdateDataServer;
import ic2.core.util.LogCategory;
import ic2.core.util.ReflectionUtil;
import ic2.core.util.Util;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

class TeUpdate {
    static final boolean debug = System.getProperty("ic2.network.debug.teupdate") != null;

    TeUpdate() {
    }

    public static void send(WorldData worldData, NetworkManager networkManager) throws IOException {
        Object object;
        BlockEntity blockEntity;
        if (worldData.tesToUpdate.isEmpty()) {
            return;
        }
        IdentityHashMap<ServerPlayer, GrowingBuffer> identityHashMap = new IdentityHashMap<ServerPlayer, GrowingBuffer>();
        ArrayList<ServerPlayer> arrayList = new ArrayList<ServerPlayer>();
        GrowingBuffer growingBuffer = new GrowingBuffer();
        for (Map.Entry<BlockEntity, TeUpdateDataServer> entry : worldData.tesToUpdate.entrySet()) {
            blockEntity = entry.getKey();
            NetworkManager.getPlayersInRange(blockEntity.getLevel(), blockEntity.getBlockPos(), arrayList);
            if (arrayList.isEmpty()) continue;
            object = entry.getValue();
            DataEncoder.encode(growingBuffer, blockEntity.getBlockPos(), false);
            growingBuffer.mark();
            growingBuffer.writeShort(0);
            for (String string : ((TeUpdateDataServer)object).getGlobalFields()) {
                NetworkManager.writeFieldData(blockEntity, string, growingBuffer);
            }
            growingBuffer.flip();
            for (ServerPlayer serverPlayer : arrayList) {
                Collection<String> collection = ((TeUpdateDataServer)object).getPlayerFields(serverPlayer);
                int n = ((TeUpdateDataServer)object).getGlobalFields().size() + collection.size();
                if (n == 0) continue;
                if (n > 65535) {
                    throw new RuntimeException("too many fields for " + blockEntity + ": " + n);
                }
                growingBuffer.reset();
                growingBuffer.writeShort(n);
                growingBuffer.rewind();
                GrowingBuffer growingBuffer2 = identityHashMap.get(serverPlayer);
                if (growingBuffer2 == null) {
                    growingBuffer2 = new GrowingBuffer(0);
                    identityHashMap.put(serverPlayer, growingBuffer2);
                    DataEncoder.encode(growingBuffer2, Util.getDimId(serverPlayer.getCommandSenderWorld()), false);
                }
                growingBuffer.writeTo(growingBuffer2);
                growingBuffer.rewind();
                for (String string2 : collection) {
                    NetworkManager.writeFieldData(blockEntity, string2, growingBuffer2);
                }
            }
            growingBuffer.clear();
            arrayList.clear();
        }
        worldData.tesToUpdate.clear();
        for (Map.Entry<ServerPlayer, GrowingBuffer> entry : identityHashMap.entrySet()) {
            ServerPlayer serverPlayer = entry.getKey();
            GrowingBuffer playerBuffer = entry.getValue();
            playerBuffer.flip();
            networkManager.sendLargePacket(serverPlayer, 0, playerBuffer);
        }
    }

    static void receive(GrowingBuffer growingBuffer) throws IOException {
        final ResourceLocation resourceLocation = (ResourceLocation)DataEncoder.getValue(DataEncoder.decode((IGrowingBuffer)growingBuffer, DataEncoder.EncodedType.ResourceLocation), null);
        final TeUpdateDataClient teUpdateDataClient = new TeUpdateDataClient();
        while (growingBuffer.hasAvailable()) {
            BlockPos blockPos = DataEncoder.decode((IGrowingBuffer)growingBuffer, BlockPos.class);
            int n = growingBuffer.readUnsignedShort();
            TeUpdateDataClient.TeData teData = teUpdateDataClient.addTe(blockPos, n);
            for (int i = 0; i < n; ++i) {
                String string = growingBuffer.readString();
                Object object = DataEncoder.decode(growingBuffer);
                if (string.equals("teBlk")) {
                    String string2 = ((String)Objects.requireNonNull(object)).split(":")[1];
                    ResourceLocation resourceLocation2 = IC2.getIdentifier(string2);
                    teData.teType = (Ic2TileEntityBlock)BuiltInRegistries.BLOCK.get(resourceLocation2);
                    continue;
                }
                teData.addField(string, object);
            }
            if (!teData.hasClass()) continue;
            Class<? extends Ic2TileEntity> clazz = teData.teType.getTeClass();
            for (TeUpdateDataClient.FieldData fieldData : teData.getFields()) {
                fieldData.field = ReflectionUtil.getFieldRecursive(clazz, fieldData.name);
            }
        }
        if (debug) {
            TeUpdate.printDebugOutput(resourceLocation, teUpdateDataClient);
        }
        IC2.sideProxy.requestTick(false, new Runnable(){

            @Override
            public void run() {
                Level level = IC2.sideProxy.getPlayerWorld();
                if (level == null || !Util.getDimId(level).equals((Object)resourceLocation)) {
                    return;
                }
                for (TeUpdateDataClient.TeData teData : teUpdateDataClient.getTes()) {
                    try {
                        TeUpdate.apply(teData, level);
                    }
                    catch (Throwable throwable) {
                        IC2.log.warn(LogCategory.Network, throwable, "TE update at %s failed.", Util.formatPosition((BlockGetter)level, teData.pos));
                    }
                }
            }
        });
    }

    private static void printDebugOutput(ResourceLocation resourceLocation, TeUpdateDataClient teUpdateDataClient) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("dimension: ");
        stringBuilder.append(resourceLocation);
        stringBuilder.append(", ");
        stringBuilder.append(teUpdateDataClient.getTes().size());
        stringBuilder.append(" tes:\n");
        for (TeUpdateDataClient.TeData teData : teUpdateDataClient.getTes()) {
            stringBuilder.append("  pos: ");
            stringBuilder.append(teData.pos.getX());
            stringBuilder.append('/');
            stringBuilder.append(teData.pos.getY());
            stringBuilder.append('/');
            stringBuilder.append(teData.pos.getZ());
            stringBuilder.append(", ");
            stringBuilder.append(teData.getFields().size());
            stringBuilder.append(" fields:\n");
            for (TeUpdateDataClient.FieldData fieldData : teData.getFields()) {
                stringBuilder.append("    ");
                stringBuilder.append(fieldData.name);
                stringBuilder.append(" = ");
                stringBuilder.append(fieldData.value);
                if (fieldData.value != null) {
                    stringBuilder.append(" (");
                    stringBuilder.append(fieldData.value.getClass().getSimpleName());
                    stringBuilder.append(')');
                }
                stringBuilder.append('\n');
            }
            if (teData.teType != null) {
                stringBuilder.append("    TE Type: ");
                stringBuilder.append(teData.teType.getName());
                stringBuilder.append('\n');
                if (teData.teType.getTeClass() != null) {
                    stringBuilder.append("    TE Class: ");
                    stringBuilder.append(teData.teType.getTeClass().getName());
                    stringBuilder.append('\n');
                    continue;
                }
                stringBuilder.append("    no TE Class\n");
                continue;
            }
            stringBuilder.append("    no TE Type\n");
        }
        stringBuilder.setLength(stringBuilder.length() - 1);
        IC2.log.info(LogCategory.Network, "Received TE Update:\n" + stringBuilder.toString());
    }

    private static void apply(TeUpdateDataClient.TeData teData, Level level) {
        if (!level.hasChunkAt(teData.pos)) {
            if (debug) {
                IC2.log.info(LogCategory.Network, "Skipping update at %s, chunk not loaded.", Util.formatPosition((BlockGetter)level, teData.pos));
            }
            return;
        }
        BlockState blockState = level.getBlockState(teData.pos);
        if (teData.teType != null && blockState.getBlock() != teData.teType) {
            if (debug) {
                IC2.log.info(LogCategory.Network, "Can't apply update to %d/%d/%d, invalid state %s.", teData.pos.getX(), teData.pos.getY(), teData.pos.getZ(), blockState);
            }
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(teData.pos);
        if (teData.hasClass() && (blockEntity == null || blockEntity.getClass() != teData.teType.getTeClass() || blockEntity.isRemoved() || blockEntity.getLevel() != level)) {
            if (debug) {
                IC2.log.info(LogCategory.Network, "Instantiating %s with %s.", Util.formatPosition((BlockGetter)level, teData.pos), teData.teType.getTeClass().getName());
            }
            assert (!blockEntity.isRemoved());
            assert (blockEntity.getLevel() == level);
        } else {
            if (blockEntity == null) {
                if (debug) {
                    IC2.log.info(LogCategory.Network, "Can't apply update at %s, no te and no teClass.", Util.formatPosition((BlockGetter)level, teData.pos));
                }
                return;
            }
            if (blockEntity.isRemoved() || blockEntity.getLevel() != level) {
                if (debug) {
                    IC2.log.warn(LogCategory.Network, "Can't apply update at %s, invalid te and no teClass.", Util.formatPosition((BlockGetter)level, teData.pos));
                }
                return;
            }
            if (debug) {
                IC2.log.info(LogCategory.Network, "TE class at %s unchanged.", Util.formatPosition((BlockGetter)level, teData.pos));
            }
        }
        for (TeUpdateDataClient.FieldData fieldData : teData.getFields()) {
            Object t = DataEncoder.getValue(fieldData.value, null);
            if (fieldData.field != null) {
                ReflectionUtil.setValue(blockEntity, fieldData.field, t);
            } else {
                ReflectionUtil.setValueRecursive(blockEntity, fieldData.name, t);
            }
            if (!(blockEntity instanceof INetworkUpdateListener)) continue;
            ((INetworkUpdateListener)blockEntity).onNetworkUpdate(fieldData.name);
        }
    }
}


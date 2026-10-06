/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityTicker
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.Fluid
 *  net.minecraft.world.phys.Vec3
 */
package ic2.core.item.tool;

import ic2.api.crops.CropCard;
import ic2.api.energy.EnergyNet;
import ic2.api.item.IBoxable;
import ic2.api.item.IDebuggable;
import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;
import ic2.api.reactor.IReactor;
import ic2.api.tile.IEnergyStorage;
import ic2.core.IC2;
import ic2.core.block.comp.Energy;
import ic2.core.block.comp.Redstone;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.generator.tileentity.TileEntityBaseGenerator;
import ic2.core.block.personal.IPersonalBlock;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.crop.TileEntityCrop;
import ic2.core.energy.grid.EnergyNetGlobal;
import ic2.core.item.InfiniteElectricItemManager;
import ic2.core.item.PriorityUsableItem;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;

public class ItemDebug
extends Item
implements PriorityUsableItem,
ISpecialElectricItem,
IBoxable {
    private static IElectricItemManager manager = null;

    public ItemDebug(Item.Properties properties) {
        super(properties);
    }

        public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext useOnContext) {
        Level level = useOnContext.getLevel();
        BlockPos blockPos = useOnContext.getClickedPos();
        Player player = useOnContext.getPlayer();
        Mode mode = ItemDebug.getMode(itemStack);
        if (IC2.keyboard.isModeSwitchKeyDown(player)) {
            if (!level.isClientSide) {
                mode = Mode.modes[(mode.ordinal() + 1) % Mode.modes.length];
                ItemDebug.setMode(itemStack, mode);
                IC2.sideProxy.messagePlayer(player, "Debug Item Mode: " + mode.getName(), new Object[0]);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof IDebuggable) {
            if (level.isClientSide) {
                return InteractionResult.PASS;
            }
            IDebuggable iDebuggable = (IDebuggable)blockEntity;
            if (iDebuggable.isDebuggable() && !level.isClientSide) {
                IC2.sideProxy.messagePlayer(player, iDebuggable.getDebugText(), new Object[0]);
            }
            return level.isClientSide ? InteractionResult.PASS : InteractionResult.SUCCESS;
        }
        Output output = new Output();
        switch (mode) {
            case InterfacesFields: 
            case InterfacesFieldsRetrace: {
                String string = ItemDebug.getPlatform(level);
                BlockState blockState = level.getBlockState(blockPos);
                Block block = blockState.getBlock();
                BlockEntity blockEntity2 = level.getBlockEntity(blockPos);
                output.both("[%s] block state: %s%nname: %s%ncls: %s%nbe: %s", string, blockState, block.getDescriptionId(), block.getClass().getName(), blockEntity2);
                if (blockEntity2 != null) {
                    output.part("[%s] interfaces:", string);
                    Class<?> tileEntityComponent = blockEntity2.getClass();
                    do {
                        for (Class<?> clazz : tileEntityComponent.getInterfaces()) {
                            output.part(' ').part(clazz.getName());
                        }
                    } while ((tileEntityComponent = tileEntityComponent.getSuperclass()) != null);
                    output.partToConsole();
                }
                output.console("block fields:");
                ItemDebug.dumpObjectFields(block, output);
                if (blockEntity2 == null) break;
                output.console("");
                output.console("tile entity fields:");
                ItemDebug.dumpObjectFields(blockEntity2, output);
                break;
            }
            case TileData: {
                if (level.isClientSide) {
                    return InteractionResult.PASS;
                }
                BlockEntity blockEntity3 = level.getBlockEntity(blockPos);
                if (blockEntity3 instanceof Ic2TileEntity) {
                    Ic2TileEntity ic2TileEntity = (Ic2TileEntity)blockEntity3;
                    output.chat("Block: Active=%b Facing=%s", ic2TileEntity.getActive(), ic2TileEntity.getFacing());
                    for (TileEntityComponent object3 : ic2TileEntity.getComponents()) {
                        TileEntityComponent tileEntityComponent;
                        if (object3 instanceof Energy) {
                            tileEntityComponent = (Energy)object3;
                            output.chat("Energy: %.2f / %.2f", ((Energy)tileEntityComponent).getEnergy(), ((Energy)tileEntityComponent).getCapacity());
                            continue;
                        }
                        if (!(object3 instanceof Redstone)) continue;
                        tileEntityComponent = (Redstone)object3;
                        output.chat("Redstone: %d", ((Redstone)tileEntityComponent).getRedstoneInput());
                    }
                }
                if (blockEntity3 instanceof TileEntityBaseGenerator) {
                    TileEntityBaseGenerator baseGenerator = (TileEntityBaseGenerator)blockEntity3;
                    output.chat("BaseGen: Fuel=%d", baseGenerator.fuel);
                }
                if (blockEntity3 instanceof IEnergyStorage) {
                    IEnergyStorage energyStorage = (IEnergyStorage)blockEntity3;
                    output.chat("EnergyStorage: Stored=%d", energyStorage.getStored());
                }
                if (blockEntity3 instanceof IReactor) {
                    IReactor reactor = (IReactor)blockEntity3;
                    output.chat("Reactor: Heat=%d MaxHeat=%d HEM=%f Output=%f", reactor.getHeat(), reactor.getMaxHeat(), Float.valueOf(reactor.getHeatEffectModifier()), Float.valueOf(reactor.getReactorEnergyOutput()));
                }
                if (blockEntity3 instanceof IPersonalBlock) {
                    IPersonalBlock personalBlock = (IPersonalBlock)blockEntity3;
                    output.chat("PersonalBlock: CanAccess=%b", personalBlock.permitsAccess(player.getGameProfile()));
                }
                if (!(blockEntity3 instanceof TileEntityCrop)) break;
                TileEntityCrop tileEntityCrop2 = (TileEntityCrop)blockEntity3;
                CropCard cropCard = tileEntityCrop2.getCrop();
                String printStream2 = cropCard != null ? cropCard.getOwner() + ":" + cropCard.getId() : "none";
                output.chat("Crop: Crop=%s Size=%d Growth=%d Gain=%d Resistance=%d Nutrients=%d Water=%d GrowthPoints=%d%n Cross=%b", printStream2, tileEntityCrop2.getCurrentAge(), tileEntityCrop2.getStatGrowth(), tileEntityCrop2.getStatGain(), tileEntityCrop2.getStatResistance(), tileEntityCrop2.getStorageNutrients(), tileEntityCrop2.getStorageWater(), tileEntityCrop2.getGrowthPoints(), tileEntityCrop2.isCrossingBase());
                break;
            }
            case EnergyNet: {
                if (level.isClientSide) {
                    return InteractionResult.PASS;
                }
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                PrintStream printStream = new PrintStream((OutputStream)byteArrayOutputStream, false, StandardCharsets.UTF_8);
                ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();
                PrintStream printStream2 = new PrintStream((OutputStream)byteArrayOutputStream, false, StandardCharsets.UTF_8);
                if (!((EnergyNetGlobal)EnergyNet.instance).dumpDebugInfo(level, blockPos, printStream, printStream2)) {
                    return InteractionResult.PASS;
                }
                printStream2.flush();
                printStream.flush();
                if (byteArrayOutputStream.size() > 0) {
                    output.console(byteArrayOutputStream.toString(StandardCharsets.UTF_8).stripTrailing());
                }
                if (byteArrayOutputStream2.size() <= 0) break;
                output.chat(byteArrayOutputStream2.toString(StandardCharsets.UTF_8).stripTrailing());
                break;
            }
            case Accelerate: 
            case AccelerateX100: {
                if (level.isClientSide) {
                    return InteractionResult.PASS;
                }
                ItemDebug.accelerate(level, blockPos, mode == Mode.Accelerate ? 1000 : 100000, output);
            }
        }
        output.flush(player);
        return level.isClientSide ? InteractionResult.PASS : InteractionResult.SUCCESS;
    }

    private static boolean accelerate(Level level, BlockPos blockPos, int n, Output output) {
        BlockEntity blockEntity;
        BlockState blockState = level.getBlockState(blockPos);
        if (!blockState.hasBlockEntity() || (blockEntity = level.getBlockEntity(blockPos)) == null) {
            if (!blockState.isRandomlyTicking()) {
                return false;
            }
            output.chat("Running up to %d ticks on % (%s).", n, blockState.getBlock(), blockPos);
            for (int i = 0; i < n && level.getBlockState(blockPos) == blockState; ++i) {
                blockState.randomTick((ServerLevel)level, blockPos, IC2.random);
                if (level.getBlockState(blockPos) == blockState) continue;
                output.chat("Ran %d ticks before a state change.", i);
                break;
            }
            return true;
        }
        BlockEntityTicker blockEntityTicker = blockState.getTicker(level, blockEntity.getType());
        if (blockEntityTicker == null) {
            return false;
        }
        output.chat("Running %s ticks on %s.", n, blockEntity);
        int n2 = 0;
        int n3 = -1;
        for (int i = 0; i < n; ++i) {
            if (blockEntity.isRemoved()) {
                ++n2;
                blockState = level.getBlockState(blockPos);
                if (!blockState.hasBlockEntity() || (blockEntity = level.getBlockEntity(blockPos)) == null || blockEntity.isRemoved() || (blockEntityTicker = blockState.getTicker(level, blockEntity.getType())) == null) {
                    n3 = i;
                    break;
                }
            }
            blockEntityTicker.tick(level, blockPos, blockState, blockEntity);
        }
        if (n2 > 0) {
            if (n3 != -1) {
                output.chat("The tile entity changed %d time(s), interrupted after %d updates.", n2, n3);
            } else {
                output.chat("The tile entity changed %d time(s).", n2);
            }
        }
        return true;
    }

    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity livingEntity, InteractionHand interactionHand) {
        return ItemDebug.handleEntity(itemStack, player, (Entity)livingEntity);
    }

    private static InteractionResult handleEntity(ItemStack itemStack, Player player, Entity entity) {
        Mode mode = ItemDebug.getMode(itemStack);
        if (mode != Mode.InterfacesFieldsRetrace) {
            return InteractionResult.PASS;
        }
        Level level = player.level();
        Output output = new Output();
        String string = ItemDebug.getPlatform(level);
        output.both("[%s] entity: %s", output, entity);
        if (entity instanceof ItemEntity) {
            ItemStack itemStack2 = ((ItemEntity)entity).getItem();
            String string2 = Util.getName(itemStack2.getItem()).toString();
            output.both("[%s] item id: %s size: %s name: %s", string, string2, StackUtil.getSize(itemStack2), itemStack2.getDescriptionId());
            output.console("NBT: %s", StackUtil.getTag(itemStack2));
        }
        output.flush(player);
        return level.isClientSide ? InteractionResult.PASS : InteractionResult.SUCCESS;
    }

    private static Mode getMode(ItemStack itemStack) {
        int n;
        CompoundTag compoundTag = StackUtil.getTag(itemStack);
        int n2 = n = compoundTag != null ? compoundTag.getInt("mode") : 0;
        if (n < 0 || n >= Mode.modes.length) {
            n = 0;
        }
        return Mode.modes[n];
    }

    private static void setMode(ItemStack itemStack, Mode mode) {
        StackUtil.getOrCreateNbtData(itemStack).putInt("mode", mode.ordinal());
    }

    private static String getPlatform(Level level) {
        if (IC2.envProxy.isClientEnv()) {
            if (!level.isClientSide) {
                return "sp server";
            }
            if (level.getServer() == null) {
                return "mp client";
            }
            return "sp client";
        }
        return "mp server";
    }

    /*
     * Could not resolve type clashes
     */
    private static void dumpObjectFields(Object object, Output output) {
        ArrayList arrayList = new ArrayList();
        Class<?> clazz = object.getClass();
        do {
            arrayList.add(clazz);
        } while ((clazz = clazz.getSuperclass()) != null);
        for (int i = arrayList.size() - 1; i >= 0; --i) {
            Class clazz2 = (Class)arrayList.get(i);
            Field[] fieldArray = clazz2.getDeclaredFields();
            boolean bl = false;
            for (Field field : fieldArray) {
                Object arrayList2;
                int n;
                Class<?> clazz3 = field.getType();
                int n2 = field.getModifiers();
                if (Modifier.isStatic(n2) && (clazz2 == Block.class || clazz2 == BlockEntity.class || clazz2 == Ic2TileEntity.class || Modifier.isFinal(n2) && (clazz3.isPrimitive() || clazz3 == String.class || Property.class.isAssignableFrom(clazz3)))) continue;
                if (!bl) {
                    output.console(clazz2.getName());
                    bl = true;
                }
                try {
                    boolean wasAccessible = field.isAccessible();
                    field.setAccessible(true);
                    arrayList2 = field.get(object);
                    field.setAccessible(wasAccessible);
                }
                catch (ReflectiveOperationException reflectiveOperationException) {
                    arrayList2 = "<can't access>";
                }
                output.console("  %s type: %s", field.getName(), clazz3.getName());
                if (!ItemDebug.isSelfDescribingClass(clazz3)) {
                    output.part("    identity hash: %x hash: %x modifiers: %x", System.identityHashCode(arrayList2), arrayList2 == null ? 0 : arrayList2.hashCode(), n2);
                    if (arrayList2 != null && arrayList2.getClass() != clazz3) {
                        output.part(" class: %s", arrayList2.getClass().getName());
                    }
                    output.partToConsole();
                }
                if (arrayList2 != null && field.getType().isArray()) {
                    ArrayList<Object> arrayList3 = new ArrayList<Object>();
                    for (int j = 0; j < Array.getLength(arrayList2); ++j) {
                        arrayList3.add(Array.get(arrayList2, j));
                    }
                    arrayList2 = arrayList3;
                }
                if (arrayList2 instanceof Iterable) {
                    output.part("    values (%s):", arrayList2 instanceof Collection ? Integer.valueOf(((Collection)arrayList2).size()) : "?");
                    n = 0;
                    for (Object t : (Iterable)arrayList2) {
                        output.part("      [%d] ", n++);
                        ItemDebug.dumpValueString(t, field, "        ", output);
                    }
                    continue;
                }
                if (arrayList2 instanceof Map) {
                    output.console("    values (%s):", ((Map)arrayList2).size());
                    for (Map.Entry<?, ?> entry : ((Map<?, ?>)arrayList2).entrySet()) {
                        output.part("      %s: ", entry.getKey());
                        ItemDebug.dumpValueString(entry.getValue(), field, "        ", output);
                    }
                    continue;
                }
                output.part("    value: ");
                ItemDebug.dumpValueString(arrayList2, field, "      ", output);
            }
        }
    }

    private static void dumpValueString(Object object, Field field, String string, Output output) {
        if (object == null) {
            output.part("<null>");
            output.partToConsole();
            return;
        }
        String valueStr;
        if (object.getClass().isArray()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Array.getLength(object); ++i) {
                Object element = Array.get(object, i);
                String elementStr;
                if (element == null) {
                    elementStr = "<null>";
                } else {
                    elementStr = element.toString();
                    if (elementStr.length() > 32) {
                        elementStr = elementStr.substring(0, 20) + "... (" + (elementStr.length() - 20) + " more)";
                    }
                }
                sb.append(" [").append(i).append("] ").append(elementStr);
            }
            valueStr = sb.toString();
        } else {
            valueStr = object.toString();
        }
        if (valueStr.length() > 100) {
            valueStr = valueStr.substring(0, 90) + "... (" + (valueStr.length() - 90) + " more)";
        }
        output.part(valueStr);
        output.partToConsole();
        if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic() || object.getClass().isArray() || object instanceof Iterable || ItemDebug.isSelfDescribingClass(object.getClass())) {
            return;
        }
        if (object instanceof Level) {
            output.console("%s dim: %s", string, Util.getDimId((Level)object));
        } else if (!(object instanceof StateDefinition || object instanceof BlockEntity || object instanceof ItemStack || object.getClass().getName().startsWith("java."))) {
            for (Class<?> clazz = object.getClass(); clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
                for (Field field2 : clazz.getDeclaredFields()) {
                    Object object4;
                    if (field2.isSynthetic() || Modifier.isStatic(field2.getModifiers())) continue;
                    try {
                        field2.setAccessible(true);
                        object4 = field2.get(object);
                    }
                    catch (Exception exception) {
                        object4 = "<can't access>";
                    }
                    String string2 = object4 == object ? "<parent>" : ItemDebug.toStringLimited(object4, 100);
                    output.console("%s%s: %s", string, field2.getName(), string2);
                }
            }
        }
    }

    private static boolean isSelfDescribingClass(Class<?> clazz) {
        return clazz.isPrimitive() || clazz.isEnum() || clazz == Class.class || clazz == String.class || clazz == BlockState.class || clazz == ResourceLocation.class || Tag.class.isAssignableFrom(clazz) || Vec3i.class.isAssignableFrom(clazz) || Vec3.class.isAssignableFrom(clazz) || Block.class.isAssignableFrom(clazz) || Item.class.isAssignableFrom(clazz) || Fluid.class.isAssignableFrom(clazz);
    }

    private static String toStringLimited(Object object, int n) {
        if (object == null) {
            return "<null>";
        }
        n = Math.max(n, 12);
        String string = object.toString();
        if (string.length() > n) {
            int n2 = n - 12;
            return string.substring(0, n2) + "... (" + (string.length() - n2) + " more)";
        }
        return string;
    }

    @Override
    public IElectricItemManager getManager(ItemStack itemStack) {
        if (manager == null) {
            manager = new InfiniteElectricItemManager();
        }
        return manager;
    }

    @Override
    public boolean canBeStoredInToolbox(ItemStack itemStack) {
        return true;
    }

    private static enum Mode {
        InterfacesFields("Interfaces and Fields"),
        InterfacesFieldsRetrace("Interfaces and Fields (liquid/entity)"),
        TileData("Tile Data"),
        EnergyNet("Energy Net"),
        Accelerate("Accelerate"),
        AccelerateX100("Accelerate x100");

        static final Mode[] modes;
        private final String name;

        private Mode(String string2) {
            this.name = string2;
        }

        String getName() {
            return this.name;
        }

        static {
            modes = Mode.values();
        }
    }

    private static class Output {
        private final StringBuilder chatSb = new StringBuilder();
        private final StringBuilder consoleSb = new StringBuilder();
        private final StringBuilder partSb = new StringBuilder();

        private Output() {
        }

        public void chat(CharSequence charSequence) {
            if (!this.chatSb.isEmpty()) {
                this.chatSb.append('\n');
            }
            this.chatSb.append(charSequence);
        }

        public void chat(String string, Object ... objectArray) {
            this.chat(String.format(string, objectArray));
        }

        public void console(CharSequence charSequence) {
            if (!this.consoleSb.isEmpty()) {
                this.consoleSb.append('\n');
            }
            this.consoleSb.append(charSequence);
        }

        public void console(String string, Object ... objectArray) {
            this.console(String.format(string, objectArray));
        }

        public void both(CharSequence charSequence) {
            this.chat(charSequence);
            this.console(charSequence);
        }

        public void both(String string, Object ... objectArray) {
            this.both(String.format(string, objectArray));
        }

        public Output part(CharSequence charSequence) {
            this.partSb.append(charSequence);
            return this;
        }

        public Output part(char c) {
            this.partSb.append(c);
            return this;
        }

        public Output part(String string, Object ... objectArray) {
            return this.part(String.format(string, objectArray));
        }

        public void partToChat() {
            this.chat(this.partSb);
            this.partSb.setLength(0);
        }

        public void partToConsole() {
            this.console(this.partSb);
            this.partSb.setLength(0);
        }

        public void partToBoth() {
            this.both(this.partSb);
            this.partSb.setLength(0);
        }

        void flush(Player player) {
            if (player.level().isClientSide) {
                System.out.println(this.consoleSb);
                for (String string : this.chatSb.toString().split("[\\r\\n]+")) {
                    IC2.sideProxy.messagePlayer(player, string, new Object[0]);
                }
            } else if (player instanceof ServerPlayer) {
                IC2.network.get(true).sendConsole((ServerPlayer)player, this.consoleSb.toString());
                IC2.network.get(true).sendChat((ServerPlayer)player, this.chatSb.toString());
            }
            this.chatSb.setLength(0);
            this.consoleSb.setLength(0);
        }
    }
}


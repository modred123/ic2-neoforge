/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Registry
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.tags.BlockTags
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.material.Fluid
 */
package ic2.core.util;

import ic2.core.IC2;
import ic2.core.Ic2Player;
import ic2.core.util.LogCategory;
import ic2.core.util.Vector3;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;

public final class Util {
    public static final Direction[] ALL_DIRS = Direction.values();
    public static final Direction[] HORIZONTAL_DIRS = Arrays.copyOfRange(ALL_DIRS, 2, 6);
    public static final Set<Direction> noFacings = Collections.emptySet();
    public static final Set<Direction> onlyNorth = Collections.unmodifiableSet(EnumSet.of(Direction.NORTH));
    public static final Set<Direction> horizontalFacings = Collections.unmodifiableSet(EnumSet.copyOf(Arrays.asList(HORIZONTAL_DIRS)));
    public static final Set<Direction> verticalFacings = Collections.unmodifiableSet(EnumSet.of(Direction.DOWN, Direction.UP));
    public static final Set<Direction> downSideFacings = Collections.unmodifiableSet(EnumSet.complementOf(EnumSet.of(Direction.UP)));
    public static final Set<Direction> allFacings = Collections.unmodifiableSet(EnumSet.allOf(Direction.class));
    public static final InteractionHand[] HANDS = InteractionHand.values();
    private static final boolean inDev = System.getProperty("INDEV") != null;
    private static final boolean includeWorldHash = System.getProperty("ic2.debug.includeworldhash") != null;
    private static final Map<Class<?>, Boolean> checkedClasses = new IdentityHashMap();

    public static int roundToNegInf(float f) {
        int n = (int)f;
        if ((float)n > f) {
            --n;
        }
        return n;
    }

    public static int roundToNegInf(double d) {
        int n = (int)d;
        if ((double)n > d) {
            --n;
        }
        return n;
    }

    public static int saturatedCast(double d) {
        if (d > 2.147483647E9) {
            return Integer.MAX_VALUE;
        }
        if (d < -2.147483648E9) {
            return Integer.MIN_VALUE;
        }
        return (int)d;
    }

    public static int limit(int n, int n2, int n3) {
        if (n <= n2) {
            return n2;
        }
        if (n >= n3) {
            return n3;
        }
        return n;
    }

    public static float limit(float f, float f2, float f3) {
        if (Float.isNaN(f) || f <= f2) {
            return f2;
        }
        if (f >= f3) {
            return f3;
        }
        return f;
    }

    public static double limit(double d, double d2, double d3) {
        if (Double.isNaN(d) || d <= d2) {
            return d2;
        }
        if (d >= d3) {
            return d3;
        }
        return d;
    }

    public static double map(double d, double d2, double d3) {
        if (d < 0.0 || Double.isNaN(d)) {
            d = 0.0;
        }
        if (d > d2) {
            d = d2;
        }
        return d / d2 * d3;
    }

    public static double lerp(double d, double d2, double d3) {
        assert (d3 >= 0.0 && d3 <= 1.0);
        return d + (d2 - d) * d3;
    }

    public static float lerp(float f, float f2, float f3) {
        assert (f3 >= 0.0f && f3 <= 1.0f);
        return f + (f2 - f) * f3;
    }

    public static int square(int n) {
        return n * n;
    }

    public static float square(float f) {
        return f * f;
    }

    public static double square(double d) {
        return d * d;
    }

    public static boolean isSimilar(float f, float f2) {
        return Math.abs(f - f2) < 1.0E-5f;
    }

    public static boolean isSimilar(double d, double d2) {
        return Math.abs(d - d2) < 1.0E-5;
    }

    public static int countInArray(Object[] objectArray, Class<?> ... classArray) {
        int n = 0;
        for (Object object : objectArray) {
            for (Class<?> clazz : classArray) {
                if (!clazz.isAssignableFrom(object.getClass())) continue;
                ++n;
            }
        }
        return n;
    }

    public static int countInArray(Object[] objectArray, Class<?> clazz) {
        int n = 0;
        for (Object object : objectArray) {
            if (!clazz.isAssignableFrom(object.getClass())) continue;
            ++n;
        }
        return n;
    }

    public static boolean checkInterfaces(Class<?> clazz) {
        Boolean bl = checkedClasses.get(clazz);
        if (bl != null) {
            return bl;
        }
        Set<Class> set = Collections.newSetFromMap(new IdentityHashMap());
        Class<?> clazz2 = clazz;
        do {
            for (Class<?> methodArray : clazz2.getInterfaces()) {
                set.add(methodArray);
            }
        } while ((clazz2 = clazz2.getSuperclass()) != null);
        boolean bl2 = true;
        for (Class clazz3 : set) {
            for (Method method : clazz3.getMethods()) {
                boolean bl3 = false;
                clazz2 = clazz;
                do {
                    try {
                        Method method2 = clazz2.getDeclaredMethod(method.getName(), method.getParameterTypes());
                        if (!method.getReturnType().isAssignableFrom(method2.getReturnType())) continue;
                        bl3 = true;
                        break;
                    }
                    catch (NoSuchMethodException noSuchMethodException) {
                        // empty catch block
                    }
                } while ((clazz2 = clazz2.getSuperclass()) != null);
                if (bl3) continue;
                IC2.log.info(LogCategory.General, "Can't find method %s.%s in %s.", method.getDeclaringClass().getName(), method.getName(), clazz.getName());
                bl2 = false;
            }
        }
        checkedClasses.put(clazz, bl2);
        return bl2;
    }

    public static boolean inDev() {
        return inDev;
    }

    public static boolean hasAssertions() {
        boolean bl = false;
        if (Util.class.desiredAssertionStatus()) {
            bl = true;
            if (!true) {
                throw new AssertionError();
            }
        }
        return bl;
    }

    public static boolean isCallingFromIc2() {
        return Util.isCallingFromIc2(1);
    }

    public static boolean isCallingFromIc2(int n) {
        int n2;
        StackTraceElement[] stackTraceElementArray = Thread.currentThread().getStackTrace();
        return stackTraceElementArray.length <= (n2 = 2 + n) || stackTraceElementArray[n2].getClassName().startsWith("ic2.");
    }

    public static Block getBlock(String string) {
        if (string == null) {
            throw new NullPointerException("null name");
        }
        return Util.getBlock(ResourceLocation.parse(string));
    }

    public static Block getBlock(ResourceLocation resourceLocation) {
        Block block = (Block)BuiltInRegistries.BLOCK.get(resourceLocation);
        if (block != Blocks.AIR) {
            return block;
        }
        if (resourceLocation.getNamespace().equals("minecraft") && resourceLocation.getPath().equals("air")) {
            return block;
        }
        return null;
    }

    public static boolean canShear(BlockState blockState) {
        return blockState.is(BlockTags.LEAVES) || blockState.is(Blocks.COBWEB) || blockState.is(Blocks.GRASS_BLOCK) || blockState.is(Blocks.FERN) || blockState.is(Blocks.DEAD_BUSH) || blockState.is(Blocks.HANGING_ROOTS) || blockState.is(Blocks.VINE) || blockState.is(Blocks.TRIPWIRE) || blockState.is(BlockTags.WOOL);
    }

    public static Vector3 getEyePosition(Entity entity) {
        return new Vector3(entity.getX(), entity.getEyeY(), entity.getZ());
    }

    public static ResourceLocation getName(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block);
    }

    public static Item getItem(String string) {
        if (string == null) {
            throw new NullPointerException("null name");
        }
        return Util.getItem(ResourceLocation.parse(string));
    }

    public static Item getItem(ResourceLocation resourceLocation) {
        return (Item)BuiltInRegistries.ITEM.get(resourceLocation);
    }

    public static Vector3 getLook(Entity entity) {
        return new Vector3(entity.getLookAngle());
    }

    public static ResourceLocation getName(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }

    public static Fluid getFluid(ResourceLocation resourceLocation) {
        return (Fluid)BuiltInRegistries.FLUID.get(resourceLocation);
    }

    public static ResourceLocation getName(Fluid fluid) {
        return BuiltInRegistries.FLUID.getKey(fluid);
    }

    public static ResourceLocation getDimId(Level level) {
        return level.dimension().location();
    }

    public static String toString(BlockEntity blockEntity) {
        if (blockEntity == null) {
            return "null";
        }
        return Util.toString(blockEntity, (BlockGetter)blockEntity.getLevel(), blockEntity.getBlockPos());
    }

    public static String toString(Object object, BlockGetter blockGetter, BlockPos blockPos) {
        return Util.toString(object, blockGetter, blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public static String toString(Object object, BlockGetter blockGetter, int n, int n2, int n3) {
        StringBuilder stringBuilder = new StringBuilder(64);
        if (object == null) {
            stringBuilder.append("null");
        } else {
            stringBuilder.append(object.getClass().getName());
            stringBuilder.append('@');
            stringBuilder.append(Integer.toHexString(System.identityHashCode(object)));
        }
        stringBuilder.append(" (");
        stringBuilder.append(Util.formatPosition(blockGetter, n, n2, n3));
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    public static String toCamel(String string) {
        String[] stringArray = string.split("_");
        StringBuilder stringBuilder = new StringBuilder();
        for (String string2 : stringArray) {
            if (stringBuilder.isEmpty()) {
                stringBuilder.append(string2);
                continue;
            }
            stringBuilder.append(string2.substring(0, 1).toUpperCase()).append(string2.substring(1));
        }
        return stringBuilder.toString();
    }

    public static String formatPosition(BlockEntity blockEntity) {
        return blockEntity != null ? Util.formatPosition((BlockGetter)blockEntity.getLevel(), blockEntity.getBlockPos()) : "(null)";
    }

    public static String formatPosition(BlockGetter blockGetter, BlockPos blockPos) {
        return Util.formatPosition(blockGetter, blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public static String formatPosition(BlockGetter blockGetter, int n, int n2, int n3) {
        ResourceLocation resourceLocation = blockGetter instanceof Level ? Util.getDimId((Level)blockGetter) : null;
        if (!includeWorldHash) {
            return Util.formatPosition(resourceLocation, n, n2, n3);
        }
        return String.format("dim %s (@%x): %d/%d/%d", resourceLocation, System.identityHashCode(blockGetter), n, n2, n3);
    }

    public static String formatPosition(ResourceLocation resourceLocation, int n, int n2, int n3) {
        return "dim " + resourceLocation + ": " + n + "/" + n2 + "/" + n3;
    }

    public static String formatPosition(BlockPos blockPos) {
        return Util.formatPosition(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public static String formatPosition(int n, int n2, int n3) {
        return n + "/" + n2 + "/" + n3;
    }

    public static String toSiString(double d, int n) {
        int n2;
        Object object;
        double d2;
        int n3;
        if (d == 0.0) {
            return "0 ";
        }
        if (Double.isNaN(d)) {
            return "NaN ";
        }
        Object object2 = "";
        if (d < 0.0) {
            object2 = "-";
            d = -d;
        }
        if (Double.isInfinite(d)) {
            return (String)object2 + "\u221e ";
        }
        double d3 = Math.log10(d);
        if (d3 >= 0.0) {
            n3 = (int)Math.floor(d3 / 3.0);
            d2 = 1.0 / Math.pow(10.0, n3 * 3);
            switch (n3) {
                case 0: {
                    object = "";
                    break;
                }
                case 1: {
                    object = "k";
                    break;
                }
                case 2: {
                    object = "M";
                    break;
                }
                case 3: {
                    object = "G";
                    break;
                }
                case 4: {
                    object = "T";
                    break;
                }
                case 5: {
                    object = "P";
                    break;
                }
                case 6: {
                    object = "E";
                    break;
                }
                case 7: {
                    object = "Z";
                    break;
                }
                case 8: {
                    object = "Y";
                    break;
                }
                default: {
                    object = "E" + n3 * 3;
                    break;
                }
            }
        } else {
            n3 = (int)Math.ceil(-d3 / 3.0);
            d2 = Math.pow(10.0, n3 * 3);
            switch (n3) {
                case 0: {
                    object = "";
                    break;
                }
                case 1: {
                    object = "m";
                    break;
                }
                case 2: {
                    object = "\u00b5";
                    break;
                }
                case 3: {
                    object = "n";
                    break;
                }
                case 4: {
                    object = "p";
                    break;
                }
                case 5: {
                    object = "f";
                    break;
                }
                case 6: {
                    object = "a";
                    break;
                }
                case 7: {
                    object = "z";
                    break;
                }
                case 8: {
                    object = "y";
                    break;
                }
                default: {
                    object = "E-" + n3 * 3;
                }
            }
        }
        n3 = (int)Math.floor(d *= d2);
        d -= (double)n3;
        int n4 = 1;
        if (n3 > 0) {
            n4 = (int)((double)n4 + Math.floor(Math.log10(n3)));
        }
        if ((double)(n2 = (int)Math.round(d * (d2 = Math.pow(10.0, n - n4)))) >= d2) {
            n2 = (int)((double)n2 - d2);
            n4 = 1;
            if (++n3 > 0) {
                n4 = (int)((double)n4 + Math.floor(Math.log10(n3)));
            }
        }
        object2 = (String)object2 + Integer.toString(n3);
        if (n > n4 && n2 != 0) {
            object2 = (String)object2 + String.format(".%0" + (n - n4) + "d", n2);
        }
        object2 = ((String)object2).replaceFirst("(\\.\\d*?)0+$", "$1");
        return (String)object2 + " " + (String)object;
    }

    public static void exit(int n) {
        Method method = null;
        try {
            method = Class.forName("java.lang.Shutdown").getDeclaredMethod("exit", Integer.TYPE);
            method.setAccessible(true);
        }
        catch (Exception exception) {
            IC2.log.warn(LogCategory.General, exception, "Method lookup failed.");
            try {
                Field field = System.class.getDeclaredField("security");
                field.setAccessible(true);
                field.set(null, null);
                method = System.class.getMethod("exit", Integer.TYPE);
            }
            catch (Exception exception2) {
                throw new Error(exception2);
            }
        }
        try {
            method.invoke(null, n);
        }
        catch (Exception exception) {
            throw new Error(exception);
        }
    }

    public static boolean isFakePlayer(Player player, boolean bl) {
        if (player == null) {
            return false;
        }
        if (!(player instanceof ServerPlayer)) {
            return true;
        }
        if (bl) {
            return IC2.envProxy.isFakePlayer(player);
        }
        return player.getClass() != ServerPlayer.class;
    }

    public static boolean isAreaLoaded(LevelReader levelReader, BlockPos blockPos, int n) {
        return levelReader.hasChunksAt(blockPos.getX() - n, blockPos.getZ() - n, blockPos.getX() + n, blockPos.getZ() + n);
    }

    public static boolean harvestBlock(Level level, BlockPos blockPos) {
        if (level.isClientSide) {
            return false;
        }
        BlockState blockState = level.getBlockState(blockPos);
        Block block = blockState.getBlock();
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        Player player = Ic2Player.get(level);
        block.playerWillDestroy(level, blockPos, blockState, player);
        if (!level.removeBlock(blockPos, false)) {
            return false;
        }
        block.destroy((LevelAccessor)level, blockPos, blockState);
        block.playerDestroy(level, player, blockPos, blockState, blockEntity, new ItemStack((ItemLike)Items.DIAMOND_PICKAXE));
        return true;
    }

    /** ex112 兼容：玩家视线射线检测（1.21.1 ClipContext） */
    public static net.minecraft.world.phys.HitResult traceBlocks(Player player, boolean includeFluids) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(4.5));
        net.minecraft.world.level.ClipContext.Fluid fluidMode = includeFluids
            ? net.minecraft.world.level.ClipContext.Fluid.ANY : net.minecraft.world.level.ClipContext.Fluid.NONE;
        return player.level().clip(new net.minecraft.world.level.ClipContext(eye, end,
            net.minecraft.world.level.ClipContext.Block.OUTLINE, fluidMode, player));
    }

    /** ex112 兼容：OreDictionary 匹配（1.21.1 用 item tag 模拟，如 "ingotBronze" -> forge:ingots/bronze） */
    public static boolean matchesOD(ItemStack stack, String odName) {
        if (stack == null || stack.isEmpty() || odName == null || odName.isEmpty()) {
            return false;
        }
        int i = 1;
        while (i < odName.length() && Character.isLowerCase(odName.charAt(i))) {
            i++;
        }
        String type = odName.substring(0, i).toLowerCase(Locale.ROOT);
        String material = odName.substring(i).toLowerCase(Locale.ROOT);
        if (material.isEmpty()) {
            return false;
        }
        String plural = type;
        if (!plural.endsWith("s")) {
            plural = plural + "s";
        }
        switch (type) {
            case "ingot": plural = "ingots"; break;
            case "ore": plural = "ores"; break;
            case "dust": plural = "dusts"; break;
            case "gem": plural = "gems"; break;
            case "block": plural = "blocks"; break;
            case "nugget": plural = "nuggets"; break;
            case "stick": plural = "sticks"; break;
            case "plate": plural = "plates"; break;
            case "gear": plural = "gears"; break;
            case "rod": plural = "rods"; break;
            default: break;
        }
        return stack.is(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("forge", plural + "/" + material)));
    }
}

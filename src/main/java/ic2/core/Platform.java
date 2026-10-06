/*
 * ex112 Platform 兼容层 1.21.1 适配版
 * 保留 IC2.platform 的全部公开方法签名（53 个文件依赖），内部改用 1.21.1 API。
 */
package ic2.core;

import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.network.GrowingBuffer;
import ic2.core.ref.Ic2ScreenHandlers;
import ic2.core.util.Util;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class Platform {
    public boolean isSimulating() {
        return !this.isRendering();
    }

    public boolean isRendering() {
        return false;
    }

    public void displayError(String error, Object ... args) {
        if (args.length > 0) {
            error = String.format(error, args);
        }
        error = "IndustrialCraft 2 Error\n\n == = IndustrialCraft 2 Error == = \n\n" + error + "\n\n ==  ==  ==  ==  ==  ==  ==  ==  ==  ==  == \n";
        error = error.replace("\n", System.getProperty("line.separator"));
        throw new RuntimeException(error);
    }

    public void displayError(Exception e, String error, Object ... args) {
        if (args.length > 0) {
            error = String.format(error, args);
        }
        this.displayError("An unexpected Exception occured.\n\n" + this.getStackTrace(e) + "\n" + error, new Object[0]);
    }

    public String getStackTrace(Exception e) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        return writer.toString();
    }

    public Player getPlayerInstance() {
        return null;
    }

    public Level getWorld(int dimId) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return null;
        }
        for (Level level : server.getAllLevels()) {
            if (level.dimension().location().hashCode() == dimId || level.dimension().location().toString().hashCode() == dimId) {
                return level;
            }
        }
        return server.overworld();
    }

    public Level getPlayerWorld() {
        return null;
    }

    public void preInit() {
    }

    public void messagePlayer(Player player, String message, Object ... args) {
        if (player instanceof ServerPlayer) {
            Component msg = args.length > 0
                ? Component.translatable(message, (Object[])this.getMessageComponents(args))
                : Component.translatable(message);
            player.sendSystemMessage(msg);
        }
    }

    private GrowingBuffer makeBuffer(Player player, IHasGui inventory, InteractionHand hand, int id) {
        GrowingBuffer growingBuffer = new GrowingBuffer(50);
        try {
            if (inventory instanceof BlockEntity be) {
                Ic2ScreenHandlers.writeManagedBeData(be, growingBuffer);
            } else {
                Ic2ScreenHandlers.writeManagedItemData(player, hand, id, growingBuffer);
            }
            inventory.writeScreenOpenData(player, hand, growingBuffer);
        }
        catch (IOException iOException) {
            throw new RuntimeException(iOException);
        }
        growingBuffer.flip();
        return growingBuffer;
    }

    public boolean launchGui(Player player, IHasGui inventory) {
        if (!Util.isFakePlayer(player, true)) {
            ServerPlayer playerMp = (ServerPlayer)player;
            GrowingBuffer growingBuffer = this.makeBuffer(player, inventory, InteractionHand.MAIN_HAND, 0);
            IC2.envProxy.openHandledScreen(playerMp, new MenuProvider(){
                public AbstractContainerMenu createMenu(int n, Inventory inv, Player p) {
                    return inventory.createServerScreenHandler(n, p);
                }
                public Component getDisplayName() {
                    // 第三十二轮：手持类 GUI（EU电表/作物分析仪……）此前一律落到 container.ic2.gui，
                    // 标题栏显示的是通用文本。改为优先用"手上那件物品的名字"。
                    if (inventory instanceof ic2.core.item.tool.HandHeldInventory handHeld) {
                        return handHeld.getContainerStack().getHoverName();
                    }
                    return inventory instanceof BlockEntity be ? IHasGui.getBeName(be) : Component.translatable("container.ic2.gui");
                }
            }, growingBuffer);
            return true;
        }
        return false;
    }

    public boolean launchSubGui(Player player, IHasGui inventory, int ID) {
        return this.launchGui(player, inventory);
    }

    public boolean launchGuiClient(Player player, IHasGui inventory, boolean isAdmin) {
        return false;
    }

    public void profilerStartSection(String section) {
    }

    public void profilerEndSection() {
    }

    public void profilerEndStartSection(String section) {
    }

    public File getMinecraftDir() {
        return new File(".");
    }

    public void playSoundSp(String sound, float f, float g) {
    }

    public void resetPlayerInAirTime(Player player) {
    }

    public int getBlockTexture(Block block, Level world, int x, int y, int z, int side) {
        return 0;
    }

    public void removePotion(LivingEntity entity, MobEffect potion) {
        entity.removeEffect(net.minecraft.core.Holder.direct(potion));
    }

    public void onPostInit() {
    }

    protected Component[] getMessageComponents(Object ... args) {
        Component[] encodedArgs = new Component[args.length];
        for (int i = 0; i < args.length; ++i) {
            encodedArgs[i] = args[i] instanceof String && ((String)args[i]).startsWith("ic2.")
                ? Component.translatable((String)args[i])
                : Component.literal(args[i].toString());
        }
        return encodedArgs;
    }

    public void requestTick(boolean simulating, Runnable runnable) {
        if (!simulating) {
            throw new IllegalStateException();
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            server.execute(runnable);
        }
    }

    public int getColorMultiplier(BlockState state, Level world, BlockPos pos, int tint) {
        throw new UnsupportedOperationException("client only");
    }
}

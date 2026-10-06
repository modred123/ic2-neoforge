package ic2.core.block.state;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockStateUtil {
    public static BlockState getState(Block block, String variant) {
        return block == null ? null : block.defaultBlockState();
    }
}

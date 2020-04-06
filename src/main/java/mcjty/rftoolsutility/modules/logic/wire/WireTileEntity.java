package mcjty.rftoolsutility.modules.logic.wire;


import mcjty.lib.tileentity.LogicTileEntity;
;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class WireTileEntity extends LogicTileEntity {

    private int loopDetector = 0;

    @Override
    public int getRedstoneOutput(BlockState state, IBlockAccess world, BlockPos pos, Direction side) {
        if (side == getFacing(state).getInputSide()) {
            return powerLevel;
        } else {
            return 0;
        }
    }

    @Override
    public void checkRedstone(World world, BlockPos pos) {
        super.checkRedstone(world, pos);
        if (loopDetector <= 0) {
            loopDetector++;
            BlockState state = world.getBlockState(pos);
            BlockPos offsetPos = pos.offset(getFacing(state).getInputSide().getOpposite());
            if (world.isBlockLoaded(offsetPos)) {
                world.neighborChanged(offsetPos, state.getBlock(), pos);
            }
            loopDetector--;
        }
    }
}

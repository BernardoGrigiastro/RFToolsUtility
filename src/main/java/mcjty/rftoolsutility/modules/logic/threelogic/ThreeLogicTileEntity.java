package mcjty.rftoolsutility.modules.logic.threelogic;

import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.tileentity.LogicTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.LogicFacing;
;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class ThreeLogicTileEntity extends LogicTileEntity {

    public static final String CMD_SETSTATE = "logic.setState";
    public static final Key<Integer> PARAM_INDEX = new Key<>("index", Type.INTEGER);
    public static final Key<Integer> PARAM_STATE = new Key<>("state", Type.INTEGER);

    private int[] logicTable = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };    // 0 == off, 1 == on, -1 == keep

    public ThreeLogicTileEntity() {
    }

//    @Override
//    public void update() {
//        if (!getWorld().isRemote) {
//            checkStateServer();
//        }
//    }
//
    public int getState(int index) {
        return logicTable[index];
    }

    public void checkRedstone() {
        int s = logicTable[powerLevel];
        if (s == -1) {
            return; // Nothing happens (keep mode)
        }
        setRedstoneState(s == 1 ? 15 : 0);
    }

    @Override
    public void readFromNBT(CompoundNBT tagCompound) {
        super.readFromNBT(tagCompound);
        powerOutput = tagCompound.getBoolean("rs") ? 15 : 0;
    }

    @Override
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        for (int i = 0 ; i < 8 ; i++) {
            logicTable[i] = tagCompound.getInteger("state" + i);
        }
    }

    @Override
    public CompoundNBT writeToNBT(CompoundNBT tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("rs", powerOutput > 0);
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        for (int i = 0 ; i < 8 ; i++) {
            tagCompound.setInteger("state" + i, logicTable[i]);
        }
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_SETSTATE.equals(command)) {
            logicTable[params.get(PARAM_INDEX)] = params.get(PARAM_STATE);
            markDirtyClient();
            checkRedstone(world, pos);
            return true;
        }
        return false;
    }

    private static Set<BlockPos> loopDetector = new HashSet<>();


    @Override
    public void checkRedstone(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (loopDetector.add(pos)) {
            try {
                LogicFacing facing = getFacing(state);
                Direction downSide = facing.getSide();
                Direction inputSide = facing.getInputSide();
                Direction leftSide = LogicSlabBlock.rotateLeft(downSide, inputSide);
                Direction rightSide = LogicSlabBlock.rotateRight(downSide, inputSide);

                int powered1 = getInputStrength(world, pos, leftSide) > 0 ? 1 : 0;
                int powered2 = getInputStrength(world, pos, inputSide) > 0 ? 2 : 0;
                int powered3 = getInputStrength(world, pos, rightSide) > 0 ? 4 : 0;
                setPowerInput(powered1 + powered2 + powered3);
                checkRedstone();
            } finally {
                loopDetector.remove(pos);
            }
        }
    }

}

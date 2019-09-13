package mcjty.rftoolsutility.apiimpl.teleportation;

import mcjty.lib.varia.GlobalCoordinate;
import mcjty.rftoolsbase.api.teleportation.ITeleportationManager;
import mcjty.rftoolsutility.modules.teleporter.TeleportConfiguration;
import mcjty.rftoolsutility.modules.teleporter.TeleporterSetup;
import mcjty.rftoolsutility.modules.teleporter.blocks.MatterReceiverTileEntity;
import mcjty.rftoolsutility.modules.teleporter.data.TeleportDestination;
import mcjty.rftoolsutility.modules.teleporter.data.TeleportDestinations;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TeleportationManager implements ITeleportationManager {

    @Override
    public String getReceiverName(World world, BlockPos pos) {
        if (world.getBlockState(pos).getBlock() == TeleporterSetup.MATTER_RECEIVER) {
            MatterReceiverTileEntity te = (MatterReceiverTileEntity) world.getTileEntity(pos);
            return te.getName();
        } else {
            return null;
        }
    }

    @Override
    public boolean createReceiver(World world, BlockPos pos, String name, int power) {
        world.setBlockState(pos, TeleporterSetup.MATTER_RECEIVER.getDefaultState(), 2);
        MatterReceiverTileEntity te = (MatterReceiverTileEntity) world.getTileEntity(pos);
        if (power == -1) {
            te.consumeEnergy(TeleportConfiguration.RECEIVER_MAXENERGY.get());
        } else {
            te.consumeEnergy(Math.min(power, TeleportConfiguration.RECEIVER_MAXENERGY.get()));
        }
        te.setName(name);
        te.markDirty();
        registerReceiver(world, pos, name);
        return true;
    }

    private void registerReceiver(World world, BlockPos pos, String name) {
        TeleportDestinations destinations = TeleportDestinations.get();
        GlobalCoordinate gc = new GlobalCoordinate(pos, world.getDimension().getType().getId());
        TeleportDestination destination = destinations.addDestination(gc);
        destination.setName(name);
        destinations.save();
    }

    @Override
    public void teleportPlayer(PlayerEntity player, int dimension, BlockPos location) {
        mcjty.lib.varia.TeleportationTools.teleportToDimension(player, dimension, location.getX(), location.getY(), location.getZ());
    }

    @Override
    public void removeReceiverDestinations(World world, int dim) {
        TeleportDestinations destinations = TeleportDestinations.get();
        destinations.removeDestinationsInDimension(dim);
        destinations.save();
    }
}

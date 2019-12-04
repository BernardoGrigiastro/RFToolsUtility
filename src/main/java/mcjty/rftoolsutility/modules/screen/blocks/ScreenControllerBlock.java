package mcjty.rftoolsutility.modules.screen.blocks;

import mcjty.lib.McJtyLib;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import mcjty.rftoolsutility.RFToolsUtility;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.List;

public class ScreenControllerBlock extends BaseBlock {

    public ScreenControllerBlock() {
        super(new BlockBuilder()
            .tileEntitySupplier(ScreenControllerTileEntity::new));
    }

    // @todo 1.14
//    @SideOnly(Side.CLIENT)
//    @Override
//    public BiFunction<ScreenControllerTileEntity, ScreenControllerContainer, GenericGuiContainer<? super ScreenControllerTileEntity>> getGuiFactory() {
//        return GuiScreenController::new;
//    }

    @Override
    public void addInformation(ItemStack itemStack, IBlockReader world, List<ITextComponent> list, ITooltipFlag flag) {
        super.addInformation(itemStack, world, list, flag);

        if (McJtyLib.proxy.isShiftKeyDown()) {
            list.add(new StringTextComponent(TextFormatting.WHITE + "Before screens can work they need to get power from"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "this controller. Even a screen that has only modules"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "that require no power will need to have a controller."));
            list.add(new StringTextComponent(TextFormatting.WHITE + "One controller can power many screens as long as they"));
            list.add(new StringTextComponent(TextFormatting.WHITE + "are in range."));
            list.add(new StringTextComponent(TextFormatting.YELLOW + "Infusing bonus: increased range for screens."));
        } else {
            list.add(new StringTextComponent(TextFormatting.WHITE + RFToolsUtility.SHIFT_MESSAGE));
        }
    }

    @Override
    public void onReplaced(BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockState newstate, boolean isMoving) {
        if (!world.isRemote) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity instanceof ScreenControllerTileEntity) {
                ((ScreenControllerTileEntity) tileEntity).detach();
            }
        }
        super.onReplaced(state, world, pos, newstate, isMoving);
    }
}

package mcjty.rftoolsutility.modules.screen.items;

import mcjty.lib.varia.Logging;
import mcjty.rftoolsbase.api.screens.IModuleGuiBuilder;
import mcjty.rftoolsbase.api.screens.IModuleProvider;
import mcjty.rftoolsutility.RFToolsUtility;
import mcjty.rftoolsutility.modules.screen.ScreenConfiguration;
import mcjty.rftoolsutility.modules.screen.modules.RedstoneScreenModule;
import mcjty.rftoolsutility.modules.screen.modulesclient.RedstoneClientScreenModule;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class RedstoneModuleItem extends Item implements IModuleProvider {

    public RedstoneModuleItem() {
        super(new Properties()
                .maxStackSize(1)
                .defaultMaxDamage(1)
                .group(RFToolsUtility.setup.getTab()));
        setRegistryName("redstone_module");
    }

//    @Override
//    @SideOnly(Side.CLIENT)
//    public void initModel() {
//        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(RFTools.MODID + ":" + getUnlocalizedName().substring(5), "inventory"));
//    }


//    @Override
//    public int getMaxItemUseDuration(ItemStack stack) {
//        return 1;
//    }

    @Override
    public Class<RedstoneScreenModule> getServerScreenModule() {
        return RedstoneScreenModule.class;
    }

    @Override
    public Class<RedstoneClientScreenModule> getClientScreenModule() {
        return RedstoneClientScreenModule.class;
    }

    @Override
    public String getModuleName() {
        return "Red";
    }

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        guiBuilder
                .label("Label:").text("text", "Label text").color("color", "Color for the label").nl()
                .label("Yes:").text("yestext", "Positive text").color("yescolor", "Color for the positive text").nl()
                .label("No:").text("notext", "Negative text").color("nocolor", "Color for the negative text").nl()
                .choices("align", "Label alignment", "Left", "Center", "Right").toggle("analog", "Analog mode", "Whether to show the exact level").nl()
                .label("Block:").block("monitor").nl();
    }

    @Override
    public void addInformation(ItemStack itemStack, @Nullable World worldIn, List<ITextComponent> list, ITooltipFlag flags) {
        super.addInformation(itemStack, worldIn, list, flags);
        list.add(new StringTextComponent(TextFormatting.GREEN + "Uses " + ScreenConfiguration.REDSTONE_RFPERTICK.get() + " RF/tick"));
        CompoundNBT tagCompound = itemStack.getTag();
        if (tagCompound != null) {
            list.add(new StringTextComponent(TextFormatting.YELLOW + "Label: " + tagCompound.getString("text")));
            int channel = tagCompound.getInt("channel");
            if (channel != -1) {
                list.add(new StringTextComponent(TextFormatting.YELLOW + "Channel: " + channel));
            } else if (tagCompound.contains("monitorx")) {
                int mx = tagCompound.getInt("monitorx");
                int my = tagCompound.getInt("monitory");
                int mz = tagCompound.getInt("monitorz");
                list.add(new StringTextComponent(TextFormatting.YELLOW + "Block at: " + mx + "," + my + "," + mz));
            }
        }
        list.add(new StringTextComponent(TextFormatting.WHITE + "Sneak right-click on a redstone transmitter or"));
        list.add(new StringTextComponent(TextFormatting.WHITE + "receiver to set the channel for this module."));
        list.add(new StringTextComponent(TextFormatting.WHITE + "Or else sneak right-click on the side of any"));
        list.add(new StringTextComponent(TextFormatting.WHITE + "block to monitor the redstone output on that side"));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        if (world.isRemote) {
            return ActionResultType.SUCCESS;
        }

        ItemStack stack = context.getItem();
        BlockPos pos = context.getPos();
        TileEntity te = world.getTileEntity(pos);
        PlayerEntity player = context.getPlayer();
        Direction facing = context.getFace();
        CompoundNBT tagCompound = stack.getOrCreateTag();
        int channel = -1;
        // @todo 1.14
//        if (te instanceof RedstoneChannelTileEntity) {
//            channel = ((RedstoneChannelTileEntity) te).getChannel(true);
//        } else {
//            // We selected a random block.
//            tagCompound.putInt("channel", -1);
//            tagCompound.putInt("monitordim", world.getDimension().getType().getId());
//            tagCompound.putInt("monitorx", pos.getX());
//            tagCompound.putInt("monitory", pos.getY());
//            tagCompound.putInt("monitorz", pos.getZ());
//            tagCompound.putInt("monitorside", facing.ordinal());
//            Logging.message(player, "Redstone module is set to " + pos);
//
//            return ActionResultType.SUCCESS;
//        }

        tagCompound.remove("monitordim");
        tagCompound.remove("monitorx");
        tagCompound.remove("monitory");
        tagCompound.remove("monitorz");
        tagCompound.remove("monitorside");

        if (channel != -1) {
            tagCompound.putInt("channel", channel);
            Logging.message(player, "Redstone module is set to channel '" + channel + "'");
        } else {
            tagCompound.remove("channel");
            Logging.message(player, "Redstone module is cleared");
        }
        return ActionResultType.SUCCESS;
    }
}
package mcjty.rftoolsutility.modules.screen.items;

import mcjty.lib.varia.BlockTools;
import mcjty.lib.varia.Logging;
import mcjty.rftoolsbase.api.screens.IModuleGuiBuilder;
import mcjty.rftoolsbase.api.screens.IModuleProvider;
import mcjty.rftoolsutility.RFToolsUtility;
import mcjty.rftoolsutility.modules.screen.ScreenConfiguration;
import mcjty.rftoolsutility.modules.screen.modules.CounterPlusScreenModule;
import mcjty.rftoolsutility.modules.screen.modulesclient.CounterPlusClientScreenModule;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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

import java.util.List;

public class CounterPlusModuleItem extends Item implements IModuleProvider {

    public CounterPlusModuleItem() {
        super(new Properties().maxStackSize(1).defaultMaxDamage(1).group(RFToolsUtility.setup.getTab()));
        setRegistryName("counterplus_module");
    }

//    @Override
//    public int getMaxItemUseDuration(ItemStack stack) {
//        return 1;
//    }

    @Override
    public Class<CounterPlusScreenModule> getServerScreenModule() {
        return CounterPlusScreenModule.class;
    }

    @Override
    public Class<CounterPlusClientScreenModule> getClientScreenModule() {
        return CounterPlusClientScreenModule.class;
    }

    @Override
    public String getModuleName() {
        return "Count";
    }

    @Override
    public void createGui(IModuleGuiBuilder guiBuilder) {
        guiBuilder
                .label("Label:").text("text", "Label text").nl()
                .label("L:").color("color", "Color for the label").label("C:").color("cntcolor", "Color for the counter").nl()
                .format("format")
                .choices("align", "Label alignment", "Left", "Center", "Right").nl()
                .label("Block:").block("monitor").nl();
    }

    @Override
    public void addInformation(ItemStack itemStack, World world, List<ITextComponent> list, ITooltipFlag flag) {
        super.addInformation(itemStack, world, list, flag);
        list.add(new StringTextComponent(TextFormatting.GREEN + "Uses " + ScreenConfiguration.COUNTERPLUS_RFPERTICK.get() + " RF/tick"));
        boolean hasTarget = false;
        CompoundNBT tagCompound = itemStack.getTag();
        if (tagCompound != null) {
            list.add(new StringTextComponent(TextFormatting.YELLOW + "Label: " + tagCompound.getString("text")));
            if (tagCompound.contains("monitorx")) {
                int monitorx = tagCompound.getInt("monitorx");
                int monitory = tagCompound.getInt("monitory");
                int monitorz = tagCompound.getInt("monitorz");
                String monitorname = tagCompound.getString("monitorname");
                list.add(new StringTextComponent(TextFormatting.YELLOW + "Monitoring: " + monitorname + " (at " + monitorx + "," + monitory + "," + monitorz + ")"));
                hasTarget = true;
            }
        }
        if (!hasTarget) {
            list.add(new StringTextComponent(TextFormatting.YELLOW + "Sneak right-click on a counter to set the"));
            list.add(new StringTextComponent(TextFormatting.YELLOW + "target for this counter module"));
        }
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        ItemStack stack = context.getItem();
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        Direction facing = context.getFace();
        PlayerEntity player = context.getPlayer();
        TileEntity te = world.getTileEntity(pos);
        CompoundNBT tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new CompoundNBT();
        }
        // @todo 1.14
//        if (te instanceof CounterTileEntity) {
//            tagCompound.putInt("monitordim", world.getDimension().getType().getId());
//            tagCompound.putInt("monitorx", pos.getX());
//            tagCompound.putInt("monitory", pos.getY());
//            tagCompound.putInt("monitorz", pos.getZ());
//            BlockState state = world.getBlockState(pos);
//            Block block = state.getBlock();
//            String name = "<invalid>";
//            if (block != null && !block.isAir(state, world, pos)) {
//                name = BlockTools.getReadableName(world, pos);
//            }
//            tagCompound.putString("monitorname", name);
//            if (world.isRemote) {
//                Logging.message(player, "Counter module is set to block '" + name + "'");
//            }
//        } else {
//            tagCompound.remove("monitordim");
//            tagCompound.remove("monitorx");
//            tagCompound.remove("monitory");
//            tagCompound.remove("monitorz");
//            tagCompound.remove("monitorname");
//            if (world.isRemote) {
//                Logging.message(player, "Counter module is cleared");
//            }
//        }
        stack.setTag(tagCompound);
        return ActionResultType.SUCCESS;
    }
}
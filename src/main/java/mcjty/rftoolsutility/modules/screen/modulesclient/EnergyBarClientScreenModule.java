package mcjty.rftoolsutility.modules.screen.modulesclient;

import com.mojang.blaze3d.platform.GlStateManager;
import mcjty.lib.varia.BlockPosTools;
import mcjty.rftoolsbase.api.screens.*;
import mcjty.rftoolsbase.api.screens.data.IModuleDataContents;
import mcjty.rftoolsutility.modules.screen.modulesclient.helper.ScreenLevelHelper;
import mcjty.rftoolsutility.modules.screen.modulesclient.helper.ScreenTextHelper;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EnergyBarClientScreenModule implements IClientScreenModule<IModuleDataContents> {

    private String line = "";
    private int color = 0xffffff;
    protected int dim = 0;
    protected BlockPos coordinate = BlockPosTools.INVALID;

    private ITextRenderHelper labelCache = new ScreenTextHelper();
    private ILevelRenderHelper rfRenderer = new ScreenLevelHelper().gradient(0xffff0000, 0xff333300);

    @Override
    public TransformMode getTransformMode() {
        return TransformMode.TEXT;
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(IModuleRenderHelper renderHelper, FontRenderer fontRenderer, int currenty, IModuleDataContents screenData, ModuleRenderInfo renderInfo) {
        GlStateManager.disableLighting();

        int xoffset;
        if (!line.isEmpty()) {
            labelCache.setup(line, 160, renderInfo);
            labelCache.renderText(0, currenty, color, renderInfo);
            xoffset = 7 + 40;
        } else {
            xoffset = 7;
        }

        if (!BlockPosTools.INVALID.equals(coordinate)) {
            rfRenderer.render(xoffset, currenty, screenData, renderInfo);
        } else {
            renderHelper.renderText(xoffset, currenty, 0xff0000, renderInfo, "<invalid>");
        }
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {

    }

    @Override
    public void setupFromNBT(CompoundNBT tagCompound, int dim, BlockPos pos) {
        if (tagCompound != null) {
            line = tagCompound.getString("text");
            if (tagCompound.contains("color")) {
                color = tagCompound.getInt("color");
            } else {
                color = 0xffffff;
            }
            int rfcolor;
            if (tagCompound.contains("rfcolor")) {
                rfcolor = tagCompound.getInt("rfcolor");
            } else {
                rfcolor = 0xffffff;
            }
            int rfcolorNeg;
            if (tagCompound.contains("rfcolor_neg")) {
                rfcolorNeg = tagCompound.getInt("rfcolor_neg");
            } else {
                rfcolorNeg = 0xffffff;
            }
            rfRenderer.color(rfcolor, rfcolorNeg);

            if (tagCompound.contains("align")) {
                String alignment = tagCompound.getString("align");
                labelCache.align(TextAlign.get(alignment));
            } else {
                labelCache.align(TextAlign.ALIGN_LEFT);
            }

            boolean hidebar = tagCompound.getBoolean("hidebar");
            boolean hidetext = tagCompound.getBoolean("hidetext");
            boolean showdiff = tagCompound.getBoolean("showdiff");
            boolean showpct = tagCompound.getBoolean("showpct");
            rfRenderer.settings(hidebar, hidetext, showpct, showdiff);

//            rfRenderer.format(FormatStyle.values()[tagCompound.getInt("format")]);
            rfRenderer.format(FormatStyle.getStyle(tagCompound.getString("format")));

            setupCoordinateFromNBT(tagCompound, dim, pos);
        }
    }

    protected void setupCoordinateFromNBT(CompoundNBT tagCompound, int dim, BlockPos pos) {
        coordinate = BlockPosTools.INVALID;
        if (tagCompound.contains("monitorx")) {
            if (tagCompound.contains("monitordim")) {
                this.dim = tagCompound.getInt("monitordim");
            } else {
                // Compatibility reasons
                this.dim = tagCompound.getInt("dim");
            }
            if (dim == this.dim) {
                BlockPos c = new BlockPos(tagCompound.getInt("monitorx"), tagCompound.getInt("monitory"), tagCompound.getInt("monitorz"));
                int dx = Math.abs(c.getX() - pos.getX());
                int dy = Math.abs(c.getY() - pos.getY());
                int dz = Math.abs(c.getZ() - pos.getZ());
                if (dx <= 64 && dy <= 64 && dz <= 64) {
                    coordinate = c;
                }
            }
        }
    }

    @Override
    public boolean needsServerData() {
        return true;
    }
}

package mcjty.rftoolsutility.modules.screen.modulesclient;

import mcjty.rftoolsbase.api.screens.IClientScreenModule;
import mcjty.rftoolsbase.api.screens.IModuleRenderHelper;
import mcjty.rftoolsbase.api.screens.ModuleRenderInfo;
import mcjty.rftoolsutility.modules.screen.modules.ComputerScreenModule;
import net.minecraft.client.gui.FontRenderer;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ComputerClientScreenModule implements IClientScreenModule<ComputerScreenModule.ModuleComputerInfo> {

    @Override
    public IClientScreenModule.TransformMode getTransformMode() {
        return TransformMode.TEXT;
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    public void render(IModuleRenderHelper renderHelper, FontRenderer fontRenderer, int currenty,
                       ComputerScreenModule.ModuleComputerInfo screenData, ModuleRenderInfo renderInfo) {
        GlStateManager.disableLighting();
        if (screenData != null) {
            int x = 7;
            for (ComputerScreenModule.ColoredText ct : screenData) {
                fontRenderer.drawString(ct.getText(), x, currenty, ct.getColor());
                x += fontRenderer.getStringWidth(ct.getText());
            }
        }
    }

    @Override
    public void mouseClick(World world, int x, int y, boolean clicked) {

    }

    @Override
    public void setupFromNBT(CompoundNBT tagCompound, int dim, BlockPos pos) {
    }

    @Override
    public boolean needsServerData() {
        return true;
    }

}

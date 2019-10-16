package mcjty.rftoolsutility.modules.screen.client;

import mcjty.rftoolsutility.modules.screen.blocks.ScreenTileEntity;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;



public class ModelScreen extends Model {

    private RendererModel renderer = new RendererModel(this, 0, 0);

    public ModelScreen(int size) {
        if (size == ScreenTileEntity.SIZE_HUGE) {
            this.renderer.addBox(-8.0F, -8.0F, -1.0F, 48, 48, 2, 0.0F);
        } else if (size == ScreenTileEntity.SIZE_LARGE) {
            this.renderer.addBox(-8.0F, -8.0F, -1.0F, 32, 32, 2, 0.0F);
        } else {
            this.renderer.addBox(-8.0F, -8.0F, -1.0F, 16, 16, 2, 0.0F);
        }
        this.renderer.setTextureSize(16, 16);
    }

    public void render() {
        this.renderer.render(0.0625F);
    }

}

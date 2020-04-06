package mcjty.rftoolsutility.modules.logic.timer;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.gui.widgets.ToggleButton;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.util.ResourceLocation;

public class GuiTimer extends GenericGuiContainer<TimerTileEntity> {

    public GuiTimer(TimerTileEntity timerTileEntity, GenericContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, timerTileEntity, container, GuiProxy.GUI_MANUAL_MAIN, "timer");
    }

    @Override
    public void initGui() {
        window = new Window(this, tileEntity, RFToolsMessages.INSTANCE, new ResourceLocation(RFTools.MODID, "gui/timer.gui"));
        super.initGui();

        initializeFields();
    }

    private void initializeFields() {
        int delay = tileEntity.getDelay();
        if (delay <= 0) {
            delay = 1;
        }
        TextField delayField = window.findChild("delay");
        delayField.setText(String.valueOf(delay));

        ToggleButton redstonePauses = window.findChild("pauses");
        redstonePauses.setPressed(tileEntity.getRedstonePauses());
    }
}

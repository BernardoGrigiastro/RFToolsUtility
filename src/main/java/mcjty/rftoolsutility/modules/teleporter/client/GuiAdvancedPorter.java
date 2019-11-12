package mcjty.rftoolsutility.modules.teleporter.client;

import mcjty.lib.gui.GuiItemScreen;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsutility.RFToolsUtility;
import mcjty.rftoolsutility.modules.teleporter.items.porter.AdvancedChargedPorterItem;
import mcjty.rftoolsutility.network.RFToolsUtilityMessages;
import mcjty.rftoolsutility.setup.CommandHandler;
import net.minecraft.client.Minecraft;

import java.awt.*;

public class GuiAdvancedPorter extends GuiItemScreen {

    private static final int xSize = 340;
    private static final int ySize = 136;

    private Panel[] panels = new Panel[AdvancedChargedPorterItem.MAXTARGETS];
    private TextField[] destinations = new TextField[AdvancedChargedPorterItem.MAXTARGETS];

    private static int target = -1;
    private static int[] targets = new int[AdvancedChargedPorterItem.MAXTARGETS];
    private static String[] names = new String[AdvancedChargedPorterItem.MAXTARGETS];

    public GuiAdvancedPorter() {
        super(RFToolsUtility.instance, RFToolsUtilityMessages.INSTANCE, xSize, ySize, 0 /* @todo 1.14 GuiProxy.GUI_MANUAL_MAIN*/, "porter");
    }

    public static void setInfo(int target, int[] targets, String[] names) {
        GuiAdvancedPorter.target = target;
        GuiAdvancedPorter.targets = targets;
        GuiAdvancedPorter.names = names;
    }

    @Override
    public void init() {
        super.init();

        int k = (this.width - xSize) / 2;
        int l = (this.height - ySize) / 2;

        Panel toplevel = new Panel(minecraft, this).setFilledRectThickness(2).setLayout(new VerticalLayout().setSpacing(0));

        for (int i = 0 ; i < AdvancedChargedPorterItem.MAXTARGETS ; i++) {
            destinations[i] = new TextField(minecraft, this);
            panels[i] = createPanel(destinations[i], i);
            toplevel.addChild(panels[i]);
        }

        toplevel.setBounds(new Rectangle(k, l, xSize, ySize));

        window = new Window(this, toplevel);

        updateInfoFromServer();
    }

    private Panel createPanel(final TextField destination, final int i) {
        return new Panel(minecraft, this).setLayout(new HorizontalLayout())
                    .addChild(destination)
                    .addChild(new Button(minecraft, this).setText("Set").setDesiredWidth(30).setDesiredHeight(16).addButtonEvent(parent -> {
                        if (targets[i] != -1) {
                            RFToolsUtilityMessages.sendToServer(CommandHandler.CMD_SET_TARGET, TypedMap.builder().put(CommandHandler.PARAM_TARGET, targets[i]));
                            target = targets[i];
                        }
                    }))
                    .addChild(new Button(minecraft, this).setText("Clear").setDesiredWidth(40).setDesiredHeight(16).addButtonEvent(parent -> {
                        if (targets[i] != -1 && targets[i] == target) {
                            target = -1;
                        }
                        RFToolsUtilityMessages.sendToServer(CommandHandler.CMD_CLEAR_TARGET, TypedMap.builder().put(CommandHandler.PARAM_TARGET, i));
                        targets[i] = -1;
                    })).setDesiredHeight(16);
    }

    private void updateInfoFromServer() {
        RFToolsUtilityMessages.sendToServer(CommandHandler.CMD_GET_TARGETS);
    }

    private void setTarget(int i) {
        panels[i].setFilledBackground(-1);
        if (targets[i] == -1) {
            destinations[i].setText("No target set");
        } else {
            destinations[i].setText(targets[i] + ": " + names[i]);
            if (targets[i] == target) {
                panels[i].setFilledBackground(0xffeedd33);
            }
        }
    }

    @Override
    public void render(int xSize_lo, int ySize_lo, float par3) {
        super.render(xSize_lo, ySize_lo, par3);

        for (int i = 0 ; i < AdvancedChargedPorterItem.MAXTARGETS ; i++) {
            setTarget(i);
        }

        drawWindow();
    }

    public static void open() {
        Minecraft.getInstance().displayGuiScreen(new GuiAdvancedPorter());
    }

}

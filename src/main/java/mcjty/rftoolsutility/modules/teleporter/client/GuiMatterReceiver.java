package mcjty.rftoolsutility.modules.teleporter.client;

import mcjty.lib.base.StyleConfig;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.typed.TypedMap;
import mcjty.rftoolsutility.RFToolsUtility;
import mcjty.rftoolsutility.modules.teleporter.blocks.MatterReceiverTileEntity;
import mcjty.rftoolsutility.network.PacketGetPlayers;
import mcjty.rftoolsutility.network.RFToolsUtilityMessages;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraftforge.energy.CapabilityEnergy;

import java.awt.*;
import java.util.List;
import java.util.*;

import static mcjty.rftoolsutility.modules.teleporter.blocks.MatterReceiverTileEntity.PARAM_PLAYER;

public class GuiMatterReceiver extends GenericGuiContainer<MatterReceiverTileEntity, GenericContainer> {
    public static final int MATTER_WIDTH = 180;
    public static final int MATTER_HEIGHT = 160;
    public static final String ACCESS_PRIVATE = "Private";
    public static final String ACCESS_PUBLIC = "Public";

    private EnergyBar energyBar;
    private ChoiceLabel privateSetting;
    private WidgetList allowedPlayers;
    private Button addButton;
    private Button delButton;
    private TextField nameField;

    // A copy of the players we're currently showing.
    private List<String> players = null;
    private int listDirty = 0;

    private static Set<String> fromServer_allowedPlayers = new HashSet<>();
    public static void storeAllowedPlayersForClient(List<String> players) {
        fromServer_allowedPlayers = new HashSet<>(players);
    }


    public GuiMatterReceiver(MatterReceiverTileEntity matterReceiverTileEntity, GenericContainer container, PlayerInventory inventory) {
        super(RFToolsUtility.instance, RFToolsUtilityMessages.INSTANCE, matterReceiverTileEntity, container, inventory, /*@todo 1.14 GuiProxy.GUI_MANUAL_MAIN*/0, "tpreceiver");

        xSize = MATTER_WIDTH;
        ySize = MATTER_HEIGHT;
    }

    @Override
    public void init() {
        super.init();

        energyBar = new EnergyBar(minecraft, this).setFilledRectThickness(1).setHorizontal().setDesiredHeight(12).setDesiredWidth(80).setShowText(false);

        TextField textField = new TextField(minecraft, this)
                .setName("name")
                .setTooltips("Use this name to", "identify this receiver", "in the dialer");
        Panel namePanel = new Panel(minecraft, this).setLayout(new HorizontalLayout()).addChild(new Label(minecraft, this).setText("Name:")).addChild(textField).setDesiredHeight(16);

        privateSetting = new ChoiceLabel(minecraft, this).addChoices(ACCESS_PUBLIC, ACCESS_PRIVATE).setDesiredHeight(14).setDesiredWidth(60).
                setName("private").
                setChoiceTooltip(ACCESS_PUBLIC, "Everyone can dial to this receiver").
                setChoiceTooltip(ACCESS_PRIVATE, "Only people in the access list below", "can dial to this receiver");
        Panel privatePanel = new Panel(minecraft, this).setLayout(new HorizontalLayout()).addChild(new Label(minecraft, this).setText("Access:")).addChild(privateSetting).setDesiredHeight(16);

        allowedPlayers = new WidgetList(minecraft, this).setName("allowedplayers");
        Slider allowedPlayerSlider = new Slider(minecraft, this).setDesiredWidth(10).setVertical().setScrollableName("allowedplayers");
        Panel allowedPlayersPanel = new Panel(minecraft, this).setLayout(new HorizontalLayout().setHorizontalMargin(3).setSpacing(1)).addChild(allowedPlayers).addChild(allowedPlayerSlider)
                .setFilledBackground(0xff9e9e9e);

        nameField = new TextField(minecraft, this);
        addButton = new Button(minecraft, this).setChannel("addplayer").setText("Add").setDesiredHeight(13).setDesiredWidth(34).setTooltips("Add a player to the access list");
        delButton = new Button(minecraft, this).setChannel("delplayer").setText("Del").setDesiredHeight(13).setDesiredWidth(34).setTooltips("Remove the selected player", "from the access list");
        Panel buttonPanel = new Panel(minecraft, this).setLayout(new HorizontalLayout()).addChildren(nameField, addButton, delButton).setDesiredHeight(16);

        Panel toplevel = new Panel(minecraft, this).setFilledRectThickness(2).setLayout(new VerticalLayout().setHorizontalMargin(3).setVerticalMargin(3).setSpacing(1)).
                addChildren(energyBar, namePanel, privatePanel, allowedPlayersPanel, buttonPanel);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, MATTER_WIDTH, MATTER_HEIGHT));
        window = new Window(this, toplevel);
        minecraft.keyboardListener.enableRepeatEvents(true);

        listDirty = 0;
        requestPlayers();

        window.bind(RFToolsUtilityMessages.INSTANCE, "name", tileEntity, MatterReceiverTileEntity.VALUE_NAME.getName());
        window.bind(RFToolsUtilityMessages.INSTANCE, "private", tileEntity, MatterReceiverTileEntity.VALUE_PRIVATE.getName());
        window.event("addplayer", (source, params) -> addPlayer());
        window.event("delplayer", (source, params) -> delPlayer());
    }

    private void addPlayer() {
        sendServerCommand(RFToolsUtilityMessages.INSTANCE, MatterReceiverTileEntity.CMD_ADDPLAYER,
                TypedMap.builder()
                        .put(PARAM_PLAYER, nameField.getText())
                        .build());
        listDirty = 0;
    }

    private void delPlayer() {
        sendServerCommand(RFToolsUtilityMessages.INSTANCE, MatterReceiverTileEntity.CMD_DELPLAYER,
                TypedMap.builder()
                        .put(PARAM_PLAYER, nameField.getText())
                        .build());
        listDirty = 0;
    }


    private void requestPlayers() {
        RFToolsUtilityMessages.INSTANCE.sendToServer(new PacketGetPlayers(tileEntity.getPos(), MatterReceiverTileEntity.CMD_GETPLAYERS, MatterReceiverTileEntity.CLIENTCMD_GETPLAYERS));
    }

    private void populatePlayers() {
        List<String> newPlayers = new ArrayList<>(fromServer_allowedPlayers);
        Collections.sort(newPlayers);
        if (newPlayers.equals(players)) {
            return;
        }

        players = new ArrayList<>(newPlayers);
        allowedPlayers.removeChildren();
        for (String player : players) {
            allowedPlayers.addChild(new Label(minecraft, this).setColor(StyleConfig.colorTextInListNormal).setText(player).setHorizontalAlignment(HorizontalAlignment.ALIGN_LEFT));
        }
    }

    private void requestListsIfNeeded() {
        listDirty--;
        if (listDirty <= 0) {
            requestPlayers();
            listDirty = 20;
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        requestListsIfNeeded();
        populatePlayers();
        enableButtons();

        drawWindow();
        tileEntity.getCapability(CapabilityEnergy.ENERGY).ifPresent(e -> {
            energyBar.setMaxValue(((GenericEnergyStorage)e).getCapacity());
            energyBar.setValue(((GenericEnergyStorage)e).getEnergy());
        });
    }

    private void enableButtons() {
        boolean isPrivate = ACCESS_PRIVATE.equals(privateSetting.getCurrentChoice());
        allowedPlayers.setEnabled(isPrivate);
        nameField.setEnabled(isPrivate);

        int isPlayerSelected = allowedPlayers.getSelected();
        delButton.setEnabled(isPrivate && (isPlayerSelected != -1));
        String name = nameField.getText();
        addButton.setEnabled(isPrivate && name != null && !name.isEmpty());
    }
}

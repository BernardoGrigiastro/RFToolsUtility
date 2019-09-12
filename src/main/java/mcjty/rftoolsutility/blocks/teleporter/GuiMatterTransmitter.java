package mcjty.rftoolsutility.blocks.teleporter;

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
import mcjty.rftoolsutility.network.PacketGetPlayers;
import mcjty.rftoolsutility.network.RFToolsUtilityMessages;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraftforge.energy.CapabilityEnergy;

import java.awt.*;
import java.util.List;
import java.util.*;

import static mcjty.rftoolsutility.blocks.teleporter.MatterTransmitterTileEntity.PARAM_PLAYER;

public class GuiMatterTransmitter extends GenericGuiContainer<MatterTransmitterTileEntity, GenericContainer> {
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


    public GuiMatterTransmitter(MatterTransmitterTileEntity transmitterTileEntity, GenericContainer container, PlayerInventory inventory) {
        super(RFToolsUtility.instance, RFToolsUtilityMessages.INSTANCE, transmitterTileEntity, container, inventory, /*@todo 1.14 GuiProxy.GUI_MANUAL_MAIN*/0, "tptransmitter");

        xSize = MATTER_WIDTH;
        ySize = MATTER_HEIGHT;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void init() {
        super.init();

        energyBar = new EnergyBar(minecraft,this).setFilledRectThickness(1).setHorizontal().setDesiredHeight(12).setDesiredWidth(80).setShowText(false);

        TextField textField = new TextField(minecraft, this)
                .setName("name")
                .setTooltips("Use this name to", "identify this transmitter", "in the dialer");
        Panel namePanel = new Panel(minecraft, this).setLayout(new HorizontalLayout()).addChild(new Label(minecraft, this).setText("Name:")).addChild(textField).setDesiredHeight(16);

        privateSetting = new ChoiceLabel(minecraft, this)
                .setName("private")
                .addChoices(ACCESS_PUBLIC, ACCESS_PRIVATE).setDesiredHeight(14).setDesiredWidth(60).
            setChoiceTooltip(ACCESS_PUBLIC, "Everyone can access this transmitter", "and change the dialing destination").
            setChoiceTooltip(ACCESS_PRIVATE, "Only people in the access list below", "can access this transmitter");
        ToggleButton beamToggle = new ToggleButton(minecraft, this)
                .setName("beam")
                .setText("Hide").setCheckMarker(true).setDesiredHeight(14).setDesiredWidth(49).setTooltips("Hide the teleportation beam");
        Panel privatePanel = new Panel(minecraft, this).setLayout(new HorizontalLayout()).addChild(new Label(minecraft, this).setText("Access:")).addChild(privateSetting).addChild(beamToggle).setDesiredHeight(16);

        allowedPlayers = new WidgetList(minecraft, this).setName("allowedplayers");
        Slider allowedPlayerSlider = new Slider(minecraft, this).setDesiredWidth(10).setVertical().setScrollableName("allowedplayers");
        Panel allowedPlayersPanel = new Panel(minecraft, this).setLayout(new HorizontalLayout().setHorizontalMargin(3).setSpacing(1)).addChild(allowedPlayers).addChild(allowedPlayerSlider)
                .setFilledBackground(0xff9e9e9e);

        nameField = new TextField(minecraft, this);
        addButton = new Button(minecraft, this).setChannel("addplayer").setText("Add").setDesiredHeight(13).setDesiredWidth(34).setTooltips("Add a player to the access list");
        delButton = new Button(minecraft, this).setChannel("delplayer").setText("Del").setDesiredHeight(13).setDesiredWidth(34).setTooltips("Remove the selected player", "from the access list");
        Panel buttonPanel = new Panel(minecraft, this).setLayout(new HorizontalLayout()).addChild(nameField).addChild(addButton).addChild(delButton).setDesiredHeight(16);

        Panel toplevel = new Panel(minecraft, this).setFilledRectThickness(2).setLayout(new VerticalLayout().setHorizontalMargin(3).setVerticalMargin(3).setSpacing(1)).
                addChildren(energyBar, namePanel, privatePanel, allowedPlayersPanel, buttonPanel);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, MATTER_WIDTH, MATTER_HEIGHT));
        window = new Window(this, toplevel);

        minecraft.keyboardListener.enableRepeatEvents(true);

        listDirty = 0;
        requestPlayers();

        window.bind(RFToolsUtilityMessages.INSTANCE, "name", tileEntity, MatterTransmitterTileEntity.VALUE_NAME.getName());
        window.bind(RFToolsUtilityMessages.INSTANCE, "private", tileEntity, MatterTransmitterTileEntity.VALUE_PRIVATE.getName());
        window.bind(RFToolsUtilityMessages.INSTANCE, "beam", tileEntity, MatterTransmitterTileEntity.VALUE_BEAM.getName());
        window.event("addplayer", (source, params) -> addPlayer());
        window.event("delplayer", (source, params) -> delPlayer());
    }

    private void addPlayer() {
        sendServerCommand(RFToolsUtilityMessages.INSTANCE, MatterTransmitterTileEntity.CMD_ADDPLAYER,
                TypedMap.builder()
                        .put(PARAM_PLAYER, nameField.getText())
                        .build());
        listDirty = 0;
    }

    private void delPlayer() {
        sendServerCommand(RFToolsUtilityMessages.INSTANCE, MatterTransmitterTileEntity.CMD_DELPLAYER,
                TypedMap.builder()
                        .put(PARAM_PLAYER, nameField.getText())
                        .build());
        listDirty = 0;
    }


    private void requestPlayers() {
        RFToolsUtilityMessages.INSTANCE.sendToServer(new PacketGetPlayers(tileEntity.getPos(), MatterTransmitterTileEntity.CMD_GETPLAYERS, MatterTransmitterTileEntity.CLIENTCMD_GETPLAYERS));
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

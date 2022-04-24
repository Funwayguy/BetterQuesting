package betterquesting.client.gui2.editors.nbt;

import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.IPanelButton;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.events.IPEventListener;
import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.api2.client.gui.events.PanelEvent;
import betterquesting.api2.client.gui.events.types.PEventButton;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.client.gui2.editors.nbt.callback.NbtEntityCallback;
import betterquesting.client.gui2.editors.nbt.callback.NbtFluidCallback;
import betterquesting.client.gui2.editors.nbt.callback.NbtItemCallback;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;

public class GuiNbtType extends GuiScreenCanvas implements IPEventListener, IVolatileScreen {
    private final NBTTagCompound tagCompound;

    public GuiNbtType(GuiScreen parent, NBTTagCompound tagCompound) {
        super(parent);
        this.tagCompound = tagCompound;
    }

    @Override
    public void initPanel() {
        super.initPanel();

        PEventBroadcaster.INSTANCE.register(this, PEventButton.class);

        // Background panel
        CanvasTextured cvBackground = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);

        PanelTextBox panTxt = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 16, 0, -32), 0), QuestTranslation.translate("betterquesting.title.json_object")).setAlignment(1);
        panTxt.setColor(PresetColor.TEXT_HEADER.getColor());
        cvBackground.addPanel(panTxt);

        cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 200, 16, 0), 0, QuestTranslation.translate("gui.back")));

        cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.MID_CENTER, -100, -32, 200, 16, 0), 1, QuestTranslation.translate("betterquesting.btn.raw_nbt")));
        cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.MID_CENTER, -100, -16, 200, 16, 0), 2, QuestTranslation.translate("betterquesting.btn.item")));
        cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.MID_CENTER, -100, 0, 200, 16, 0), 3, QuestTranslation.translate("betterquesting.btn.fluid")));
        cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.MID_CENTER, -100, 16, 200, 16, 0), 4, QuestTranslation.translate("betterquesting.btn.entity")));
    }

    @Override
    public void onPanelEvent(PanelEvent event) {
        if (event instanceof PEventButton) {
            onButtonPress((PEventButton) event);
        }
    }

    private void onButtonPress(PEventButton event) {
        IPanelButton btn = event.getButton();

        switch (btn.getButtonID()) {
            case 0: // Back
            {
                mc.displayGuiScreen(this.parent);
                break;
            }
            case 1: // Raw NBT
            {
                mc.displayGuiScreen(new GuiNbtEditor(this.parent, tagCompound, null));
                break;
            }
            case 2: // Item
            {
                mc.displayGuiScreen(new GuiItemSelection(this.parent, tagCompound, new NbtItemCallback(tagCompound)));
                break;
            }
            case 3: // Fluid
            {
                mc.displayGuiScreen(new GuiFluidSelection(this.parent, tagCompound, new NbtFluidCallback(tagCompound)));
                break;
            }
            case 4: // Entity
            {
                mc.displayGuiScreen(new GuiEntitySelection(this.parent, tagCompound, new NbtEntityCallback(tagCompound)));
                break;
            }
        }
    }
}

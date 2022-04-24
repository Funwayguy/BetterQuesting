package betterquesting.client.gui2.editors.nbt;

import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.misc.ICallback;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.IPanelButton;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.controls.PanelTextField;
import betterquesting.api2.client.gui.controls.filters.FieldFilterNumber;
import betterquesting.api2.client.gui.controls.filters.FieldFilterString;
import betterquesting.api2.client.gui.events.IPEventListener;
import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.api2.client.gui.events.PanelEvent;
import betterquesting.api2.client.gui.events.types.PEventButton;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelFluidSlot;
import betterquesting.api2.client.gui.panels.content.PanelItemSlot;
import betterquesting.api2.client.gui.panels.content.PanelLine;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasFluidDatabase;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetLine;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.utils.QuestTranslation;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector4f;

public class GuiFluidSelection extends GuiScreenCanvas implements IPEventListener, IVolatileScreen {
    private final ICallback<FluidStack> callback;
    private FluidStack itemStack;

    private PanelTextField<Integer> fieldSize;
    private PanelFluidSlot itemPreview;

    public GuiFluidSelection(GuiScreen parent, NBTTagCompound tag, ICallback<FluidStack> callback) {
        this(parent, JsonHelper.JsonToFluidStack(tag), callback);
    }

    public GuiFluidSelection(GuiScreen parent, FluidStack stack, ICallback<FluidStack> callback) {
        super(parent);
        this.callback = callback;
        this.itemStack = stack;
    }

    public void initPanel() {
        super.initPanel();

        PEventBroadcaster.INSTANCE.register(this, PEventButton.class);
        Keyboard.enableRepeatEvents(true);

        // Background panel
        CanvasTextured cvBackground = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);

        cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 200, 16, 0), 0, QuestTranslation.translate("gui.done")));

        PanelTextBox txTitle = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 16, 0, -32), 0), QuestTranslation.translate("betterquesting.title.select_fluid")).setAlignment(1);
        txTitle.setColor(PresetColor.TEXT_HEADER.getColor());
        cvBackground.addPanel(txTitle);

        // === RIGHT PANEL ===

        CanvasEmpty cvRight = new CanvasEmpty(new GuiTransform(GuiAlign.HALF_RIGHT, new GuiPadding(8, 32, 16, 32), 0));
        cvBackground.addPanel(cvRight);

        CanvasFluidDatabase cvDatabase = new CanvasFluidDatabase(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 16, 8, 0), 0), 1);
        cvRight.addPanel(cvDatabase);

        PanelTextField<String> searchBox = new PanelTextField<>(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 0, 8, -16), 0), "", FieldFilterString.INSTANCE);
        searchBox.setCallback(cvDatabase::setSearchFilter).setWatermark("Search...");
        cvRight.addPanel(searchBox);

        PanelVScrollBar scEdit = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-8, 16, 0, 0), 0));
        cvDatabase.setScrollDriverY(scEdit);
        cvRight.addPanel(scEdit);

        // === TOP LEFT PANEL ===

        CanvasEmpty cvTopLeft = new CanvasEmpty(new GuiTransform(new Vector4f(0F, 0F, 0.5F, 0.4F), new GuiPadding(16, 32, 8, 8), 0));
        cvBackground.addPanel(cvTopLeft);

        PanelTextBox txSelection = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 0, 0, -16), 0), QuestTranslation.translate("betterquesting.gui.selection"));
        txSelection.setColor(PresetColor.TEXT_MAIN.getColor());
        cvTopLeft.addPanel(txSelection);

        itemPreview = new PanelFluidSlot(new GuiTransform(GuiAlign.TOP_LEFT, 0, 16, 36, 36, 0), 99, itemStack);
        cvTopLeft.addPanel(itemPreview);

        PanelTextBox txMulti = new PanelTextBox(new GuiTransform(GuiAlign.TOP_LEFT, 36, 20, 16, 12, 0), "x").setAlignment(1);
        txMulti.setColor(PresetColor.TEXT_MAIN.getColor());
        cvTopLeft.addPanel(txMulti);

        fieldSize = new PanelTextField<>(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(52, 16, 0, -32), 0), itemStack == null ? "1" : ("" + itemStack.amount), FieldFilterNumber.INT);
        cvTopLeft.addPanel(fieldSize);
        fieldSize.setCallback(value -> {
            if (itemStack != null) itemStack.amount = value;
        });

        // === BOTTOM LEFT PANEL ===

        CanvasEmpty cvBottomLeft = new CanvasEmpty(new GuiTransform(new Vector4f(0F, 0.4F, 0.5F, 1F), new GuiPadding(16, 8, 8, 32), 0));
        cvBackground.addPanel(cvBottomLeft);

        PanelTextBox txInvo = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 0, 0, -16), 0), QuestTranslation.translate("container.inventory"));
        txInvo.setColor(PresetColor.TEXT_MAIN.getColor());
        cvBottomLeft.addPanel(txInvo);

        IInventory inventory = mc.player.inventory;

        float iScale = Math.min(cvBottomLeft.getTransform().getWidth() / 162F, (cvBottomLeft.getTransform().getHeight() - 20) / 72F);
        int slotSize = (int) Math.floor(18 * iScale);

        for (int i = 0; i < 27; i++) // Main inventory
        {
            int x = (i % 9) * slotSize;
            int y = (i / 9) * slotSize + 16;

            ItemStack tmp = inventory.getStackInSlot(i + 9);
            BigItemStack invoStack = tmp.isEmpty() ? null : new BigItemStack(tmp);

            cvBottomLeft.addPanel(new PanelItemSlot(new GuiTransform(GuiAlign.TOP_LEFT, x, y, slotSize, slotSize, 0), 2, invoStack, true));

        }

        for (int i = 0; i < 9; i++) // Hotbar
        {
            int x = (i % 9) * slotSize;

            ItemStack tmp = inventory.getStackInSlot(i);
            BigItemStack invoStack = tmp.isEmpty() ? null : new BigItemStack(tmp);

            cvBottomLeft.addPanel(new PanelItemSlot(new GuiTransform(GuiAlign.TOP_LEFT, x, 20 + (3 * slotSize), slotSize, slotSize, 0), 2, invoStack, true));
        }

        // === DIVIDERS ===

        IGuiRect ls0 = new GuiTransform(GuiAlign.TOP_CENTER, 0, 32, 0, 0, 0);
        ls0.setParent(cvBackground.getTransform());
        IGuiRect le0 = new GuiTransform(GuiAlign.BOTTOM_CENTER, 0, -32, 0, 0, 0);
        le0.setParent(cvBackground.getTransform());
        PanelLine paLine0 = new PanelLine(ls0, le0, PresetLine.GUI_DIVIDER.getLine(), 1, PresetColor.GUI_DIVIDER.getColor(), 1);
        cvBackground.addPanel(paLine0);
    }

    @Override
    public void onPanelEvent(PanelEvent event) {
        if (event instanceof PEventButton) {
            onButtonPress((PEventButton) event);
        }
    }

    @SuppressWarnings("unchecked")
    private void onButtonPress(PEventButton event) {
        IPanelButton btn = event.getButton();

        if (btn.getButtonID() == 0) // Exit
        {
            if (callback != null) {
                callback.setValue(itemStack);
            }

            mc.displayGuiScreen(this.parent);
        } else if (btn.getButtonID() == 1 && btn instanceof PanelButtonStorage) {
            FluidStack fluid = ((PanelButtonStorage<FluidStack>) btn).getStoredValue();

            if (fluid != null) {
                itemStack = fluid.copy();
                itemPreview.setStoredValue(itemStack);
                fieldSize.setText("" + itemStack.amount);
            }
        } else if (btn.getButtonID() == 2 && btn instanceof PanelButtonStorage) {
            BigItemStack tmp = ((PanelButtonStorage<BigItemStack>) btn).getStoredValue();
            FluidStack fluid = tmp == null ? null : FluidUtil.getFluidContained(tmp.getBaseStack());

            if (fluid != null) {
                itemStack = fluid.copy();
                itemPreview.setStoredValue(itemStack);
                fieldSize.setText("" + itemStack.amount);
            }
        }
    }
}

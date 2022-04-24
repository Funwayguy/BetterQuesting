package betterquesting.client.gui2.editors.nbt;

import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.IPanelButton;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.controls.PanelTextField;
import betterquesting.api2.client.gui.controls.filters.FieldFilterString;
import betterquesting.api2.client.gui.events.IPEventListener;
import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.api2.client.gui.events.PanelEvent;
import betterquesting.api2.client.gui.events.types.PEventButton;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.utils.QuestTranslation;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.*;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class GuiNbtAdd extends GuiScreenCanvas implements IPEventListener, IVolatileScreen {
    private final NBTBase nbt;
    private final int index;

    private PanelTextField<String> flKey;
    private final List<PanelButtonStorage<NBTBase>> options = new ArrayList<>();
    private NBTBase selected = null;
    private PanelButton btnConfirm;
    //private PanelTextBox txtKey;

    public GuiNbtAdd(GuiScreen parent, NBTTagCompound compoundTag) {
        super(parent);
        this.nbt = compoundTag;
        this.index = -1;
    }

    public GuiNbtAdd(GuiScreen parent, NBTTagList list, int index) {
        super(parent);
        this.nbt = list;
        this.index = index;
    }

    @Override
    public void initPanel() {
        super.initPanel();

        PEventBroadcaster.INSTANCE.register(this, PEventButton.class);
        Keyboard.enableRepeatEvents(true);

        // Background panel
        CanvasTextured cvBackground = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);

        cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 100, 16, 0), 0, QuestTranslation.translate("gui.cancel")));

        btnConfirm = new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, 0, -16, 100, 16, 0), 1, QuestTranslation.translate("gui.done"));
        cvBackground.addPanel(btnConfirm);

        PanelTextBox panTxt = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 16, 0, -32), 0), QuestTranslation.translate("betterquesting.title.json_add")).setAlignment(1);
        panTxt.setColor(PresetColor.TEXT_HEADER.getColor());
        cvBackground.addPanel(panTxt);

        if (nbt.getId() == 10) // NBTTagCompound
        {
            btnConfirm.setActive(false);

            PanelTextBox txKeyTitle = new PanelTextBox(new GuiTransform(GuiAlign.TOP_CENTER, -100, 36, 200, 12, 0), TextFormatting.RED + QuestTranslation.translate("betterquesting.gui.no_key"));
            txKeyTitle.setColor(PresetColor.TEXT_MAIN.getColor());
            cvBackground.addPanel(txKeyTitle);

            flKey = new PanelTextField<>(new GuiTransform(GuiAlign.TOP_CENTER, -100, 48, 200, 16, 0), "", FieldFilterString.INSTANCE);
            cvBackground.addPanel(flKey);

            flKey.setCallback(value -> {
                if (value.isEmpty()) {
                    txKeyTitle.setText(TextFormatting.RED + QuestTranslation.translate("betterquesting.gui.no_key"));
                } else if (((NBTTagCompound) nbt).hasKey(value)) {
                    txKeyTitle.setText(TextFormatting.RED + QuestTranslation.translate("betterquesting.gui.duplicate_key"));
                } else {
                    txKeyTitle.setText(QuestTranslation.translate("betterquesting.gui.key"));
                }

                updateConfirm();
            });
        }

        options.clear();

        int n = 0;

        // Standard Objects
        options.add(new PanelButtonStorage<>(new GuiTransform(GuiAlign.MID_CENTER, 0, n * 16, 100, 16, 0), 2, QuestTranslation.translate("betterquesting.btn.item"), JsonHelper.ItemStackToJson(new BigItemStack(Blocks.STONE), new NBTTagCompound())));
        options.add(new PanelButtonStorage<>(new GuiTransform(GuiAlign.MID_CENTER, 100, n++ * 16, 92, 16, 0), 2, QuestTranslation.translate("betterquesting.btn.fluid"), JsonHelper.FluidStackToJson(new FluidStack(FluidRegistry.WATER, 1000), new NBTTagCompound())));
        options.add(new PanelButtonStorage<>(new GuiTransform(GuiAlign.MID_CENTER, 0, n++ * 16, 192, 16, 0), 2, QuestTranslation.translate("betterquesting.btn.entity"), JsonHelper.EntityToJson(new EntityPig(mc.world), new NBTTagCompound())));

        // NBT types
        options.add(new PanelButtonStorage<>(new GuiTransform(GuiAlign.MID_CENTER, 0, n++ * 16, 192, 16, 0), 2, NBTTagString.class.getSimpleName(), new NBTTagString("")));
        options.add(new PanelButtonStorage<>(new GuiTransform(GuiAlign.MID_CENTER, 0, n++ * 16, 192, 16, 0), 2, NBTTagCompound.class.getSimpleName(), new NBTTagCompound()));
        options.add(new PanelButtonStorage<>(new GuiTransform(GuiAlign.MID_CENTER, 0, n++ * 16, 192, 16, 0), 2, NBTTagList.class.getSimpleName(), new NBTTagList()));
        options.add(new PanelButtonStorage<>(new GuiTransform(GuiAlign.MID_CENTER, 0, n++ * 16, 192, 16, 0), 2, NBTTagByte.class.getSimpleName(), new NBTTagByte((byte) 0)));
        options.add(new PanelButtonStorage<>(new GuiTransform(GuiAlign.MID_CENTER, 0, n++ * 16, 192, 16, 0), 2, NBTTagShort.class.getSimpleName(), new NBTTagShort((short) 0)));
        options.add(new PanelButtonStorage<>(new GuiTransform(GuiAlign.MID_CENTER, 0, n++ * 16, 192, 16, 0), 2, NBTTagInt.class.getSimpleName(), new NBTTagInt(0)));
        options.add(new PanelButtonStorage<>(new GuiTransform(GuiAlign.MID_CENTER, 0, n++ * 16, 192, 16, 0), 2, NBTTagLong.class.getSimpleName(), new NBTTagLong(0L)));
        options.add(new PanelButtonStorage<>(new GuiTransform(GuiAlign.MID_CENTER, 0, n++ * 16, 192, 16, 0), 2, NBTTagFloat.class.getSimpleName(), new NBTTagFloat(0F)));
        options.add(new PanelButtonStorage<>(new GuiTransform(GuiAlign.MID_CENTER, 0, n++ * 16, 192, 16, 0), 2, NBTTagDouble.class.getSimpleName(), new NBTTagDouble(0D)));
        options.add(new PanelButtonStorage<>(new GuiTransform(GuiAlign.MID_CENTER, 0, n++ * 16, 192, 16, 0), 2, NBTTagByteArray.class.getSimpleName(), new NBTTagByteArray(new byte[0])));
        options.add(new PanelButtonStorage<>(new GuiTransform(GuiAlign.MID_CENTER, 0, n++ * 16, 192, 16, 0), 2, NBTTagIntArray.class.getSimpleName(), new NBTTagIntArray(new int[0])));
        options.add(new PanelButtonStorage<>(new GuiTransform(GuiAlign.MID_CENTER, 0, n * 16, 192, 16, 0), 2, NBTTagLongArray.class.getSimpleName(), new NBTTagLongArray(new long[0])));

        CanvasScrolling cvOptions = new CanvasScrolling(new GuiTransform(new Vector4f(0.5F, 0F, 0.5F, 1F), new GuiPadding(-100, 64, -92, 32), 0));
        cvBackground.addPanel(cvOptions);

        for (PanelButtonStorage<NBTBase> btn : options) {
            cvOptions.addPanel(btn);
        }

        PanelVScrollBar scOptions = new PanelVScrollBar(new GuiTransform(new Vector4f(0.5F, 0F, 0.5F, 1F), new GuiPadding(92, 64, -100, 32), 0));
        cvBackground.addPanel(scOptions);
        cvOptions.setScrollDriverY(scOptions);
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

        switch (btn.getButtonID()) {
            case 0: // Cancel
            {
                mc.displayGuiScreen(this.parent);
                break;
            }
            case 1: // Confirm
            {
                if (selected == null) {
                    return;
                } else if (nbt.getId() == 10) {
                    ((NBTTagCompound) nbt).setTag(flKey.getValue(), selected);
                } else if (nbt.getId() == 9) {
                    NBTTagList l = (NBTTagList) nbt;

                    if (index == l.tagCount()) {
                        l.appendTag(selected);
                    } else {
                        // Shift entries up manually
                        for (int n = l.tagCount() - 1; n >= index; n--) {
                            l.set(n + 1, l.get(n));
                        }

                        l.set(index, selected);
                    }
                }

                mc.displayGuiScreen(this.parent);
                break;
            }
            case 2: // Select this
            {
                selected = ((PanelButtonStorage<NBTBase>) btn).getStoredValue();

                for (PanelButtonStorage<NBTBase> b : options) {
                    b.setActive(true);
                }

                btn.setActive(false);

                updateConfirm();
                break;
            }
        }
    }

    private void updateConfirm() {
        if (flKey == null) {
            btnConfirm.setActive(selected != null);
        } else if (flKey.getValue().isEmpty() || ((NBTTagCompound) nbt).hasKey(flKey.getValue())) {
            btnConfirm.setActive(false);
        } else {
            btnConfirm.setActive(selected != null);
        }
    }
}

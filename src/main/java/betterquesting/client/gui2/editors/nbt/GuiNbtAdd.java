package betterquesting.client.gui2.editors.nbt;

import betterquesting.abs.misc.GuiAnchor;
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
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.*;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public class GuiNbtAdd extends GuiScreenCanvas implements IPEventListener, IVolatileScreen
{
    private final INBT nbt;
    private final int index;
    
    private PanelTextField<String> flKey;
    private final List<PanelButtonStorage<INBT>> options = new ArrayList<>();
    private INBT selected = null;
    private PanelButton btnConfirm;
    //private PanelTextBox txtKey;
    
    public GuiNbtAdd(Screen parent, CompoundNBT compoundTag)
    {
        super(parent);
        this.nbt = compoundTag;
        this.index = -1;
    }
    
    public GuiNbtAdd(Screen parent, ListNBT list, int index)
    {
        super(parent);
        this.nbt = list;
        this.index = index;
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
        PEventBroadcaster.INSTANCE.register(this, PEventButton.class);
        Minecraft.getInstance().keyboardListener.enableRepeatEvents(true);
        
        // Background panel
        CanvasTextured cvBackground = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);
    
        cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 100, 16, 0), 0, QuestTranslation.translate("gui.cancel")));
        
        btnConfirm = new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, 0, -16, 100, 16, 0), 1, QuestTranslation.translate("gui.done"));
        cvBackground.addPanel(btnConfirm);
        
        PanelTextBox panTxt = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 16, 0, -32), 0), QuestTranslation.translate("betterquesting.title.json_add")).setAlignment(1);
        panTxt.setColor(PresetColor.TEXT_HEADER.getColor());
        cvBackground.addPanel(panTxt);
        
        if(nbt.getId() == 10) // NBTTagCompound
        {
            btnConfirm.setActive(false);
            
            PanelTextBox txKeyTitle = new PanelTextBox(new GuiTransform(GuiAlign.TOP_CENTER, -100, 36, 200, 12, 0), TextFormatting.RED + QuestTranslation.translate("betterquesting.gui.no_key"));
            txKeyTitle.setColor(PresetColor.TEXT_MAIN.getColor());
            cvBackground.addPanel(txKeyTitle);
            
            flKey = new PanelTextField<>(new GuiTransform(GuiAlign.TOP_CENTER, -100, 48, 200, 16, 0), "", FieldFilterString.INSTANCE);
            cvBackground.addPanel(flKey);
            
            flKey.setCallback(value -> {
                if(value.isEmpty())
                {
                    txKeyTitle.setText(TextFormatting.RED + QuestTranslation.translate("betterquesting.gui.no_key"));
                } else if(((CompoundNBT)nbt).contains(value))
                {
                    txKeyTitle.setText(TextFormatting.RED + QuestTranslation.translate("betterquesting.gui.duplicate_key"));
                } else
                {
                    txKeyTitle.setText(QuestTranslation.translate("betterquesting.gui.key"));
                }
                
                updateConfirm();
            });
        }
        
        options.clear();
        
        int n = 0;
        
        // Standard Objects
        options.add(new PanelButtonStorage<>(new GuiTransform(GuiAlign.MID_CENTER, 0, n * 16, 100, 16, 0), 2, QuestTranslation.translate("betterquesting.btn.item"), JsonHelper.ItemStackToJson(new BigItemStack(Blocks.STONE), new CompoundNBT())));
        options.add(new PanelButtonStorage<>(new GuiTransform(GuiAlign.MID_CENTER, 100, n++ * 16, 92, 16, 0), 2, QuestTranslation.translate("betterquesting.btn.fluid"), JsonHelper.FluidStackToJson(new FluidStack(Fluids.WATER, 1000), new CompoundNBT())));
        options.add(new PanelButtonStorage<>(new GuiTransform(GuiAlign.MID_CENTER, 0, n++ * 16, 192, 16, 0), 2, QuestTranslation.translate("betterquesting.btn.entity"), JsonHelper.EntityToJson(new PigEntity(EntityType.PIG, minecraft.world), new CompoundNBT())));

        // NBT types
        options.add(new PanelButtonStorage<>(new GuiTransform(GuiAlign.MID_CENTER, 0, n++ * 16, 192, 16, 0), 2, StringNBT.class.getSimpleName(), new StringNBT("")));
        options.add(new PanelButtonStorage<>(new GuiTransform(GuiAlign.MID_CENTER, 0, n++ * 16, 192, 16, 0), 2, CompoundNBT.class.getSimpleName(), new CompoundNBT()));
        options.add(new PanelButtonStorage<>(new GuiTransform(GuiAlign.MID_CENTER, 0, n++ * 16, 192, 16, 0), 2, ListNBT.class.getSimpleName(), new ListNBT()));
        options.add(new PanelButtonStorage<>(new GuiTransform(GuiAlign.MID_CENTER, 0, n++ * 16, 192, 16, 0), 2, ByteNBT.class.getSimpleName(), new ByteNBT((byte)0)));
        options.add(new PanelButtonStorage<>(new GuiTransform(GuiAlign.MID_CENTER, 0, n++ * 16, 192, 16, 0), 2, ShortNBT.class.getSimpleName(), new ShortNBT((short)0)));
        options.add(new PanelButtonStorage<>(new GuiTransform(GuiAlign.MID_CENTER, 0, n++ * 16, 192, 16, 0), 2, IntNBT.class.getSimpleName(), new IntNBT(0)));
        options.add(new PanelButtonStorage<>(new GuiTransform(GuiAlign.MID_CENTER, 0, n++ * 16, 192, 16, 0), 2, LongNBT.class.getSimpleName(), new LongNBT(0L)));
        options.add(new PanelButtonStorage<>(new GuiTransform(GuiAlign.MID_CENTER, 0, n++ * 16, 192, 16, 0), 2, FloatNBT.class.getSimpleName(), new FloatNBT(0F)));
        options.add(new PanelButtonStorage<>(new GuiTransform(GuiAlign.MID_CENTER, 0, n++ * 16, 192, 16, 0), 2, DoubleNBT.class.getSimpleName(), new DoubleNBT(0D)));
        options.add(new PanelButtonStorage<>(new GuiTransform(GuiAlign.MID_CENTER, 0, n++ * 16, 192, 16, 0), 2, ByteArrayNBT.class.getSimpleName(), new ByteArrayNBT(new byte[0])));
        options.add(new PanelButtonStorage<>(new GuiTransform(GuiAlign.MID_CENTER, 0, n++ * 16, 192, 16, 0), 2, IntArrayNBT.class.getSimpleName(), new IntArrayNBT(new int[0])));
        options.add(new PanelButtonStorage<>(new GuiTransform(GuiAlign.MID_CENTER, 0, n * 16, 192, 16, 0), 2, LongArrayNBT.class.getSimpleName(), new LongArrayNBT(new long[0])));
    
        CanvasScrolling cvOptions = new CanvasScrolling(new GuiTransform(new GuiAnchor(0.5F, 0F, 0.5F, 1F), new GuiPadding(-100, 64, -92, 32), 0));
        cvBackground.addPanel(cvOptions);
        
        for(PanelButtonStorage<INBT> btn : options)
        {
            cvOptions.addPanel(btn);
        }
    
        PanelVScrollBar scOptions = new PanelVScrollBar(new GuiTransform(new GuiAnchor(0.5F, 0F, 0.5F, 1F), new GuiPadding(92, 64, -100, 32), 0));
        cvBackground.addPanel(scOptions);
        cvOptions.setScrollDriverY(scOptions);
    }
    
    @Override
    public void onPanelEvent(PanelEvent event)
    {
        if(event instanceof PEventButton)
        {
            onButtonPress((PEventButton)event);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void onButtonPress(PEventButton event)
    {
        IPanelButton btn = event.getButton();
    
        switch(btn.getButtonID())
        {
            case 0: // Cancel
            {
                minecraft.displayGuiScreen(this.parent);
                break;
            }
            case 1: // Confirm
            {
                if(selected == null)
                {
                    return;
                } else if(nbt.getId() == 10)
                {
                    ((CompoundNBT)nbt).put(flKey.getValue(), selected);
                } else if(nbt.getId() == 9)
                {
                    ListNBT l = (ListNBT)nbt;
                    
                    if(index == l.size())
                    {
                        l.add(selected);
                    } else
                    {
                        // Shift entries up manually
                        for(int n = l.size() - 1; n >= index; n--)
                        {
                            l.set(n + 1, l.get(n));
                        }
                        
                        l.set(index, selected);
                    }
                }
                
                minecraft.displayGuiScreen(this.parent);
                break;
            }
            case 2: // Select this
            {
                selected = ((PanelButtonStorage<INBT>)btn).getStoredValue();
                
                for(PanelButtonStorage<INBT> b : options)
                {
                    b.setActive(true);
                }
                
                btn.setActive(false);
                
                updateConfirm();
                break;
            }
        }
    }
    
    private void updateConfirm()
    {
        if(flKey == null)
        {
            btnConfirm.setActive(selected != null);
        } else if(flKey.getValue().isEmpty() || ((CompoundNBT)nbt).contains(flKey.getValue()))
        {
            btnConfirm.setActive(false);
        } else
        {
            btnConfirm.setActive(selected != null);
        }
    }
}

package betterquesting.client.gui2.editors;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.controls.PanelTextField;
import betterquesting.api2.client.gui.controls.filters.FieldFilterNumber;
import betterquesting.api2.client.gui.misc.*;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelLine;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.themes.gui_args.GArgsNBT;
import betterquesting.api2.client.gui.themes.presets.*;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.network.handlers.NetLootImport;
import betterquesting.questing.rewards.loot.LootGroup;
import betterquesting.questing.rewards.loot.LootRegistry;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector4f;

import java.text.DecimalFormat;
import java.util.List;

public class GuiEditLootEntry extends GuiScreenCanvas
{
    private LootGroup lootGroup;
    private final int groupID;
    
    private LootGroup.LootEntry selEntry;
    private int selectedID = -1;
    
    private CanvasScrolling lootList;
    private PanelTextBox fieldName;
    private PanelTextField<Integer> fieldWeight;
    private PanelTextBox textWeight;
    
    private final DecimalFormat numFormat = new DecimalFormat("0.##");
    
    public GuiEditLootEntry(GuiScreen parent, LootGroup group)
    {
        super(parent);
        this.lootGroup = group;
        this.groupID = LootRegistry.INSTANCE.getID(group);
        this.setVolatile(true);
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
    
        Keyboard.enableRepeatEvents(true);
    
        CanvasTextured cvBackground = new CanvasTextured(new GuiTransform(), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);
    
        cvBackground.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(16, 16, 16, -32), 0), QuestTranslation.translate("bq_standard.title.edit_loot_groups")).setAlignment(1).setColor(PresetColor.TEXT_HEADER.getColor()));
        
        // === LEFT SIDE ===
    
        CanvasEmpty cvLeft = new CanvasEmpty(new GuiTransform(GuiAlign.HALF_LEFT, new GuiPadding(16, 32, 8, 24), 0));
        cvBackground.addPanel(cvLeft);
        
        lootList = new CanvasScrolling(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 8, 24), 0));
        cvLeft.addPanel(lootList);
    
        PanelVScrollBar scList = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-8, 0, 0, 24), 0));
        cvLeft.addPanel(scList);
        lootList.setScrollDriverY(scList);
    
        cvLeft.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_EDGE, new GuiPadding(0, -16, 0, 0), 0), -1, QuestTranslation.translate("betterquesting.btn.new"))
        {
            @Override
            public void onButtonClick()
            {
                lootGroup.add(lootGroup.nextID(), new LootGroup.LootEntry());
                sendChanges();
            }
        });
        
        // === RIGHT SIDE ==
        
        CanvasEmpty cvRight = new CanvasEmpty(new GuiTransform(GuiAlign.HALF_RIGHT, new GuiPadding(8, 32, 16, 24), 0));
        cvBackground.addPanel(cvRight);
        
        cvRight.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 4, 0, -16), 0), QuestTranslation.translate("betterquesting.gui.name")).setColor(PresetColor.TEXT_MAIN.getColor()));
        
        fieldName = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 16, 0, -32), 0), selEntry != null ? "#" + selectedID : "#--").setColor(PresetColor.TEXT_MAIN.getColor());
        cvRight.addPanel(fieldName);
        
        cvRight.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 36, 0, -48), 0), QuestTranslation.translate("bq_standard.gui.weight")).setColor(PresetColor.TEXT_MAIN.getColor()));
        
        fieldWeight = new PanelTextField<>(new GuiTransform(new Vector4f(0F, 0F, 0.5F, 0F), new GuiPadding(0, 48, 0, -64), 0), "" + (selEntry != null ? selEntry.weight : 1), FieldFilterNumber.INT);
        fieldWeight.setCallback(value ->
        {
            if(selEntry == null) return;
            if(fieldWeight.getValue() <= 0) fieldWeight.setText("1");
            selEntry.weight = fieldWeight.getValue();
            int totalWeight = lootGroup.getTotalWeight();
            float chance = selEntry.weight / (float)totalWeight * 100F;
            textWeight.setText("/" + totalWeight + " (" + numFormat.format(chance) + "%)");
        });
        cvRight.addPanel(fieldWeight);
        
        textWeight = new PanelTextBox(new GuiTransform(new Vector4f(0.5F, 0F, 1F, 0F), new GuiPadding(4, 52, 0, -64), 0), "/1 (100%)").setColor(PresetColor.TEXT_MAIN.getColor());
        cvRight.addPanel(textWeight);
        
        final GuiScreen screenRef = this;
        cvRight.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_EDGE, new GuiPadding(0, -16, 0, 0), 0), -1, QuestTranslation.translate("bq_standard.btn.add_remove_drops"))
        {
            @Override
            public void onButtonClick()
            {
                if(selEntry != null)
                {
                    final NBTTagCompound eTag = selEntry.writeToNBT(new NBTTagCompound());
                    mc.displayGuiScreen(QuestingAPI.getAPI(ApiReference.THEME_REG).getGui(PresetGUIs.EDIT_NBT, new GArgsNBT<>(screenRef, eTag.getTagList("items", 10), value -> {
                        LootGroup lg = LootRegistry.INSTANCE.getValue(groupID);
                        LootGroup.LootEntry le = lg == null ? null : lg.getValue(selectedID);
                        if(le != null)
                        {
                            le.readFromNBT(eTag);
                            sendChanges();
                        }
                    }, null)));
                }
            }
        });
        
        // === MISC ===
        
        cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 200, 16, 0), -1, QuestTranslation.translate("gui.done"))
        {
            @Override
            public void onButtonClick()
            {
                sendChanges();
                mc.displayGuiScreen(parent);
            }
        });
        
        // === DIVIDERS ===
        
		IGuiRect ls0 = new GuiTransform(GuiAlign.TOP_CENTER, 0, 32, 0, 0, 0);
		ls0.setParent(cvBackground.getTransform());
		IGuiRect le0 = new GuiTransform(GuiAlign.BOTTOM_CENTER, 0, -24, 0, 0, 0);
		le0.setParent(cvBackground.getTransform());
		PanelLine paLine0 = new PanelLine(ls0, le0, PresetLine.GUI_DIVIDER.getLine(), 1, PresetColor.GUI_DIVIDER.getColor(), 1);
		cvBackground.addPanel(paLine0);
        
        refreshEntries();
    }
    
    @Override
    public void drawPanel(int mx, int my, float partialTick)
    {
        if(LootRegistry.INSTANCE.updateUI)
        {
            LootRegistry.INSTANCE.updateUI = false;
            lootGroup = LootRegistry.INSTANCE.getValue(groupID);
            if(lootGroup == null)
            {
                mc.displayGuiScreen(parent);
                return;
            }

            if(selectedID >= 0)
            {
                selEntry = lootGroup.getValue(selectedID);
                
                if(selEntry == null)
                {
                    selectedID = -1;
                    
                    fieldName.setText("");
                    fieldWeight.setText("1");
                    textWeight.setText("/1 (100%)");
                } else
                {
                    selEntry.weight = fieldWeight.getValue();
                    
                    int totalWeight = lootGroup.getTotalWeight();
                    float chance = selEntry.weight / (float)totalWeight * 100F;
                    textWeight.setText("/" + totalWeight + " (" + numFormat.format(chance) + "%)");
                }
            }
            
            refreshEntries();
        }
        
        super.drawPanel(mx, my, partialTick);
    }
    
    private void refreshEntries()
    {
        lootList.resetCanvas();
        int lWidth = lootList.getTransform().getWidth();
        final List<DBEntry<LootGroup.LootEntry>> lgAry = lootGroup.getEntries();
        
        for(int i = 0; i < lgAry.size(); i++)
        {
            lootList.addPanel(new PanelButtonStorage<>(new GuiRectangle(0, i * 16, 16, 16, 0), -1, "", lgAry.get(i)).setCallback(value ->
            {
                lootGroup.removeID(value.getID());
                refreshEntries();
                sendChanges();
            }).setIcon(PresetIcon.ICON_TRASH.getTexture()));
            
            lootList.addPanel(new PanelButtonStorage<>(new GuiRectangle(16, i * 16, lWidth - 16, 16, 0), -1, "#" + lgAry.get(i).getID(), lgAry.get(i)).setCallback(value ->
            {
                if(selEntry != null) sendChanges();
                selectedID = value.getID();
                selEntry = value.getValue();
                fieldName.setText("#" + selectedID);
                fieldWeight.setText("" + selEntry.weight);
                
                int totalWeight = lootGroup.getTotalWeight();
                float chance = selEntry.weight / (float)totalWeight * 100F;
                textWeight.setText("/" + totalWeight + " (" + numFormat.format(chance) + "%)");
            }));
        }
    }
    
    private void sendChanges()
    {
        NetLootImport.importLoot(LootRegistry.INSTANCE.writeToNBT(new NBTTagCompound(), null));
    }
}

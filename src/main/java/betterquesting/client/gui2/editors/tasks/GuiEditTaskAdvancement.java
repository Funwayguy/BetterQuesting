package betterquesting.client.gui2.editors.tasks;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.controls.PanelTextField;
import betterquesting.api2.client.gui.controls.filters.FieldFilterString;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.resources.textures.ItemTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.questing.tasks.TaskAdvancement;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

public class GuiEditTaskAdvancement extends GuiScreenCanvas implements IVolatileScreen
{
    private final DBEntry<IQuest> quest;
    private final TaskAdvancement task;
    
    private ResourceLocation selected;
    
    public GuiEditTaskAdvancement(GuiScreen parent, DBEntry<IQuest> quest, TaskAdvancement task)
    {
        super(parent);
        this.quest = quest;
        this.task = task;
        
        selected = task.advID;
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        Keyboard.enableRepeatEvents(true);
        
        CanvasTextured cvBackground = new CanvasTextured(new GuiTransform(), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);
        
        cvBackground.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(16, 16, 16, -32), 0), QuestTranslation.translate("bq_standard.title.edit_advancement")).setAlignment(1).setColor(PresetColor.TEXT_HEADER.getColor()));
    
        CanvasAdvancementSearch cvAdvList = new CanvasAdvancementSearch(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(16, 48, 24, 24), 0), mc.player.connection.getAdvancementManager().getAdvancementList())
        {
            private final List<PanelButtonStorage<Advancement>> btnList = new ArrayList<>();
            
            @Override
            public void refreshSearch()
            {
                super.refreshSearch();
                btnList.clear();
            }
            
            @Override
            protected boolean addResult(Advancement entry, int index, int cachedWidth)
            {
                DisplayInfo disp = entry.getDisplay();
                this.addPanel(new PanelGeneric(new GuiRectangle(0, index * 24, 24, 24, 0), PresetTexture.ITEM_FRAME.getTexture()));
                if(disp != null)this.addPanel(new PanelGeneric(new GuiRectangle(0, index * 24, 24, 24, -1), new ItemTexture(new BigItemStack(disp.getIcon()))));
                
                PanelButtonStorage<Advancement> btnAdv = new PanelButtonStorage<>(new GuiRectangle(24, index * 24, cachedWidth - 24, 24, 0), -1, disp != null ? disp.getTitle().getFormattedText() : entry.getId().toString(), entry);
                btnAdv.setActive(!entry.getId().equals(selected));
                btnAdv.setCallback(value -> {
                    selected = value.getId();
                    for(PanelButtonStorage<Advancement> b : btnList) b.setActive(!b.getStoredValue().getId().equals(selected));
                });
                if(disp != null)
                {
                    btnAdv.setTooltip(RenderUtils.splitString(disp.getDescription().getFormattedText(), 128, mc.fontRenderer));
                }
                this.addPanel(btnAdv);
                btnList.add(btnAdv);
                return true;
            }
        };
        cvBackground.addPanel(cvAdvList);
    
        PanelVScrollBar scAdv = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(-24, 48, 16, 24), 0));
        cvBackground.addPanel(scAdv);
        cvAdvList.setScrollDriverY(scAdv);
    
        PanelTextField<String> tfSearch = new PanelTextField<>(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(16, 32, 16, -48), 0), "", FieldFilterString.INSTANCE);
        tfSearch.setWatermark("Search...");
        cvBackground.addPanel(tfSearch);
        tfSearch.setCallback(cvAdvList::setSearchFilter);
        
        cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 200, 16, 0), -1, QuestTranslation.translate("gui.done"))
        {
            @Override
            public void onButtonClick()
            {
                sendChanges();
                mc.displayGuiScreen(parent);
            }
        });
    }
    
    private static final ResourceLocation QUEST_EDIT = new ResourceLocation("betterquesting:quest_edit"); // TODO: Really need to make the native packet types accessible in the API
    private void sendChanges()
    {
        task.advID = selected;
		NBTTagCompound payload = new NBTTagCompound();
        NBTTagList dataList = new NBTTagList();
        NBTTagCompound entry = new NBTTagCompound();
        entry.setInteger("questID", quest.getID());
		entry.setTag("config", quest.getValue().writeToNBT(new NBTTagCompound()));
		dataList.appendTag(entry);
		payload.setTag("data", dataList);
		payload.setInteger("action", 0); // Action: Update data
		QuestingAPI.getAPI(ApiReference.PACKET_SENDER).sendToServer(new QuestingPacket(QUEST_EDIT, payload));
    }
}

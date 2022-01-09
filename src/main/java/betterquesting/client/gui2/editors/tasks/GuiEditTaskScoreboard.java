package betterquesting.client.gui2.editors.tasks;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.controls.PanelTextField;
import betterquesting.api2.client.gui.controls.filters.FieldFilterNumber;
import betterquesting.api2.client.gui.controls.filters.FieldFilterString;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.themes.gui_args.GArgsNBT;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetGUIs;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.questing.tasks.TaskScoreboard;
import betterquesting.questing.tasks.TaskScoreboard.ScoreOperation;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

public class GuiEditTaskScoreboard extends GuiScreenCanvas
{
    private final DBEntry<IQuest> quest;
    private final TaskScoreboard task;
    
    public GuiEditTaskScoreboard(GuiScreen parent, DBEntry<IQuest> quest, TaskScoreboard task)
    {
        super(parent);
        this.quest = quest;
        this.task = task;
        this.setVolatile(true);
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
        Keyboard.enableRepeatEvents(true);
        
        CanvasTextured cvBackground = new CanvasTextured(new GuiTransform(), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);
        
        cvBackground.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(16, 16, 16, -32), 0), QuestTranslation.translate("bq_standard.title.edit_scoreboard")).setAlignment(1).setColor(PresetColor.TEXT_HEADER.getColor()));
        
        cvBackground.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.MID_CENTER, -100, -28, 50, 12, 0), QuestTranslation.translate("betterquesting.gui.name")).setColor(PresetColor.TEXT_MAIN.getColor()));
        cvBackground.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.MID_CENTER, -100, -12, 50, 12, 0), "ID").setColor(PresetColor.TEXT_MAIN.getColor())); // TODO: Localise this?
        
        cvBackground.addPanel(new PanelTextField<>(new GuiTransform(GuiAlign.MID_CENTER, -50, -32, 150, 16, 0), task.scoreDisp, FieldFilterString.INSTANCE).setCallback(value -> task.scoreDisp = value));
        cvBackground.addPanel(new PanelTextField<>(new GuiTransform(GuiAlign.MID_CENTER, -50, -16, 150, 16, 0), task.scoreName, FieldFilterString.INSTANCE).setCallback(value -> task.scoreName = value));
        
        cvBackground.addPanel(new PanelButtonStorage<ScoreOperation>(new GuiTransform(GuiAlign.MID_CENTER, -100, 0, 50, 16, 0), -1, task.operation.GetText(), task.operation)
        {
            @Override
            public void onButtonClick()
            {
                ScoreOperation[] v = ScoreOperation.values();
                ScoreOperation n = v[(getStoredValue().ordinal() + 1)%v.length];
                this.setStoredValue(n);
                this.setText(n.GetText());
                task.operation = n;
            }
        });
        
        cvBackground.addPanel(new PanelTextField<>(new GuiTransform(GuiAlign.MID_CENTER, -50, 0, 150, 16, 0), "" + task.target, FieldFilterNumber.INT).setCallback(value -> task.target = value));
        
        final GuiScreen screenRef = this;
        cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.MID_CENTER, -100, 16, 200, 16, 0), -1, QuestTranslation.translate("betterquesting.btn.advanced"))
        {
            @Override
            public void onButtonClick()
            {
                mc.displayGuiScreen(QuestingAPI.getAPI(ApiReference.THEME_REG).getGui(PresetGUIs.EDIT_NBT, new GArgsNBT<>(screenRef, task.writeToNBT(new NBTTagCompound()), task::readFromNBT, null)));
            }
        });
        
        cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 200, 16, 0), -1, QuestTranslation.translate("gui.back"))
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

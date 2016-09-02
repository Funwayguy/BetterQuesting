package betterquesting.client.gui.editors;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.client.gui.INeedsRefresh;
import betterquesting.api.client.gui.IVolatileScreen;
import betterquesting.api.client.gui.premade.controls.GuiButtonThemed;
import betterquesting.api.client.gui.premade.screens.GuiScreenThemed;
import betterquesting.api.enums.EnumLogic;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.enums.EnumQuestVisibility;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.PacketTypeNative;
import betterquesting.api.network.PreparedPayload;
import betterquesting.api.quests.IQuestContainer;
import betterquesting.api.quests.properties.QuestProperties;
import betterquesting.api.utils.NBTConverter;
import betterquesting.client.gui.editors.json.GuiJsonObject;
import betterquesting.client.gui.misc.GuiBigTextField;
import betterquesting.client.gui.misc.ITextEditor;
import betterquesting.network.PacketSender;
import betterquesting.quests.QuestDatabase;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiQuestEditor extends GuiScreenThemed implements ITextEditor, IVolatileScreen, INeedsRefresh
{
	private JsonObject lastEdit;
	private int id = -1;
	private IQuestContainer quest;
	
	private GuiTextField titleField;
	private GuiBigTextField descField;
	
	private GuiButtonThemed btnMain;
	private GuiButtonThemed btnLogic;
	private GuiButtonThemed btnVis;
	
	public GuiQuestEditor(GuiScreen parent, IQuestContainer quest)
	{
		super(parent, I18n.format("betterquesting.title.edit_quest", I18n.format(quest.getUnlocalisedName())));
		this.quest = quest;
		this.id = QuestDatabase.INSTANCE.getKey(quest);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void initGui()
	{
		super.initGui();
		
		this.setTitle(I18n.format("betterquesting.title.edit_quest", I18n.format(quest.getUnlocalisedName())));
		
		if(lastEdit != null)
		{
			JsonObject prog = new JsonObject();
			quest.writeToJson(prog, EnumSaveType.PROGRESS);
			quest.readFromJson(lastEdit, EnumSaveType.CONFIG);
			quest.readFromJson(prog, EnumSaveType.PROGRESS);
			lastEdit = null;
			SendChanges();
		}
		
		titleField = new GuiTextField(this.fontRendererObj, width/2 - 99, height/2 - 68 + 1, 198, 18);
		titleField.setMaxStringLength(Integer.MAX_VALUE);
		titleField.setText(quest.getUnlocalisedName());
		
		descField = new GuiBigTextField(this.fontRendererObj, width/2 - 99, height/2 - 28 + 1, 198, 18).enableBigEdit(this, 0);
		descField.setMaxStringLength(Integer.MAX_VALUE);
		descField.setText(quest.getUnlocalisedDescription());
		
		GuiButtonThemed btn = new GuiButtonThemed(1, width/2, height/2 + 28, 100, 20, I18n.format("betterquesting.btn.rewards"), true);
		this.buttonList.add(btn);
		btn = new GuiButtonThemed(2, width/2 - 100, height/2 + 28, 100, 20, I18n.format("betterquesting.btn.tasks"), true);
		this.buttonList.add(btn);
		btn = new GuiButtonThemed(3, width/2 - 100, height/2 + 48, 100, 20, I18n.format("betterquesting.btn.requirements"), true);
		this.buttonList.add(btn);
		btn = new GuiButtonThemed(4, width/2, height/2 + 68, 100, 20, I18n.format("betterquesting.btn.advanced"), true);
		this.buttonList.add(btn);
		
		btnMain = new GuiButtonThemed(5, width/2 - 100, height/2 + 8, 200, 20, I18n.format("betterquesting.btn.is_main") + ": " + quest.getInfo().getProperty(QuestProperties.MAIN), true);
		this.buttonList.add(btnMain);
		btnLogic = new GuiButtonThemed(6, width/2, height/2 + 48, 100, 20, I18n.format("betterquesting.btn.logic") + ": " + quest.getInfo().getProperty(QuestProperties.LOGIC_QUEST), true);
		this.buttonList.add(btnLogic);
		btnVis = new GuiButtonThemed(7, width/2 - 100, height/2 + 68, 100, 20, I18n.format("betterquesting.btn.show") + ": " + quest.getInfo().getProperty(QuestProperties.VISIBILITY), true);
		this.buttonList.add(btnVis);
	}
	
	@Override
	public void refreshGui()
	{
		this.quest = QuestDatabase.INSTANCE.getValue(id);
		
		if(quest == null)
		{
			mc.displayGuiScreen(parent);
			return;
		}
		
		lastEdit = null;
		
		titleField.setText(quest.getUnlocalisedName());
		descField.setText(quest.getUnlocalisedDescription());
		
		btnMain.displayString = I18n.format("betterquesting.btn.is_main") + ": " + quest.getInfo().getProperty(QuestProperties.MAIN);
		btnLogic.displayString = I18n.format("betterquesting.btn.logic") + ": " + quest.getInfo().getProperty(QuestProperties.LOGIC_QUEST);
		btnVis.displayString = I18n.format("betterquesting.btn.show") + ": " + quest.getInfo().getProperty(QuestProperties.VISIBILITY);
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		titleField.drawTextBox();
		descField.drawTextBox(mx, my, partialTick);

		mc.fontRenderer.drawString(I18n.format("betterquesting.gui.name"), width/2 - 100, height/2 - 80, getTextColor(), false);
		mc.fontRenderer.drawString(I18n.format("betterquesting.gui.description"), width/2 - 100, height/2 - 40, getTextColor(), false);
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id == 1) // Rewards
		{
			mc.displayGuiScreen(new GuiRewardEditor(this, quest));
		} else if(button.id == 2) // Tasks
		{
			mc.displayGuiScreen(new GuiTaskEditor(this, quest));
		} else if(button.id == 3) // Prerequisites
		{
			mc.displayGuiScreen(new GuiPrerequisiteEditor(this, quest));
		} else if(button.id == 4) // Raw JSON
		{
			this.lastEdit = new JsonObject();
			quest.writeToJson(lastEdit, EnumSaveType.CONFIG);
			mc.displayGuiScreen(new GuiJsonObject(this, lastEdit));
		} else if(button.id == 5)
		{
			boolean main = !quest.getInfo().getProperty(QuestProperties.MAIN);
			quest.getInfo().setProperty(QuestProperties.MAIN, main);
			button.displayString = I18n.format("betterquesting.btn.is_main") + ": " + main;
			SendChanges();
		} else if(button.id == 6)
		{
			EnumLogic[] logicList = EnumLogic.values();
			EnumLogic logic = quest.getInfo().getProperty(QuestProperties.LOGIC_QUEST);
			logic = logicList[(logic.ordinal() + 1)%logicList.length];
			quest.getInfo().setProperty(QuestProperties.LOGIC_QUEST, logic);
			button.displayString = I18n.format("betterquesting.btn.logic") + ": " + logic;
			SendChanges();
		} else if(button.id == 7)
		{
			EnumQuestVisibility[] visList = EnumQuestVisibility.values();
			EnumQuestVisibility vis = quest.getInfo().getProperty(QuestProperties.VISIBILITY);
			vis = visList[(vis.ordinal() + 1)%visList.length];
			quest.getInfo().setProperty(QuestProperties.VISIBILITY, vis);
			button.displayString =  I18n.format("betterquesting.btn.show") + ": " + vis;
			SendChanges();
		}
	}

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
	@Override
    protected void keyTyped(char character, int keyCode)
    {
        super.keyTyped(character, keyCode);
        
        titleField.textboxKeyTyped(character, keyCode);
        descField.textboxKeyTyped(character, keyCode);
    }
	
    /**
     * Called when the mouse is clicked.
     */
	@Override
    protected void mouseClicked(int mx, int my, int click)
    {
		super.mouseClicked(mx, my, click);
		
		titleField.mouseClicked(mx, my, click);
		descField.mouseClicked(mx, my, click);
		
		boolean flag = false; // Just in case measure to prevent multiple update calls
		
		if(!titleField.isFocused() && !titleField.getText().equals(quest.getUnlocalisedName()))
		{
			// Apply changes, this way is automatic and doesn't require pressing Enter
			quest.getInfo().setProperty(QuestProperties.NAME, titleField.getText());
			flag = true;
		}
		
		if(!descField.isFocused() && !descField.getText().equals(quest.getUnlocalisedDescription()))
		{
			// Apply changes, this way is automatic and doesn't require pressing Enter
			quest.getInfo().setProperty(QuestProperties.DESC, descField.getText());
			flag = true;
		}
		
		if(flag)
		{
			SendChanges();
		}
    }
	
	// If the changes are approved by the server, it will be broadcast to all players including the editor
	public void SendChanges()
	{
		JsonObject base = new JsonObject();
		base.add("config", quest.writeToJson(new JsonObject(), EnumSaveType.CONFIG));
		base.add("progress", quest.writeToJson(new JsonObject(), EnumSaveType.PROGRESS));
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("action", EnumPacketAction.EDIT.ordinal()); // Action: Update data
		tags.setInteger("questID", id);
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		PacketSender.INSTANCE.sendToServer(new PreparedPayload(PacketTypeNative.QUEST_EDIT.GetLocation(), tags));
	}

	@Override
	public void setText(int id, String text)
	{
		if(id == 0)
		{
			if(descField != null)
			{
				descField.setText(text);
			}
			
			quest.getInfo().setProperty(QuestProperties.DESC, text);
			SendChanges();
		}
	}
}

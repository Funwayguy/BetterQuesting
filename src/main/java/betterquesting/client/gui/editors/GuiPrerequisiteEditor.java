package betterquesting.client.gui.editors;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiBigTextField;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.lists.GuiScrollingButtons;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuest;
import betterquesting.api.utils.NBTConverter;
import betterquesting.api.utils.RenderUtils;
import betterquesting.client.gui.GuiQuestInstance;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiPrerequisiteEditor extends GuiScreenThemed implements IVolatileScreen, INeedsRefresh
{
	private int questID = -1;
	private IQuest quest;
	
	private GuiBigTextField searchBox;
	private List<Integer> searchResults = new ArrayList<Integer>();
	
	private GuiScrollingButtons dbBtnList;
	private GuiScrollingButtons prBtnList;
	
	public GuiPrerequisiteEditor(GuiScreen parent, IQuest quest)
	{
		super(parent, "betterquesting.title.pre_requisites");
		this.quest = quest;
		this.questID = QuestDatabase.INSTANCE.getKey(quest);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void initGui()
	{
		super.initGui();
		
		if(quest == null)
		{
			mc.displayGuiScreen(parent);
		}
		
		int btnWidth = sizeX/2 - 16;
		int sx = sizeX - 32;
		
		this.searchBox = new GuiBigTextField(mc.fontRenderer, guiLeft + sizeX/2 + 8, guiTop + 48, btnWidth - 16, 20);
		this.searchBox.setWatermark(I18n.format("betterquesting.gui.search"));
		this.buttonList.add(new GuiButtonThemed(1, guiLeft + 16 + sx/4*3 - 50, guiTop + sizeY - 48, 100, 20, I18n.format("betterquesting.btn.new"), true));
		
		prBtnList = new GuiScrollingButtons(mc, guiLeft + 16, guiTop + 48, btnWidth - 8, sizeY - 96);
		dbBtnList = new GuiScrollingButtons(mc, guiLeft + sizeX/2 + 8, guiTop + 68, btnWidth - 8, sizeY - 116);
		this.embedded.add(prBtnList);
		this.embedded.add(dbBtnList);
		
		RefreshSearch();
		RefreshColumns();
	}
	
	@Override
	public void refreshGui()
	{
		IQuest tmp = QuestDatabase.INSTANCE.getValue(questID);
		
		if(tmp == null)
		{
			mc.displayGuiScreen(parent);
			return;
		}
		
		quest = tmp;
    	
		RefreshSearch();
		RefreshColumns();
	}
	
	@Override
	public void drawBackPanel(int mx, int my, float partialTick)
	{
		super.drawBackPanel(mx, my, partialTick);
		
		RenderUtils.DrawLine(width/2, guiTop + 32, width/2, guiTop + sizeY - 32, 2F, getTextColor());
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		GL11.glColor4f(1F, 1F, 1F, 1F);
		
		int sx = sizeX - 32;
		String txt = I18n.format(quest == null? "ERROR" : quest.getUnlocalisedName());
		mc.fontRenderer.drawString(txt, guiLeft + 16 + sx/4 - mc.fontRenderer.getStringWidth(txt)/2, guiTop + 32, getTextColor(), false);
		txt = I18n.format("betterquesting.gui.database");
		mc.fontRenderer.drawString(txt, guiLeft + 16 + sx/4*3 - mc.fontRenderer.getStringWidth(txt)/2, guiTop + 32, getTextColor(), false);
		
		GL11.glColor4f(1F, 1F, 1F, 1F);
		
		searchBox.drawTextBox(mx, my, partialTick);
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id == 1)
		{
			createQuest();
		} else if(button.id > 1)
		{
			int column = button.id&7;
			int id = (button.id >> 3) - 2;
			IQuest q = QuestDatabase.INSTANCE.getValue(id);
			
			if(id < 0 || q == null)
			{
				return; // Invalid quest ID
			} else if(column == 0 || column == 3) // Edit quest
			{
				mc.displayGuiScreen(new GuiQuestInstance(this, q));
			} else if(column == 1) // Remove quest
			{
				quest.getPrerequisites().remove(q);
				SendChanges();
			} else if(column == 4) // Delete quest
			{
				NBTTagCompound tags = new NBTTagCompound();
				tags.setInteger("action", EnumPacketAction.REMOVE.ordinal()); // Delete quest
				tags.setInteger("questID", id);
				PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tags));
			} else if(column == 2) // Add quest
			{
				quest.getPrerequisites().add(q);
				SendChanges();
			}
		}
	}
	
	public void createQuest()
	{
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("action", EnumPacketAction.ADD.ordinal());
		PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tag));
	}
	
	public void SendChanges()
	{
		NBTTagCompound tags = new NBTTagCompound();
		JsonObject base = new JsonObject();
		base.add("config", quest.writeToJson(new JsonObject(), EnumSaveType.CONFIG));
		base.add("progress", quest.writeToJson(new JsonObject(), EnumSaveType.PROGRESS));
		tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		tags.setInteger("questID", QuestDatabase.INSTANCE.getKey(quest));
		tags.setInteger("action", EnumPacketAction.EDIT.ordinal());
		PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tags));
	}
	
	public void RefreshColumns()
	{
		prBtnList.getEntryList().clear();
		
		for(IQuest prq : quest.getPrerequisites())
		{
			int qID = QuestDatabase.INSTANCE.getKey(prq);
			int btnWidth = prBtnList.getListWidth();
			int bID = (2 + qID) << 3; // First 3 bits reserved for column index
			
			GuiButtonThemed btn1 = new GuiButtonThemed(bID + 0, 0,0, btnWidth - 20, 20, I18n.format(prq.getUnlocalisedName()));
			GuiButtonThemed btn2 = new GuiButtonThemed(bID + 1, 0,0, 20, 20, EnumChatFormatting.YELLOW + ">");
			
			prBtnList.addButtonRow(btn1, btn2);
		}
    	
		dbBtnList.getEntryList().clear();
		
		for(int qID : searchResults)
		{
			IQuest dbQ = QuestDatabase.INSTANCE.getValue(qID);
			
			if(dbQ == null)
			{
				continue;
			}
			
			int bWidth = dbBtnList.getListWidth();
			int bID = (2 + qID) << 3; // First 3 bits reserved for column index
			GuiButtonThemed btn3 = new GuiButtonThemed(bID + 2, 0, 0, 20, 20, EnumChatFormatting.GREEN + "<");
			btn3.enabled = dbQ != null && quest != dbQ && !quest.getPrerequisites().contains(dbQ);
			GuiButtonThemed btn4 = new GuiButtonThemed(bID + 3, 0, 0, bWidth - 40, 20, I18n.format(dbQ.getUnlocalisedName()));
			GuiButtonThemed btn5 = new GuiButtonThemed(bID + 4, 0, 0, 20, 20, "" + EnumChatFormatting.BOLD + EnumChatFormatting.RED + "x");
			
			dbBtnList.addButtonRow(btn3, btn4, btn5);
		}
	}
	
    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
	@Override
    protected void keyTyped(char character, int num)
    {
		super.keyTyped(character, num);
		String prevTxt = searchBox.getText();
		
		searchBox.textboxKeyTyped(character, num);
		
		if(!searchBox.getText().equalsIgnoreCase(prevTxt))
		{
			RefreshSearch();
			RefreshColumns();
		}
    }
	
	public void RefreshSearch()
	{
		searchResults.clear();
		String query = searchBox.getText().toLowerCase();
		
		for(int id : QuestDatabase.INSTANCE.getAllKeys())
		{
			IQuest q = QuestDatabase.INSTANCE.getValue(id);
			
			if(query.length() <= 0 || q.getUnlocalisedName().toLowerCase().contains(query) || I18n.format(q.getUnlocalisedName()).toLowerCase().contains(query) || query.equalsIgnoreCase("" + id))
			{
				searchResults.add(id);
			}
		}
	}
	
	@Override
	public void mouseClicked(int mx, int my, int type)
	{
		super.mouseClicked(mx, my, type);
		this.searchBox.mouseClicked(mx, my, type);
		
		if(type != 0)
		{
			return;
		}
		
		GuiButtonThemed btn1 = prBtnList.getButtonUnderMouse(mx, my);
		
		if(btn1 != null && btn1.mousePressed(mc, mx, my))
		{
			btn1.func_146113_a(mc.getSoundHandler());
			this.actionPerformed(btn1);
			return;
		}
		
		GuiButtonThemed btn2 = dbBtnList.getButtonUnderMouse(mx, my);
		
		if(btn2 != null && btn2.mousePressed(mc, mx, my))
		{
			btn2.func_146113_a(mc.getSoundHandler());
			this.actionPerformed(btn2);
			return;
		}
	}
}

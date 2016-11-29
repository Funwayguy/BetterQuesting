package betterquesting.client.gui.editors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineEntry;
import betterquesting.api.utils.NBTConverter;
import betterquesting.api.utils.RenderUtils;
import betterquesting.client.gui.GuiQuestInstance;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import betterquesting.questing.QuestLineEntry;
import com.google.gson.JsonObject;

@SideOnly(Side.CLIENT)
public class GuiQuestLineEditorB extends GuiScreenThemed implements IVolatileScreen, INeedsRefresh
{
	private int lineID = -1;
	private IQuestLine line;
	
	GuiBigTextField searchBox;
	List<Integer> searchResults = new ArrayList<Integer>();
	List<Integer> lineQuests = new ArrayList<Integer>();
	
	private GuiScrollingButtons dbBtnList;
	private GuiScrollingButtons qlBtnList;
	
	public GuiQuestLineEditorB(GuiScreen parent, IQuestLine line)
	{
		super(parent, I18n.format("betterquesting.title.edit_line2", line == null? "?" : I18n.format(line.getUnlocalisedName())));
		this.line = line;
		this.lineID = QuestLineDatabase.INSTANCE.getKey(line);
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
		int btnWidth = sizeX/2 - 16;
		int sx = sizeX - 32;
		
		this.searchBox = new GuiBigTextField(mc.fontRendererObj, guiLeft + sizeX/2 + 9, guiTop + 49, btnWidth - 18, 18);
		this.searchBox.setWatermark(I18n.format("betterquesting.gui.search"));
		this.buttonList.add(new GuiButtonThemed(1, guiLeft + 16 + sx/4*3 - 50, guiTop + sizeY - 48, 100, 20, I18n.format("betterquesting.btn.new"), true));
		
		qlBtnList = new GuiScrollingButtons(mc, guiLeft + 16, guiTop + 48, btnWidth - 8, sizeY - 96);
		dbBtnList = new GuiScrollingButtons(mc, guiLeft + sizeX/2 + 8, guiTop + 68, btnWidth - 8, sizeY - 116);
		this.embedded.add(qlBtnList);
		this.embedded.add(dbBtnList);
		
		RefreshSearch();
		RefreshColumns();
	}
	
	@Override
	public void refreshGui()
	{
		this.line = QuestLineDatabase.INSTANCE.getValue(lineID);
		
		if(lineID >= 0 && line == null)
		{
			mc.displayGuiScreen(parent);
			return;
		}
		
		setTitle(I18n.format("betterquesting.title.edit_line2", line == null? "?" : I18n.format(line.getUnlocalisedName())));
		
		RefreshSearch();
		RefreshColumns();
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		GlStateManager.color(1F, 1F, 1F, 1F);
		mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
		
		RenderUtils.DrawLine(width/2, guiTop + 32, width/2, guiTop + sizeY - 32, 2F, getTextColor());
		
		int sx = sizeX - 32;
		String txt = I18n.format("betterquesting.gui.quest_line");
		mc.fontRendererObj.drawString(txt, guiLeft + 16 + sx/4 - mc.fontRendererObj.getStringWidth(txt)/2, guiTop + 32, getTextColor(), false);
		txt = I18n.format("betterquesting.gui.database");
		mc.fontRendererObj.drawString(txt, guiLeft + 16 + sx/4*3 - mc.fontRendererObj.getStringWidth(txt)/2, guiTop + 32, getTextColor(), false);
		
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
			int column = button.id&7; // Line listing (0 = quest, 1 = quest delete, 2 = registry)
			int id = (button.id >> 3) - 2;
			
			if(column == 0 || column == 3) // Edit quest
			{
				if(id >= 0)
				{
					IQuest q = QuestDatabase.INSTANCE.getValue(id);
					
					if(q != null)
					{
						mc.displayGuiScreen(new GuiQuestInstance(this, q));
					}
				}
			} else if(column == 1 && line != null) // Remove quest
			{
				line.removeKey(id);
				//RefreshColumns();
				SendChanges(EnumPacketAction.EDIT, lineID);
			} else if(column == 4 && id >= 0) // Delete quest
			{
				NBTTagCompound tags = new NBTTagCompound();
				tags.setInteger("action", EnumPacketAction.REMOVE.ordinal()); // Delete quest
				tags.setInteger("questID", id);
				PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tags));
			} else if(column == 2 && line != null && id >= 0) // Add quest
			{
				IQuestLineEntry qe = new QuestLineEntry(0, 0);
				int x1 = 0;
				int y1 = 0;
				
				topLoop:
				while(true)
				{
					for(IQuestLineEntry qe2 : line.getAllValues())
					{
						int x2 = qe2.getPosX();
						int y2 = qe2.getPosY();
						int s2 = qe2.getSize();
						
						if(x1 >= x2 && x1 < x2 + s2 && y1 >= y2 && y1 < y2 + s2)
						{
							x1 += s2;
							x2 += s2;
							continue topLoop; // We're in the way, move over and try again
						}
					}
					
					break;
				}
				
				qe.setPosition(x1, y1);
				line.add(qe, id);
				RefreshColumns();
				SendChanges(EnumPacketAction.EDIT, lineID);
			}
		}
	}
	
	@Override
	public void mouseScroll(int mx, int my, int scroll)
	{
		super.mouseScroll(mx, my, scroll);
	}
	
	public void createQuest()
	{
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("action", EnumPacketAction.ADD.ordinal());
		PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.QUEST_EDIT.GetLocation(), tag));
	}
	
	public void SendChanges(EnumPacketAction action, int lineID)
	{
		if(action == null)
		{
			return;
		}
		
		NBTTagCompound tags = new NBTTagCompound();
		
		if(action == EnumPacketAction.EDIT && line != null)
		{
			JsonObject base = new JsonObject();
			base.add("line", line.writeToJson(new JsonObject(), EnumSaveType.CONFIG));
			tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		}
		
		tags.setInteger("action", action.ordinal());
		tags.setInteger("lineID", QuestLineDatabase.INSTANCE.getKey(line));
		
		PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.LINE_EDIT.GetLocation(), tags));
	}
	
	public void RefreshColumns()
	{
		if(line == null)
		{
			lineQuests.clear();
		} else
		{
			lineQuests = line.getAllKeys();
		}
    	
		qlBtnList.getEntryList().clear();
		
		for(int qID : lineQuests)
		{
			IQuest quest = QuestDatabase.INSTANCE.getValue(qID);
			
			if(quest == null)
			{
				continue;
			}
			
			int bWidth = qlBtnList.getListWidth();
			int bID = (2 + qID) << 3; // First 3 bits reserved for column index
			GuiButtonThemed btn1 = new GuiButtonThemed(bID + 0, 0, 0, bWidth - 20, 20, I18n.format(quest.getUnlocalisedName()));
			GuiButtonThemed btn2 = new GuiButtonThemed(bID + 1, 0, 0, 20, 20, TextFormatting.YELLOW + ">");
			
			qlBtnList.addButtonRow(btn1, btn2);
		}
    	
		dbBtnList.getEntryList().clear();
		
		for(int qID : searchResults)
		{
			IQuest quest = QuestDatabase.INSTANCE.getValue(qID);
			
			if(quest == null)
			{
				continue;
			}
			
			int bWidth = dbBtnList.getListWidth();
			int bID = (2 + qID) << 3; // First 3 bits reserved for column index
			GuiButtonThemed btn3 = new GuiButtonThemed(bID + 2, 0, 0, 20, 20, TextFormatting.GREEN + "<");
			btn3.enabled = line != null && !lineQuests.contains(qID);
			GuiButtonThemed btn4 = new GuiButtonThemed(bID + 3, 0, 0, bWidth - 40, 20, I18n.format(quest.getUnlocalisedName()));
			GuiButtonThemed btn5 = new GuiButtonThemed(bID + 4, 0, 0, 20, 20, "" + TextFormatting.BOLD + TextFormatting.RED + "x");
			
			dbBtnList.addButtonRow(btn3, btn4, btn5);
		}
	}
	
    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
	@Override
    protected void keyTyped(char character, int num) throws IOException
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
		searchResults = new ArrayList<Integer>();
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
	public void mouseClicked(int mx, int my, int type) throws IOException
	{
		super.mouseClicked(mx, my, type);
		this.searchBox.mouseClicked(mx, my, type);
		
		if(type != 0)
		{
			return;
		}
		
		GuiButtonThemed btn1 = qlBtnList.getButtonUnderMouse(mx, my);
		
		if(btn1 != null)
		{
			btn1.playPressSound(mc.getSoundHandler());
			this.actionPerformed(btn1);
			return;
		}
		
		GuiButtonThemed btn2 = dbBtnList.getButtonUnderMouse(mx, my);
		
		if(btn2 != null)
		{
			btn2.playPressSound(mc.getSoundHandler());
			this.actionPerformed(btn2);
			return;
		}
	}
}

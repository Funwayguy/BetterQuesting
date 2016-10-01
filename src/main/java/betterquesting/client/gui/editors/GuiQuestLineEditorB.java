package betterquesting.client.gui.editors;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiBigTextField;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.network.PacketTypeNative;
import betterquesting.api.network.PreparedPayload;
import betterquesting.api.quests.IQuest;
import betterquesting.api.quests.IQuestLine;
import betterquesting.api.quests.IQuestLineEntry;
import betterquesting.api.utils.NBTConverter;
import betterquesting.api.utils.RenderUtils;
import betterquesting.client.gui.GuiQuestInstance;
import betterquesting.network.PacketSender;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestLineDatabase;
import betterquesting.quests.QuestLineEntry;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiQuestLineEditorB extends GuiScreenThemed implements IVolatileScreen, INeedsRefresh
{
	int lineID = -1;
	IQuestLine line;
	int leftScroll = 0;
	int rightScroll = 0;
	int maxRowsL = 0;
	int maxRowsR = 0;
	GuiBigTextField searchBox;
	List<Integer> searchResults = new ArrayList<Integer>();
	List<Integer> lineQuests = new ArrayList<Integer>();
	
	public GuiQuestLineEditorB(GuiScreen parent, IQuestLine line)
	{
		super(parent, I18n.format("betterquesting.title.edit_line2", line == null? "?" : I18n.format(line.getUnlocalisedName())));
		this.line = line;
		this.lineID = QuestLineDatabase.INSTANCE.getKey(line);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void initGui()
	{
		super.initGui();
		
		maxRowsL = (sizeY - 96)/20;
		maxRowsR = (sizeY - 116)/20;
		int btnWidth = sizeX/2 - 16;
		int sx = sizeX - 32;
		
		this.searchBox = new GuiBigTextField(mc.fontRenderer, guiLeft + sizeX/2 + 8, guiTop + 48, btnWidth - 16, 20);
		this.searchBox.setWatermark(I18n.format("betterquesting.gui.search"));
		this.buttonList.add(new GuiButtonThemed(1, guiLeft + 16 + sx/4*3 - 50, guiTop + sizeY - 48, 100, 20, I18n.format("betterquesting.btn.new"), true));
		
		// Left main buttons
		for(int i = 0; i < maxRowsL; i++)
		{
			GuiButtonThemed btn = new GuiButtonThemed(this.buttonList.size(), guiLeft + 16, guiTop + 48 + (i*20), btnWidth - 36, 20, "NULL", true);
			this.buttonList.add(btn);
		}
		
		// Left delete buttons
		for(int i = 0; i < maxRowsL; i++)
		{
			GuiButtonThemed btn = new GuiButtonThemed(this.buttonList.size(), guiLeft + 16 + btnWidth - 36, guiTop + 48 + (i*20), 20, 20, "" + EnumChatFormatting.YELLOW + EnumChatFormatting.BOLD + ">", true);
			this.buttonList.add(btn);
		}
		
		// Right main buttons
		for(int i = 0; i < maxRowsR; i++)
		{
			GuiButtonThemed btn = new GuiButtonThemed(this.buttonList.size(), guiLeft + sizeX/2 + 28, guiTop + 68 + (i*20), btnWidth - 56, 20, "NULL", true);
			this.buttonList.add(btn);
		}
		
		// Right delete buttons
		for(int i = 0; i < maxRowsR; i++)
		{
			GuiButtonThemed btn = new GuiButtonThemed(this.buttonList.size(), guiLeft + sizeX/2 + 28 + btnWidth - 56, guiTop + 68 + (i*20), 20, 20, "" + EnumChatFormatting.RED + EnumChatFormatting.BOLD + "x", true);
			this.buttonList.add(btn);
		}
		
		// Right add buttons
		for(int i = 0; i < maxRowsR; i++)
		{
			GuiButtonThemed btn = new GuiButtonThemed(this.buttonList.size(), guiLeft + sizeX/2 + 8, guiTop + 68 + (i*20), 20, 20, "" + EnumChatFormatting.GREEN + EnumChatFormatting.BOLD + "<", true);
			this.buttonList.add(btn);
		}
		
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
		
		GL11.glColor4f(1F, 1F, 1F, 1F);
		mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
		
		// Left scroll bar
		this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 48, 248, 0, 8, 20);
		int s = 20;
		while(s < (maxRowsL - 1) * 20)
		{
			this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 48 + s, 248, 20, 8, 20);
			s += 20;
		}
		this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 48 + s, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 48 + (int)Math.max(0, s * (float)leftScroll/(line == null? 1 : lineQuests.size() - maxRowsL)), 248, 60, 8, 20);
		
		// Right scroll bar
		this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 68, 248, 0, 8, 20);
		s = 20;
		while(s < (maxRowsR - 1) * 20)
		{
			this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 68 + s, 248, 20, 8, 20);
			s += 20;
		}
		
		this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 68 + s, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 68 + (int)Math.max(0, s * (float)rightScroll/(searchResults.size() - maxRowsL)), 248, 60, 8, 20);
		
		RenderUtils.DrawLine(width/2, guiTop + 32, width/2, guiTop + sizeY - 32, 2F, getTextColor());
		
		int sx = sizeX - 32;
		String txt = I18n.format("betterquesting.gui.quest_line");
		mc.fontRenderer.drawString(txt, guiLeft + 16 + sx/4 - mc.fontRenderer.getStringWidth(txt)/2, guiTop + 32, getTextColor(), false);
		txt = I18n.format("betterquesting.gui.database");
		mc.fontRenderer.drawString(txt, guiLeft + 16 + sx/4*3 - mc.fontRenderer.getStringWidth(txt)/2, guiTop + 32, getTextColor(), false);
		
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
			int n1 = button.id - 2; // Line index
			int n2 = n1/maxRowsL; // Line listing (0 = quest, 1 = quest delete, 2 = registry)
			int n3 = n1%maxRowsL + leftScroll; // Quest list index
			int n4 = n1%maxRowsL + rightScroll; // Registry list index
			
			if(n2 >= 2) // Right list needs some modifications to work properly
			{
				n1 -= maxRowsL*2;
				n2 = 2 + n1/maxRowsR;
				n4 = n1%maxRowsR + rightScroll;
			}
			
			if(n2 == 0) // Edit quest
			{
				if(line != null && n3 >= 0 && n3 < lineQuests.size())
				{
					IQuest q = QuestDatabase.INSTANCE.getValue(searchResults.get(n3));
					
					if(q != null)
					{
						mc.displayGuiScreen(new GuiQuestInstance(this, q));
					}
				}
			} else if(n2 == 1) // Remove quest
			{
				if(!(line == null || n3 < 0 || n3 >= lineQuests.size()))
				{
					line.removeKey(lineQuests.get(n3));
					RefreshColumns();
				}
			} else if(n2 == 2) // Edit quest
			{
				if(!(n4 < 0 || n4 >= searchResults.size()))
				{
					IQuest q = QuestDatabase.INSTANCE.getValue(searchResults.get(n4));
					
					if(q != null)
					{
						mc.displayGuiScreen(new GuiQuestInstance(this, q));
					}
				}
			} else if(n2 == 3) // Delete quest
			{
				if(!(n4 < 0 || n4 >= searchResults.size()))
				{
					NBTTagCompound tags = new NBTTagCompound();
					tags.setInteger("action", 1); // Delete quest
					tags.setInteger("questID", searchResults.get(n4));
					PacketSender.INSTANCE.sendToServer(new PreparedPayload(PacketTypeNative.QUEST_EDIT.GetLocation(), tags));
				}
			} else if(n2 == 4) // Add quest
			{
				if(!(line == null || n4 < 0 || n4 >= searchResults.size()))
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
					line.add(qe, searchResults.get(n4));
					RefreshColumns();
				}
			}
		}
	}
	
	@Override
	public void mouseScroll(int mx, int my, int scroll)
	{
		super.mouseScroll(mx, my, scroll);
        
        if(scroll != 0 && isWithin(mx, my, this.guiLeft, this.guiTop, sizeX/2, sizeY))
        {
    		leftScroll = line == null? 0 : Math.max(0, MathHelper.clamp_int(leftScroll + scroll, 0, line.size() - maxRowsL));
    		RefreshColumns();
        }
        
        if(scroll != 0 && isWithin(mx, my, this.guiLeft + sizeX/2, this.guiTop, sizeX/2, sizeY))
        {
        	rightScroll = Math.max(0, MathHelper.clamp_int(rightScroll + scroll, 0, searchResults.size() - maxRowsR));
        	RefreshColumns();
        }
	}
	
	public void createQuest()
	{
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("action", EnumPacketAction.ADD.ordinal());
		PacketSender.INSTANCE.sendToServer(new PreparedPayload(PacketTypeNative.QUEST_EDIT.GetLocation(), tag));
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
			base.add("entries", line.writeToJson(new JsonObject(), EnumSaveType.CONFIG));
			tags.setTag("data", NBTConverter.JSONtoNBT_Object(base, new NBTTagCompound()));
		}
		
		tags.setInteger("action", action.ordinal());
		tags.setInteger("lineID", QuestLineDatabase.INSTANCE.getKey(line));
		
		PacketSender.INSTANCE.sendToServer(new PreparedPayload(PacketTypeNative.LINE_EDIT.GetLocation(), tags));
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
    	
		leftScroll = line == null? 0 : Math.max(0, MathHelper.clamp_int(leftScroll, 0, lineQuests.size() - maxRowsL));
    	rightScroll = Math.max(0, MathHelper.clamp_int(rightScroll, 0, searchResults.size() - maxRowsR));
    	
		@SuppressWarnings("unchecked")
		List<GuiButton> btnList = this.buttonList;
		
		for(int i = 2; i < btnList.size(); i++)
		{
			GuiButton btn = btnList.get(i);
			int n1 = i - 2;
			int n2 = n1/maxRowsL; // Button listing (0 = quest, 1 = quest delete, 2 = registry)
			int n3 = n1%maxRowsL + leftScroll; // Quest list index
			int n4 = n1%maxRowsL + rightScroll; // Registry list index
			
			if(n2 >= 2) // Right list needs some modifications to work properly
			{
				n1 -= maxRowsL*2;
				n2 = 2 + n1/maxRowsR;
				n4 = n1%maxRowsR + rightScroll;
			}
			
			if(n2 == 0) // Edit quest
			{
				if(line == null || n3 < 0 || n3 >= lineQuests.size())
				{
					btn.displayString = "NULL";
					btn.visible = btn.enabled = false;
				} else
				{
					IQuest q = QuestDatabase.INSTANCE.getValue(lineQuests.get(n3));
					btn.visible = btn.enabled = q != null;
					btn.displayString = q == null? "NULL" : I18n.format(q.getUnlocalisedName());
				}
			} else if(n2 == 1) // Remove quest
			{
				btn.visible = btn.enabled = line != null && !(n3 < 0 || n3 >= lineQuests.size());
			} else if(n2 == 2) // Edit quest
			{
				if(n4 < 0 || n4 >= searchResults.size())
				{
					btn.displayString = "NULL";
					btn.visible = btn.enabled = false;
				} else
				{
					IQuest q = QuestDatabase.INSTANCE.getValue(searchResults.get(n4));
					btn.visible = btn.enabled = true;//q != null;
					btn.displayString = q == null? "NULL" : I18n.format(q.getUnlocalisedName());
				}
			} else if(n2 == 3) // Delete quest
			{
				btn.visible = btn.enabled = !(n4 < 0 || n4 >= searchResults.size());
			} else if(n2 == 4) // Add quest
			{
				if(n4 < 0 || n4 >= searchResults.size())
				{
					btn.visible = btn.enabled = false;
				} else
				{
					int id = searchResults.get(n4);
					btn.visible = true;
					btn.enabled = line != null && !lineQuests.contains(id);
				}
			}
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
	public void mouseClicked(int mx, int my, int type)
	{
		super.mouseClicked(mx, my, type);
		this.searchBox.mouseClicked(mx, my, type);
	}
}

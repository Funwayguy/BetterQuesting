package betterquesting.client.gui.editors;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.GuiQuestInstance;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiBigTextField;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.gui.misc.IVolatileScreen;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.network.PacketAssembly;
import betterquesting.network.PacketTypeRegistry.BQPacketType;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.utils.NBTConverter;
import betterquesting.utils.RenderUtils;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiPrerequisiteEditor extends GuiQuesting implements IVolatileScreen
{
	QuestInstance quest;
	int leftScroll = 0;
	int rightScroll = 0;
	int maxRowsL = 0;
	int maxRowsR = 0;
	GuiBigTextField searchBox;
	ArrayList<QuestInstance> searchResults = new ArrayList<QuestInstance>();
	
	public GuiPrerequisiteEditor(GuiScreen parent, QuestInstance quest)
	{
		super(parent, "betterquesting.title.pre_requisites");
		this.quest = quest;
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
		
		maxRowsL = (sizeY - 80)/20;
		maxRowsR = (sizeY - 116)/20;
		int btnWidth = sizeX/2 - 16;
		int sx = sizeX - 32;
		
		this.searchBox = new GuiBigTextField(mc.fontRenderer, guiLeft + sizeX/2 + 8, guiTop + 48, btnWidth - 16, 20);
		this.searchBox.setWatermark(I18n.format("betterquesting.gui.search"));
		this.buttonList.add(new GuiButtonQuesting(1, guiLeft + 16 + sx/4*3 - 50, guiTop + sizeY - 48, 100, 20, I18n.format("betterquesting.btn.new")));
		
		// Left main buttons
		for(int i = 0; i < maxRowsL; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + 16, guiTop + 48 + (i*20), btnWidth - 36, 20, "NULL");
			this.buttonList.add(btn);
		}
		
		// Left delete buttons
		for(int i = 0; i < maxRowsL; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + 16 + btnWidth - 36, guiTop + 48 + (i*20), 20, 20, "" + EnumChatFormatting.YELLOW + EnumChatFormatting.BOLD + ">");
			this.buttonList.add(btn);
		}
		
		// Right main buttons
		for(int i = 0; i < maxRowsR; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft +  + sizeX/2 + 28, guiTop + 68 + (i*20), btnWidth - 56, 20, "NULL");
			this.buttonList.add(btn);
		}
		
		// Right delete buttons
		for(int i = 0; i < maxRowsR; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + sizeX/2 + 28 + btnWidth - 56, guiTop + 68 + (i*20), 20, 20, "" + EnumChatFormatting.RED + EnumChatFormatting.BOLD + "x");
			this.buttonList.add(btn);
		}
		
		// Right add buttons
		for(int i = 0; i < maxRowsR; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + sizeX/2 + 8, guiTop + 68 + (i*20), 20, 20, "" + EnumChatFormatting.GREEN + EnumChatFormatting.BOLD + "<");
			this.buttonList.add(btn);
		}
		
		RefreshSearch();
		RefreshColumns();
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		if(QuestDatabase.updateUI)
		{
			QuestDatabase.updateUI = false;
			RefreshSearch();
			RefreshColumns();
		}
		
		GL11.glColor4f(1F, 1F, 1F, 1F);
		mc.renderEngine.bindTexture(ThemeRegistry.curTheme().guiTexture());
		
		// Left scroll bar
		this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 48, 248, 0, 8, 20);
		int s = 20;
		while(s < (maxRowsL - 1) * 20)
		{
			this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 48 + s, 248, 20, 8, 20);
			s += 20;
		}
		this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 48 + s, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 48 + (int)Math.max(0, s * (float)leftScroll/(quest == null? 1 : quest.preRequisites.size() - maxRowsL)), 248, 60, 8, 20);
		
		// Right scroll bar
		this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 68, 248, 0, 8, 20);
		s = 20;
		while(s < (maxRowsR - 1) * 20)
		{
			this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 68 + s, 248, 20, 8, 20);
			s += 20;
		}
		
		this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 68 + s, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 68 + (int)Math.max(0, s * (float)rightScroll/(searchResults.size() - maxRowsR)), 248, 60, 8, 20);
		
		RenderUtils.DrawLine(width/2, guiTop + 32, width/2, guiTop + sizeY - 32, 2F, ThemeRegistry.curTheme().textColor());
		
		int sx = sizeX - 32;
		String txt = I18n.format(quest == null? "ERROR" : quest.name);
		mc.fontRenderer.drawString(txt, guiLeft + 16 + sx/4 - mc.fontRenderer.getStringWidth(txt)/2, guiTop + 32, ThemeRegistry.curTheme().textColor().getRGB(), false);
		txt = I18n.format("betterquesting.gui.database");
		mc.fontRenderer.drawString(txt, guiLeft + 16 + sx/4*3 - mc.fontRenderer.getStringWidth(txt)/2, guiTop + 32, ThemeRegistry.curTheme().textColor().getRGB(), false);
		
		searchBox.drawTextBox();
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id == 1)
		{
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("action", 1);
			PacketAssembly.SendToServer(BQPacketType.LINE_EDIT.GetLocation(), tags);
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
				if(quest != null && n3 >= 0 && n3 < quest.preRequisites.size())
				{
					mc.displayGuiScreen(new GuiQuestInstance(this, quest.preRequisites.get(n3)));
				}
			} else if(n2 == 1) // Remove quest
			{
				if(!(quest == null || n3 < 0 || n3 >= quest.preRequisites.size()))
				{
					quest.preRequisites.remove(n3);
					SendChanges();
				}
			} else if(n2 == 2) // Edit quest
			{
				if(!(n4 < 0 || n4 >= searchResults.size()))
				{
					mc.displayGuiScreen(new GuiQuestInstance(this, searchResults.get(n4)));
				}
			} else if(n2 == 3) // Delete quest
			{
				if(!(n4 < 0 || n4 >= searchResults.size()))
				{
					NBTTagCompound tags = new NBTTagCompound();
					tags.setInteger("action", 1); // Delete quest
					tags.setInteger("questID", searchResults.get(n4).questID);
					PacketAssembly.SendToServer(BQPacketType.QUEST_EDIT.GetLocation(), tags);
				}
			} else if(n2 == 4) // Add quest
			{
				if(!(quest == null || n4 < 0 || n4 >= searchResults.size()))
				{
					quest.preRequisites.add(searchResults.get(n4));
					SendChanges();
				}
			}
		}
	}
	
    /**
     * Handles mouse input.
     */
	@Override
    public void handleMouseInput()
    {
		super.handleMouseInput();
		
        int mx = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int my = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int SDX = (int)-Math.signum(Mouse.getEventDWheel());
        
        if(SDX != 0 && isWithin(mx, my, this.guiLeft, this.guiTop, sizeX/2, sizeY))
        {
    		leftScroll = quest == null? 0 : Math.max(0, MathHelper.clamp_int(leftScroll + SDX, 0, quest.preRequisites.size() - maxRowsL));
    		RefreshColumns();
        }
        
        if(SDX != 0 && isWithin(mx, my, this.guiLeft + sizeX/2, this.guiTop, sizeX/2, sizeY))
        {
        	rightScroll = Math.max(0, MathHelper.clamp_int(rightScroll + SDX, 0, searchResults.size() - maxRowsR));
        	RefreshColumns();
        }
    }
	
	public void SendChanges()
	{
		JsonObject json1 = new JsonObject();
		quest.writeToJSON(json1);
		JsonObject json2 = new JsonObject();
		quest.writeProgressToJSON(json2);
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("action", 0); // Action: Update data
		tags.setInteger("questID", quest.questID);
		tags.setTag("Data", NBTConverter.JSONtoNBT_Object(json1, new NBTTagCompound()));
		tags.setTag("Progress", NBTConverter.JSONtoNBT_Object(json2, new NBTTagCompound()));
		PacketAssembly.SendToServer(BQPacketType.QUEST_EDIT.GetLocation(), tags);
	}
	
	public void RefreshColumns()
	{
		leftScroll = quest == null? 0 : Math.max(0, MathHelper.clamp_int(leftScroll, 0, quest.preRequisites.size() - maxRowsL));
    	rightScroll = Math.max(0, MathHelper.clamp_int(rightScroll, 0, searchResults.size() - maxRowsR));
    	
    	if(quest != null && !QuestDatabase.questDB.containsValue(quest))
		{
    		quest = QuestDatabase.getQuestByID(quest.questID);
    		
			if(quest == null)
			{
				mc.displayGuiScreen(parent);
			}
		}
    	
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
				if(quest == null || n3 < 0 || n3 >= quest.preRequisites.size())
				{
					btn.displayString = "NULL";
					btn.visible = btn.enabled = false;
				} else
				{
					btn.visible = btn.enabled = true;
					btn.displayString = quest.preRequisites.get(n3).name;
				}
			} else if(n2 == 1) // Remove quest
			{
				btn.visible = btn.enabled = quest != null && !(n3 < 0 || n3 >= quest.preRequisites.size());
			} else if(n2 == 2) // Edit quest
			{
				if(n4 < 0 || n4 >= searchResults.size())
				{
					btn.displayString = "NULL";
					btn.visible = btn.enabled = false;
				} else
				{
					QuestInstance q = searchResults.get(n4);
					btn.visible = btn.enabled = true;
					btn.displayString = q.name;
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
					QuestInstance q = searchResults.get(n4);
					btn.visible = true;
					btn.enabled = quest != null && !quest.preRequisites.contains(q) && quest != q;
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
		searchResults = new ArrayList<QuestInstance>();
		String query = searchBox.getText().toLowerCase();
		
		for(QuestInstance q : QuestDatabase.questDB.values())
		{
			if(q == null)
			{
				continue;
			}
			
			if(q.name.toLowerCase().contains(query) || I18n.format(q.name).toLowerCase().contains(query) || query.equalsIgnoreCase("" + q.questID))
			{
				searchResults.add(q);
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

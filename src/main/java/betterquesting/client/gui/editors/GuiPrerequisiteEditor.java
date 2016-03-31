package betterquesting.client.gui.editors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import betterquesting.client.gui.GuiQuestInstance;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.gui.misc.IVolatileScreen;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketQuesting.PacketDataType;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.utils.NBTConverter;
import betterquesting.utils.RenderUtils;
import com.google.gson.JsonObject;

@SideOnly(Side.CLIENT)
public class GuiPrerequisiteEditor extends GuiQuesting implements IVolatileScreen
{
	QuestInstance quest;
	int leftScroll = 0;
	int rightScroll = 0;
	int maxRowsL = 0;
	int maxRowsR = 0;
	GuiTextField searchBox;
	ArrayList<QuestInstance> searchResults = new ArrayList<QuestInstance>();
	
	public GuiPrerequisiteEditor(GuiScreen parent, QuestInstance quest)
	{
		super(parent, "betterquesting.title.pre_requisites");
		this.quest = quest;
	}
	
	@Override
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
		
		this.searchBox = new GuiTextField(0, mc.fontRendererObj, guiLeft + sizeX/2 + 8, guiTop + 48, btnWidth - 16, 20);
		this.buttonList.add(new GuiButtonQuesting(1, guiLeft + 16 + sx/4*3 - 50, guiTop + sizeY - 48, 100, 20, I18n.translateToLocalFormatted("betterquesting.btn.new")));
		
		// Left main buttons
		for(int i = 0; i < maxRowsL; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + 16, guiTop + 48 + (i*20), btnWidth - 36, 20, "NULL");
			this.buttonList.add(btn);
		}
		
		// Left delete buttons
		for(int i = 0; i < maxRowsL; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + 16 + btnWidth - 36, guiTop + 48 + (i*20), 20, 20, "" + TextFormatting.YELLOW + TextFormatting.BOLD + ">");
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
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + sizeX/2 + 28 + btnWidth - 56, guiTop + 68 + (i*20), 20, 20, "" + TextFormatting.RED + TextFormatting.BOLD + "x");
			this.buttonList.add(btn);
		}
		
		// Right add buttons
		for(int i = 0; i < maxRowsR; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + sizeX/2 + 8, guiTop + 68 + (i*20), 20, 20, "" + TextFormatting.GREEN + TextFormatting.BOLD + "<");
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
		
		GlStateManager.color(1F, 1F, 1F, 1F);
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
		String txt = I18n.translateToLocalFormatted(quest == null? "ERROR" : quest.name);
		mc.fontRendererObj.drawString(txt, guiLeft + 16 + sx/4 - mc.fontRendererObj.getStringWidth(txt)/2, guiTop + 32, ThemeRegistry.curTheme().textColor().getRGB(), false);
		txt = I18n.translateToLocalFormatted("betterquesting.gui.database");
		mc.fontRendererObj.drawString(txt, guiLeft + 16 + sx/4*3 - mc.fontRendererObj.getStringWidth(txt)/2, guiTop + 32, ThemeRegistry.curTheme().textColor().getRGB(), false);
		
		searchBox.drawTextBox();
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id == 1)
		{
			NBTTagCompound tags = new NBTTagCompound();
			//tags.setInteger("ID", 6);
			tags.setInteger("action", 1);
			//BetterQuesting.instance.network.sendToServer(new PacketQuesting(tags));
			BetterQuesting.instance.network.sendToServer(PacketDataType.LINE_EDIT.makePacket(tags));
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
					//tags.setInteger("ID", 5);
					tags.setInteger("action", 1); // Delete quest
					tags.setInteger("questID", searchResults.get(n4).questID);
					//BetterQuesting.instance.network.sendToServer(new PacketQuesting(tags));
					BetterQuesting.instance.network.sendToServer(PacketDataType.QUEST_EDIT.makePacket(tags));
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
    public void handleMouseInput() throws IOException
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
		JsonObject json = new JsonObject();
		quest.writeToJSON(json);
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("action", 0); // Action: Update data
		tags.setInteger("questID", quest.questID);
		tags.setTag("Data", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
		BetterQuesting.instance.network.sendToServer(PacketDataType.QUEST_EDIT.makePacket(tags));
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
		searchResults = new ArrayList<QuestInstance>();
		String query = searchBox.getText().toLowerCase();
		
		for(QuestInstance q : QuestDatabase.questDB.values())
		{
			if(q == null)
			{
				continue;
			}
			
			if(q.name.toLowerCase().contains(query) || I18n.translateToLocalFormatted(q.name).toLowerCase().contains(query) || query.equalsIgnoreCase("" + q.questID))
			{
				searchResults.add(q);
			}
		}
	}
	
	@Override
	public void mouseClicked(int mx, int my, int type) throws IOException
	{
		super.mouseClicked(mx, my, type);
		this.searchBox.mouseClicked(mx, my, type);
	}
}

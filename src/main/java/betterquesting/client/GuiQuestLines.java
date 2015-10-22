package betterquesting.client;

import java.util.ArrayList;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import betterquesting.client.buttons.GuiButtonQuestInstance;
import betterquesting.client.buttons.GuiButtonQuestLine;
import betterquesting.client.buttons.GuiButtonQuesting;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestLine;

public class GuiQuestLines extends GuiQuesting
{
	GuiButtonQuestLine selected;
	ResourceLocation mapTex = new ResourceLocation("textures/map/map_background.png");
	ArrayList<GuiButtonQuestLine> qlBtns = new ArrayList<GuiButtonQuestLine>();
	int listScroll = 0;
	int maxRows = 0;
	int boxScrollX = 0;
	int boxScrollY = 0;
	int maxScrollX = 128;
	int maxScrollY = 128;
	
	public GuiQuestLines(GuiScreen parent)
	{
		super(parent, "Quest Lines");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();
		
		qlBtns.clear();
		selected = null;
		listScroll = 0;
		maxRows = (sizeY - 64)/20;
		((GuiButton)this.buttonList.get(0)).xPosition = this.width/2 - 100;
		((GuiButton)this.buttonList.get(0)).width = 100;
		
		GuiButtonQuesting btnEdit = new GuiButtonQuesting(1, this.width/2, this.guiTop + this.sizeY - 16, 100, 20, "Edit");
		btnEdit.enabled = true;
		this.buttonList.add(btnEdit);
		
		int i = 0;
		for(QuestLine line : QuestDatabase.questLines)
		{
			GuiButtonQuestLine btnLine = new GuiButtonQuestLine(buttonList.size(), this.guiLeft + 16, this.guiTop + 32 + i, 142, 20, line);
			btnLine.enabled = false;
			for(GuiButtonQuestInstance btnQuest : btnLine.buttonTree)
			{
				btnQuest.SetClampingBounds(this.guiLeft + 175, this.guiTop + 32, this.sizeX - (34 + 150 + 8), this.sizeY - 64);
				btnQuest.xPosition += this.guiLeft + 175;
				btnQuest.yPosition += this.guiTop + 32;
				
				for(GuiButtonQuestInstance p : btnLine.buttonTree)
				{
					if(p.enabled && p.visible)
					{
						btnLine.enabled = true;
					}
					
					if(btnQuest.quest.preRequisites.contains(p.quest))
					{
						btnQuest.parent = p;
						break;
					}
				}
			}
			buttonList.add(btnLine);
			qlBtns.add(btnLine);
			i += 20;
		}
		
		UpdateScroll();
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		this.mc.renderEngine.bindTexture(guiTexture);
		
		GL11.glPushMatrix();
		int mapSX = this.sizeX - (32 + 150 + 8);
		int mapSY = this.sizeY - 64;
		float scaleX = mapSX/128F;
		float scaleY = mapSY/128F;
		GL11.glScalef(scaleX, scaleY, 1F);
		this.drawTexturedModalRect(MathHelper.floor_float((this.guiLeft + 174)/scaleX), MathHelper.floor_float((this.guiTop + 32)/scaleY), 0, 128, 128, 128);
		GL11.glPopMatrix();
		
		this.drawTexturedModalRect(this.guiLeft + 16 + 142, this.guiTop + 32, 248, 0, 8, 20);
		int i = 20;
		while(i < sizeY - 84)
		{
			this.drawTexturedModalRect(this.guiLeft + 16 + 142, this.guiTop + 32 + i, 248, 20, 8, 20);
			i += 20;
		}
		this.drawTexturedModalRect(this.guiLeft + 16 + 142, this.guiTop + 32 + i, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + 16 + 142, this.guiTop + 32 + (int)Math.max(0, i * (float)listScroll/(float)(qlBtns.size() - maxRows)), 248, 60, 8, 20);
		
		if(selected != null)
		{
			for(GuiButtonQuestInstance btnQuest : selected.buttonTree)
			{
				btnQuest.SetScrollOffset(boxScrollX, boxScrollY);
				btnQuest.drawButton(mc, mx, my);
			}
		}
	}
	
	boolean flag = false;
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		flag = true;
		
		super.actionPerformed(button);
		
		if(button.id == 1)
		{
			mc.displayGuiScreen(new GuiQuestLineEditor(this));
			// Quest line editor
		} else if(button instanceof GuiButtonQuestLine)
		{
			selected = (GuiButtonQuestLine)button;
			boxScrollX = 0;
			boxScrollY = 0;
			maxScrollX = 0;
			maxScrollY = 0;
			
			for(GuiButtonQuestInstance btnQuest : selected.buttonTree)
			{
				if(btnQuest.visible)
				{
					maxScrollX = maxScrollX < btnQuest.xPosition? btnQuest.xPosition : maxScrollX;
					maxScrollY = maxScrollY < btnQuest.yPosition + btnQuest.height? btnQuest.yPosition + btnQuest.height : maxScrollY;
				}
			}
			
			maxScrollX -= this.guiLeft + 175;
			//maxScrollY -= this.guiTop - this.sizeY + 256; // This needs fixing
			maxScrollX -= this.sizeX - (34 + 150 + 8) - 100;
		}
	}
	
	@Override
    protected void mouseClicked(int mx, int my, int type)
    {
		flag = false;
		
		super.mouseClicked(mx, my, type);
		
		if(!flag && selected != null)
		{
			for(GuiButtonQuestInstance btnQuest : selected.buttonTree)
			{
				if(btnQuest.mousePressed(mc, mx, my))
				{
					flag = true;
					btnQuest.func_146113_a(this.mc.getSoundHandler());
					mc.displayGuiScreen(new GuiQuestInstance(this, btnQuest.quest));
					break;
				}
			}
		}
    }
	
	@Override
	public void handleMouseInput()
    {
		super.handleMouseInput();
		
        int mx = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int my = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int SDX = (int)-Math.signum(Mouse.getDWheel());
        
        if(SDX != 0 && isWithin(mx, my, this.guiLeft, this.guiTop, sizeX/2, sizeY))
        {
    		listScroll = Math.max(0, MathHelper.clamp_int(listScroll + SDX, 0, qlBtns.size() - maxRows));
    		UpdateScroll();
        }
        
    	if(!flag && Mouse.isButtonDown(0))
    	{
    		this.boxScrollX += Mouse.getEventDX() * this.width / this.mc.displayWidth;
    		this.boxScrollY -= Mouse.getEventDY() * this.height / this.mc.displayHeight;
    		this.boxScrollX = -maxScrollX >= 0 ? MathHelper.clamp_int(this.boxScrollX, 0, -maxScrollX) : MathHelper.clamp_int(this.boxScrollX, -maxScrollX, 0);
    		this.boxScrollY = maxScrollY <= 0 ? MathHelper.clamp_int(this.boxScrollY, maxScrollY, 0) : MathHelper.clamp_int(this.boxScrollY, 0, maxScrollY);
    	}
    }
	
	public void UpdateScroll()
	{
		for(int i = 0; i < qlBtns.size(); i++)
		{
			GuiButtonQuestLine btn = qlBtns.get(i);
			int n = i - listScroll;
			
			if(n < 0 || n >= maxRows)
			{
				btn.visible = false;
			} else
			{
				btn.visible = true;
				btn.yPosition = this.guiTop + 32 + n*20;
			}
		}
	}
}

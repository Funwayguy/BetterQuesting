package betterquesting.client;

import org.lwjgl.opengl.GL11;
import betterquesting.client.buttons.GuiButtonQuesting;
import betterquesting.quests.QuestLine;
import betterquesting.quests.QuestLineDB;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class GuiQuestLines extends GuiQuesting
{
	ResourceLocation mapTex = new ResourceLocation("textures/map/map_background.png");
	int leftScroll = 0;
	int rightScroll = 0;
	
	public GuiQuestLines(GuiScreen parent)
	{
		super(parent, "Quest Lines");
		// Request quest list sync
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();
		
		((GuiButton)this.buttonList.get(0)).xPosition = this.width/2 - 100;
		((GuiButton)this.buttonList.get(0)).width = 100;
		
		GuiButtonQuesting btnEdit = new GuiButtonQuesting(1, this.width/2, this.guiTop + this.sizeY - 16, 100, 20, "Edit");
		btnEdit.enabled = true;//!this.mc.theWorld.isRemote;
		this.buttonList.add(btnEdit);
		
		int i = 0;
		for(QuestLine line : QuestLineDB.allLines)
		{
			buttonList.add(new GuiButtonQuesting(buttonList.size(), this.guiLeft + 16, this.guiTop + 32 + i, 142, 20, line.name));
			i += 20;
		}
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		this.mc.renderEngine.bindTexture(guiTexture);
		
		GL11.glPushMatrix();
		float scaleX = (this.sizeX - (32 + 150 + 8))/128F;
		float scaleY = (this.sizeY - 64)/128F;
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
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id == 1)
		{
			
		} else if(button.id == 2)
		{
			
		} else
		{
			
		}
	}
	
	public void UpdateScroll()
	{
		
	}
}

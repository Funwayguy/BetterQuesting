package betterquesting.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.gui.party.GuiManageParty;
import betterquesting.client.gui.party.GuiNoParty;
import betterquesting.core.BQ_Settings;
import betterquesting.party.PartyInstance;
import betterquesting.party.PartyManager;
import betterquesting.quests.QuestDatabase;

public class GuiHome extends GuiQuesting
{
	ResourceLocation homeGui = new ResourceLocation(BQ_Settings.titleCard);
	
	public GuiHome(GuiScreen parent)
	{
		super(parent, "");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();
		this.buttonList.clear(); // We need to move some buttons around so we're starting over
		
		int bw = (sizeX - 32)/4;
		
		GuiButtonQuesting btn = new GuiButtonQuesting(0, guiLeft + 16, guiTop + sizeY - 48, bw, 32, I18n.format("betterquesting.home.exit"));
		this.buttonList.add(btn);
		btn = new GuiButtonQuesting(1, guiLeft + 16 + bw, guiTop + sizeY - 48, bw, 32, I18n.format("betterquesting.home.quests"));
		this.buttonList.add(btn);
		btn = new GuiButtonQuesting(2, guiLeft + 16 + bw*2, guiTop + sizeY - 48, bw, 32, I18n.format("betterquesting.home.party"));
		this.buttonList.add(btn);
		btn = new GuiButtonQuesting(3, guiLeft + 16 + bw*3, guiTop + sizeY - 48, bw, 32, I18n.format("betterquesting.home.theme"));
		this.buttonList.add(btn);
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		mc.renderEngine.bindTexture(homeGui);
		
		GL11.glPushMatrix();
		float sw = (sizeX - 32)/256F;
		float sh = (sizeY - 64)/128F;
		GL11.glTranslatef(guiLeft + 16, guiTop + 16, 0F);
		GL11.glScalef(sw, sh, 1F);
		this.drawTexturedModalRect(0, 0, 0, 0, 256, 128);
		GL11.glPopMatrix();
		
		int tx = (int)((sizeX - 32) * BQ_Settings.titleAlignX) + BQ_Settings.titleOffX + guiLeft + 16;
		int ty = (int)((sizeY - 64) * BQ_Settings.titleAlignY) + BQ_Settings.titleOffY + guiTop + 16;
		this.drawTexturedModalRect(tx, ty, 0, 128, 256, 128);
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id == 1)
		{
			if(BQ_Settings.useBookmark && GuiQuestLinesMain.bookmarked != null && QuestDatabase.questDB.containsValue(GuiQuestLinesMain.bookmarked.quest))
			{
				mc.displayGuiScreen(GuiQuestLinesMain.bookmarked);
			} else
			{
				mc.displayGuiScreen(new GuiQuestLinesMain(this));
			}
		} else if(button.id == 2)
		{
			PartyInstance party = PartyManager.GetParty(mc.thePlayer.getUniqueID());
			
			if(party != null)
			{
				mc.displayGuiScreen(new GuiManageParty(this, party));
			} else
			{
				mc.displayGuiScreen(new GuiNoParty(this));
			}
		} else if(button.id == 3)
		{
			mc.displayGuiScreen(new GuiThemeSelect(this));
		}
	}
}

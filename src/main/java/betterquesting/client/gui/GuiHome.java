package betterquesting.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.party.IParty;
import betterquesting.client.gui.party.GuiManageParty;
import betterquesting.client.gui.party.GuiNoParty;
import betterquesting.core.BQ_Settings;
import betterquesting.party.PartyManager;

public class GuiHome extends GuiScreenThemed
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
		
		GuiButtonThemed btn = new GuiButtonThemed(0, guiLeft + 16, guiTop + sizeY - 48, bw, 32, I18n.format("betterquesting.home.exit"), true);
		this.buttonList.add(btn);
		btn = new GuiButtonThemed(1, guiLeft + 16 + bw, guiTop + sizeY - 48, bw, 32, I18n.format("betterquesting.home.quests"), true);
		this.buttonList.add(btn);
		btn = new GuiButtonThemed(2, guiLeft + 16 + bw*2, guiTop + sizeY - 48, bw, 32, I18n.format("betterquesting.home.party"), true);
		this.buttonList.add(btn);
		btn = new GuiButtonThemed(3, guiLeft + 16 + bw*3, guiTop + sizeY - 48, bw, 32, I18n.format("betterquesting.home.theme"), true);
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
			if(BQ_Settings.useBookmark && GuiQuestLinesMain.bookmarked != null)
			{
				mc.displayGuiScreen(GuiQuestLinesMain.bookmarked);
			} else
			{
				mc.displayGuiScreen(new GuiQuestLinesMain(this));
				//mc.displayGuiScreen(new betterquesting.api.client.gui.premade.screens.GuiTestScreen(this));
			}
		} else if(button.id == 2)
		{
			IParty party = PartyManager.INSTANCE.getUserParty(mc.thePlayer.getUniqueID());
			
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

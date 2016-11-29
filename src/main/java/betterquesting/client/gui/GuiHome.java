package betterquesting.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.party.IParty;
import betterquesting.client.gui.party.GuiManageParty;
import betterquesting.client.gui.party.GuiNoParty;
import betterquesting.core.BQ_Settings;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.QuestSettings;

public class GuiHome extends GuiScreenThemed
{
	private ResourceLocation homeGui;
	private float ancX = 0.5F;
	private float ancY = 0.5F;
	private int offX = 0;
	private int offY = 0;
	
	public GuiHome(GuiScreen parent)
	{
		super(parent, "");
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		this.buttonList.clear(); // We need to move some buttons around so we're starting over
		
		homeGui = new ResourceLocation(QuestSettings.INSTANCE.getProperty(NativeProps.HOME_IMAGE));
		ancX = QuestSettings.INSTANCE.getProperty(NativeProps.HOME_ANC_X).floatValue();
		ancY = QuestSettings.INSTANCE.getProperty(NativeProps.HOME_ANC_Y).floatValue();
		offX = QuestSettings.INSTANCE.getProperty(NativeProps.HOME_OFF_X).intValue();
		offY = QuestSettings.INSTANCE.getProperty(NativeProps.HOME_OFF_Y).intValue();
		
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
		
		GlStateManager.pushMatrix();
		float sw = (sizeX - 32)/256F;
		float sh = (sizeY - 64)/128F;
		GlStateManager.translate(guiLeft + 16, guiTop + 16, 0F);
		GlStateManager.scale(sw, sh, 1F);
		this.drawTexturedModalRect(0, 0, 0, 0, 256, 128);
		GlStateManager.popMatrix();
		
		int tx = (int)((sizeX - 32) * ancX) + offX + guiLeft + 16;
		int ty = (int)((sizeY - 64) * ancY) + offY + guiTop + 16;
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
			}
		} else if(button.id == 2)
		{
			IParty party = PartyManager.INSTANCE.getUserParty(QuestingAPI.getQuestingUUID(mc.thePlayer));
			
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

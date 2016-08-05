package betterquesting.client.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import betterquesting.api.client.gui.premade.screens.GuiScreenThemed;
import betterquesting.api.client.themes.IThemeBase;
import betterquesting.api.enums.EnumQuestState;
import betterquesting.api.utils.RenderUtils;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.registry.ThemeRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiThemeSelect extends GuiScreenThemed
{
	List<IThemeBase> themeList = new ArrayList<IThemeBase>();
	int leftScroll = 0;
	int maxRows = 0;
	
	public GuiThemeSelect(GuiScreen parent)
	{
		super(parent, I18n.format("betterquesting.title.select_theme"));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();
		
		ThemeRegistry.INSTANCE.reloadThemes();
		themeList.clear();
		themeList.addAll(ThemeRegistry.INSTANCE.getAllThemes());
		
		maxRows = (sizeY - 80)/20;
		int btnWidth = sizeX/2 - 32;
		
		// Quest Line - Main
		for(int i = 0; i < maxRows; i++)
		{
			GuiButtonQuesting btn = new GuiButtonQuesting(this.buttonList.size(), guiLeft + 16, guiTop + 32 + (i*20), btnWidth, 20, "NULL");
			this.buttonList.add(btn);
		}
		
		RefreshColumns();
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		GL11.glColor4f(1F, 1F, 1F, 1F);
		mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
		
		int btnWidth = sizeX/2 - 32;
		
		// Left scroll bar
		this.drawTexturedModalRect(guiLeft + 16 + btnWidth, this.guiTop + 32, 248, 0, 8, 20);
		int s = 20;
		while(s < (maxRows - 1) * 20)
		{
			this.drawTexturedModalRect(guiLeft + 16 + btnWidth, this.guiTop + 32 + s, 248, 20, 8, 20);
			s += 20;
		}
		this.drawTexturedModalRect(guiLeft + 16 + btnWidth, this.guiTop + 32 + s, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + 16 + btnWidth, this.guiTop + 32 + (int)Math.max(0, s * (float)leftScroll/(themeList.size() - maxRows)), 248, 60, 8, 20);
		
		RenderUtils.DrawLine(width/2, guiTop + 32, width/2, guiTop + sizeY - 48, 2F, getTextColor());
		
		GL11.glPushMatrix();
		float scale = ((sizeX - 32)/2)/128F;
		scale = Math.min(scale, (sizeY - 64)/128F);
		
		GL11.glScalef(scale, scale, 1F);
		
		int cx = (int)((guiLeft + sizeX/4 * 3)/scale);
		int cy = (int)((guiTop + sizeY/2)/scale);
		
		this.drawTexturedModalRect(cx - 64, cy - 64, 0, 128, 128, 128);
		
		this.drawTexturedModalRect(cx - 9, cy - 24, 0, 48, 18, 18);
		
    	int ci = currentTheme().getQuestIconColor(null, EnumQuestState.values()[(int)(Minecraft.getSystemTime()/2000)%4], (int)(Minecraft.getSystemTime()/1000)%2 + 1);
		float r = (float)(ci >> 16 & 255) / 255.0F;
        float g = (float)(ci >> 8 & 255) / 255.0F;
        float b = (float)(ci & 255) / 255.0F;
    	GL11.glColor4f(r, g, b, 1F);
    	
		this.drawTexturedModalRect(cx + 16, cy + 8, 0, 104, 24, 24);
		this.drawTexturedModalRect(cx - 40, cy + 8, 24, 104, 24, 24);
		
    	int cl = currentTheme().getQuestLineColor(null, EnumQuestState.values()[(int)(Minecraft.getSystemTime()/2000)%4]);
    	RenderUtils.DrawLine(cx - 16, cy + 20, cx + 16, cy + 20, 4, cl);
    	
    	GL11.glColor4f(1F, 1F, 1F, 1F);
    	
    	String txt = EnumChatFormatting.BOLD + "EXAMPLE";
    	mc.fontRenderer.drawString(txt, cx - mc.fontRenderer.getStringWidth(txt)/2, cy - 32 - mc.fontRenderer.FONT_HEIGHT, getTextColor());
    	
    	RenderUtils.RenderItemStack(mc, new ItemStack(Items.enchanted_book), cx - 8, cy - 23, "");
		
		GL11.glPopMatrix();
	}
	
	@Override
	public void actionPerformed(GuiButton btn)
	{
		super.actionPerformed(btn);
		
		if(btn.id > 0)
		{
			int n1 = btn.id - 1; // Line index
			int n2 = n1/maxRows; // Line listing (0 = main)
			int n3 = n1%maxRows + leftScroll; // Theme list index
			
			if(n2 == 0)
			{
				ThemeRegistry.INSTANCE.setCurrentTheme(themeList.get(n3));
				RefreshColumns();
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
    		leftScroll = Math.max(0, MathHelper.clamp_int(leftScroll + SDX, 0, themeList.size() - maxRows));
    		RefreshColumns();
        }
    }
	
	public void RefreshColumns()
	{
		leftScroll = Math.max(0, MathHelper.clamp_int(leftScroll, 0, themeList.size() - maxRows));
		
		@SuppressWarnings("unchecked")
		List<GuiButton> btnList = this.buttonList;
		
		for(int i = 1; i < btnList.size(); i++)
		{
			GuiButton btn = btnList.get(i);
			int n1 = btn.id - 1; // Line index
			int n2 = n1/maxRows; // Line listing (0 = line, 1 = delete)
			int n3 = n1%maxRows + leftScroll; // Quest list index
			
			if(n2 == 0)
			{
				if(n3 >= 0 && n3 < themeList.size())
				{
					IThemeBase theme = themeList.get(n3);
					btn.displayString = theme.getDisplayName();
					btn.visible = true;
					btn.enabled = currentTheme() != theme;
				} else
				{
					btn.displayString = "NULL";
					btn.enabled = btn.visible = false;
				}
			}
		}
	}
}

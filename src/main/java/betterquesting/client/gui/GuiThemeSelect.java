package betterquesting.client.gui;

import java.awt.Color;
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
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.themes.ThemeBase;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.utils.RenderUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiThemeSelect extends GuiQuesting
{
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
		
		ThemeRegistry.RefreshResourceThemes();
		
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
		mc.renderEngine.bindTexture(ThemeRegistry.curTheme().guiTexture());
		
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
		this.drawTexturedModalRect(guiLeft + 16 + btnWidth, this.guiTop + 32 + (int)Math.max(0, s * (float)leftScroll/(ThemeRegistry.GetAllThemes().size() - maxRows)), 248, 60, 8, 20);
		
		RenderUtils.DrawLine(width/2, guiTop + 32, width/2, guiTop + sizeY - 48, 2F, ThemeRegistry.curTheme().textColor());
		
		GL11.glPushMatrix();
		float scale = ((sizeX - 32)/2)/128F;
		scale = Math.min(scale, (sizeY - 64)/128F);
		
		GL11.glScalef(scale, scale, 1F);
		
		int cx = (int)((guiLeft + sizeX/4 * 3)/scale);
		int cy = (int)((guiTop + sizeY/2)/scale);
		
		this.drawTexturedModalRect(cx - 64, cy - 64, 0, 128, 128, 128);
		
		this.drawTexturedModalRect(cx - 9, cy - 24, 0, 48, 18, 18);
		
    	Color ci = ThemeRegistry.curTheme().getIconColor((int)(Minecraft.getSystemTime()/1000)%2 + 1, (int)(Minecraft.getSystemTime()/2000)%4, (Minecraft.getSystemTime()/8000)%2 == 0);
    	GL11.glColor4f(ci.getRed()/255F, ci.getGreen()/255F, ci.getBlue()/255F, 1F);
    	
		this.drawTexturedModalRect(cx + 16, cy + 8, 0, 104, 24, 24);
		this.drawTexturedModalRect(cx - 40, cy + 8, 24, 104, 24, 24);
		
    	Color cl = ThemeRegistry.curTheme().getLineColor(MathHelper.clamp_int((int)(Minecraft.getSystemTime()/2000)%4, 0, 2), (Minecraft.getSystemTime()/8000)%2 == 0);
    	RenderUtils.DrawLine(cx - 16, cy + 20, cx + 16, cy + 20, 4, cl);
    	
    	GL11.glColor4f(1F, 1F, 1F, 1F);
    	
    	String txt = EnumChatFormatting.BOLD + "EXAMPLE";
    	mc.fontRenderer.drawString(txt, cx - mc.fontRenderer.getStringWidth(txt)/2, cy - 32 - mc.fontRenderer.FONT_HEIGHT, ThemeRegistry.curTheme().textColor().getRGB());
    	
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
				ThemeRegistry.setTheme(ThemeRegistry.getId(ThemeRegistry.GetAllThemes().get(n3)));
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
    		leftScroll = Math.max(0, MathHelper.clamp_int(leftScroll + SDX, 0, ThemeRegistry.GetAllThemes().size() - maxRows));
    		RefreshColumns();
        }
    }
	
	public void RefreshColumns()
	{
		leftScroll = Math.max(0, MathHelper.clamp_int(leftScroll, 0, ThemeRegistry.GetAllThemes().size() - maxRows));
		
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
				if(n3 >= 0 && n3 < ThemeRegistry.GetAllThemes().size())
				{
					ThemeBase theme = ThemeRegistry.GetAllThemes().get(n3);
					btn.displayString = theme.GetName();
					btn.visible = true;
					btn.enabled = ThemeRegistry.curTheme() != theme;
				} else
				{
					btn.displayString = "NULL";
					btn.enabled = btn.visible = false;
				}
			}
		}
	}
}

package betterquesting.api.client.gui.controls;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.themes.ITheme;
import betterquesting.api.placeholders.ThemeDummy;

/**
 * Version of GuiButton that uses BetterQuesting theme and scales better
 */
public class GuiButtonThemed extends GuiButton
{
	public Minecraft mc = null;
	
	private boolean txtShadow = true;
	
	private ResourceLocation icon = null;
	private boolean is = false;
	private int iu = 0;
	private int iv = 0;
	private int iw = 0;
	private int ih = 0;
	
	private List<String> tooltip = new ArrayList<String>();
	
	public GuiButtonThemed(int id, int posX, int posY, String text)
	{
		this(id, posX, posY, 200, 20, text, true);
	}
	
	public GuiButtonThemed(int id, int posX, int posY, int width, int height, String text)
	{
		this(id, posX, posY, width, height, text, true);
	}
	
	public GuiButtonThemed(int id, int posX, int posY, int width, int height, String text, boolean shadow)
	{
		super(id, posX, posY, width, height, text);
		this.mc = Minecraft.getMinecraft();
		this.txtShadow = shadow;
	}
	
	/**
	 * Sets icon underneath text. Can be set to scale with the button
	 */
	public GuiButtonThemed setIcon(ResourceLocation texture, int u, int v, int w, int h, boolean stretch)
	{
		this.icon = texture;
		this.is = stretch;
		this.iu = u;
		this.iv = v;
		this.iw = w;
		this.ih = h;
		return this;
	}
	
	public List<String> getTooltip()
	{
		return tooltip;
	}
	
	public void setTooltip(List<String> tooltip)
	{
		this.tooltip = tooltip;
	}
	
	@Override
	public void drawButton(Minecraft mc, int mx, int my)
	{
        if (this.visible)
        {
            FontRenderer fontrenderer = mc.fontRendererObj;
            mc.getTextureManager().bindTexture(this.currentTheme().getGuiTexture());
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = mx >= this.xPosition && my >= this.yPosition && mx < this.xPosition + this.width && my < this.yPosition + this.height;
            int k = this.getHoverState(this.hovered);
            GlStateManager.enableBlend();
            GlStateManager.enableBlend();
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            
            GlStateManager.pushMatrix();
            float sh = height/20F;
            float sw = width >= 196? width/200F : 1F;
            float py = yPosition/sh;
            float px = xPosition/sw;
            GlStateManager.scale(sw, sh, 1F);
            
            if(width >= 196)
            {
                GlStateManager.translate(px, py, 0F); // Fixes floating point errors related to position
            	this.drawTexturedModalRect(0, 0, 48, k * 20, 200, 20);
            } else
            {
            	this.drawTexturedModalRect((int)px, (int)py, 48, k * 20, this.width / 2, 20);
            	this.drawTexturedModalRect((int)px + width / 2, (int)py, 248 - this.width / 2, k * 20, this.width / 2, 20);
            }
            
            GlStateManager.popMatrix();
            
            this.mouseDragged(mc, mx, my);
            int l = 14737632;

            if (packedFGColour != 0)
            {
                l = packedFGColour;
            }
            else if (!this.enabled)
            {
                l = 10526880;
            }
            else if (this.hovered)
            {
                l = 16777120;
            }
            
            if(icon != null)
            {
            	mc.renderEngine.bindTexture(icon);
            	if(is)
            	{
            		float iScale = Math.min(width/(float)iw, height/(float)ih);
            		GlStateManager.pushMatrix();
            		GlStateManager.scale(iScale, iScale, 1F);
            		GlStateManager.translate(xPosition/iScale, yPosition/iScale, 0F);
            		this.drawTexturedModalRect((int)(width/2 - (iw*iScale)/2F), (int)(height/2 - (ih*iScale)/2F), iu, iv, iw, ih);
            		GlStateManager.popMatrix();
            	} else
            	{
            		this.drawTexturedModalRect(xPosition + width/2 - iw/2, yPosition + height/2 - ih/2, iu, iv, iw, ih);
            	}
            }
            
            String txt = this.displayString;
            
            if(fontrenderer.getStringWidth(txt) > width) // Auto crop text to keep things tidy
            {
            	int dotWidth = fontrenderer.getStringWidth("...");
            	txt = fontrenderer.trimStringToWidth(txt, width - dotWidth) + "...";
            }
            
            this.drawCenteredString(fontrenderer, txt, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, l, txtShadow);
        }
	}
	
    public void drawCenteredString(FontRenderer font, String text, int x, int y, int color, boolean shadow)
    {
        font.drawString(text, x - font.getStringWidth(text) / 2, y, color, shadow);
    }
    
    public void drawString(FontRenderer font, String text, int x, int y, int color, boolean shadow)
    {
        font.drawString(text, x, y, color, shadow);
    }
    
	public ITheme currentTheme()
	{
		if(QuestingAPI.getAPI(ApiReference.THEME_REG) != null)
		{
			return QuestingAPI.getAPI(ApiReference.THEME_REG).getCurrentTheme();
		} else
		{
			return ThemeDummy.INSTANCE;
		}
	}
}

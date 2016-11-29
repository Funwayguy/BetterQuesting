package betterquesting.api.client.gui;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.client.config.GuiUtils;
import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.themes.ITheme;
import betterquesting.api.placeholders.ThemeDummy;

public abstract class GuiElement
{
	public float zLevel = 0F;
	
	/**
	 * Shortcut method for obtaining the current theme.
	 */
	public static ITheme currentTheme()
	{
		if(QuestingAPI.getAPI(ApiReference.THEME_REG) != null)
		{
			return QuestingAPI.getAPI(ApiReference.THEME_REG).getCurrentTheme();
		} else
		{
			return ThemeDummy.INSTANCE;
		}
	}
	
	/**
	 * Returns the current theme color
	 */
	public static int getTextColor()
	{
		return currentTheme().getTextColor();
	}
	
    public void drawCenteredString(FontRenderer font, String text, int x, int y, int color, boolean shadow)
    {
        font.drawString(text, x - font.getStringWidth(text) / 2, y, color, shadow);
    }
    
    public void drawString(FontRenderer font, String text, int x, int y, int color, boolean shadow)
    {
        font.drawString(text, x, y, color, shadow);
    }
    
    public void drawTexturedModalRect(int x, int y, int u, int v, int w, int h)
    {
    	GuiUtils.drawTexturedModalRect(x, y, u, v, w, h, zLevel);
    }
    
    public void drawTexturedModelRectFromIcon(int x, int y, TextureAtlasSprite sprite, int w, int h)
    {
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        vertexbuffer.pos((double)(x + 0), (double)(y + h), (double)this.zLevel).tex((double)sprite.getMinU(), (double)sprite.getMaxV()).endVertex();
        vertexbuffer.pos((double)(x + w), (double)(y + h), (double)this.zLevel).tex((double)sprite.getMaxU(), (double)sprite.getMaxV()).endVertex();
        vertexbuffer.pos((double)(x + w), (double)(y + 0), (double)this.zLevel).tex((double)sprite.getMaxU(), (double)sprite.getMinV()).endVertex();
        vertexbuffer.pos((double)(x + 0), (double)(y + 0), (double)this.zLevel).tex((double)sprite.getMinU(), (double)sprite.getMinV()).endVertex();
        tessellator.draw();
    }
    
    public void drawRect(int left, int top, int right, int bottom, int color)
    {
        if (left < right)
        {
            int i = left;
            left = right;
            right = i;
        }

        if (top < bottom)
        {
            int j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(f, f1, f2, f3);
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION);
        vertexbuffer.pos((double)left, (double)bottom, 0.0D).endVertex();
        vertexbuffer.pos((double)right, (double)bottom, 0.0D).endVertex();
        vertexbuffer.pos((double)right, (double)top, 0.0D).endVertex();
        vertexbuffer.pos((double)left, (double)top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
    
    public void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor)
    {
    	GuiUtils.drawGradientRect((int)this.zLevel, left, top, right, bottom, startColor, endColor);
    }
    
    public void drawTooltip(List<String> textLines, int x, int y, FontRenderer fontRendererObj)
    {
    	Minecraft mc = Minecraft.getMinecraft();
        GuiUtils.drawHoveringText(textLines, x, y, mc.displayWidth, mc.displayHeight, -1, fontRendererObj);
    }
    
    public boolean isWithin(int xIn, int yIn, int x, int y, int w, int h)
    {
    	return xIn >= x && xIn < x + w && yIn >= y && yIn < y + h;
    }
}

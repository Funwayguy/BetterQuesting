package betterquesting.api2.client.gui.resources.textures;

import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

public class FluidTexture implements IGuiTexture
{
    private static final IGuiColor defColor = new GuiColorStatic(255, 255, 255, 255);
    
    private final FluidStack fluid;
    private final boolean showCount;
    private final boolean keepAspect;
    
    // Dummy value
    private final IGuiRect bounds = new GuiRectangle(0, 0, 16, 16);
    
    public FluidTexture(FluidStack fluid)
    {
        this(fluid, false, true);
    }
    
    // TODO: Add tiling option
    public FluidTexture(FluidStack fluid, boolean showCount, boolean keepAspect)
    {
        this.fluid = fluid;
        this.showCount = showCount;
        this.keepAspect = keepAspect;
    }
    
    @Override
    public void drawTexture(int x, int y, int width, int height, float zDepth, float partialTick)
    {
        this.drawTexture(x, y, width, height, zDepth, partialTick, defColor);
    }
    
    @Override
    public void drawTexture(int x, int y, int width, int height, float zDepth, float partialTick, IGuiColor color)
    {
	    if(width <= 0 || height <= 0) return;
	    
        float sx = width/16F;
        float sy = height/16F;
        
        double dx = 0;
        double dy = 0;
        
        if(keepAspect)
        {
            float sa = Math.min(sx, sy);
    
            dx = Math.floor((sx - sa) * 8F);
            dy = Math.floor((sy - sa) * 8F);
            
            sx = sa;
            sy = sa;
        }
    
        GL11.glPushMatrix();
    
        GL11.glTranslated(x + dx, y + dy, 0);
        GL11.glScalef(sx, sy, 1F);
        
        int fCol = fluid.getFluid().getColor(fluid);
        float a = (fCol >> 24 & 255) / 255F;
        float r = (fCol >> 16 & 255) / 255F;
        float g = (fCol >> 8 & 255) / 255F;
        float b = (fCol & 255) / 255F;
        a = a + color.getAlpha() / 2F;
        r = r + color.getRed() / 2F;
        g = g + color.getGreen() / 2F;
        b = b + color.getBlue() / 2F;
        GL11.glColor4f(r, g, b, a);
        
        // TODO: Add tiling option
        
        Minecraft mc = Minecraft.getMinecraft();
        mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
        IIcon icon = fluid.getFluid().getIcon() != null ? fluid.getFluid().getIcon() : ((TextureMap)mc.renderEngine.getTexture(TextureMap.locationBlocksTexture)).getAtlasSprite("missigno");
        this.drawTexturedModalRect(0, 0, 0, icon, 16, 16);
        
        // TODO: Draw amount
    
        GL11.glPopMatrix();
    }
    
    @Override
    public ResourceLocation getTexture()
    {
        return PresetTexture.TX_NULL;
    }
    
    @Override
    public IGuiRect getBounds()
    {
        return bounds;
    }
    
    private void drawTexturedModalRect(double xCoord, double yCoord, double zDepth, IIcon textureSprite, double widthIn, double heightIn)
    {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(xCoord, yCoord + heightIn, zDepth, textureSprite.getMinU(), textureSprite.getMaxV());
        tessellator.addVertexWithUV(xCoord + widthIn, yCoord + heightIn, zDepth, textureSprite.getMaxU(), textureSprite.getMaxV());
        tessellator.addVertexWithUV(xCoord + widthIn, yCoord, zDepth, textureSprite.getMaxU(), textureSprite.getMinV());
        tessellator.addVertexWithUV(xCoord, yCoord, zDepth, textureSprite.getMinU(), textureSprite.getMinV());
        tessellator.draw();
    }
}

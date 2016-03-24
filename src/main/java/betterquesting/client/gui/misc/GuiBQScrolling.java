package betterquesting.client.gui.misc;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.utils.RenderUtils;

/**
 * Similar to GuiScrollingList but with slight modifications to better suit the style and themes of Better Questing
 */
public abstract class GuiBQScrolling
{
    private final Minecraft client;
    protected final int listWidth;
    protected final int listHeight;
    protected final int top;
    protected final int bottom;
    private final int right;
    protected final int left;
    protected final int slotHeight;
    private int scrollUpActionId;
    private int scrollDownActionId;
    protected int mouseX;
    protected int mouseY;
    private float initialMouseClickY = -2.0F;
    private float scrollFactor;
    private float scrollDistance;
    private int selectedIndex = -1;
    private long lastClickTime = 0L;
    private boolean field_25123_p = true;
    private boolean field_27262_q;
    private int field_27261_r;

    public GuiBQScrolling(Minecraft client, int width, int height, int top, int bottom, int left, int entryHeight)
    {
        this.client = client;
        this.listWidth = width;
        this.listHeight = height;
        this.top = top;
        this.bottom = bottom;
        this.slotHeight = entryHeight;
        this.left = left;
        this.right = width + this.left;
    }

    public void func_27258_a(boolean p_27258_1_)
    {
        this.field_25123_p = p_27258_1_;
    }

    protected void func_27259_a(boolean p_27259_1_, int p_27259_2_)
    {
        this.field_27262_q = p_27259_1_;
        this.field_27261_r = p_27259_2_;

        if (!p_27259_1_)
        {
            this.field_27261_r = 0;
        }
    }

    protected abstract int getSize();

    protected abstract void elementClicked(int index, boolean doubleClick);

    protected abstract boolean isSelected(int index);

    protected int getContentHeight()
    {
        return this.getSize() * this.slotHeight + this.field_27261_r;
    }

    protected abstract void drawBackground();

    protected abstract void drawSlot(int index, int var2, int posY, int var4, Tessellator var5);

    protected void func_27260_a(int p_27260_1_, int p_27260_2_, Tessellator p_27260_3_) {}

    protected void func_27255_a(int p_27255_1_, int p_27255_2_) {}

    protected void func_27257_b(int p_27257_1_, int p_27257_2_) {}

    public int func_27256_c(int p_27256_1_, int p_27256_2_)

    {
        int var3 = this.left + 1;
        int var4 = this.left + this.listWidth - 7;
        int var5 = p_27256_2_ - this.top - this.field_27261_r + (int)this.scrollDistance - 4;
        int var6 = var5 / this.slotHeight;
        return p_27256_1_ >= var3 && p_27256_1_ <= var4 && var6 >= 0 && var5 >= 0 && var6 < this.getSize() ? var6 : -1;
    }

    public void registerScrollButtons(@SuppressWarnings("rawtypes") List p_22240_1_, int p_22240_2_, int p_22240_3_)
    {
        this.scrollUpActionId = p_22240_2_;
        this.scrollDownActionId = p_22240_3_;
    }

    private void applyScrollLimits()
    {
        int var1 = this.getContentHeight() - (this.bottom - this.top - 4);

        if (var1 < 0)
        {
            var1 /= 2;
        }

        if (this.scrollDistance < 0.0F)
        {
            this.scrollDistance = 0.0F;
        }

        if (this.scrollDistance > (float)var1)
        {
            this.scrollDistance = (float)var1;
        }
    }
    
    public void SetScroll(float f)
    {
        int var1 = this.getContentHeight() - (this.bottom - this.top - 4);

        if (var1 < 0)
        {
            var1 /= 2;
        }
        
        f = MathHelper.clamp_float(f, 0F, 1F);
        scrollDistance = f * var1;
        
        if (this.scrollDistance < 0.0F)
        {
            this.scrollDistance = 0.0F;
        }

        if (this.scrollDistance > (float)var1)
        {
            this.scrollDistance = (float)var1;
        }
    }

    public void actionPerformed(GuiButton button)
    {
        if (button.enabled)
        {
            if (button.id == this.scrollUpActionId)
            {
                this.scrollDistance -= (float)(this.slotHeight * 2 / 3);
                this.initialMouseClickY = -2.0F;
                this.applyScrollLimits();
            }
            else if (button.id == this.scrollDownActionId)
            {
                this.scrollDistance += (float)(this.slotHeight * 2 / 3);
                this.initialMouseClickY = -2.0F;
                this.applyScrollLimits();
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY, float p_22243_3_)
    {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.drawBackground();
        int listLength = this.getSize();
        int scrollBarXStart = this.left + this.listWidth - 8;
        int scrollBarXEnd = scrollBarXStart + 8;
        int boxLeft = this.left;
        int boxRight = scrollBarXStart-1;
        int var10;
        int var11;
        int var13;
        int var19;

        if (Mouse.isButtonDown(0))
        {
            if (this.initialMouseClickY == -1.0F)
            {
                boolean var7 = true;

                if (mouseY >= this.top && mouseY <= this.bottom && mouseX >= this.left && mouseX <= this.left + this.listWidth)
                {
                    var10 = mouseY - this.top - this.field_27261_r + (int)this.scrollDistance - 4;
                    var11 = var10 / this.slotHeight;

                    if (mouseX >= boxLeft && mouseX <= boxRight && var11 >= 0 && var10 >= 0 && var11 < listLength)
                    {
                        boolean var12 = var11 == this.selectedIndex && System.currentTimeMillis() - this.lastClickTime < 250L;
                        this.elementClicked(var11, var12);
                        this.selectedIndex = var11;
                        this.lastClickTime = System.currentTimeMillis();
                    }
                    else if (mouseX >= boxLeft && mouseX <= boxRight && var10 < 0)
                    {
                        this.func_27255_a(mouseX - boxLeft, mouseY - this.top + (int)this.scrollDistance - 4);
                        var7 = false;
                    }

                    if (mouseX >= scrollBarXStart && mouseX <= scrollBarXEnd)
                    {
                        this.scrollFactor = -1.0F;
                        var19 = this.getContentHeight() - (this.bottom - this.top - 4);

                        if (var19 < 1)
                        {
                            var19 = 1;
                        }

                        var13 = 20;//(int)((float)((this.bottom - this.top) * (this.bottom - this.top)) / (float)this.getContentHeight());

                        /*if (var13 < 32)
                        {
                            var13 = 32;
                        }

                        if (var13 > this.bottom - this.top - 8)
                        {
                            var13 = this.bottom - this.top - 8;
                        }*/

                        this.scrollFactor /= (float)(this.bottom - this.top - var13) / (float)var19;
                    }
                    else
                    {
                        this.scrollFactor = 1.0F;
                    }

                    if (var7)
                    {
                        this.initialMouseClickY = (float)mouseY;
                    }
                    else
                    {
                        this.initialMouseClickY = -2.0F;
                    }
                }
                else
                {
                    this.initialMouseClickY = -2.0F;
                }
            }
            else if (this.initialMouseClickY >= 0.0F)
            {
                this.scrollDistance -= ((float)mouseY - this.initialMouseClickY) * this.scrollFactor;
                this.initialMouseClickY = (float)mouseY;
            }
        }
        else
        {
            // Slightly modified to account for the possibility of multiple scrolling areas on the same screen
            if (mouseY >= top && mouseY <= bottom && mouseX >= left && mouseX <= right)
            {
	            while (Mouse.next())
	            {
	                int var16 = Mouse.getEventDWheel();
	                
	                if(var16 == 0)
	                {
	                	continue;
	                } else if (var16 > 0)
                    {
                        var16 = -1;
                    }
                    else if (var16 < 0)
                    {
                        var16 = 1;
                    }

                    this.scrollDistance += (float)(var16 * this.slotHeight / 2);
                }
            }

            this.initialMouseClickY = -1.0F;
        }

        this.applyScrollLimits();
        Tessellator var18 = Tessellator.getInstance();
        VertexBuffer worldr = var18.getBuffer();
        
        /*if (this.client.theWorld != null)
        {
            this.drawGradientRect(this.left, this.top, this.right, this.bottom, -1072689136, -804253680);
        }
        else
        {
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_FOG);
            this.client.renderEngine.bindTexture(Gui.optionsBackground);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            float var17 = 32.0F;
            var18.startDrawingQuads();
            var18.setColorOpaque_I(2105376);
            var18.addVertexWithUV((double)this.left, (double)this.bottom, 0.0D, (double)((float)this.left / var17), (double)((float)(this.bottom + (int)this.scrollDistance) / var17));
            var18.addVertexWithUV((double)this.right, (double)this.bottom, 0.0D, (double)((float)this.right / var17), (double)((float)(this.bottom + (int)this.scrollDistance) / var17));
            var18.addVertexWithUV((double)this.right, (double)this.top, 0.0D, (double)((float)this.right / var17), (double)((float)(this.top + (int)this.scrollDistance) / var17));
            var18.addVertexWithUV((double)this.left, (double)this.top, 0.0D, (double)((float)this.left / var17), (double)((float)(this.top + (int)this.scrollDistance) / var17));
            var18.draw();
        }*/
        
 //        boxRight = this.listWidth / 2 - 92 - 16;
        var10 = this.top + 4 - (int)this.scrollDistance;

        if (this.field_27262_q)
        {
            this.func_27260_a(boxRight, var10, var18);
        }

        for (var11 = 0; var11 < listLength; ++var11)
        {
            var19 = var10 + var11 * this.slotHeight + this.field_27261_r;
            var13 = this.slotHeight - 4;

            if (var19 <= this.bottom && var19 + var13 >= this.top)
            {
                if (this.field_25123_p && this.isSelected(var11))
                {
                    int min = this.left;
                    int max = boxRight;
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.disableTexture2D();
                    worldr.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                    worldr.pos(min,     var19 + var13 + 2, 0).tex(0, 1).color(0x80, 0x80, 0x80, 0xFF).endVertex();
                    worldr.pos(max,     var19 + var13 + 2, 0).tex(1, 1).color(0x80, 0x80, 0x80, 0xFF).endVertex();
                    worldr.pos(max,     var19         - 2, 0).tex(1, 0).color(0x80, 0x80, 0x80, 0xFF).endVertex();
                    worldr.pos(min,     var19         - 2, 0).tex(0, 0).color(0x80, 0x80, 0x80, 0xFF).endVertex();
                    worldr.pos(min + 1, var19 + var13 + 1, 0).tex(0, 1).color(0x00, 0x00, 0x00, 0xFF).endVertex();
                    worldr.pos(max - 1, var19 + var13 + 1, 0).tex(1, 1).color(0x00, 0x00, 0x00, 0xFF).endVertex();
                    worldr.pos(max - 1, var19         - 1, 0).tex(1, 0).color(0x00, 0x00, 0x00, 0xFF).endVertex();
                    worldr.pos(min + 1, var19         - 1, 0).tex(0, 0).color(0x00, 0x00, 0x00, 0xFF).endVertex();
                    var18.draw();
                    GlStateManager.enableTexture2D();
                }

                this.drawSlot(var11, boxRight, var19, var13, var18);
            }
        }

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        //byte var20 = 4;
        if (this.client.theWorld == null)
        {
            this.drawGradientRect(this.left, this.top, this.right, this.bottom, 0xC0101010, 0xD0101010);
        }
        
        RenderUtils.DrawLine(left, top, right, top, 1F, ThemeRegistry.curTheme().textColor());
        RenderUtils.DrawLine(left, bottom, right, bottom, 1F, ThemeRegistry.curTheme().textColor());
        
        /*GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        var18.startDrawingQuads();
        var18.setColorRGBA_I(0, 0);
        var18.addVertexWithUV((double)this.left, (double)(this.top + var20), 0.0D, 0.0D, 1.0D);
        var18.addVertexWithUV((double)this.right, (double)(this.top + var20), 0.0D, 1.0D, 1.0D);
        var18.setColorRGBA_I(0, 255);
        var18.addVertexWithUV((double)this.right, (double)this.top, 0.0D, 1.0D, 0.0D);
        var18.addVertexWithUV((double)this.left, (double)this.top, 0.0D, 0.0D, 0.0D);
        var18.draw();
        var18.startDrawingQuads();
        var18.setColorRGBA_I(0, 255);
        var18.addVertexWithUV((double)this.left, (double)this.bottom, 0.0D, 0.0D, 1.0D);
        var18.addVertexWithUV((double)this.right, (double)this.bottom, 0.0D, 1.0D, 1.0D);
        var18.setColorRGBA_I(0, 0);
        var18.addVertexWithUV((double)this.right, (double)(this.bottom - var20), 0.0D, 1.0D, 0.0D);
        var18.addVertexWithUV((double)this.left, (double)(this.bottom - var20), 0.0D, 0.0D, 0.0D);
        var18.draw();*/
        var19 = this.getContentHeight() - (this.bottom - this.top - 4);

        if (var19 > 0)
        {
            /*var13 = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();

            if (var13 < 32)
            {
                var13 = 32;
            }*/
        	
        	var13 = 20;

            /*if (var13 > this.bottom - this.top - 8)
            {
                var13 = this.bottom - this.top - 8;
            }*/
            
            int var14 = (int)this.scrollDistance * (this.bottom - this.top - var13) / var19 + this.top;
            
            if (var14 < this.top)
            {
                var14 = this.top;
            }
            
            client.renderEngine.bindTexture(ThemeRegistry.curTheme().guiTexture());
            
            // Makes things easier on my brain to pre-calculate all this first
            int n1 = listHeight%20;
            n1 = n1 == 0? 20 : n1; // Cannot be 0
            int n2 = n1/2;
            int n3 = n1 - n2;
            
            this.drawTexturedModalRect(scrollBarXStart, top, 248, 0, 8, n2);
            this.drawTexturedModalRect(scrollBarXStart, bottom - n3, 248, 40 + (20 - n3), 8, n3);
            
            /*System.out.println("n2: " + n2);
            System.out.println("n3: " + n3);
            System.out.println("height: " + listHeight);
            System.out.println("result: " + (listHeight - (n2 + n3)));*/
            
            for(int i = 0; i < (listHeight - (n2 + n3))/20; i++)
            {
            	this.drawTexturedModalRect(scrollBarXStart, top + n2 + (i * 20), 248, 20, 8, 20);
            }
            
            //System.out.println("Scroll: " + scrollDistance + "/" + this.getContentHeight());
            this.drawTexturedModalRect(scrollBarXStart, var14, 248, 60, 8, 20);
            
            /*var18.startDrawingQuads();
            var18.setColorRGBA_I(0, 255);
            var18.addVertexWithUV((double)scrollBarXStart, (double)this.bottom, 0.0D, 0.0D, 1.0D);
            var18.addVertexWithUV((double)scrollBarXEnd, (double)this.bottom, 0.0D, 1.0D, 1.0D);
            var18.addVertexWithUV((double)scrollBarXEnd, (double)this.top, 0.0D, 1.0D, 0.0D);
            var18.addVertexWithUV((double)scrollBarXStart, (double)this.top, 0.0D, 0.0D, 0.0D);
            var18.draw();
            var18.startDrawingQuads();
            var18.setColorRGBA_I(8421504, 255);
            var18.addVertexWithUV((double)scrollBarXStart, (double)(var14 + var13), 0.0D, 0.0D, 1.0D);
            var18.addVertexWithUV((double)scrollBarXEnd, (double)(var14 + var13), 0.0D, 1.0D, 1.0D);
            var18.addVertexWithUV((double)scrollBarXEnd, (double)var14, 0.0D, 1.0D, 0.0D);
            var18.addVertexWithUV((double)scrollBarXStart, (double)var14, 0.0D, 0.0D, 0.0D);
            var18.draw();
            var18.startDrawingQuads();
            var18.setColorRGBA_I(12632256, 255);
            var18.addVertexWithUV((double)scrollBarXStart, (double)(var14 + var13 - 1), 0.0D, 0.0D, 1.0D);
            var18.addVertexWithUV((double)(scrollBarXEnd - 1), (double)(var14 + var13 - 1), 0.0D, 1.0D, 1.0D);
            var18.addVertexWithUV((double)(scrollBarXEnd - 1), (double)var14, 0.0D, 1.0D, 0.0D);
            var18.addVertexWithUV((double)scrollBarXStart, (double)var14, 0.0D, 0.0D, 0.0D);
            var18.draw();*/
        }

        /*this.func_27257_b(mouseX, mouseY);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);*/
    }

    protected void drawGradientRect(int left, int top, int right, int bottom, int color1, int color2)
    {
        float a1 = (float)(color1 >> 24 & 255) / 255.0F;
        float r1 = (float)(color1 >> 16 & 255) / 255.0F;
        float g1 = (float)(color1 >>  8 & 255) / 255.0F;
        float b1 = (float)(color1       & 255) / 255.0F;
        float a2 = (float)(color2 >> 24 & 255) / 255.0F;
        float r2 = (float)(color2 >> 16 & 255) / 255.0F;
        float g2 = (float)(color2 >>  8 & 255) / 255.0F;
        float b2 = (float)(color2       & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer worldrenderer = tessellator.getBuffer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(right, top, 0.0D).color(r1, g1, b1, a1).endVertex();
        worldrenderer.pos(left,  top, 0.0D).color(r1, g1, b1, a1).endVertex();
        worldrenderer.pos(left,  bottom, 0.0D).color(r2, g2, b2, a2).endVertex();
        worldrenderer.pos(right, bottom, 0.0D).color(r2, g2, b2, a2).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    /**
     * Draws a textured rectangle at the stored z-value. Args: x, y, u, v, width, height
     */
    public void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height)
    {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer worldrenderer = tessellator.getBuffer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos((double)(x + 0), (double)(y + height), 0).tex((double)((float)(textureX + 0) * f), (double)((float)(textureY + height) * f1)).endVertex();
        worldrenderer.pos((double)(x + width), (double)(y + height), 0).tex((double)((float)(textureX + width) * f), (double)((float)(textureY + height) * f1)).endVertex();
        worldrenderer.pos((double)(x + width), (double)(y + 0), 0).tex((double)((float)(textureX + width) * f), (double)((float)(textureY + 0) * f1)).endVertex();
        worldrenderer.pos((double)(x + 0), (double)(y + 0), 0).tex((double)((float)(textureX + 0) * f), (double)((float)(textureY + 0) * f1)).endVertex();
        tessellator.draw();
    }
}
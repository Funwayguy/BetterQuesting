package betterquesting.api.utils;

import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.core.BetterQuesting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// TODO: Move text related stuff to its own utility class
@SideOnly(Side.CLIENT)
public class RenderUtils {
    public static final String REGEX_NUMBER = "[^\\.0123456789-]"; // I keep screwing this up so now it's reusable

    public static void RenderItemStack(Minecraft mc, ItemStack stack, int x, int y, String text) {
        RenderItemStack(mc, stack, x, y, text, Color.WHITE.getRGB());
    }

    public static void RenderItemStack(Minecraft mc, ItemStack stack, int x, int y, String text, Color color) {
        RenderItemStack(mc, stack, x, y, text, color.getRGB());
    }

    public static void RenderItemStack(Minecraft mc, ItemStack stack, int x, int y, String text, int color) {
        RenderItemStack(mc, stack, x, y, 16F, text, color);
    }

    public static void RenderItemStack(Minecraft mc, ItemStack stack, int x, int y, float z, String text, int color) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        GlStateManager.pushMatrix();
        RenderItem itemRender = mc.getRenderItem();
        float preZ = itemRender.zLevel;

        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        GlStateManager.color(r, g, b);
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableDepth();

        GlStateManager.translate(0.0F, 0.0F, z);
        itemRender.zLevel = -150F; // Counters internal Z depth change so that GL translation makes sense

        FontRenderer font = stack.getItem().getFontRenderer(stack);
        if (font == null) font = mc.fontRenderer;

        try {
            itemRender.renderItemAndEffectIntoGUI(stack, x, y);

            if (stack.getCount() != 1 || text != null) {
                GlStateManager.pushMatrix();

                int w = getStringWidth(text, font);
                float tx;
                float ty;
                float s = 1F;

                if (w > 17) {
                    s = 17F / w;
                    tx = 0;
                    ty = 17 - font.FONT_HEIGHT * s;
                } else {
                    tx = 17 - w;
                    ty = 18 - font.FONT_HEIGHT;
                }

                GlStateManager.translate(x + tx, y + ty, 0);
                GlStateManager.scale(s, s, 1F);

                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableBlend();

                font.drawString(text, 0, 0, 16777215, true);

                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
                GlStateManager.enableBlend();

                GlStateManager.popMatrix();
            }

            itemRender.renderItemOverlayIntoGUI(font, stack, x, y, "");
        } catch (Exception e) {
            BetterQuesting.logger.warn("Unabled to render item " + stack, e);
        }

        GlStateManager.disableDepth();
        RenderHelper.disableStandardItemLighting();

        itemRender.zLevel = preZ; // Just in case

        GlStateManager.popMatrix();
    }

    public static void RenderEntity(int posX, int posY, int scale, float rotation, float pitch, Entity entity) {
        RenderEntity(posX, posY, 64F, scale, rotation, pitch, entity);
    }

    public static void RenderEntity(float posX, float posY, float posZ, int scale, float rotation, float pitch, Entity entity) {
        try {
            GlStateManager.enableColorMaterial();
            GlStateManager.pushMatrix();
            GlStateManager.enableDepth();
            GlStateManager.translate(posX, posY, posZ);
            GlStateManager.scale((float) -scale, (float) scale, (float) scale); // Not entirely sure why mobs are flipped but this is how vanilla GUIs fix it so...
            GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(pitch, 1F, 0F, 0F);
            GlStateManager.rotate(rotation, 0F, 1F, 0F);
            float f3 = entity.rotationYaw;
            float f4 = entity.rotationPitch;
            float f5 = entity.prevRotationYaw;
            float f6 = entity.prevRotationPitch;
            entity.rotationYaw = 0;
            entity.rotationPitch = 0;
            entity.prevRotationYaw = 0;
            entity.prevRotationPitch = 0;
            EntityLivingBase livingBase = (entity instanceof EntityLivingBase) ? (EntityLivingBase) entity : null;
            float f7 = livingBase == null ? 0 : livingBase.renderYawOffset;
            float f8 = livingBase == null ? 0 : livingBase.rotationYawHead;
            float f9 = livingBase == null ? 0 : livingBase.prevRotationYawHead;
            if (livingBase != null) {
                livingBase.renderYawOffset = 0;
                livingBase.rotationYawHead = 0;
                livingBase.prevRotationYawHead = 0;
            }

            RenderHelper.enableStandardItemLighting();
            RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
            rendermanager.setPlayerViewY(180.0F);
            rendermanager.renderEntity(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
            entity.rotationYaw = f3;
            entity.rotationPitch = f4;
            entity.prevRotationYaw = f5;
            entity.prevRotationPitch = f6;
            if (livingBase != null) {
                livingBase.renderYawOffset = f7;
                livingBase.rotationYawHead = f8;
                livingBase.prevRotationYawHead = f9;
            }
            GlStateManager.disableDepth();
            GlStateManager.popMatrix();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.disableTexture2D();
            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.enableTexture2D(); // Breaks subsequent text rendering if not included
            GlStateManager.disableColorMaterial();
        } catch (Exception e) {
            // Hides rendering errors with entities which are common for invalid/technical entities
        }
    }

    public static void DrawLine(int x1, int y1, int x2, int y2, float width, int color) {
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        GlStateManager.pushMatrix();

        GlStateManager.disableTexture2D();
        GlStateManager.color(r, g, b, 1F);
        GL11.glLineWidth(width);

        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x2, y2);
        GL11.glEnd();

        GlStateManager.enableTexture2D();
        GlStateManager.color(1F, 1F, 1F, 1F);

        GlStateManager.popMatrix();
    }

    public static void drawSplitString(FontRenderer renderer, String string, int x, int y, int width, int color, boolean shadow) {
        drawSplitString(renderer, string, x, y, width, color, shadow, 0, splitString(string, width, renderer).size() - 1);
    }

    public static void drawSplitString(FontRenderer renderer, String string, int x, int y, int width, int color, boolean shadow, int start, int end) {
        drawHighlightedSplitString(renderer, string, x, y, width, color, shadow, start, end, 0, 0, 0);
    }

    // TODO: Clean this up. The list of parameters is getting a bit excessive

    public static void drawHighlightedSplitString(FontRenderer renderer, String string, int x, int y, int width, int color, boolean shadow, int highlightColor, int highlightStart, int highlightEnd) {
        drawHighlightedSplitString(renderer, string, x, y, width, color, shadow, 0, splitString(string, width, renderer).size() - 1, highlightColor, highlightStart, highlightEnd);
    }

    public static void drawHighlightedSplitString(FontRenderer renderer, String string, int x, int y, int width, int color, boolean shadow, int start, int end, int highlightColor, int highlightStart, int highlightEnd) {
        if (renderer == null || string == null || string.length() <= 0 || start > end) {
            return;
        }

        string = string.replaceAll("\r", ""); //Line endings from localizations break things so we remove them

        List<String> list = splitString(string, width, renderer);
        List<String> noFormat = splitStringWithoutFormat(string, width, renderer); // Needed for accurate highlight index positions

        if (list.size() != noFormat.size()) {
            //BetterQuesting.logger.error("Line count mismatch (" + list.size() + " != " + noFormat.size() + ") while drawing formatted text!");
            return;
        }

        int hlStart = Math.min(highlightStart, highlightEnd);
        int hlEnd = Math.max(highlightStart, highlightEnd);
        int idxStart = 0;

        for (int i = 0; i < start; i++) {
            if (i >= noFormat.size()) {
                break;
            }

            idxStart += noFormat.get(i).length();
        }

        // Text rendering is very vulnerable to colour leaking
        GlStateManager.color(1F, 1F, 1F, 1F);

        for (int i = start; i <= end; i++) {
            if (i < 0 || i >= list.size()) {
                continue;
            }

            renderer.drawString(list.get(i), x, y + (renderer.FONT_HEIGHT * (i - start)), color, shadow);

            // DEBUG
			/*boolean b = (System.currentTimeMillis()/1000)%2 == 0;
			
			if(b)
			{
				renderer.drawString(i + ": " + list.get(i), x, y + (renderer.FONT_HEIGHT * (i - start)), color, shadow);
			}
			
			if(i >= noFormat.size())
			{
				continue;
			}
			
			if(!b)
			{
				renderer.drawString(i + ": " + noFormat.get(i), x, y + (renderer.FONT_HEIGHT * (i - start)), color, shadow);
			}*/

            int lineSize = noFormat.get(i).length();
            int idxEnd = idxStart + lineSize;

            int i1 = Math.max(idxStart, hlStart) - idxStart;
            int i2 = Math.min(idxEnd, hlEnd) - idxStart;

            if (!(i1 == i2 || i1 < 0 || i2 < 0 || i1 > lineSize || i2 > lineSize)) {
                String lastFormat = FontRenderer.getFormatFromString(list.get(i));
                int x1 = getStringWidth(lastFormat + noFormat.get(i).substring(0, i1), renderer);
                int x2 = getStringWidth(lastFormat + noFormat.get(i).substring(0, i2), renderer);

                drawHighlightBox(x + x1, y + (renderer.FONT_HEIGHT * (i - start)), x + x2, y + (renderer.FONT_HEIGHT * (i - start)) + renderer.FONT_HEIGHT, highlightColor);
            }

            idxStart = idxEnd;
        }
    }

    public static void drawHighlightedString(FontRenderer renderer, String string, int x, int y, int color, boolean shadow, int highlightColor, int highlightStart, int highlightEnd) {
        if (renderer == null || string == null || string.length() <= 0) {
            return;
        }

        renderer.drawString(string, x, y, color, shadow);

        int hlStart = Math.min(highlightStart, highlightEnd);
        int hlEnd = Math.max(highlightStart, highlightEnd);
        int size = string.length();

        int i1 = MathHelper.clamp(hlStart, 0, size);
        int i2 = MathHelper.clamp(hlEnd, 0, size);

        if (i1 != i2) {
            int x1 = getStringWidth(string.substring(0, i1), renderer);
            int x2 = getStringWidth(string.substring(0, i2), renderer);

            drawHighlightBox(x + x1, y, x + x2, y + renderer.FONT_HEIGHT, highlightColor);
        }
    }

    public static void drawHighlightBox(IGuiRect rect, IGuiColor color) {
        drawHighlightBox(rect.getX(), rect.getY(), rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight(), color.getRGB());
    }

    public static void drawHighlightBox(int left, int top, int right, int bottom, int color) {
        if (left < right) {
            int i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            int j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;

        GlStateManager.pushMatrix();

        GL11.glDisable(GL11.GL_TEXTURE_2D);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.color(f, f1, f2, f3);
        GlStateManager.disableTexture2D();
        GlStateManager.enableColorLogic();
        GlStateManager.colorLogicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
        bufferbuilder.pos((double) left, (double) bottom, 0.0D).endVertex();
        bufferbuilder.pos((double) right, (double) bottom, 0.0D).endVertex();
        bufferbuilder.pos((double) right, (double) top, 0.0D).endVertex();
        bufferbuilder.pos((double) left, (double) top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();

        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GlStateManager.popMatrix();
    }

    public static void drawColoredRect(IGuiRect rect, IGuiColor color) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vertexbuffer = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        color.applyGlColor();
        vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        vertexbuffer.pos((double) rect.getX(), (double) rect.getY() + rect.getHeight(), 0.0D).endVertex();
        vertexbuffer.pos((double) rect.getX() + rect.getWidth(), (double) rect.getY() + rect.getHeight(), 0.0D).endVertex();
        vertexbuffer.pos((double) rect.getX() + rect.getWidth(), (double) rect.getY(), 0.0D).endVertex();
        vertexbuffer.pos((double) rect.getX(), (double) rect.getY(), 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private static final IGuiColor STENCIL_COLOR = new GuiColorStatic(0, 0, 0, 255);
    private static int stencilDepth = 0;

    public static void startScissor(IGuiRect rect) {
        if (stencilDepth >= 255) {
            throw new IndexOutOfBoundsException("Exceeded the maximum number of nested stencils (255)");
        }

        if (stencilDepth == 0) {
            GL11.glEnable(GL11.GL_STENCIL_TEST);
            GL11.glStencilMask(0xFF);
            GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        }

        // Note: This is faster with inverted logic (skips depth tests when writing)
        GL11.glStencilFunc(GL11.GL_LESS, stencilDepth, 0xFF);
        GL11.glStencilOp(GL11.GL_INCR, GL11.GL_KEEP, GL11.GL_KEEP);
        GL11.glStencilMask(0xFF);

        GL11.glColorMask(false, false, false, false);
        GL11.glDepthMask(false);

        drawColoredRect(rect, STENCIL_COLOR);

        GL11.glStencilMask(0x00);
        GL11.glStencilFunc(GL11.GL_EQUAL, stencilDepth + 1, 0xFF);

        GL11.glColorMask(true, true, true, true);
        GL11.glDepthMask(true);

        stencilDepth++;
    }

    private static void fillScreen() {
        int w = Minecraft.getMinecraft().displayWidth;
        int h = Minecraft.getMinecraft().displayHeight;

        GL11.glPushAttrib(GL11.GL_TEXTURE_BIT | GL11.GL_DEPTH_TEST | GL11.GL_LIGHTING);

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(0, w, h, 0, -1, 1);  //or whatever size you want

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();

        drawColoredRect(new GuiRectangle(0, 0, w, h), STENCIL_COLOR);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();

        GL11.glPopAttrib();
    }

    /**
     * Pops the last scissor off the stack and returns to the last parent scissor or disables it if there are none
     */
    public static void endScissor() {
        stencilDepth--;

        if (stencilDepth < 0) {
            throw new IndexOutOfBoundsException("No stencil to end");
        } else if (stencilDepth == 0) {
            GL11.glStencilMask(0xFF);
            GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT); // Note: Clearing actually requires the mask to be enabled

            GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
            GL11.glStencilMask(0x00);

            GL11.glDisable(GL11.GL_STENCIL_TEST);
        } else {
            GL11.glStencilFunc(GL11.GL_LEQUAL, stencilDepth, 0xFF);
            GL11.glStencilOp(GL11.GL_DECR, GL11.GL_KEEP, GL11.GL_KEEP);
            GL11.glStencilMask(0xFF);

            GL11.glColorMask(false, false, false, false);
            GL11.glDepthMask(false);

            fillScreen();

            GL11.glColorMask(true, true, true, true);
            GL11.glDepthMask(true);

            GL11.glStencilFunc(GL11.GL_EQUAL, stencilDepth, 0xFF);
            GL11.glStencilMask(0x00);
        }
    }

    /**
     * Similar to normally splitting a string with the fontRenderer however this variant does
     * not attempt to preserve the formatting between lines. This is particularly important when the
     * index positions in the text are required to match the original unwrapped text.
     */
    public static List<String> splitStringWithoutFormat(String str, int wrapWidth, FontRenderer font) {
        List<String> list = new ArrayList<>();

        String lastFormat = ""; // Formatting like bold can affect the wrapping width
        String temp = str;

        while (true) {
            int i = sizeStringToWidth(lastFormat + temp, wrapWidth, font); // Cut to size WITH formatting
            i -= lastFormat.length(); // Remove formatting characters from count

            if (temp.length() <= i) {
                list.add(temp);
                break;
            } else {
                String s = temp.substring(0, i);
                char c0 = temp.charAt(i);
                boolean flag = c0 == ' ' || c0 == '\n';
                lastFormat = FontRenderer.getFormatFromString(lastFormat + s);
                temp = temp.substring(i + (flag ? 1 : 0));
                // NOTE: The index actually stops just before the space/nl so we don't need to remove it from THIS line. This is why the previous line moves forward by one for the NEXT line
                list.add(s + (flag ? "\n" : "")); // Although we need to remove the spaces between each line we have to replace them with invisible new line characters to preserve the index count

                if (temp.length() <= 0 && !flag) {
                    break;
                }
            }
        }

        return list;
    }

    public static List<String> splitString(String str, int wrapWidth, FontRenderer font) {
        List<String> list = new ArrayList<>();

        String temp = str;

        while (true) {
            int i = sizeStringToWidth(temp, wrapWidth, font); // Cut to size WITH formatting

            if (temp.length() <= i) {
                list.add(temp);
                break;
            } else {
                String s = temp.substring(0, i);
                char c0 = temp.charAt(i);
                boolean flag = c0 == ' ' || c0 == '\n';
                temp = FontRenderer.getFormatFromString(s) + temp.substring(i + (flag ? 1 : 0));
                list.add(s);

                if (temp.length() <= 0 && !flag) {
                    break;
                }
            }
        }

        return list;
    }

    /**
     * Returns the index position under a given set of coordinates in a piece of text
     */
    public static int getCursorPos(String text, int x, FontRenderer font) {
        if (text.length() <= 0) {
            return 0;
        }

        int i = 0;

        for (; i < text.length(); i++) {
            if (getStringWidth(text.substring(0, i + 1), font) > x) {
                break;
            }
        }

        if (i - 1 >= 0 && text.charAt(i - 1) == '\n') {
            return i - 1;
        }

        return i;
    }

    /**
     * Returns the index position under a given set of coordinates in a wrapped piece of text
     */
    public static int getCursorPos(String text, int x, int y, int width, FontRenderer font) {
        List<String> tLines = RenderUtils.splitStringWithoutFormat(text, width, font);

        if (tLines.size() <= 0) {
            return 0;
        }

        int row = MathHelper.clamp(y / font.FONT_HEIGHT, 0, tLines.size() - 1);
        String lastFormat = "";
        String line;
        int idx = 0;

        for (int i = 0; i < row; i++) {
            line = tLines.get(i);
            idx += line.length();
            lastFormat = FontRenderer.getFormatFromString(lastFormat + line);
        }

        return idx + getCursorPos(lastFormat + tLines.get(row), x, font) - lastFormat.length();
    }

    private static int sizeStringToWidth(String str, int wrapWidth, FontRenderer font) {
        int i = str.length();
        int j = 0;
        int k = 0;
        int l = -1;

        for (boolean flag = false; k < i; ++k) {
            char c0 = str.charAt(k);

            switch (c0) {
                case '\n':
                    --k;
                    break;
                case ' ':
                    l = k;
                default:
                    j += font.getCharWidth(c0);

                    if (flag) {
                        ++j;
                    }

                    break;
                case '\u00a7':

                    if (k < i - 1) {
                        ++k;
                        char c1 = str.charAt(k);

                        if (c1 != 'l' && c1 != 'L') {
                            if (c1 == 'r' || c1 == 'R' || isFormatColor(c1)) {
                                flag = false;
                            }
                        } else {
                            flag = true;
                        }
                    }
            }

            if (c0 == '\n') {
                ++k;
                l = k;
                break;
            }

            if (j > wrapWidth) {
                break;
            }
        }

        return k != i && l != -1 && l < k ? l : k;
    }

    private static boolean isFormatColor(char colorChar) {
        return colorChar >= '0' && colorChar <= '9' || colorChar >= 'a' && colorChar <= 'f' || colorChar >= 'A' && colorChar <= 'F';
    }

    public static float lerpFloat(float f1, float f2, float blend) {
        return (f2 * blend) + (f1 * (1F - blend));
    }

    public static double lerpDouble(double d1, double d2, double blend) {
        return (d2 * blend) + (d1 * (1D - blend));
    }

    public static int lerpRGB(int c1, int c2, float blend) {
        float a1 = c1 >> 24 & 255;
        float r1 = c1 >> 16 & 255;
        float g1 = c1 >> 8 & 255;
        float b1 = c1 & 255;

        float a2 = c2 >> 24 & 255;
        float r2 = c2 >> 16 & 255;
        float g2 = c2 >> 8 & 255;
        float b2 = c2 & 255;

        int a3 = (int) lerpFloat(a1, a2, blend);
        int r3 = (int) lerpFloat(r1, r2, blend);
        int g3 = (int) lerpFloat(g1, g2, blend);
        int b3 = (int) lerpFloat(b1, b2, blend);

        return (a3 << 24) + (r3 << 16) + (g3 << 8) + b3;
    }

    public static void drawHoveringText(List<String> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight, int maxTextWidth, FontRenderer font) {
        drawHoveringText(ItemStack.EMPTY, textLines, mouseX, mouseY, screenWidth, screenHeight, maxTextWidth, font);
    }

    /**
     * Modified version of Forge's tooltip rendering that doesn't adjust Z depth
     */
    public static void drawHoveringText(@Nonnull final ItemStack stack, List<String> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight, int maxTextWidth, FontRenderer font) {
        if (textLines == null || textLines.isEmpty()) return;

        RenderTooltipEvent.Pre event = new RenderTooltipEvent.Pre(stack, textLines, mouseX, mouseY, screenWidth, screenHeight, maxTextWidth, font);
        if (MinecraftForge.EVENT_BUS.post(event)) return;

        mouseX = event.getX();
        mouseY = event.getY();
        screenWidth = event.getScreenWidth();
        screenHeight = event.getScreenHeight();
        maxTextWidth = event.getMaxWidth();
        font = event.getFontRenderer();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0F, 0F, 32F);
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        //GlStateManager.enableDepth();
        GlStateManager.disableDepth();
        int tooltipTextWidth = 0;

        for (String textLine : textLines) {
            int textLineWidth = getStringWidth(textLine, font);

            if (textLineWidth > tooltipTextWidth) {
                tooltipTextWidth = textLineWidth;
            }
        }

        boolean needsWrap = false;

        int titleLinesCount = 1;
        int tooltipX = mouseX + 12;

        if (tooltipX + tooltipTextWidth + 4 > screenWidth) {
            tooltipX = mouseX - 16 - tooltipTextWidth;

            if (tooltipX < 4) // if the tooltip doesn't fit on the screen
            {
                if (mouseX > screenWidth / 2) {
                    tooltipTextWidth = mouseX - 12 - 8;
                } else {
                    tooltipTextWidth = screenWidth - 16 - mouseX;
                }
                needsWrap = true;
            }
        }

        if (maxTextWidth > 0 && tooltipTextWidth > maxTextWidth) {
            tooltipTextWidth = maxTextWidth;
            needsWrap = true;
        }

        if (needsWrap) {
            int wrappedTooltipWidth = 0;
            List<String> wrappedTextLines = new ArrayList<>();

            for (int i = 0; i < textLines.size(); i++) {
                String textLine = textLines.get(i);
                List<String> wrappedLine = font.listFormattedStringToWidth(textLine, tooltipTextWidth);
                if (i == 0) {
                    titleLinesCount = wrappedLine.size();
                }

                for (String line : wrappedLine) {
                    int lineWidth = getStringWidth(line, font);
                    if (lineWidth > wrappedTooltipWidth) {
                        wrappedTooltipWidth = lineWidth;
                    }
                    wrappedTextLines.add(line);
                }
            }

            tooltipTextWidth = wrappedTooltipWidth;
            textLines = wrappedTextLines;

            if (mouseX > screenWidth / 2) {
                tooltipX = mouseX - 16 - tooltipTextWidth;
            } else {
                tooltipX = mouseX + 12;
            }
        }

        int tooltipY = mouseY - 12;
        int tooltipHeight = 8;

        if (textLines.size() > 1) {
            tooltipHeight += (textLines.size() - 1) * 10;

            if (textLines.size() > titleLinesCount) {
                tooltipHeight += 2; // gap between title lines and next lines
            }
        }

        if (tooltipY < 4) {
            tooltipY = 4;
        } else if (tooltipY + tooltipHeight + 4 > screenHeight) {
            tooltipY = screenHeight - tooltipHeight - 4;
        }
		
		/*int backgroundColor = 0xF0100010;
		int borderColorStart = 0x505000FF;
		int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
		
		RenderTooltipEvent.Color colorEvent = new RenderTooltipEvent.Color(stack, textLines, tooltipX, tooltipY, font, backgroundColor, borderColorStart, borderColorEnd);
		MinecraftForge.EVENT_BUS.post(colorEvent);
		backgroundColor = colorEvent.getBackground();
		borderColorStart = colorEvent.getBorderStart();
		borderColorEnd = colorEvent.getBorderEnd();
		
		GuiUtils.drawGradientRect(0, tooltipX - 3, tooltipY - 4, tooltipX + tooltipTextWidth + 3, tooltipY - 3, backgroundColor, backgroundColor);
		GuiUtils.drawGradientRect(0, tooltipX - 3, tooltipY + tooltipHeight + 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 4, backgroundColor, backgroundColor);
		GuiUtils.drawGradientRect(0, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
		GuiUtils.drawGradientRect(0, tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
		GuiUtils.drawGradientRect(0, tooltipX + tooltipTextWidth + 3, tooltipY - 3, tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
		GuiUtils.drawGradientRect(0, tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
		GuiUtils.drawGradientRect(0, tooltipX + tooltipTextWidth + 2, tooltipY - 3 + 1, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
		GuiUtils.drawGradientRect(0, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3, tooltipY - 3 + 1, borderColorStart, borderColorStart);
		GuiUtils.drawGradientRect(0, tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, borderColorEnd, borderColorEnd);

		MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostBackground(stack, textLines, tooltipX, tooltipY, font, tooltipTextWidth, tooltipHeight));*/
        PresetTexture.TOOLTIP_BG.getTexture().drawTexture(tooltipX - 4, tooltipY - 4, tooltipTextWidth + 8, tooltipHeight + 8, 0F, 1F);
        int tooltipTop = tooltipY;

        GlStateManager.translate(0F, 0F, 0.1F);

        for (int lineNumber = 0; lineNumber < textLines.size(); ++lineNumber) {
            String line = textLines.get(lineNumber);
            font.drawStringWithShadow(line, (float) tooltipX, (float) tooltipY, -1);

            if (lineNumber + 1 == titleLinesCount) {
                tooltipY += 2;
            }

            tooltipY += 10;
        }

        MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostText(stack, textLines, tooltipX, tooltipTop, font, tooltipTextWidth, tooltipHeight));

        GlStateManager.enableLighting();
        //GlStateManager.disableDepth();
        //GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.popMatrix();
    }

    /**
     * A version of getStringWidth that actually behaves according to the format resetting rules of colour codes. Minecraft's built in one is busted!
     */
    public static int getStringWidth(String text, FontRenderer font) {
        if (text == null || text.length() == 0) return 0;

        int maxWidth = 0;
        int curLineWidth = 0;
        boolean bold = false;

        for (int j = 0; j < text.length(); ++j) {
            char c0 = text.charAt(j);
            int k = font.getCharWidth(c0);

            if (k < 0 && j < text.length() - 1) // k should only be negative when the section sign has been used!
            {
                // Move the caret to the formatting character and read from there
                ++j;
                c0 = text.charAt(j);

                if (c0 != 'l' && c0 != 'L') {
                    int ci = "0123456789abcdefklmnor".indexOf(String.valueOf(c0).toLowerCase(Locale.ROOT).charAt(0));
                    //if (c0 == 'r' || c0 == 'R') // Minecraft's original implemention. This is broken...
                    if (ci < 16 || ci == 21) // Reset bolding. Now supporting colour AND reset codes!
                    {
                        bold = false;
                    }
                } else // This is the bold format on. Time to get T H I C C
                {
                    bold = true;
                }

                k = 0; // Fix the negative value the section symbol previously set
            }

            curLineWidth += k;

            if (bold && k > 0) // This is a bolded normal character which is 1px thicker
            {
                ++curLineWidth;
            }

            if (c0 == '\n') // New line. Reset counting width
            {
                maxWidth = Math.max(maxWidth, curLineWidth);
                curLineWidth = 0;
            }
        }

        return Math.max(maxWidth, curLineWidth);
    }
}

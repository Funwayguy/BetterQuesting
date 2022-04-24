package betterquesting.api2.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class BqFontRenderer extends FontRenderer {
    public static final BqFontRenderer FONT_UNICODE = new BqFontRenderer(true);
    public static final BqFontRenderer FONT_STANDARD = new BqFontRenderer(false);

    private static final ResourceLocation[] UNICODE_PAGE_LOCATIONS = new ResourceLocation[256];

    private boolean isSmall = false;

    public BqFontRenderer(boolean unicode) {
        super(Minecraft.getMinecraft().gameSettings, new ResourceLocation("minecraft:textures/font/ascii.png"), Minecraft.getMinecraft().renderEngine, unicode);
        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(this);
    }

    @Override
    public int drawString(String text, float x, float y, int color, boolean dropShadow) {
        return drawStringScaled(text, x, y, color, dropShadow, 1F);
    }

    public int drawStringScaled(String text, float x, float y, int color, boolean shadow, float scale) {
        if (scale <= 0F) return 0;

        Minecraft mc = Minecraft.getMinecraft();

        if (scale == 1F && !(mc.gameSettings.guiScale > 0 && mc.gameSettings.guiScale < 3)) {
            isSmall = false;
            return super.drawString(text, x, y, color, shadow);
        } else {
            isSmall = (mc.gameSettings.guiScale > 0 && mc.gameSettings.guiScale < 3) || scale <= 0.5F;

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, 0F);
            GlStateManager.scale(scale, scale, 0F);

            int r = super.drawString(text, 0, 0, color, shadow);

            GlStateManager.popMatrix();

            return r;
        }
    }

    @Override
    protected float renderUnicodeChar(char ch, boolean italic) {
        int i = this.glyphWidth[ch] & 255;

        if (i == 0) {
            return 0.0F;
        } else {
            int j = ch / 256;
            this.loadGlyphTexture(j);
            int k = i >>> 4;
            int l = i & 15;
            double f = (double) k;
            double f1 = (double) (l + 1);
            double f2 = (double) (ch % 16 * 16) + f;
            double f3 = (double) ((ch & 255) / 16 * 16);
            double f4 = f1 - f - 0.02D;
            double f5 = italic ? 1.0D : 0.0D;
            double ys = 7.99D;

            if (isSmall) {
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            }

            //GlStateManager.glTexCoord2f(f2 / 256.0F, f3 / 256.0F);
            //GlStateManager.glVertex3f(this.posX + f5, this.posY, 0.0F);
            //GlStateManager.glTexCoord2f(f2 / 256.0F, (f3 + 15.98F) / 256.0F);
            //GlStateManager.glVertex3f(this.posX - f5, this.posY + ys, 0.0F);
            //GlStateManager.glTexCoord2f((f2 + f4) / 256.0F, f3 / 256.0F);
            //GlStateManager.glVertex3f(this.posX + f4 / 2.0F + f5, this.posY, 0.0F);
            //GlStateManager.glTexCoord2f((f2 + f4) / 256.0F, (f3 + 15.98F) / 256.0F);
            //GlStateManager.glVertex3f(this.posX + f4 / 2.0F - f5, this.posY + ys, 0.0F);

            GlStateManager.glBegin(GL11.GL_TRIANGLE_STRIP);
            GL11.glTexCoord2d(f2 / 256.0D, f3 / 256.0D);
            GL11.glVertex3d(this.posX + f5, this.posY, 0.0D);
            GL11.glTexCoord2d(f2 / 256.0D, (f3 + 15.98D) / 256.0D);
            GL11.glVertex3d(this.posX - f5, this.posY + ys, 0.0D);
            GL11.glTexCoord2d((f2 + f4) / 256.0D, f3 / 256.0D);
            GL11.glVertex3d(this.posX + f4 / 2.0D + f5, this.posY, 0.0D);
            GL11.glTexCoord2d((f2 + f4) / 256.0D, (f3 + 15.98D) / 256.0D);
            GL11.glVertex3d(this.posX + f4 / 2.0D - f5, this.posY + ys, 0.0D);
            GlStateManager.glEnd();

            if (isSmall) {
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            }

            return (float) ((f1 - f) / 2.0D + 1.0D);
        }
    }

    @Override
    protected float renderDefaultChar(int ch, boolean italic) {
        int i = ch % 16 * 8;
        int j = ch / 16 * 8;
        int k = italic ? 1 : 0;
        bindTexture(this.locationFontTexture);
        int l = this.charWidth[ch];
        float f = (float) l - 0.01F;

        if (isSmall) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        }

        GlStateManager.glBegin(GL11.GL_TRIANGLE_STRIP);
        GlStateManager.glTexCoord2f((float) i / 128.0F, (float) j / 128.0F);
        GlStateManager.glVertex3f(this.posX + (float) k, this.posY, 0.0F);
        GlStateManager.glTexCoord2f((float) i / 128.0F, ((float) j + 7.99F) / 128.0F);
        GlStateManager.glVertex3f(this.posX - (float) k, this.posY + 7.99F, 0.0F);
        GlStateManager.glTexCoord2f(((float) i + f - 1.0F) / 128.0F, (float) j / 128.0F);
        GlStateManager.glVertex3f(this.posX + f - 1.0F + (float) k, this.posY, 0.0F);
        GlStateManager.glTexCoord2f(((float) i + f - 1.0F) / 128.0F, ((float) j + 7.99F) / 128.0F);
        GlStateManager.glVertex3f(this.posX + f - 1.0F - (float) k, this.posY + 7.99F, 0.0F);
        GlStateManager.glEnd();

        if (isSmall) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        }

        return (float) l;
    }

    private ResourceLocation getUnicodePageLocation(int page) {
        if (UNICODE_PAGE_LOCATIONS[page] == null) {
            UNICODE_PAGE_LOCATIONS[page] = new ResourceLocation(String.format("textures/font/unicode_page_%02x.png", page));
        }

        return UNICODE_PAGE_LOCATIONS[page];
    }

    private void loadGlyphTexture(int page) {
        bindTexture(this.getUnicodePageLocation(page));
    }

    // Fixed version of vanilla
    @Override
    public String trimStringToWidth(String text, int width, boolean reverse) {
        StringBuilder stringbuilder = new StringBuilder();
        int i = 0;
        int j = reverse ? text.length() - 1 : 0;
        int k = reverse ? -1 : 1;
        boolean flag = false;
        boolean flag1 = false;

        for (int l = j; l >= 0 && l < text.length() && i < width; l += k) {
            char c0 = text.charAt(l);
            int i1 = this.getCharWidth(c0);

            if (flag) {
                flag = false;

                if (c0 != 'l' && c0 != 'L') {
                    if (c0 == 'r' || c0 == 'R' || isFormatColor(c0)) {
                        flag1 = false;
                    }
                } else {
                    flag1 = true;
                }
            } else if (i1 < 0) {
                flag = true;
            } else {
                i += i1;

                if (flag1) {
                    ++i;
                }
            }

            if (i > width) {
                break;
            }

            if (reverse) {
                stringbuilder.insert(0, c0);
            } else {
                stringbuilder.append(c0);
            }
        }

        return stringbuilder.toString();
    }

    @Override
    public int getStringWidth(String text) {
        if (text == null) {
            return 0;
        } else {
            int i = 0;
            boolean flag = false;

            for (int j = 0; j < text.length(); ++j) {
                char c0 = text.charAt(j);
                int k = this.getCharWidth(c0);

                if (k < 0 && j < text.length() - 1) {
                    ++j;
                    c0 = text.charAt(j);

                    if (c0 != 'l' && c0 != 'L') {
                        if (c0 == 'r' || c0 == 'R' || isFormatColor(c0)) {
                            flag = false;
                        }
                    } else {
                        flag = true;
                    }

                    k = 0;
                }

                i += k;

                if (flag && k > 0) {
                    ++i;
                }
            }

            return i;
        }
    }

    public static boolean isFormatColor(char colorChar) {
        return colorChar >= '0' && colorChar <= '9' || colorChar >= 'a' && colorChar <= 'f' || colorChar >= 'A' && colorChar <= 'F';
    }
}

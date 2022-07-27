package betterquesting.client;

import betterquesting.api.storage.BQ_Settings;
import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.utils.QuestTranslation;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class QuestNotification {
    public static void ScheduleNotice(String mainTxt, String subTxt, ItemStack icon, String sound) {
        if (BQ_Settings.questNotices) notices.add(new QuestNotice(mainTxt, subTxt, icon, sound));
    }

    private static final List<QuestNotice> notices = new ArrayList<>();

    public static void resetNotices() {
        notices.clear();
    }

    @SubscribeEvent
    public void onDrawScreen(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.HELMET) return;
        if (notices.size() <= 0) return;

        if (notices.size() >= 20 || !BQ_Settings.questNotices) {
            notices.clear();
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int width = resolution.getScaledWidth();
        int height = resolution.getScaledHeight();
        QuestNotice notice = notices.get(0);

        if (!notice.init) {
            if (mc.isGamePaused() || mc.currentScreen != null) {
                return; // Do not start showing a new notice if the player isn't looking
            }

            notice.init = true;
            notice.startTime = Minecraft.getSystemTime();
            mc.getSoundHandler()
                    .playSound(PositionedSoundRecord.func_147674_a(new ResourceLocation(notice.sound), 1.0F));
        }

        if (notice.getTime() >= 6F) {
            notices.remove(0);
            return;
        }

        GL11.glPushMatrix();

        float scale = width > 600 ? 1.5F : 1F;

        GL11.glScalef(scale, scale, scale);
        width = MathHelper.ceiling_float_int(width / scale);
        height = MathHelper.ceiling_float_int(height / scale);

        float alpha = notice.getTime() <= 4F ? Math.min(1F, notice.getTime()) : Math.max(0F, 5F - notice.getTime());
        alpha = MathHelper.clamp_float(alpha, 0.02F, 1F);
        int color = new Color(1F, 1F, 1F, alpha).getRGB();

        GL11.glColor4f(1F, 1F, 1F, alpha);

        if (notice.icon != null) {
            RenderUtils.RenderItemStack(
                    mc, notice.icon, width / 2 - 8, height / 4 - 20, "", new Color(1F, 1F, 1F, alpha));
        }

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        String tmp = EnumChatFormatting.UNDERLINE + "" + EnumChatFormatting.BOLD
                + QuestTranslation.translate(notice.mainTxt);
        int txtW = RenderUtils.getStringWidth(tmp, mc.fontRenderer);
        mc.fontRenderer.drawString(tmp, width / 2 - txtW / 2, height / 4, color, false);

        tmp = QuestTranslation.translate(notice.subTxt);
        txtW = RenderUtils.getStringWidth(tmp, mc.fontRenderer);
        mc.fontRenderer.drawString(tmp, width / 2 - txtW / 2, height / 4 + 12, color, false);

        // GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    public static class QuestNotice {
        public long startTime;
        public boolean init = false;
        private final String mainTxt;
        private final String subTxt;
        private final ItemStack icon;
        private final String sound;

        public QuestNotice(String mainTxt, String subTxt, ItemStack icon, String sound) {
            this.startTime = Minecraft.getSystemTime();
            this.mainTxt = mainTxt;
            this.subTxt = subTxt;
            this.icon = icon;
            this.sound = sound;
        }

        public float getTime() {
            return (Minecraft.getSystemTime() - startTime) / 1000F;
        }
    }
}

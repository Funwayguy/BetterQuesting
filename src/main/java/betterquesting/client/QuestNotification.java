package betterquesting.client;

import betterquesting.api.storage.BQ_Settings;
import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.utils.QuestTranslation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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
        if (event.getType() != ElementType.ALL) return;
        if (notices.size() <= 0) return;

        if (notices.size() >= 20 || !BQ_Settings.questNotices) {
            notices.clear();
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution resolution = new ScaledResolution(mc);
        int width = resolution.getScaledWidth();
        int height = resolution.getScaledHeight();
        QuestNotice notice = notices.get(0);

        if (!notice.init) {
            if (mc.isGamePaused() || mc.currentScreen != null) {
                return; // Do not start showing a new notice if the player isn't looking
            }

            notice.init = true;
            notice.startTime = Minecraft.getSystemTime();
            mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(new SoundEvent(new ResourceLocation(notice.sound)), 1.0F));
        }

        if (notice.getTime() >= 6F) {
            notices.remove(0);
            return;
        }

        GlStateManager.pushMatrix();

        float scale = width > 600 ? 1.5F : 1F;

        GlStateManager.scale(scale, scale, scale);
        width = MathHelper.ceil(width / scale);
        height = MathHelper.ceil(height / scale);

        float alpha = notice.getTime() <= 4F ? Math.min(1F, notice.getTime()) : Math.max(0F, 5F - notice.getTime());
        alpha = MathHelper.clamp(alpha, 0.02F, 1F);
        int color = new Color(1F, 1F, 1F, alpha).getRGB();

        GlStateManager.color(1F, 1F, 1F, alpha);

        if (notice.icon != null) {
            RenderUtils.RenderItemStack(mc, notice.icon, width / 2 - 8, height / 4 - 20, "", new Color(1F, 1F, 1F, alpha));
        }

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

        String tmp = TextFormatting.UNDERLINE + "" + TextFormatting.BOLD + QuestTranslation.translate(notice.mainTxt);
        int txtW = RenderUtils.getStringWidth(tmp, mc.fontRenderer);
        mc.fontRenderer.drawString(tmp, width / 2 - txtW / 2, height / 4, color, false);

        tmp = QuestTranslation.translate(notice.subTxt);
        txtW = RenderUtils.getStringWidth(tmp, mc.fontRenderer);
        mc.fontRenderer.drawString(tmp, width / 2 - txtW / 2, height / 4 + 12, color, false);

        //GlStateManager.disableBlend();
        GlStateManager.popMatrix();
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

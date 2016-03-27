package betterquesting.client;

import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import betterquesting.core.BQ_Settings;
import betterquesting.utils.RenderUtils;

@SideOnly(Side.CLIENT)
public class QuestNotification
{
	public static void ScheduleNotice(String mainTxt, String subTxt, ItemStack icon, int sound)
	{
		String sndName = "random.levelup";
		
		switch(sound)
		{
			case 0:
				sndName = BQ_Settings.noticeUnlock;
				break;
			case 2:
				sndName = BQ_Settings.noticeComplete;
				break;
			default:
				sndName = BQ_Settings.noticeUpdate;
				break;
		}
		
		notices.add(new QuestNotice(mainTxt, subTxt, icon, sndName));
	}
	
	static ArrayList<QuestNotice> notices = new ArrayList<QuestNotice>();
	
	@SubscribeEvent
	public void onDrawScreen(RenderGameOverlayEvent.Post event)
	{
		if(event.type != RenderGameOverlayEvent.ElementType.HELMET || notices.size() <= 0)
		{
			return;
		}
		
		Minecraft mc = Minecraft.getMinecraft();
		ScaledResolution resolution = new ScaledResolution(mc);
		int width = resolution.getScaledWidth();
		int height = resolution.getScaledHeight();
		QuestNotice notice = notices.get(0);
		
		if(!notice.init)
		{
			if(mc.isGamePaused() || mc.currentScreen != null)
			{
				return; // Do not start showing a new notice if the player isn't looking
			}
			
			notice.init = true;
			notice.startTime = Minecraft.getSystemTime();
			mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(new SoundEvent(new ResourceLocation(notice.sound)), 1.0F));
		}
		
		if(notice.getTime() >= 6F)
		{
			notices.remove(0);
			return;
		}
		
		GL11.glPushMatrix();
		
		float scale = width > 600? 1.5F : 1F;
		
		GL11.glScalef(scale, scale, scale);
		width = MathHelper.ceiling_float_int(width/scale);
		height = MathHelper.ceiling_float_int(height/scale);
		
		float alpha = notice.getTime() <= 4F? Math.min(1F, notice.getTime()) : Math.max(0F, 5F - notice.getTime());
		alpha = MathHelper.clamp_float(alpha, 0.02F, 1F);
		int color = new Color(1F, 1F, 1F, alpha).getRGB();
		
		GL11.glColor4f(1F, 1F, 1F, alpha);
		
		if(notice.icon != null)
		{
			RenderUtils.RenderItemStack(mc, notice.icon, width/2 - 8, height/4 - 20, "",  new Color(1F, 1F, 1F, alpha));
		}

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
     	
		String tmp = TextFormatting.UNDERLINE + "" + TextFormatting.BOLD + I18n.translateToLocalFormatted(notice.mainTxt);
		int txtW = mc.fontRendererObj.getStringWidth(tmp);
		mc.fontRendererObj.drawString(tmp, width/2 - txtW/2, height/4, color, false);
		
		tmp = I18n.translateToLocalFormatted(notice.subTxt);
		txtW = mc.fontRendererObj.getStringWidth(tmp);
		mc.fontRendererObj.drawString(tmp, width/2 - txtW/2, height/4 + 12, color, false);
		
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glPopMatrix();
	}
	
	public static class QuestNotice
	{
		long startTime = 0;
		public boolean init = false;
		public String mainTxt = "";
		public String subTxt = "";
		public ItemStack icon = null;
		public String sound = "random.levelup";
		
		public QuestNotice(String mainTxt, String subTxt, ItemStack icon, String sound)
		{
			this.startTime = Minecraft.getSystemTime();
			this.mainTxt = mainTxt;
			this.subTxt = subTxt;
			this.icon = icon;
			this.sound = sound;
		}
		
		public float getTime()
		{
			return (Minecraft.getSystemTime() - startTime)/1000F;
		}
	}
}

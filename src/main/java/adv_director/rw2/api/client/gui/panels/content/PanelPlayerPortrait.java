package adv_director.rw2.api.client.gui.panels.content;

import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import adv_director.api.utils.RenderUtils;
import adv_director.rw2.api.client.gui.controls.IValueIO;
import adv_director.rw2.api.client.gui.events.PanelEvent;
import adv_director.rw2.api.client.gui.misc.GuiRectangle;
import adv_director.rw2.api.client.gui.misc.IGuiRect;
import adv_director.rw2.api.client.gui.panels.IGuiPanel;
import adv_director.rw2.api.utils.EntityPlayerPreview;
import com.mojang.authlib.GameProfile;

public class PanelPlayerPortrait implements IGuiPanel
{
	private final IGuiRect transform;
	
	private final AbstractClientPlayer player;
	private final ResourceLocation resource;
	
	private final IValueIO<Float> basePitch;
	private final IValueIO<Float> baseYaw;
	private IValueIO<Float> pitchDriver;
	private IValueIO<Float> yawDriver;
	
	public PanelPlayerPortrait(IGuiRect rect, UUID playerID, String username)
	{
		this(rect, new EntityPlayerPreview(Minecraft.getMinecraft().theWorld, new GameProfile(playerID, username)));
	}
	
	public PanelPlayerPortrait(IGuiRect rect, AbstractClientPlayer player)
	{
		this.transform = rect;
		this.player = player;
		this.player.limbSwing = 0F;
		this.player.limbSwingAmount = 0F;
		this.player.rotationYawHead = 0F;
		
		this.resource = player.getLocationSkin();
		
		if(Minecraft.getMinecraft().getTextureManager().getTexture(resource) == null)
		{
			AbstractClientPlayer.getDownloadImageSkin(resource, player.getGameProfile().getName());
		}
		
		this.basePitch = new IValueIO<Float>()
		{
			private float val = 15F;
			
			@Override
			public Float readValue()
			{
				return val;
			}

			@Override
			public void writeValue(Float value)
			{
				this.val = value;
			}
		};
		this.pitchDriver = basePitch;
		
		this.baseYaw = new IValueIO<Float>()
		{
			private float val = -30F;
			
			@Override
			public Float readValue()
			{
				return val;
			}

			@Override
			public void writeValue(Float value)
			{
				this.val = value;
			}
		};
		this.yawDriver = baseYaw;
	}
	
	public PanelPlayerPortrait setRotationFixed(float pitch, float yaw)
	{
		this.pitchDriver = basePitch;
		this.yawDriver = baseYaw;
		basePitch.writeValue(pitch);
		baseYaw.writeValue(yaw);
		return this;
	}
	
	public PanelPlayerPortrait setRotationDriven(IValueIO<Float> pitch, IValueIO<Float> yaw)
	{
		this.pitchDriver = pitch == null? basePitch : pitch;
		this.yawDriver = yaw == null? baseYaw : yaw;
		return this;
	}
	
	@Override
	public void initPanel()
	{
	}
	
	@Override
	public IGuiRect getTransform()
	{
		return transform;
	}
	
	@Override
	public void drawPanel(int mx, int my, float partialTick)
	{
		IGuiRect bounds = this.getTransform();
		GlStateManager.pushMatrix();
		Minecraft mc = Minecraft.getMinecraft();
		RenderUtils.startScissor(mc, new GuiRectangle(bounds));
		
		GlStateManager.color(1F, 1F, 1F, 1F);
		int scale = Math.min(bounds.getWidth(), bounds.getHeight());
		/*double d0 = (Minecraft.getSystemTime()%5000L)/5000D;
		double d1 = (Math.sin(Math.toRadians(d0 * 360D)) + 1D)/2D;
		double d2 = (Math.cos(Math.toRadians(d0 * 360D)) + 1D)/2D;
		d1 = d1 * 5D + 10D;
		d2 = d2 * -10D;*/
		RenderUtils.RenderEntity(bounds.getX() + bounds.getWidth()/2, bounds.getY() + bounds.getHeight()/2 + (int)(scale*1.2F), scale, yawDriver.readValue(), pitchDriver.readValue(), player);
		
		RenderUtils.endScissor(mc);
		GlStateManager.popMatrix();
	}
	
	@Override
	public boolean onMouseClick(int mx, int my, int click)
	{
		return false;
	}
	
	@Override
	public boolean onMouseScroll(int mx, int my, int scroll)
	{
		return false;
	}
	
	@Override
	public void onKeyTyped(char c, int keycode)
	{
	}
	
	@Override
	public void onPanelEvent(PanelEvent event)
	{
	}
	
	@Override
	public List<String> getTooltip(int mx, int my)
	{
		return null;
	}
}

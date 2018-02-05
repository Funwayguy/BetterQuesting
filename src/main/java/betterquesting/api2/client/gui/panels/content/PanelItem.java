package betterquesting.api2.client.gui.panels.content;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.util.ITooltipFlag;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.resources.IGuiTexture;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;

public class PanelItem implements IGuiPanel
{
	private final IGuiRect transform;
	private final BigItemStack stack;
	private boolean showCount = false;
	private boolean showFrame = false;
	private boolean showTooltip = true;
	private final IGuiTexture frame;
	
	public PanelItem(IGuiRect rect, BigItemStack item)
	{
		this(rect, item, false, false, true);
	}
	
	public PanelItem(IGuiRect rect, BigItemStack item, boolean showCount, boolean showFrame, boolean showTooltip)
	{
		this.transform = rect;
		this.stack = item;
		this.showFrame = showFrame;
		this.showCount = showCount;
		this.frame = PresetTexture.ITEM_FRAME.getTexture();
	}
	
	@Override
	public IGuiRect getTransform()
	{
		return transform;
	}
	
	@Override
	public void initPanel()
	{
	}
	
	@Override
	public void drawPanel(int mx, int my, float partialTick)
	{
		Minecraft mc = Minecraft.getMinecraft();
		
		float sx = (transform.getWidth() - 2)/16F;
		float sy = (transform.getHeight() - 2)/16F;
		
		float sa = Math.min(sx, sy);
		
		int dx = (int)Math.floor((sx - sa) * 8F) + 1;
		int dy = (int)Math.floor((sy - sa) * 8F) + 1;
		
		
		GlStateManager.pushMatrix();
		GlStateManager.color(1F, 1F, 1F, 1F);
		
		if(showFrame)
		{
			this.frame.drawTexture(transform.getX(), transform.getY(), transform.getWidth(), transform.getHeight(), 0F, partialTick);
		}
		
		GlStateManager.translate(transform.getX() + dx, transform.getY() + dy, 0);
		GlStateManager.scale(sa, sa, 1F);
		RenderUtils.RenderItemStack(mc, stack.getBaseStack(), 0, 0, (showCount && stack.stackSize > 1) ? ("" + stack.stackSize) : "");
		
		GlStateManager.popMatrix();
	}
	
	@Override
	public boolean onMouseClick(int mx, int my, int button)
	{
		return false;
	}
	
	@Override
	public boolean onMouseRelease(int mx, int my, int button)
	{
		return false;
	}
	
	@Override
	public boolean onMouseScroll(int mx, int my, int scroll)
	{
		return false;
	}
	
	@Override
	public boolean onKeyTyped(char c, int keycode)
	{
		return false;
	}
	
	@Override
	public List<String> getTooltip(int mx, int my)
	{
		if(transform.contains(mx, my))
		{
			Minecraft mc = Minecraft.getMinecraft();
			return !showTooltip ? null : stack.getBaseStack().getTooltip(mc.player, mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
		}
		
		return null;
	}
	
}

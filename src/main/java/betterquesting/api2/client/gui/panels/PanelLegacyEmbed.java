package betterquesting.api2.client.gui.panels;

import java.util.List;
import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api2.client.gui.misc.IGuiRect;

/**
 * <b>WARNING:</b> For use ONLY when IGuiEmbedded cannot be reasonably replaced currently
 */
public class PanelLegacyEmbed implements IGuiPanel
{
	private final IGuiRect transform;
	private final IGuiEmbedded embed;
	
	public PanelLegacyEmbed(IGuiRect rect, IGuiEmbedded embed)
	{
		this.transform = rect;
		this.embed = embed;
	}
	
	@Override
	public IGuiRect getTransform()
	{
		return this.transform;
	}
	
	@Override
	public void initPanel()
	{
	}
	
	@Override
	public void drawPanel(int mx, int my, float partialTick)
	{
		embed.drawBackground(mx, my, partialTick);
		embed.drawForeground(mx, my, partialTick); // Second pass highly likely to breakdown
	}
	
	@Override
	public boolean onMouseClick(int mx, int my, int button)
	{
		embed.onMouseClick(mx, my, button);
		
		// Unable to determine use
		return transform.contains(mx, my);
	}
	
	@Override
	public boolean onMouseRelease(int mx, int my, int button)
	{
		// Unable to determine use
		return transform.contains(mx, my);
	}
	
	@Override
	public boolean onMouseScroll(int mx, int my, int scroll)
	{
		embed.onMouseScroll(mx, my, scroll);
		
		// Unable to determine use
		return transform.contains(mx, my);
	}
	
	@Override
	public boolean onKeyTyped(char c, int keycode)
	{
		embed.onKeyTyped(c, keycode);
		
		// Unable to determine use
		return true;
	}
	
	@Override
	public List<String> getTooltip(int mx, int my)
	{
		return null;
	}
}

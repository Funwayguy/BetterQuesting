package betterquesting.client.gui2;

import java.awt.Color;
import java.util.List;
import net.minecraft.client.renderer.GlStateManager;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.resources.IGuiTexture;
import betterquesting.api2.client.gui.resources.SlideShowTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;

public class PanelQuestPreview implements IGuiPanel
{
	private final IGuiRect bounds;
	
	private IGuiTexture iconTex;
	private final PresetColor[] iconCol = new PresetColor[4];
	
	public PanelQuestPreview(IGuiRect bounds)
	{
		this.bounds = bounds;
	}
	
	@Override
	public IGuiRect getTransform()
	{
		return bounds;
	}
	
	@Override
	public void initPanel()
	{
		this.iconTex = new SlideShowTexture(new IGuiTexture[]{
			PresetTexture.QUEST_NORM_0.getTexture(),
			PresetTexture.QUEST_NORM_1.getTexture(),
			PresetTexture.QUEST_NORM_2.getTexture(),
			PresetTexture.QUEST_NORM_3.getTexture(),
			PresetTexture.QUEST_MAIN_0.getTexture(),
			PresetTexture.QUEST_MAIN_1.getTexture(),
			PresetTexture.QUEST_MAIN_2.getTexture(),
			PresetTexture.QUEST_MAIN_3.getTexture(),
			PresetTexture.QUEST_AUX_0.getTexture(),
			PresetTexture.QUEST_AUX_1.getTexture(),
			PresetTexture.QUEST_AUX_2.getTexture(),
			PresetTexture.QUEST_AUX_3.getTexture(),
		}, 1F);
		
		this.iconCol[0] = PresetColor.QUEST_ICON_LOCKED;
		this.iconCol[1] = PresetColor.QUEST_ICON_UNLOCKED;
		this.iconCol[2] = PresetColor.QUEST_ICON_PENDING;
		this.iconCol[3] = PresetColor.QUEST_ICON_COMPLETE;
	}
	
	@Override
	public void drawPanel(int mx, int my, float partialTick)
	{
		if(iconTex == null)
		{
			return;
		}
		
		IGuiRect bounds = this.getTransform();
		GlStateManager.pushMatrix();
		Color c = new Color(iconCol[(int)(System.currentTimeMillis()%4000)/1000].getColor());
		GlStateManager.color(c.getRed()/255F, c.getGreen()/255F, c.getBlue()/255F, c.getAlpha()/255F);
		iconTex.drawTexture(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight(), 0F, partialTick);
		GlStateManager.popMatrix();
	}
	
	@Override
	public boolean onMouseClick(int mx, int my, int click)
	{
		return false;
	}
	
	@Override
	public boolean onMouseRelease(int mx, int my, int click)
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
		return null;
	}
}

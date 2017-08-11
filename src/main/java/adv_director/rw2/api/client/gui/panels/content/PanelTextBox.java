package adv_director.rw2.api.client.gui.panels.content;

import java.awt.Color;
import java.util.List;
import org.lwjgl.util.Rectangle;
import adv_director.api.utils.RenderUtils;
import adv_director.rw2.api.client.gui.events.IPanelEvent;
import adv_director.rw2.api.client.gui.panels.IGuiPanel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class PanelTextBox implements IGuiPanel
{
	private final Rectangle bounds = new Rectangle(0, 0, 1, 1);
	private IGuiPanel parent;
	
	private String text = "";
	private boolean shadow = false;
	private int color = Color.BLACK.getRGB();
	private boolean autoFit = true;
	
	private int lines = 1; // Cached number of lines
	
	public PanelTextBox setText(String text)
	{
		this.text = text;
		return this;
	}
	
	public PanelTextBox setColor(int color)
	{
		this.color = color;
		return this;
	}
	
	public PanelTextBox enableShadow(boolean enable)
	{
		this.shadow = enable;
		return this;
	}
	
	public PanelTextBox enableAutoFit(boolean enable)
	{
		this.autoFit = enable;
		return this;
	}
	
	@Override
	public IGuiPanel getParentPanel()
	{
		return parent;
	}
	
	@Override
	public void setParentPanel(IGuiPanel panel)
	{
		this.parent = panel;
	}
	
	@Override
	public void initPanel()
	{
		FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
		
		if(!autoFit)
		{
			lines = (bounds.getHeight() / fr.FONT_HEIGHT) - 1;
			return;
		}
		
		List<String> sl = fr.listFormattedStringToWidth(text, bounds.getWidth());
		lines = sl.size() - 1;
		bounds.setHeight(fr.FONT_HEIGHT * sl.size());
	}
	
	@Override
	public void updateBounds(Rectangle bounds)
	{
		this.bounds.setBounds(bounds);
	}
	
	@Override
	public Rectangle getBounds()
	{
		return bounds;
	}
	
	@Override
	public void drawPanel(int mx, int my, float partialTick)
	{
		FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
		RenderUtils.drawSplitString(fr, text, bounds.getX(), bounds.getY(), bounds.getWidth(), color, shadow, 0, lines);
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
	public void onPanelEvent(IPanelEvent event)
	{
	}
	
	@Override
	public List<String> getTooltip(int mx, int my)
	{
		return null;
	}
}

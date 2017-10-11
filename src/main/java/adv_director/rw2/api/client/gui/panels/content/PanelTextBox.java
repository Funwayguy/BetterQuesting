package adv_director.rw2.api.client.gui.panels.content;

import java.awt.Color;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import adv_director.api.utils.RenderUtils;
import adv_director.rw2.api.client.gui.events.PanelEvent;
import adv_director.rw2.api.client.gui.misc.GuiRectangle;
import adv_director.rw2.api.client.gui.misc.IGuiRect;
import adv_director.rw2.api.client.gui.panels.IGuiPanel;

public class PanelTextBox implements IGuiPanel
{
	private IGuiRect transform;
	
	private String text = "";
	private boolean shadow = false;
	private int color = Color.BLACK.getRGB();
	private boolean autoFit = true;
	
	private int lines = 1; // Cached number of lines
	
	public PanelTextBox(IGuiRect rect, String text)
	{
		this.transform = rect;
		this.setText(text);
	}
	
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
	public IGuiRect getTransform()
	{
		return transform;
	}
	
	@Override
	public void initPanel()
	{
		IGuiRect bounds = this.getTransform();
		FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
		
		if(!autoFit)
		{
			lines = (bounds.getHeight() / fr.FONT_HEIGHT) - 1;
			return;
		}
		
		List<String> sl = fr.listFormattedStringToWidth(text, bounds.getWidth());
		lines = sl.size() - 1;
		
		this.transform = new GuiRectangle(bounds.getX(), bounds.getY(), bounds.getWidth(), fr.FONT_HEIGHT * sl.size());
	}
	
	@Override
	public void drawPanel(int mx, int my, float partialTick)
	{
		IGuiRect bounds = this.getTransform();
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
	public void onPanelEvent(PanelEvent event)
	{
	}
	
	@Override
	public List<String> getTooltip(int mx, int my)
	{
		return null;
	}
}

package betterquesting.api2.client.gui.panels.content;

import java.awt.Color;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.math.MathHelper;
import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;

public class PanelTextBox implements IGuiPanel
{
	private IGuiRect transform;
	
	private String text = "";
	private boolean shadow = false;
	private int color = Color.BLACK.getRGB();
	private boolean autoFit = false;
	private int align = 0;
	
	private int lines = 1; // Cached number of lines
	
	public PanelTextBox(IGuiRect rect, String text)
	{
		this.transform = rect;
		this.setText(text);
	}
	
	public PanelTextBox setText(String text)
	{
		this.text = text;
		
		IGuiRect bounds = this.getTransform();
		FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
		
		if(autoFit)
		{
			List<String> sl = fr.listFormattedStringToWidth(text, bounds.getWidth());
			lines = sl.size() - 1;
			
			this.transform = new GuiRectangle(bounds.getX(), bounds.getY(), bounds.getWidth(), fr.FONT_HEIGHT * sl.size());
		} else
		{
			lines = (bounds.getHeight() / fr.FONT_HEIGHT) - 1;
		}
		
		return this;
	}
	
	public PanelTextBox setColor(int color)
	{
		this.color = color;
		return this;
	}
	
	public PanelTextBox setAlignment(int align)
	{
		this.align = MathHelper.clamp(align, 0, 2);
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
		FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
		
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
		FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
		
		if(align == 2)
		{
			int w = fr.getStringWidth(text);
			RenderUtils.drawSplitString(fr, text, bounds.getX() + bounds.getWidth() - w, bounds.getY(), Math.min(w, bounds.getWidth()), color, shadow, 0, lines);
		} else if(align == 1)
		{
			int w = fr.getStringWidth(text);
			RenderUtils.drawSplitString(fr, text, bounds.getX() + bounds.getWidth()/2 - w/2, bounds.getY(), bounds.getWidth()/2 - w/2, color, shadow, 0, lines);
		} else
		{
			RenderUtils.drawSplitString(fr, text, bounds.getX(), bounds.getY(), bounds.getWidth(), color, shadow, 0, lines);
		}
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
	public List<String> getTooltip(int mx, int my)
	{
		return null;
	}
}

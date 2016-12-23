package betterquesting.api.client.gui.lists;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.MathHelper;
import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.utils.RenderUtils;

/**
 * Creates a scrolling region of split text. Line spacing is slightly larger for better readability
 */
public class GuiScrollingText extends GuiScrollingBase<GuiScrollingText.ScrollingEntryText>
{
	private final FontRenderer fontRenderer;
	private String rawText = "";
	
	public GuiScrollingText(Minecraft mc, int x, int y, int width, int height)
	{
		this(mc, x, y, width, height, "");
	}
	
	public GuiScrollingText(Minecraft mc, int x, int y, int width, int height, String text)
	{
		super(mc, x, y, width, height);
		this.fontRenderer = Minecraft.getMinecraft().fontRenderer;
		this.SetText(text);
		this.allowDragScroll(true);
	}
	
	public void SetText(String txt)
	{
		this.rawText = txt.replaceAll("\r", "");
		
		if(this.getEntryList().size() != 1)
		{
			this.getEntryList().clear();
			this.getEntryList().add(new ScrollingEntryText(fontRenderer, rawText, getListWidth()));
		} else
		{
			this.getEntryList().get(0).setText(rawText);
		}
	}
	
	public String getText()
	{
		return rawText;
	}
	
	public int getCursorPos(int mx, int my)
	{
		if(this.getEntryList().size() > 0)
		{
			return this.getEntryList().get(0).getCursorPos(mx, my);
		}
		
		return 0;
	}
	
	public static class ScrollingEntryText extends GuiElement implements IScrollingEntry
	{
		private final FontRenderer font;
		private String text = "";
		private int lw = 1;
		private int lh = 8;
		private int lx = 0;
		private int ly = 0;
		
		public ScrollingEntryText(FontRenderer font, String text, int width)
		{
			this.font = font;
			this.setText(text);
		}
		
		public void setText(String text)
		{
			this.text = text;
		}
		
		@Override
		public void drawBackground(int mx, int my, int px, int py, int width)
		{
			List<?> tLines = font.listFormattedStringToWidth(text, width);
			
			lw = width;
			lh = tLines.size() * font.FONT_HEIGHT + 2;
			lx = px;
			ly = py;
			
			RenderUtils.drawSplitString(font, text, px, py + 2, width, getTextColor(), false);
		}
		
		@Override
		public void drawForeground(int mx, int my, int px, int py, int width)
		{
		}
		
		@Override
		public void onMouseClick(int mx, int my, int px, int py, int click, int index)
		{
		}
		
		public int getCursorPos(int mx, int my)
		{
			List<String> tLines = RenderUtils.splitStringWithoutFormat(text, lw, font);
			
			if(tLines.size() <= 0)
			{
				return 0;
			}
			
			int idx = 0;
			
			int row = MathHelper.clamp_int((my - ly - 2)/font.FONT_HEIGHT, 0, tLines.size() - 1);
			String lastFormat = "";
			
			for(int i = 0; i < row; i++)
			{
				String line = tLines.get(i);
				idx += line.length();
				lastFormat = RenderUtils.getFormatFromString(lastFormat + line);
			}
			
			String line = tLines.get(row);
			
			for(int i = 0; i < line.length(); i++)
			{
				if(font.getStringWidth(lastFormat + line.substring(0, i + 1)) > mx - lx)
				{
					break;
				} else
				{
					idx++;
				}
			}
			
			return idx;
		}
		
		@Override
		public int getHeight()
		{
			return lh;
		}
		
		@Override
		public boolean canDrawOutsideBox(boolean isForeground)
		{
			return false;
		}
	}
}

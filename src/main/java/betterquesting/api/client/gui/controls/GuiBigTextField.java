package betterquesting.api.client.gui.controls;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Mouse;
import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.misc.ICallback;

public class GuiBigTextField extends GuiTextField
{
	private GuiButtonThemed bigEdit;
	private ICallback<String> host;
	private FontRenderer fontrenderer;
	private String watermark = "";
	private Color wmColor = new Color(96, 96, 96);
	
	public GuiBigTextField(FontRenderer fontrenderer, int posX, int posY, int width, int height)
	{
		super(fontrenderer, posX, posY, width, height);
		this.fontrenderer = fontrenderer;
	}
	
	public GuiBigTextField enableBigEdit(ICallback<String> host)
	{
		bigEdit = new GuiButtonThemed(0, this.xPosition + width - 19, this.yPosition - 1, 20, height + 2, "Aa", true);
		this.host = host;
		return this;
	}
	
	public GuiBigTextField setWatermark(String text)
	{
		watermark = text == null? "" : text;
		return this;
	}
	
    /**
     * Args: x, y, buttonClicked
     */
	@Override
    public void mouseClicked(int mx, int my, int p_146192_3_)
    {
		Minecraft mc = Minecraft.getMinecraft();
		
        if(bigEdit != null && bigEdit.mousePressed(mc, mx, my))
        {
        	bigEdit.func_146113_a(mc.getSoundHandler());
        	QuestingAPI.getAPI(ApiReference.GUI_HELPER).openTextEditor(mc.currentScreen, host, getText());
        	return;
        } else if(host != null)
        {
        	width -= 20;
        	super.mouseClicked(mx, my, p_146192_3_);
        	width += 20;
        } else
        {
        	super.mouseClicked(mx, my, p_146192_3_);
        }
    }
	
	/**
	 * Use <i>drawTextBox(int mx, int my, float partialTick)</i>
	 */
	@Override
	@Deprecated
	public void drawTextBox()
	{
		Minecraft mc = Minecraft.getMinecraft();
		
		int mx = 0;
		int my = 0;
		
		if(mc.currentScreen != null)
		{
	        mx = Mouse.getEventX() * mc.currentScreen.width / mc.displayWidth;
	        my = mc.currentScreen.height - Mouse.getEventY() * mc.currentScreen.height / mc.displayHeight - 1;
		}
		
		this.drawTextBox(mx, my, 1F);
	}
	
	public void drawTextBox(int mx, int my, float partialTick)
	{
		if(bigEdit != null)
		{
			bigEdit.xPosition = this.xPosition + width - 19;
			bigEdit.yPosition = this.yPosition - 1;
			bigEdit.height = this.height + 2;
	        bigEdit.drawButton(Minecraft.getMinecraft(), mx, my);
		}
		
		if(host != null)
		{
			width -= 20;
			super.drawTextBox();
			width += 20;
		} else
		{
			super.drawTextBox();
		}
		
		if(getText().length() <= 0 && watermark.length() > 0 && !isFocused())
		{
			this.fontrenderer.drawString(watermark, this.xPosition + 4, this.yPosition + height/2 - 4, wmColor.getRGB(), false);
		}
	}
}

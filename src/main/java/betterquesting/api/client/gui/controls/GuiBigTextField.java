package betterquesting.api.client.gui.controls;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.misc.ICallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Mouse;

import java.awt.*;

@Deprecated
public class GuiBigTextField extends GuiTextField
{
	private GuiButtonThemed bigEdit;
	private ICallback<String> host;
	private FontRenderer fontrenderer;
	private String watermark = "";
	private Color wmColor = new Color(96, 96, 96);
	
	public GuiBigTextField(FontRenderer fontrenderer, int posX, int posY, int width, int height)
	{
		super(0, fontrenderer, posX, posY, width, height);
		this.fontrenderer = fontrenderer;
	}
	
	public GuiBigTextField enableBigEdit(ICallback<String> host)
	{
		this.width -= 20;
		bigEdit = new GuiButtonThemed(0, this.x + width + 1, this.y - 1, 20, height + 2, "Aa", true);
		this.host = host;
		return this;
	}
	
	public GuiBigTextField setWatermark(String text)
	{
		watermark = text == null? "" : text;
		return this;
	}
	
	@Override
    public boolean mouseClicked(int mx, int my, int p_146192_3_)
    {
		Minecraft mc = Minecraft.getMinecraft();
		
        if(bigEdit != null && bigEdit.mousePressed(mc, mx, my))
        {
        	bigEdit.playPressSound(mc.getSoundHandler());
        	QuestingAPI.getAPI(ApiReference.GUI_HELPER).openTextEditor(mc.currentScreen, host, getText());
        	return true;
        } else
        {
        	return super.mouseClicked(mx, my, p_146192_3_);
        }
    }
	
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
			bigEdit.x = this.x + width + 1;
			bigEdit.y = this.y - 1;
			bigEdit.height = this.height + 2;
	        bigEdit.drawButton(Minecraft.getMinecraft(), mx, my, partialTick);
		}
		
		super.drawTextBox();
		
		if(getText().length() <= 0 && watermark.length() > 0 && !isFocused())
		{
			this.fontrenderer.drawString(watermark, this.x + 4, this.y + height/2 - 4, wmColor.getRGB(), false);
		}
	}
}

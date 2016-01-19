package betterquesting.client.gui.misc;

import org.lwjgl.input.Mouse;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.editors.GuiTextEditor;
import betterquesting.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.EnumChatFormatting;

public class GuiBigTextField extends GuiTextField
{
	int hostID;
	ITextEditor host;
	GuiTextEditor editor;
	
	public GuiBigTextField(FontRenderer fontrenderer, int posX, int posY, int width, int height)
	{
		super(fontrenderer, posX, posY, width, height);
	}
	
	public GuiBigTextField enableBigEdit(ITextEditor host, int id)
	{
		this.host = host;
		this.hostID = id;
		return this;
	}

    /**
     * Args: x, y, buttonClicked
     */
	@Override
    public void mouseClicked(int mx, int my, int p_146192_3_)
    {
        if(host != null && mx >= xPosition + width - 20 && mx < xPosition + width && my >= yPosition && my < yPosition + height)
        {
        	editor = new GuiTextEditor(Minecraft.getMinecraft().currentScreen, getText()).setHost(host, hostID);
        	Minecraft.getMinecraft().displayGuiScreen(editor);
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
	
	@Override
	public void drawTextBox()
	{
		Minecraft mc = Minecraft.getMinecraft();
		
		if(mc.currentScreen instanceof GuiQuesting && host != null)
		{
	        int i = Mouse.getEventX() * mc.currentScreen.width / mc.displayWidth;
	        int j = mc.currentScreen.height - Mouse.getEventY() * mc.currentScreen.height / mc.displayHeight - 1;
	        int bs = ((GuiQuesting)mc.currentScreen).isWithin(i, j, xPosition + width - 19, yPosition - 1, 20, height + 2, false)? 2 : 1;
			RenderUtils.DrawFakeButton((GuiQuesting)mc.currentScreen, xPosition + width - 19, yPosition - 1, 20, height + 2, EnumChatFormatting.BOLD + "A" + EnumChatFormatting.RESET + "a", bs);
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
	}
}

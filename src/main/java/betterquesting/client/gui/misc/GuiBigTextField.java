package betterquesting.client.gui.misc;

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
		this.width -= 20;
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
        if(host != null && mx >= xPosition + width && mx < xPosition + width + 20 && my >= yPosition && my < yPosition + height)
        {
        	editor = new GuiTextEditor(Minecraft.getMinecraft().currentScreen, getText()).setHost(host, hostID);
        	Minecraft.getMinecraft().displayGuiScreen(editor);
        	return;
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
			RenderUtils.DrawFakeButton((GuiQuesting)mc.currentScreen, xPosition + width + 1, yPosition - 1, 20, height + 2, EnumChatFormatting.BOLD + "A" + EnumChatFormatting.RESET + "a", 1);
		}
		
		super.drawTextBox();
	}
}

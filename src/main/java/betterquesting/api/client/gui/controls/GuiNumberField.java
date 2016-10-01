package betterquesting.api.client.gui.controls;

import java.text.NumberFormat;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import betterquesting.api.utils.RenderUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiNumberField extends GuiTextField
{
	public GuiNumberField(FontRenderer renderer, int posX, int posY, int sizeX, int sizeY)
	{
		super(renderer, posX, posY, sizeX, sizeY);
		this.setMaxStringLength(Integer.MAX_VALUE);
	}
	
	@Override
	public void writeText(String text)
	{
		super.writeText(text.replaceAll(RenderUtils.REGEX_NUMBER, "")); // Type new text stripping out illegal characters
	}
	
	@Override
	public void setText(String text)
	{
		super.setText(text.replaceAll(RenderUtils.REGEX_NUMBER, ""));
	}
	
	@Override
	public void mouseClicked(int mx, int my, int click)
	{
		super.mouseClicked(mx, my, click);
		
		if(!isFocused())
		{
			String txt = super.getText().replaceAll(RenderUtils.REGEX_NUMBER, "");
			txt = txt.length() <= 0? "0" : txt;
			setText(txt);
		}
	}
	
	public Number getNumber()
	{
		try
		{
			return NumberFormat.getInstance().parse(super.getText());
		} catch(Exception e)
		{
			return 0;
		}
	}
	
	@Override
	public String getText()
	{
		return "" + getNumber();
	}
}

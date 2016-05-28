package betterquesting.client.gui.misc;

import java.text.NumberFormat;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import betterquesting.client.gui.GuiQuesting;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

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
		super.writeText(text.replaceAll(GuiQuesting.numRegex, "")); // Type new text stripping out illegal characters
	}
	
	@Override
	public void setText(String text)
	{
		super.setText(text.replaceAll(GuiQuesting.numRegex, ""));
	}
	
	@Override
	public void mouseClicked(int mx, int my, int click)
	{
		super.mouseClicked(mx, my, click);
		
		if(!isFocused())
		{
			String txt = super.getText().replaceAll(GuiQuesting.numRegex, "");
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

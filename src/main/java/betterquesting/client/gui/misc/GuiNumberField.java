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
	
	/*@Override
	public boolean textboxKeyTyped(char character, int num)
	{
		String before = this.getText();
		boolean reset = false;
		
		try // Ensure the previous text is valid, set to 0 if not
		{
			NumberFormat.getInstance().parse(before);
		} catch(Exception e)
		{
			before = "" + 0;
			reset = true;
		}
		
		boolean flag = super.textboxKeyTyped(character, num);
		
		if(this.getText().length() <= 0)
		{
			this.setText("" + 0);
			reset = true;
		} else
		{
			try // Ensure the new text is valid, revert if it isn't
			{
				NumberFormat.getInstance().parse(this.getText());
				reset = false;
			} catch(Exception e)
			{
				this.setText(before);
			}
		}
		
		if(reset)
		{
			this.setCursorPosition(0);
		}
		
		return flag;
	}*/
	
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

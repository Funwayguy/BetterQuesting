package betterquesting.client;

import java.text.NumberFormat;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

public class GuiNumberField extends GuiTextField
{
	public GuiNumberField(FontRenderer renderer, int posX, int posY, int sizeX, int sizeY)
	{
		super(renderer, posX, posY, sizeX, sizeY);
	}
	
	@Override
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
}

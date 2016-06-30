package betterquesting.client.gui.misc;

import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;

public class GuiYesNoLocked extends GuiYesNo
{
    public GuiYesNoLocked(GuiYesNoCallback callback, String txt1, String txt2, int id)
    {
    	super(callback, txt1, txt2, id);
    }
    
	public GuiYesNoLocked(GuiYesNoCallback callback, String txt1, String txt2, String txtConfirm, String txtCancel, int id)
	{
		super(callback, txt1, txt2, txtConfirm, txtCancel, id);
	}
	
	/**
	 * Disables escaping
	 */
	@Override
	protected void keyTyped(char character, int keyCode){}
}

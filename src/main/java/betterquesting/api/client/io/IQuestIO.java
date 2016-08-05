package betterquesting.api.client.io;

import net.minecraft.client.gui.GuiScreen;

/**
 * Used as a basis for quest importers/exporters
 */
public interface IQuestIO
{
	public String getUnlocalisedName();
	public String getUnlocalisedDescrition();
	public GuiScreen openGui(GuiScreen parent);
}

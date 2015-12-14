package betterquesting.importers;

import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;

public abstract class ImporterBase
{
	public abstract String getUnlocalisedName();
	public abstract GuiEmbedded getGui(GuiQuesting screen, int posX, int posY, int sizeX, int sizeY);
}

package betterquesting.api;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IQuestingExpansion
{
	public void registerCommon(IQuestingAPI API);
	
	/**
	 * Only themes, tool, exporters and importers should be registered here
	 */
	@SideOnly(Side.CLIENT)
	public void registerClient(IQuestingAPI API);
}

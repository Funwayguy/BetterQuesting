package adv_director.rw2.api.client.gui.events;

import adv_director.rw2.api.client.gui.panels.IGuiPanel;
import net.minecraft.client.Minecraft;

public interface IPanelEvent
{
	public static void postPanelEvent(IPanelEvent event)
	{
		Minecraft mc = Minecraft.getMinecraft();
		
		if(mc.currentScreen instanceof IGuiPanel)
		{
			((IGuiPanel)mc.currentScreen).onPanelEvent(event);
		}
	}
}

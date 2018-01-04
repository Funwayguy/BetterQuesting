package betterquesting.api2.client.gui.misc;

import java.util.Comparator;
import betterquesting.api2.client.gui.panels.IGuiPanel;

public class ComparatorGuiDepth implements Comparator<IGuiPanel>
{
	public static ComparatorGuiDepth INSTANCE = new ComparatorGuiDepth();
	
	@Override
	public int compare(IGuiPanel o1, IGuiPanel o2)
	{
		return o1.getTransform().compareTo(o2.getTransform());
	}
}

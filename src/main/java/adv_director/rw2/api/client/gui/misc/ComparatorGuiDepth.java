package adv_director.rw2.api.client.gui.misc;

import java.util.Comparator;
import adv_director.rw2.api.client.gui.panels.IGuiPanel;

public class ComparatorGuiDepth implements Comparator<IGuiPanel>
{
	public static ComparatorGuiDepth INSTANCE = new ComparatorGuiDepth();
	
	@Override
	public int compare(IGuiPanel o1, IGuiPanel o2)
	{
		return o1.getTransform().compareTo(o2.getTransform());
	}
}

package betterquesting.client.toolbox;

import java.util.ArrayList;

public class ToolboxRegistry
{
	static ArrayList<ToolboxTab> toolTabs = new ArrayList<ToolboxTab>();
	
	public static void registerToolTab(ToolboxTab tab)
	{
		if(toolTabs.contains(tab))
		{
			return;
		}
		
		toolTabs.add(tab);
	}
	
	@SuppressWarnings("unchecked")
	public static ArrayList<ToolboxTab> getList()
	{
		return (ArrayList<ToolboxTab>)toolTabs.clone();
	}
}

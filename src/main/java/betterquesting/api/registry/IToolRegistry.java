package betterquesting.api.registry;

import java.util.List;
import betterquesting.api.client.toolbox.IToolboxTab;

public interface IToolRegistry
{
	public void registerToolbox(IToolboxTab toolbox);
	public List<IToolboxTab> getAllTools();
}

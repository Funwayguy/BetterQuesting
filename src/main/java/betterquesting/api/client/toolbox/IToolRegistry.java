package betterquesting.api.client.toolbox;

import java.util.List;

public interface IToolRegistry
{
	public void registerToolbox(IToolboxTab toolbox);
	public List<IToolboxTab> getAllTools();
}

package betterquesting.api.registry;

import java.util.List;
import betterquesting.api.client.io.IQuestIO;

public interface IQuestIORegistry
{
	public void registerImporter(IQuestIO importer);
	public List<IQuestIO> getImporters();
	
	public void registerExporter(IQuestIO exporter);
	public List<IQuestIO> getExporters();
}

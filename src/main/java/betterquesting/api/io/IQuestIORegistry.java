package betterquesting.api.io;

import java.util.List;

public interface IQuestIORegistry
{
	public void registerImporter(IQuestIO importer);
	public List<IQuestIO> getImporters();
	
	public void registerExporter(IQuestIO exporter);
	public List<IQuestIO> getExporters();
}

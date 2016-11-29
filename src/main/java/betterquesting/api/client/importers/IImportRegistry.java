package betterquesting.api.client.importers;

import java.util.List;

public interface IImportRegistry
{
	public void registerImporter(IImporter importer);
	public List<IImporter> getImporters();
}

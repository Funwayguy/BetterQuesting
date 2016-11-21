package betterquesting.api.importer;

import java.util.List;

public interface IImportRegistry
{
	public void registerImporter(IImporter importer);
	public List<IImporter> getImporters();
}

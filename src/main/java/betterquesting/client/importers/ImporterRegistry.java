package betterquesting.client.importers;

import java.util.ArrayList;
import java.util.List;
import betterquesting.api.client.importers.IImportRegistry;
import betterquesting.api.client.importers.IImporter;

public final class ImporterRegistry implements IImportRegistry
{
	public static final ImporterRegistry INSTANCE = new ImporterRegistry();
	
	private ArrayList<IImporter> importers = new ArrayList<IImporter>();
	
	private ImporterRegistry()
	{
	}
	
	@Override
	public void registerImporter(IImporter imp)
	{
		if(imp == null)
		{
			throw new NullPointerException("Tried to register null quest importer");
		}
		
		if(importers.contains(imp))
		{
			throw new IllegalArgumentException("Unable to register duplicate quest importer");
		}
		
		importers.add(imp);
	}
	
	@Override
	public List<IImporter> getImporters()
	{
		return importers;
	}
}

package betterquesting.importers;

import java.util.ArrayList;

public class ImporterRegistry
{
	static ArrayList<ImporterBase> importers = new ArrayList<ImporterBase>();
	
	public static void registerImporter(ImporterBase imp)
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
	
	public static ArrayList<ImporterBase> getImporters()
	{
		return new ArrayList<ImporterBase>(importers);
	}
}

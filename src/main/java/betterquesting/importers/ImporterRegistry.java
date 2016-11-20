package betterquesting.importers;

import java.util.ArrayList;
import java.util.List;
import betterquesting.api.io.IQuestIO;
import betterquesting.api.io.IQuestIORegistry;

public final class ImporterRegistry implements IQuestIORegistry
{
	public static final ImporterRegistry INSTANCE = new ImporterRegistry();
	
	private ArrayList<IQuestIO> importers = new ArrayList<IQuestIO>();
	private ArrayList<IQuestIO> exporters = new ArrayList<IQuestIO>();
	
	private ImporterRegistry()
	{
	}
	
	@Override
	public void registerImporter(IQuestIO imp)
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
	public List<IQuestIO> getImporters()
	{
		return importers;
	}
	
	@Override
	public void registerExporter(IQuestIO exp)
	{
		if(exp == null)
		{
			throw new NullPointerException("Tried to register null quest exporter");
		}
		
		if(importers.contains(exp))
		{
			throw new IllegalArgumentException("Unable to register duplicate quest exporter");
		}
		
		exporters.add(exp);
	}
	
	@Override
	public List<IQuestIO> getExporters()
	{
		return exporters;
	}
}

package adv_director.api.client.importers;

import java.io.File;
import java.io.FileFilter;
import adv_director.api.questing.IQuestDatabase;
import adv_director.api.questing.IQuestLineDatabase;

/**
 * Used as a basis for quest importers
 */
public interface IImporter
{
	public String getUnlocalisedName();
	public String getUnlocalisedDescription();
	
	public FileFilter getFileFilter();
	
	public void loadFiles(IQuestDatabase questDB, IQuestLineDatabase lineDB, File[] files);
}
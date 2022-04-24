package betterquesting.api.client.importers;

import betterquesting.api.questing.IQuestDatabase;
import betterquesting.api.questing.IQuestLineDatabase;

import java.io.File;
import java.io.FileFilter;

/**
 * Used as a basis for quest importers
 */
public interface IImporter {
    String getUnlocalisedName();

    String getUnlocalisedDescription();

    FileFilter getFileFilter();

    void loadFiles(IQuestDatabase questDB, IQuestLineDatabase lineDB, File[] files);
}
package betterquesting.importers.ftbq;

import java.io.File;
import java.io.FileFilter;

public class FTBQFileFIlter implements FileFilter
{
    @Override
    public boolean accept(File pathname)
    {
        return pathname != null && (pathname.isDirectory() || pathname.getName().equalsIgnoreCase("index.nbt"));
    }
}

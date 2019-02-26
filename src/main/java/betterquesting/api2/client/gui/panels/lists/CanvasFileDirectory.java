package betterquesting.api2.client.gui.panels.lists;

import betterquesting.api2.client.gui.misc.IGuiRect;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileFilter;
import java.util.*;

public abstract class CanvasFileDirectory extends CanvasSearch<File, File>
{
    private static final FileSort sorter = new FileSort();
    private final FileFilter filter;
    private File curDir;
    
    public CanvasFileDirectory(IGuiRect rect, @Nonnull File dirStart, FileFilter filter)
    {
        super(rect);
        this.filter = filter;
        this.curDir = dirStart;
        
        if(!curDir.isDirectory()) curDir = curDir.getParentFile();
    }
    
    public void setCurDirectory(@Nonnull File file)
    {
        curDir = file;
        this.refreshSearch();
        this.scrollY.writeValue(0F);
    }
    
    @Override
    protected Iterator<File> getIterator()
    {
        File[] files = !curDir.isDirectory() ? new File[0] : curDir.listFiles(filter);
        if(files == null) files = new File[0];
        List<File> fList = Arrays.asList(files);
        fList.sort(sorter);
        return fList.iterator();
    }
    
    @Override
    protected void queryMatches(File value, String query, ArrayDeque<File> results)
    {
        if(value.getName().toLowerCase().contains(query.toLowerCase())) results.add(value);
    }
    
    private static class FileSort implements Comparator<File>
    {
        @Override
        public int compare(File f1, File f2)
        {
            if(f1 == null || f2 == null)
            {
                return 0;
            } else if(f1.isDirectory() == f2.isDirectory())
            {
                return f1.getName().compareTo(f2.getName());
            } else
            {
                return f1.isDirectory() ? -1 : 1;
            }
        }
        
        @Override
        public boolean equals(Object obj)
        {
            return obj == this;
        }
    }
}

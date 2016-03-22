package betterquesting.client.gui.editors.explorer;

import java.io.File;
import java.io.FileFilter;

public class FileExtentionFilter implements FileFilter
{
	public final String ext;
	
	public FileExtentionFilter(String extension)
	{
		ext = extension.startsWith(".")? extension : "." + extension;
	}
	
	@Override
	public boolean accept(File pathname)
	{
		return pathname != null && (pathname.isDirectory() || pathname.getAbsolutePath().endsWith(ext));
	}
}

package betterquesting.api.utils;

import java.io.File;
import java.io.FileFilter;

public class FileExtensionFilter implements FileFilter
{
	public final String ext;
	
	public FileExtensionFilter(String extension)
	{
		ext = extension.startsWith(".")? extension : "." + extension;
	}
	
	@Override
	public boolean accept(File pathname)
	{
		return pathname != null && (pathname.isDirectory() || pathname.getAbsolutePath().endsWith(ext));
	}
}

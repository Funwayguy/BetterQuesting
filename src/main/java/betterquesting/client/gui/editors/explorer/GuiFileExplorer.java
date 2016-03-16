package betterquesting.client.gui.editors.explorer;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import net.minecraft.client.gui.GuiScreen;
import betterquesting.client.gui.GuiQuesting;

public class GuiFileExplorer extends GuiQuesting
{
	IFileCallback callback;
	File directory;
	File[] contents;
	FileFilter filter;
	int scroll = 0;
	int maxRows = 0;
	
	ArrayList<File> selected = new ArrayList<File>();
	
	public GuiFileExplorer(GuiScreen parent, IFileCallback callback, File directory, FileFilter filter)
	{
		super(parent, "betterquesting.title.file");
		this.callback = callback;
		this.directory = directory;
		this.filter = filter; // Can be null
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
		contents = directory.listFiles(filter);
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		
	}
	
	public void refreshColumns()
	{
		
	}
}

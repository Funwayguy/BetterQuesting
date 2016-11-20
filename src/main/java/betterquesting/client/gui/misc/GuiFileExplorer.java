package betterquesting.client.gui.misc;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.IVolatileScreen;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.other.IFileCallback;
import betterquesting.api.utils.RenderUtils;

public class GuiFileExplorer extends GuiScreenThemed implements IVolatileScreen
{
	IFileCallback callback;
	File directory;
	File[] contents;
	FileFilter filter;
	boolean multiSelect = true;
	
	int leftScroll = 0;
	int rightScroll = 0;
	int maxRowsL = 0;
	int maxRowsR = 0;
	
	ArrayList<File> selected = new ArrayList<File>();
	
	public GuiFileExplorer(GuiScreen parent, IFileCallback callback, File directory, FileFilter filter)
	{
		super(parent, directory.getAbsolutePath());
		this.callback = callback;
		this.directory = directory.getAbsoluteFile();
		this.filter = filter; // Can be null
	}
	
	public GuiFileExplorer setSelected(ArrayList<File> prevSelect)
	{
		selected = prevSelect;
		return this;
	}
	
	public GuiFileExplorer setMultiSelect(boolean enabled)
	{
		this.multiSelect = enabled;
		return this;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void initGui()
	{
		super.initGui();
		
		maxRowsL = (sizeY - 96)/20;
		maxRowsR = (sizeY - 96)/20;
		int btnWidth = sizeX/2 - 16;
		int sx = sizeX - 32;
		
		this.buttonList.add(new GuiButtonThemed(1, guiLeft + 16 + sx/4*3 - 50, guiTop + sizeY - 48, 100, 20, "...", true));
		
		// Left main buttons
		for(int i = 0; i < maxRowsL; i++)
		{
			GuiButtonThemed btn = new GuiButtonThemed(this.buttonList.size(), guiLeft + 16, guiTop + 48 + (i*20), btnWidth - 36, 20, "NULL", true);
			this.buttonList.add(btn);
		}
		
		// Left remove buttons
		for(int i = 0; i < maxRowsL; i++)
		{
			GuiButtonThemed btn = new GuiButtonThemed(this.buttonList.size(), guiLeft + 16 + btnWidth - 36, guiTop + 48 + (i*20), 20, 20, "" + EnumChatFormatting.RED + EnumChatFormatting.BOLD + "x", true);
			this.buttonList.add(btn);
		}
		
		// Right main buttons
		for(int i = 0; i < maxRowsR; i++)
		{
			GuiButtonThemed btn = new GuiButtonThemed(this.buttonList.size(), guiLeft + sizeX/2 + 28, guiTop + 48 + (i*20), btnWidth - 36, 20, "NULL", true);
			this.buttonList.add(btn);
		}
		
		// Right add buttons
		for(int i = 0; i < maxRowsR; i++)
		{
			GuiButtonThemed btn = new GuiButtonThemed(this.buttonList.size(), guiLeft + sizeX/2 + 8, guiTop + 48 + (i*20), 20, 20, "" + EnumChatFormatting.GREEN + EnumChatFormatting.BOLD + "<", true);
			this.buttonList.add(btn);
		}
		
		contents = directory.listFiles(filter);
		contents = contents != null? contents : new File[0];
		RefreshColumns();
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		GL11.glColor4f(1F, 1F, 1F, 1F);
		mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
		
		// Left scroll bar
		this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 48, 248, 0, 8, 20);
		int s = 20;
		while(s < (maxRowsL - 1) * 20)
		{
			this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 48 + s, 248, 20, 8, 20);
			s += 20;
		}
		this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 48 + s, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + sizeX/2 - 16, this.guiTop + 48 + (int)Math.max(0, s * (float)leftScroll/(selected.size() - maxRowsL)), 248, 60, 8, 20);
		
		// Right scroll bar
		this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 48, 248, 0, 8, 20);
		s = 20;
		while(s < (maxRowsR - 1) * 20)
		{
			this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 48 + s, 248, 20, 8, 20);
			s += 20;
		}
		
		this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 48 + s, 248, 40, 8, 20);
		this.drawTexturedModalRect(guiLeft + sizeX - 24, this.guiTop + 48 + (int)Math.max(0, s * (float)rightScroll/(contents.length - maxRowsL)), 248, 60, 8, 20);
		
		RenderUtils.DrawLine(width/2, guiTop + 32, width/2, guiTop + sizeY - 32, 2F, getTextColor());
		
		int sx = sizeX - 32;
		String txt = I18n.format("betterquesting.gui.selection");
		mc.fontRenderer.drawString(txt, guiLeft + 16 + sx/4 - mc.fontRenderer.getStringWidth(txt)/2, guiTop + 32, getTextColor(), false);
		txt = I18n.format("betterquesting.gui.folder");
		mc.fontRenderer.drawString(txt, guiLeft + 16 + sx/4*3 - mc.fontRenderer.getStringWidth(txt)/2, guiTop + 32, getTextColor(), false);
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id == 0)
		{
			if(callback != null)
	    	{
	    		callback.setFiles(selected.toArray(new File[0]));
	    	}
		} else if(button.id == 1)
		{
			if(directory.getParentFile() != null)
			{
				mc.displayGuiScreen(new GuiFileExplorer(parent, callback, directory.getParentFile(), filter).setSelected(selected));
			}
		} else if(button.id > 1)
		{
			int n1 = button.id - 2; // Line index
			int n2 = n1/maxRowsL; // Line listing (0 = quest, 1 = quest delete, 2 = registry)
			int n3 = n1%maxRowsL + leftScroll; // Quest list index
			int n4 = n1%maxRowsL + rightScroll; // Registry list index
			
			if(n2 >= 2) // Right list needs some modifications to work properly
			{
				n1 -= maxRowsL*2;
				n2 = 2 + n1/maxRowsR;
				n4 = n1%maxRowsR + rightScroll;
			}
			
			if(n2 == 0) // Open file? 
			{
				// Do nothing...
			} else if(n2 == 1) // Remove quest
			{
				if(!(n3 < 0 || n3 >= selected.size()))
				{
					selected.remove(n3);
					RefreshColumns();
				}
			} else if(n2 == 2) // Open directory?
			{
				if(!(n4 < 0 || n4 >= contents.length))
				{
					mc.displayGuiScreen(new GuiFileExplorer(parent, callback, contents[n4], filter).setSelected(selected));
				}
			} else if(n2 == 3) // Add file
			{
				if(!(n4 < 0 || n4 >= contents.length))
				{
					File f = contents[n4];
					
					if(f != null)
					{
						if(!multiSelect)
						{
							selected.clear();
						}
						
						selected.add(f);
						RefreshColumns();
					}
				}
			}
		}
	}
	
	public void RefreshColumns()
	{
		leftScroll = Math.max(0, MathHelper.clamp_int(leftScroll, 0, selected.size() - maxRowsL));
    	rightScroll = Math.max(0, MathHelper.clamp_int(rightScroll, 0, contents.length - maxRowsR));
    	
		@SuppressWarnings("unchecked")
		List<GuiButton> btnList = this.buttonList;
		
		for(int i = 2; i < btnList.size(); i++)
		{
			GuiButton btn = btnList.get(i);
			int n1 = i - 2;
			int n2 = n1/maxRowsL; // Button listing (0 = quest, 1 = quest delete, 2 = registry)
			int n3 = n1%maxRowsL + leftScroll; // Quest list index
			int n4 = n1%maxRowsL + rightScroll; // Registry list index
			
			if(n2 >= 2) // Right list needs some modifications to work properly
			{
				n1 -= maxRowsL*2;
				n2 = 2 + n1/maxRowsR;
				n4 = n1%maxRowsR + rightScroll;
			}
			
			if(n2 == 0) // Edit file?
			{
				if(n3 < 0 || n3 >= selected.size())
				{
					btn.displayString = "NULL";
					btn.visible = btn.enabled = false;
				} else
				{
					btn.visible = true;
					btn.enabled = false;
					btn.displayString = I18n.format(selected.get(n3).getName());
				}
			} else if(n2 == 1) // Remove file
			{
				btn.visible = btn.enabled = !(n3 < 0 || n3 >= selected.size());
			} else if(n2 == 2) // Open directory?
			{
				if(n4 < 0 || n4 >= contents.length)
				{
					btn.displayString = "NULL";
					btn.visible = btn.enabled = false;
				} else
				{
					File f = contents[n4];
					btn.visible = true;
					btn.enabled = f.isDirectory();
					btn.displayString = f.getName();
				}
			} else if(n2 == 3) // Add file
			{
				if(n4 < 0 || n4 >= contents.length)
				{
					btn.visible = btn.enabled = false;
				} else
				{
					File f = contents[n4];
					btn.visible = !f.isDirectory();
					btn.enabled = f != null && !f.isDirectory() && !selected.contains(f);
				}
			}
		}
	}
	
	@Override
	public void mouseScroll(int mx, int my, int scroll)
	{
		super.mouseScroll(mx, my, scroll);
		
		boolean refresh = false;
		
		if(scroll != 0 && isWithin(mx, my, this.guiLeft, this.guiTop, sizeX/2, sizeY))
        {
    		leftScroll = Math.max(0, MathHelper.clamp_int(leftScroll + scroll, 0, selected.size() - maxRowsL));
    		refresh = true;
        }
        
        if(scroll != 0 && isWithin(mx, my, this.guiLeft + sizeX/2, this.guiTop, sizeX/2, sizeY))
        {
        	rightScroll = Math.max(0, MathHelper.clamp_int(rightScroll + scroll, 0, contents.length - maxRowsR));
        	refresh = true;
        }
        
        if(refresh)
        {
        	this.RefreshColumns();
        }
	}
}

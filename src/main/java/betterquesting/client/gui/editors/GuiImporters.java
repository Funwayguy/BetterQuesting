package betterquesting.client.gui.editors;

import java.util.ArrayList;
import net.minecraft.client.gui.GuiScreen;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.importer.ImporterBase;
import betterquesting.importer.ImporterRegistry;

public class GuiImporters extends GuiQuesting
{
	ArrayList<ImporterBase> cachedImporters = new ArrayList<ImporterBase>();
	GuiEmbedded guiLeft = null;
	GuiEmbedded guiRight = null;
	int scroll = 0;
	
	public GuiImporters(GuiScreen parent)
	{
		super(parent, "betterquesting.title.importers");
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
		scroll = 0;
		cachedImporters = ImporterRegistry.getImporters();
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		if(guiLeft != null)
		{
			guiLeft.drawGui(mx, my, partialTick);
		}
		
		if(guiRight != null)
		{
			guiRight.drawGui(mx, my, partialTick);
		}
	}
	
	@Override
	public void handleMouseInput()
	{
		super.handleMouseInput();
		
		if(guiLeft != null)
		{
			guiLeft.handleMouse();
		}
		
		if(guiRight != null)
		{
			guiRight.handleMouse();
		}
	}
}

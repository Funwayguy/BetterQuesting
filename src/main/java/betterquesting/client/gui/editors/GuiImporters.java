package betterquesting.client.gui.editors;

import java.io.File;
import java.io.IOException;

import betterquesting.core.BetterQuesting;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiButtonStorage;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.lists.GuiScrollingButtons;
import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api.client.importers.IImporter;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.misc.IMultiCallback;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuestDatabase;
import betterquesting.api.questing.IQuestLineDatabase;
import betterquesting.api.utils.RenderUtils;
import betterquesting.client.gui.misc.GuiFileExplorer;
import betterquesting.client.importers.ImportedQuestLines;
import betterquesting.client.importers.ImportedQuests;
import betterquesting.client.importers.ImporterRegistry;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;

public class GuiImporters extends GuiScreenThemed implements IMultiCallback<File>
{
	private GuiScrollingButtons btnList;
	private IGuiEmbedded impGui = null;
	private IImporter selected = null;
	
	public GuiImporters(GuiScreen parent)
	{
		super(parent, "betterquesting.title.importers");
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
		btnList = new GuiScrollingButtons(mc, guiLeft + 16, guiTop + 32, sizeX/2 - 24, sizeY - 64);
		
		for(IImporter imp : ImporterRegistry.INSTANCE.getImporters())
		{
			GuiButtonStorage<IImporter> btnImp = new GuiButtonStorage<IImporter>(0, 0, 0, btnList.getListWidth(), 20, I18n.format(imp.getUnlocalisedName()));
			btnImp.setStored(imp);
			btnList.addButtonRow(btnImp);
		}
		
		this.embedded.add(btnList);
		
		int btnX = guiLeft + 16 + ((sizeX - 32)/4)*3 - 50;
		this.buttonList.add(new GuiButtonThemed(1, btnX, guiTop + sizeY - 52, 100, 20, I18n.format("betterquesting.btn.import")));
	}
	
	@Override
	public void drawBackPanel(int mx, int my, float partialTick)
	{
		super.drawBackPanel(mx, my, partialTick);
		
		RenderUtils.DrawLine(guiLeft + sizeX/2, this.guiTop + 32, guiLeft + sizeX/2, this.guiTop + sizeY - 32, 2F, getTextColor());
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id == 1 && selected != null)
		{
			mc.displayGuiScreen(new GuiFileExplorer(this, this, new File("."), selected.getFileFilter()));
		}
	}
	
	@Override
	public void mouseClicked(int mx, int my, int click) throws IOException
	{
		super.mouseClicked(mx, my, click);
		
		GuiButtonThemed btn = this.btnList.getButtonUnderMouse(mx, my);
		
		if(click == 0 && btn != null && btn.mousePressed(mc, mx, my))
		{
			btn.playPressSound(mc.getSoundHandler());
			@SuppressWarnings("unchecked")
			IImporter imp = ((GuiButtonStorage<IImporter>)btn).getStored();
			
			this.selected = imp;
			
			if(impGui != null)
			{
				this.embedded.remove(impGui);
			}
			
			impGui = new GuiImporterEmbedded(imp, guiLeft + sizeX/2 + 8, guiTop + 32, sizeX/2 - 24, sizeY - 84);
			this.embedded.add(impGui);
		}
	}

	@Override
	public void setValues(File[] files)
	{
		if(selected != null)
		{
			ImportedQuests questDB = new ImportedQuests();
			IQuestLineDatabase lineDB = new ImportedQuestLines();
			
			selected.loadFiles(questDB, lineDB, files);
			
			if(questDB.size() > 0 || lineDB.size() > 0)
			{
				NBTTagCompound jsonBase = new NBTTagCompound();
				jsonBase.setTag("quests", questDB.writeToNBT(new NBTTagList(), EnumSaveType.CONFIG));
				jsonBase.setTag("lines", lineDB.writeToNBT(new NBTTagList(), EnumSaveType.CONFIG));
				
				NBTTagCompound tag = new NBTTagCompound();
				tag.setTag("data", jsonBase);
				PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.IMPORT.GetLocation(), tag));
				
				mc.displayGuiScreen(parent);
			}
		}
	}
}

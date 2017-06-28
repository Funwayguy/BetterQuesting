package adv_director.client.gui.editors;

import java.io.File;
import java.io.IOException;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import adv_director.api.client.gui.GuiScreenThemed;
import adv_director.api.client.gui.controls.GuiButtonStorage;
import adv_director.api.client.gui.controls.GuiButtonThemed;
import adv_director.api.client.gui.lists.GuiScrollingButtons;
import adv_director.api.client.gui.misc.IGuiEmbedded;
import adv_director.api.client.importers.IImporter;
import adv_director.api.enums.EnumSaveType;
import adv_director.api.misc.IMultiCallback;
import adv_director.api.network.QuestingPacket;
import adv_director.api.questing.IQuestDatabase;
import adv_director.api.questing.IQuestLineDatabase;
import adv_director.api.utils.NBTConverter;
import adv_director.api.utils.RenderUtils;
import adv_director.client.gui.misc.GuiFileExplorer;
import adv_director.client.importers.ImportedQuestLines;
import adv_director.client.importers.ImportedQuests;
import adv_director.client.importers.ImporterRegistry;
import adv_director.network.PacketSender;
import adv_director.network.PacketTypeNative;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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
			IQuestDatabase questDB = new ImportedQuests();
			IQuestLineDatabase lineDB = new ImportedQuestLines();
			
			selected.loadFiles(questDB, lineDB, files);
			
			if(questDB.size() > 0 || lineDB.size() > 0)
			{
				// TODO: Open selection dialog
				
				JsonObject jsonBase = new JsonObject();
				jsonBase.add("quests", questDB.writeToJson(new JsonArray(), EnumSaveType.CONFIG));
				jsonBase.add("lines", lineDB.writeToJson(new JsonArray(), EnumSaveType.CONFIG));
				
				NBTTagCompound tag = new NBTTagCompound();
				tag.setTag("data", NBTConverter.JSONtoNBT_Object(jsonBase, new NBTTagCompound()));
				PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.IMPORT.GetLocation(), tag));
				
				mc.displayGuiScreen(parent);
			}
		}
	}
}

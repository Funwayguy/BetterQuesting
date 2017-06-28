package adv_director.client.gui.editors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import adv_director.api.client.gui.GuiElement;
import adv_director.api.client.gui.lists.GuiScrollingText;
import adv_director.api.client.gui.misc.IGuiEmbedded;
import adv_director.api.client.importers.IImporter;

public class GuiImporterEmbedded extends GuiElement implements IGuiEmbedded
{
	private final Minecraft mc;
	private final IImporter importer;
	
	private GuiScrollingText txtScroll;
	
	private int posX = 0;
	private int posY = 0;
	private int sizeX = 0;
	
	public GuiImporterEmbedded(IImporter importer, int posX, int posY, int sizeX, int sizeY)
	{
		this.mc = Minecraft.getMinecraft();
		this.importer = importer;
		
		this.posX = posX;
		this.posY = posY;
		this.sizeX = sizeX;
		
		this.txtScroll = new GuiScrollingText(mc, posX, posY + 16, sizeX, sizeY - 16);
		this.txtScroll.SetText(I18n.format(importer.getUnlocalisedDescription()));
	}
	
	@Override
	public void drawBackground(int mx, int my, float partialTick)
	{
		txtScroll.drawBackground(mx, my, partialTick);
		
		this.drawCenteredString(mc.fontRendererObj, I18n.format(importer.getUnlocalisedName()), posX + sizeX/2, posY + 4, getTextColor(), false);
	}
	
	@Override
	public void drawForeground(int mx, int my, float partialTick)
	{
		txtScroll.drawForeground(mx, my, partialTick);
	}
	
	@Override
	public void onMouseClick(int mx, int my, int click)
	{
	}
	
	@Override
	public void onMouseScroll(int mx, int my, int scroll)
	{
	}
	
	@Override
	public void onKeyTyped(char c, int keyCode)
	{
	}
	
}

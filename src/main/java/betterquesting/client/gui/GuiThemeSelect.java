package betterquesting.client.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiButtonStorage;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.lists.GuiScrollingButtons;
import betterquesting.api.client.themes.ITheme;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.utils.RenderUtils;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.misc.DummyQuest;

@Deprecated
@SideOnly(Side.CLIENT)
public class GuiThemeSelect extends GuiScreenThemed
{
	private GuiScrollingButtons btnList;
	private List<ITheme> themeList = new ArrayList<ITheme>();
	
	public GuiThemeSelect(GuiScreen parent)
	{
		super(parent, I18n.format("betterquesting.title.select_theme"));
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
		ThemeRegistry.INSTANCE.reloadThemes();
		themeList.clear();
		themeList.addAll(ThemeRegistry.INSTANCE.getAllThemes());
		
		btnList = new GuiScrollingButtons(mc, guiLeft + 16, guiTop + 32, sizeX/2 - 24, sizeY - 64);
		this.embedded.add(btnList);
		
		RefreshColumns();
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		GlStateManager.color(1F, 1F, 1F, 1F);
		
		RenderUtils.DrawLine(width/2, guiTop + 32, width/2, guiTop + sizeY - 32, 2F, getTextColor());
		
		GlStateManager.pushMatrix();
		
		mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
		float scale = (sizeX/2 - 24)/128F;
		scale = Math.min(scale, (sizeY - 64)/128F);
		
		GlStateManager.translate(guiLeft + sizeX/2 + 8 + (sizeX - 48)/4 - 64*scale, guiTop + sizeY/2 - 64*scale, 0);
		GlStateManager.scale(scale, scale, 1F);
		
		this.drawTexturedModalRect(0, 0, 0, 128, 128, 128);
		
		this.drawTexturedModalRect(55, 40, 0, 48, 18, 18);
		
		DummyQuest.dummyQuest.getProperties().setProperty(NativeProps.MAIN, true);
		currentTheme().getRenderer().drawIcon(DummyQuest.dummyQuest, DummyQuest.dummyID, 80, 72, 24, 24, (int)(mx / scale), (int)(my / scale), partialTick);
		DummyQuest.dummyQuest.getProperties().setProperty(NativeProps.MAIN, false);
		currentTheme().getRenderer().drawIcon(DummyQuest.dummyQuest, DummyQuest.dummyID, 24, 72, 24, 24, (int)(mx / scale), (int)(my / scale), partialTick);
		
		currentTheme().getRenderer().drawLine(DummyQuest.dummyQuest, DummyQuest.dummyID, 48, 84, 80, 84, mx, my, partialTick);
    	
    	GlStateManager.color(1F, 1F, 1F, 1F);
    	
    	String txt = TextFormatting.BOLD + "EXAMPLE";
    	mc.fontRenderer.drawString(txt, 64 - mc.fontRenderer.getStringWidth(txt)/2, 32 - mc.fontRenderer.FONT_HEIGHT, getTextColor());
    	
    	RenderUtils.RenderItemStack(mc, new ItemStack(Items.ENCHANTED_BOOK), 56, 41, "");
		
		GlStateManager.popMatrix();
	}
	
	@Override
	public void actionPerformed(GuiButton btn)
	{
		super.actionPerformed(btn);
		
		if(btn.id == 1)
		{
			@SuppressWarnings("unchecked")
			GuiButtonStorage<ITheme> btnTheme = (GuiButtonStorage<ITheme>)btn;
			ThemeRegistry.INSTANCE.setCurrentTheme(btnTheme.getStored());
			
			RefreshColumns();
		}
	}
	
	//@SuppressWarnings("unchecked")
	public void RefreshColumns()
	{
		btnList.getEntryList().clear();
		
		for(ITheme th : themeList)
		{
			GuiButtonStorage<ITheme> btn = new GuiButtonStorage<ITheme>(1, 0, 0, btnList.getListWidth(), 20, th.getDisplayName());
			btn.setStored(th);
			btn.enabled = th != currentTheme();
			btnList.addButtonRow(btn);
		}
	}
	
	@Override
	public void mouseClicked(int mx, int my, int click) throws IOException
	{
		super.mouseClicked(mx, my, click);
		
		GuiButtonThemed btn = btnList.getButtonUnderMouse(mx, my);
		
		if(btn != null && btn.mousePressed(mc, mx, my))
		{
			btn.playPressSound(mc.getSoundHandler());
			actionPerformed(btn);
		}
	}
}

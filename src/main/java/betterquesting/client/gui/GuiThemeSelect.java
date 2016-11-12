package betterquesting.client.gui;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiButtonStorage;
import betterquesting.api.client.gui.lists.GuiScrollingButtons;
import betterquesting.api.client.themes.IThemeBase;
import betterquesting.api.enums.EnumQuestState;
import betterquesting.api.utils.RenderUtils;
import betterquesting.registry.ThemeRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiThemeSelect extends GuiScreenThemed
{
	private GuiScrollingButtons btnList;
	private List<IThemeBase> themeList = new ArrayList<IThemeBase>();
	
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
		
		GL11.glColor4f(1F, 1F, 1F, 1F);
		
		RenderUtils.DrawLine(width/2, guiTop + 32, width/2, guiTop + sizeY - 32, 2F, getTextColor());
		
		GL11.glPushMatrix();
		
		mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
		float scale = ((sizeX - 32)/2)/128F;
		scale = Math.min(scale, (sizeY - 64)/128F);
		
		GL11.glScalef(scale, scale, 1F);
		
		int cx = (int)((guiLeft + sizeX/4 * 3)/scale);
		int cy = (int)((guiTop + sizeY/2)/scale);
		
		this.drawTexturedModalRect(cx - 64, cy - 64, 0, 128, 128, 128);
		
		this.drawTexturedModalRect(cx - 9, cy - 24, 0, 48, 18, 18);
		
    	int ci = currentTheme().getQuestIconColor(null, EnumQuestState.values()[(int)(Minecraft.getSystemTime()/2000)%4], (int)(Minecraft.getSystemTime()/1000)%2 + 1);
		float r = (float)(ci >> 16 & 255) / 255.0F;
        float g = (float)(ci >> 8 & 255) / 255.0F;
        float b = (float)(ci & 255) / 255.0F;
    	GL11.glColor4f(r, g, b, 1F);
    	
		this.drawTexturedModalRect(cx + 16, cy + 8, 0, 104, 24, 24);
		this.drawTexturedModalRect(cx - 40, cy + 8, 24, 104, 24, 24);
		
    	int cl = currentTheme().getQuestLineColor(null, EnumQuestState.values()[(int)(Minecraft.getSystemTime()/2000)%4]);
    	RenderUtils.DrawLine(cx - 16, cy + 20, cx + 16, cy + 20, 4, cl);
    	
    	GL11.glColor4f(1F, 1F, 1F, 1F);
    	
    	String txt = EnumChatFormatting.BOLD + "EXAMPLE";
    	mc.fontRenderer.drawString(txt, cx - mc.fontRenderer.getStringWidth(txt)/2, cy - 32 - mc.fontRenderer.FONT_HEIGHT, getTextColor());
    	
    	RenderUtils.RenderItemStack(mc, new ItemStack(Items.enchanted_book), cx - 8, cy - 23, "");
		
		GL11.glPopMatrix();
	}
	
	@Override
	public void actionPerformed(GuiButton btn)
	{
		super.actionPerformed(btn);
		
		if(btn.id > 0)
		{
			ThemeRegistry.INSTANCE.setCurrentTheme(themeList.get(btn.id - 1));
		}
	}
	
	//@SuppressWarnings("unchecked")
	public void RefreshColumns()
	{
		btnList.getEntryList().clear();
		
		for(int i = 0; i < themeList.size(); i++)
		{
			IThemeBase th = themeList.get(i);
			GuiButtonStorage<IThemeBase> btn = new GuiButtonStorage<IThemeBase>(1 + i, 0, 0, btnList.getListWidth(), 20, th.getDisplayName());
			btn.setStored(th);
			btnList.addButtonRow(btn);
		}
	}
}

package betterquesting.client.toolbox;

import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.toolbox.IToolTab;
import betterquesting.client.gui2.CanvasQuestLine;
import betterquesting.client.gui2.editors.designer.PanelTabMain;
import betterquesting.client.gui2.editors.designer.PanelToolController;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class ToolboxTabMain implements IToolTab
{
	public static final ToolboxTabMain INSTANCE = new ToolboxTabMain();
	
	private int dragSnap = 2;
	private int[] snaps = new int[]{1,4,6,8,12,16,24,32};
	
	@Override
	public String getUnlocalisedName()
	{
		return "betterquesting.toolbox.tab.main";
	}
	
	@Override
	public IGuiPanel getTabGui(IGuiRect rect, CanvasQuestLine cvQuestLine, PanelToolController toolController)
	{
		return new PanelTabMain(rect, cvQuestLine, toolController);
	}
	
	public void toggleSnap()
	{
		dragSnap = (dragSnap + 1)%snaps.length;
	}
	
	public int getSnapValue()
	{
		return snaps[dragSnap%snaps.length];
	}
	
	public int getSnapIndex()
	{
		return dragSnap;
	}
	
	public void drawGrid(CanvasQuestLine ui)
	{
		if(getSnapValue() <= 1) return;
		
		float zs = ui.getZoom();
		
		float offX = -ui.getScrollX();
		while(offX < 0) offX += getSnapValue();
		offX = (offX % getSnapValue()) * zs;
		int midX = -ui.getScrollX() / getSnapValue();
		
		float offY = -ui.getScrollY();
		while(offY < 0) offY += getSnapValue();
		offY = (offY % getSnapValue()) * zs;
		int midY = -ui.getScrollY() / getSnapValue();
		
		int x = ui.getTransform().getX();
		int y = ui.getTransform().getY();
		int width = ui.getTransform().getWidth();
		int height = ui.getTransform().getHeight();
		int divX = (int)Math.ceil((width - offX) / (zs * getSnapValue()));
		int divY = (int)Math.ceil((height - offY) / (zs * getSnapValue()));
        
        IGuiColor gMinor = PresetColor.GRID_MINOR.getColor();
        IGuiColor gMajor = PresetColor.GRID_MAJOR.getColor();
		
		for(int i = 0; i < divX; i++)
		{
			GlStateManager.pushMatrix();
			GL11.glEnable(GL11.GL_LINE_STIPPLE);
			if(i != midX)
			{
				GL11.glLineStipple(2, (short)0b1010101010101010);
			}
			int lx = x + (int)(i * getSnapValue() * zs + offX);
			RenderUtils.DrawLine(lx, y, lx, y + height, i == midX ? 2F : 1F, i == midX ? gMajor.getRGB() : gMinor.getRGB());
			GL11.glLineStipple(1, (short)0xFFFF);
			GL11.glDisable(GL11.GL_LINE_STIPPLE);
			GlStateManager.popMatrix();
		}
		
		for(int j = 0; j < divY; j++)
		{
			GlStateManager.pushMatrix();
			GL11.glEnable(GL11.GL_LINE_STIPPLE);
			if(j != midY)
			{
				GL11.glLineStipple(2, (short)0b1010101010101010);
			}
			int ly = y + (int)(j * getSnapValue() * zs + offY);
			RenderUtils.DrawLine(x, ly, x + width, ly, j == midY ? 2F : 1F, j == midY ? gMajor.getRGB() : gMinor.getRGB());
			GL11.glLineStipple(1, (short)0xFFFF);
			GL11.glDisable(GL11.GL_LINE_STIPPLE);
			GlStateManager.popMatrix();
		}
	}
}

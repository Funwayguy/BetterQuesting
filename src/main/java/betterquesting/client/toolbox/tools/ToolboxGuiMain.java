package betterquesting.client.toolbox.tools;

import java.util.ArrayList;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.GuiQuestLinesEmbedded;
import betterquesting.client.gui.editors.GuiQuestLineDesigner;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.client.toolbox.ToolboxGui;
import betterquesting.client.toolbox.ToolboxTool;
import betterquesting.core.BetterQuesting;
import betterquesting.utils.RenderUtils;

public class ToolboxGuiMain extends ToolboxGui
{
	ArrayList<GuiButtonQuesting> list = new ArrayList<GuiButtonQuesting>();
	private static int dragSnap = 2;
	private static int[] snaps = new int[]{1,4,6,8,12,24};
	
	GuiButtonQuesting btnOpen;
	GuiButtonQuesting btnNew;
	GuiButtonQuesting btnGrab;
	GuiButtonQuesting btnSnap;
	GuiButtonQuesting btnLink;
	GuiButtonQuesting btnCopy;
	GuiButtonQuesting btnRem;
	GuiButtonQuesting btnDel;
	
	public ToolboxGuiMain(GuiQuestLineDesigner designer, int posX, int posY, int sizeX, int sizeY)
	{
		super(designer, posX, posY, sizeX, sizeY);
	}
	
	@Override
	public void refreshGui()
	{
		ToolboxTool curTool = designer.getEmbeddedGui().getCurrentTool();
		
		list.clear();
		
		btnOpen = new GuiButtonQuesting(0, posX + 8, posY + 8, 20, 20, "");
		btnOpen.setIcon(new ResourceLocation(BetterQuesting.MODID + ":textures/gui/editor_icons.png"), 112, 0, 16, 16, true);
		setButtonTooltip(btnOpen, I18n.format("betterquesting.toolbox.tool.open.name"), I18n.format("betterquesting.toolbox.tool.open.desc"));
		list.add(btnOpen);
		btnOpen.enabled = curTool != ToolboxTabMain.instance.toolOpen;
		
		btnNew = new GuiButtonQuesting(1, posX + 36, posY + 8, 20, 20, "");
		btnNew.setIcon(new ResourceLocation(BetterQuesting.MODID + ":textures/gui/editor_icons.png"), 48, 16, 16, 16, true);
		setButtonTooltip(btnNew, I18n.format("betterquesting.toolbox.tool.new.name"), I18n.format("betterquesting.toolbox.tool.new.desc"));
		list.add(btnNew);
		btnNew.enabled = curTool != ToolboxTabMain.instance.toolNew;
		
		btnGrab = new GuiButtonQuesting(2, posX + 8, posY + 36, 20, 20, "");
		btnGrab.setIcon(new ResourceLocation(BetterQuesting.MODID + ":textures/gui/editor_icons.png"), 48, 0, 16, 16, true);
		setButtonTooltip(btnGrab, I18n.format("betterquesting.toolbox.tool.grab.name"), I18n.format("betterquesting.toolbox.tool.grab.desc"));
		list.add(btnGrab);
		btnGrab.enabled = curTool != ToolboxTabMain.instance.toolGrab;
		
		btnSnap = new GuiButtonQuesting(2, posX + 36, posY + 36, 20, 20, TextFormatting.BLACK.toString() + dragSnap);
		btnSnap.setIcon(new ResourceLocation(BetterQuesting.MODID + ":textures/gui/editor_icons.png"), 64, 0, 16, 16, true);
		setButtonTooltip(btnSnap, I18n.format("betterquesting.toolbox.tool.snap.name"), I18n.format("betterquesting.toolbox.tool.snap.desc"));
		btnSnap.disableShadow();
		list.add(btnSnap);
		
		btnLink = new GuiButtonQuesting(2, posX + 8, posY + 64, 20, 20, "");
		btnLink.setIcon(new ResourceLocation(BetterQuesting.MODID + ":textures/gui/editor_icons.png"), 80, 0, 16, 16, true);
		setButtonTooltip(btnLink, I18n.format("betterquesting.toolbox.tool.link.name"), I18n.format("betterquesting.toolbox.tool.link.desc"));
		list.add(btnLink);
		btnLink.enabled = curTool != ToolboxTabMain.instance.toolLink;
		
		btnCopy = new GuiButtonQuesting(2, posX + 36, posY + 64, 20, 20, "");
		btnCopy.setIcon(new ResourceLocation(BetterQuesting.MODID + ":textures/gui/editor_icons.png"), 32, 0, 16, 16, true);
		setButtonTooltip(btnCopy, I18n.format("betterquesting.toolbox.tool.copy.name"), I18n.format("betterquesting.toolbox.tool.copy.desc"));
		list.add(btnCopy);
		btnCopy.enabled = curTool != ToolboxTabMain.instance.toolCopy;
		
		btnRem = new GuiButtonQuesting(2, posX + 8, posY + 92, 20, 20, "");
		btnRem.setIcon(new ResourceLocation(BetterQuesting.MODID + ":textures/gui/editor_icons.png"), 112, 16, 16, 16, true);
		setButtonTooltip(btnRem, I18n.format("betterquesting.toolbox.tool.remove.name"), I18n.format("betterquesting.toolbox.tool.remove.desc"));
		list.add(btnRem);
		btnRem.enabled = curTool != ToolboxTabMain.instance.toolRem;
		
		btnDel = new GuiButtonQuesting(2, posX + 36, posY + 92, 20, 20, "");
		btnDel.setIcon(new ResourceLocation(BetterQuesting.MODID + ":textures/gui/editor_icons.png"), 16, 0, 16, 16, true);
		setButtonTooltip(btnDel, I18n.format("betterquesting.toolbox.tool.delete.name"), I18n.format("betterquesting.toolbox.tool.delete.desc"));
		list.add(btnDel);
		btnDel.enabled = curTool != ToolboxTabMain.instance.toolDel;
		
		if(designer.getEmbeddedGui() != null && designer.getEmbeddedGui().getCurrentTool() == null)
		{
			designer.getEmbeddedGui().setCurrentTool(ToolboxTabMain.instance.toolOpen);
			resetButtons();
			btnOpen.enabled = false;
		}
	}
	
	private void resetButtons()
	{
		for(GuiButtonQuesting btn : list)
		{
			btn.enabled = true;
		}
	}
	
	@Override
	public void drawGui(int mx, int my, float partialTick)
	{
		btnOpen.drawButton(screen.mc, mx, my);
		btnNew.drawButton(screen.mc, mx, my);
		btnGrab.drawButton(screen.mc, mx, my);
		btnSnap.drawButton(screen.mc, mx, my);
		btnLink.drawButton(screen.mc, mx, my);
		btnCopy.drawButton(screen.mc, mx, my);
		btnRem.drawButton(screen.mc, mx, my);
		btnDel.drawButton(screen.mc, mx, my);
	}
	
	@Override
	public void drawOverlays(int mx, int my, float partialTick)
	{
		GuiButtonQuesting bTip = null;
		
		bTip = btnOpen.isMouseOver()? btnOpen : bTip;
		bTip = btnNew.isMouseOver()? btnNew : bTip;
		bTip = btnGrab.isMouseOver()? btnGrab : bTip;
		bTip = btnSnap.isMouseOver()? btnSnap : bTip;
		bTip = btnLink.isMouseOver()? btnLink : bTip;
		bTip = btnCopy.isMouseOver()? btnCopy : bTip;
		bTip = btnRem.isMouseOver()? btnRem : bTip;
		bTip = btnDel.isMouseOver()? btnDel : bTip;
		
		if(bTip != null)
		{
			ArrayList<String> sTip = bTip.getTooltip();
			
			if(sTip != null && sTip.size() > 0)
			{
				designer.DrawTooltip(bTip.getTooltip(), mx, my);
			}
		}
	}
	
	/**
	 * Draws the active snap grid (called from supported tools)
	 */
	public static void drawGrid(GuiQuestLinesEmbedded ui)
	{
		if(getSnapValue() <= 1)
		{
			return;
		}
		
		int minI = ui.getRelativeX(ui.getPosX());
		int minJ = ui.getRelativeY(ui.getPosY());
		minI -= minI%getSnapValue();
		minJ -= minJ%getSnapValue();
		int maxI = ui.getRelativeX(ui.getPosX() + ui.getWidth());
		int maxJ = ui.getRelativeY(ui.getPosY() + ui.getHeight());
		
		for(int i = minI; i < maxI; i += getSnapValue())
		{
			GlStateManager.pushMatrix();
			GL11.glEnable(GL11.GL_LINE_STIPPLE);
			if(i != 0)
			{
				//GL11.glLineStipple(2, (short)0b1010101010101010); // 1.7 upward only
				GL11.glLineStipple(2, (short)43690);
			}
			RenderUtils.DrawLine(ui.getScreenX(i), ui.getPosY(), ui.getScreenX(i), ui.getPosY() + ui.getHeight(), i == 0? 2F : 1F, ThemeRegistry.curTheme().textColor());
			GL11.glLineStipple(1, (short)0xFFFF);
			GL11.glDisable(GL11.GL_LINE_STIPPLE);
			GlStateManager.popMatrix();
		}
		
		for(int j = minJ; j < maxJ; j += getSnapValue())
		{
			GlStateManager.pushMatrix();
			GL11.glEnable(GL11.GL_LINE_STIPPLE);
			if(j != 0)
			{
				//GL11.glLineStipple(2, (short)0b1010101010101010); // 1.7 upward only
				GL11.glLineStipple(2, (short)43690);
			}
			RenderUtils.DrawLine(ui.getPosX(), ui.getScreenY(j), ui.getPosX() + ui.getWidth(), ui.getScreenY(j), j == 0? 2F : 1F, ThemeRegistry.curTheme().textColor());
			GL11.glLineStipple(1, (short)0xFFFF);
			GL11.glDisable(GL11.GL_LINE_STIPPLE);
			GlStateManager.popMatrix();
		}
	}
	
	@Override
	public void mouseClick(int mx, int my, int click)
	{
		super.mouseClick(mx, my, click);
		
		if(btnOpen.mousePressed(screen.mc, mx, my))
		{
			resetButtons();
			btnOpen.playPressSound(screen.mc.getSoundHandler());
			btnOpen.enabled = false;
			designer.getEmbeddedGui().setCurrentTool(ToolboxTabMain.instance.toolOpen);
		} else if(btnSnap.mousePressed(screen.mc, mx, my))
		{
			btnSnap.playPressSound(screen.mc.getSoundHandler());
			toggleSnap();
			btnSnap.displayString = TextFormatting.BLACK.toString() + dragSnap;
		} else if(btnGrab.mousePressed(screen.mc, mx, my))
		{
			resetButtons();
			btnGrab.playPressSound(screen.mc.getSoundHandler());
			btnGrab.enabled = false;
			designer.getEmbeddedGui().setCurrentTool(ToolboxTabMain.instance.toolGrab);
		} else if(btnNew.mousePressed(screen.mc, mx, my))
		{
			resetButtons();
			btnNew.playPressSound(screen.mc.getSoundHandler());
			btnNew.enabled = false;
			designer.getEmbeddedGui().setCurrentTool(ToolboxTabMain.instance.toolNew);
		} else if(btnCopy.mousePressed(screen.mc, mx, my))
		{
			resetButtons();
			btnCopy.playPressSound(screen.mc.getSoundHandler());
			btnCopy.enabled = false;
			designer.getEmbeddedGui().setCurrentTool(ToolboxTabMain.instance.toolCopy);
		} else if(btnLink.mousePressed(screen.mc, mx, my))
		{
			resetButtons();
			btnLink.playPressSound(screen.mc.getSoundHandler());
			btnLink.enabled = false;
			designer.getEmbeddedGui().setCurrentTool(ToolboxTabMain.instance.toolLink);
		} else if(btnDel.mousePressed(screen.mc, mx, my))
		{
			resetButtons();
			btnDel.playPressSound(screen.mc.getSoundHandler());
			btnDel.enabled = false;
			designer.getEmbeddedGui().setCurrentTool(ToolboxTabMain.instance.toolDel);
		} else if(btnRem.mousePressed(screen.mc, mx, my))
		{
			resetButtons();
			btnRem.playPressSound(screen.mc.getSoundHandler());
			btnRem.enabled = false;
			designer.getEmbeddedGui().setCurrentTool(ToolboxTabMain.instance.toolRem);
		}
	}
	
	private void setButtonTooltip(GuiButtonQuesting btn, String title, String desc)
	{
		ArrayList<String> list = new ArrayList<String>();
		list.add(title);
		list.addAll(designer.mc.fontRendererObj.listFormattedStringToWidth(TextFormatting.GRAY + desc, 128));
		btn.setTooltip(list);
	}
	
	public static void toggleSnap()
	{
		dragSnap = (dragSnap + 1)%snaps.length;
	}
	
	public static int getSnapValue()
	{
		return snaps[dragSnap%snaps.length];
	}
	
	public static int getSnapIndex()
	{
		return dragSnap;
	}
}

package betterquesting.client.toolbox;

import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.lists.GuiScrollingButtons;
import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api.client.gui.misc.IGuiQuestLine;
import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.utils.RenderUtils;
import betterquesting.core.BetterQuesting;

public class ToolboxGuiMain extends GuiElement implements IGuiEmbedded
{
	private IGuiQuestLine gui;
	
	private ArrayList<GuiButtonThemed> list = new ArrayList<GuiButtonThemed>();
	private static int dragSnap = 2;
	private static int[] snaps = new int[]{1,4,6,8,12,24};
	
	private GuiButtonThemed btnOpen;
	private GuiButtonThemed btnNew;
	private GuiButtonThemed btnGrab;
	private GuiButtonThemed btnSnap;
	private GuiButtonThemed btnLink;
	private GuiButtonThemed btnCopy;
	private GuiButtonThemed btnRem;
	private GuiButtonThemed btnDel;
	private GuiButtonThemed btnCom;
	private GuiButtonThemed btnRes;
	private GuiButtonThemed btnIco;
	private GuiButtonThemed btnSca;
	private GuiButtonThemed btnRaw;
	
	private GuiScrollingButtons btnList;
	
	public ToolboxGuiMain(IGuiQuestLine gui, int posX, int posY, int sizeX, int sizeY)
	{
		this.gui = gui;
		
		IToolboxTool curTool = gui.getActiveTool();
		
		list.clear();
		btnList = new GuiScrollingButtons(Minecraft.getMinecraft(), posX, posY, sizeX, sizeY);
		
		btnOpen = new GuiButtonThemed(0, posX + 8, posY + 8, (sizeX - 8)/2, 20, "", false);
		btnOpen.setIcon(new ResourceLocation(BetterQuesting.MODID + ":textures/gui/editor_icons.png"), 112, 0, 16, 16, true);
		setButtonTooltip(btnOpen, I18n.format("betterquesting.toolbox.tool.open.name"), I18n.format("betterquesting.toolbox.tool.open.desc"));
		list.add(btnOpen);
		btnOpen.enabled = curTool != ToolboxTabMain.instance.toolOpen;
		
		btnNew = new GuiButtonThemed(1, posX + 36, posY + 8, (sizeX - 8)/2, 20, "", false);
		btnNew.setIcon(new ResourceLocation(BetterQuesting.MODID + ":textures/gui/editor_icons.png"), 48, 16, 16, 16, true);
		setButtonTooltip(btnNew, I18n.format("betterquesting.toolbox.tool.new.name"), I18n.format("betterquesting.toolbox.tool.new.desc"));
		list.add(btnNew);
		btnNew.enabled = curTool != ToolboxTabMain.instance.toolNew;
		
		btnList.addButtonRow(btnOpen, btnNew);
		
		btnGrab = new GuiButtonThemed(2, posX + 8, posY + 36, (sizeX - 8)/2, 20, "", false);
		btnGrab.setIcon(new ResourceLocation(BetterQuesting.MODID + ":textures/gui/editor_icons.png"), 48, 0, 16, 16, true);
		setButtonTooltip(btnGrab, I18n.format("betterquesting.toolbox.tool.grab.name"), I18n.format("betterquesting.toolbox.tool.grab.desc"));
		list.add(btnGrab);
		btnGrab.enabled = curTool != ToolboxTabMain.instance.toolGrab;
		
		btnSnap = new GuiButtonThemed(2, posX + 36, posY + 36, (sizeX - 8)/2, 20, EnumChatFormatting.BLACK.toString() + dragSnap, false);
		btnSnap.setIcon(new ResourceLocation(BetterQuesting.MODID + ":textures/gui/editor_icons.png"), 64, 0, 16, 16, true);
		setButtonTooltip(btnSnap, I18n.format("betterquesting.toolbox.tool.snap.name"), I18n.format("betterquesting.toolbox.tool.snap.desc"));
		list.add(btnSnap);
		
		btnList.addButtonRow(btnGrab, btnSnap);
		
		btnLink = new GuiButtonThemed(2, posX + 8, posY + 64, (sizeX - 8)/2, 20, "", false);
		btnLink.setIcon(new ResourceLocation(BetterQuesting.MODID + ":textures/gui/editor_icons.png"), 80, 0, 16, 16, true);
		setButtonTooltip(btnLink, I18n.format("betterquesting.toolbox.tool.link.name"), I18n.format("betterquesting.toolbox.tool.link.desc"));
		list.add(btnLink);
		btnLink.enabled = curTool != ToolboxTabMain.instance.toolLink;
		
		btnCopy = new GuiButtonThemed(2, posX + 36, posY + 64, (sizeX - 8)/2, 20, "", false);
		btnCopy.setIcon(new ResourceLocation(BetterQuesting.MODID + ":textures/gui/editor_icons.png"), 32, 0, 16, 16, true);
		setButtonTooltip(btnCopy, I18n.format("betterquesting.toolbox.tool.copy.name"), I18n.format("betterquesting.toolbox.tool.copy.desc"));
		list.add(btnCopy);
		btnCopy.enabled = curTool != ToolboxTabMain.instance.toolCopy;
		
		btnList.addButtonRow(btnLink, btnCopy);
		
		btnRem = new GuiButtonThemed(2, posX + 8, posY + 92, (sizeX - 8)/2, 20, "", false);
		btnRem.setIcon(new ResourceLocation(BetterQuesting.MODID + ":textures/gui/editor_icons.png"), 112, 16, 16, 16, true);
		setButtonTooltip(btnRem, I18n.format("betterquesting.toolbox.tool.remove.name"), I18n.format("betterquesting.toolbox.tool.remove.desc"));
		list.add(btnRem);
		btnRem.enabled = curTool != ToolboxTabMain.instance.toolRem;
		
		btnDel = new GuiButtonThemed(2, posX + 36, posY + 92, (sizeX - 8)/2, 20, "", false);
		btnDel.setIcon(new ResourceLocation(BetterQuesting.MODID + ":textures/gui/editor_icons.png"), 16, 0, 16, 16, true);
		setButtonTooltip(btnDel, I18n.format("betterquesting.toolbox.tool.delete.name"), I18n.format("betterquesting.toolbox.tool.delete.desc"));
		list.add(btnDel);
		btnDel.enabled = curTool != ToolboxTabMain.instance.toolDel;
		
		btnList.addButtonRow(btnRem, btnDel);
		
		btnCom = new GuiButtonThemed(2, posX + 8, posY + 120, (sizeX - 8)/2, 20, "", false);
		btnCom.setIcon(new ResourceLocation(BetterQuesting.MODID + ":textures/gui/editor_icons.png"), 128, 0, 16, 16, true);
		setButtonTooltip(btnCom, I18n.format("betterquesting.toolbox.tool.complete.name"), I18n.format("betterquesting.toolbox.tool.complete.desc"));
		list.add(btnCom);
		btnCom.enabled = curTool != ToolboxTabMain.instance.toolCom;
		
		btnRes = new GuiButtonThemed(2, posX + 36, posY + 120, (sizeX - 8)/2, 20, "", false);
		btnRes.setIcon(new ResourceLocation(BetterQuesting.MODID + ":textures/gui/editor_icons.png"), 128, 16, 16, 16, true);
		setButtonTooltip(btnRes, I18n.format("betterquesting.toolbox.tool.reset.name"), I18n.format("betterquesting.toolbox.tool.reset.desc"));
		list.add(btnRes);
		btnRes.enabled = curTool != ToolboxTabMain.instance.toolRes;
		
		btnList.addButtonRow(btnCom, btnRes);
		
		btnIco = new GuiButtonThemed(2, posX + 8, posY + 148, (sizeX - 8)/2, 20, "", false);
		btnIco.setIcon(new ResourceLocation(BetterQuesting.MODID + ":textures/gui/editor_icons.png"), 144, 0, 16, 16, true);
		setButtonTooltip(btnIco, I18n.format("betterquesting.toolbox.tool.icon.name"), I18n.format("betterquesting.toolbox.tool.icon.desc"));
		list.add(btnIco);
		btnIco.enabled = curTool != ToolboxTabMain.instance.toolIco;
		
		btnSca = new GuiButtonThemed(2, posX + 36, posY + 148, (sizeX - 8)/2, 20, "", false);
		btnSca.setIcon(new ResourceLocation(BetterQuesting.MODID + ":textures/gui/editor_icons.png"), 144, 16, 16, 16, true);
		setButtonTooltip(btnSca, I18n.format("betterquesting.toolbox.tool.scale.name"), I18n.format("betterquesting.toolbox.tool.scale.desc"));
		list.add(btnSca);
		btnSca.enabled = curTool != ToolboxTabMain.instance.toolSca;
		
		btnList.addButtonRow(btnIco, btnSca);
		
		btnRaw = new GuiButtonThemed(2, posX + 8, posY + 172, (sizeX - 8)/2, 20, "", false);
		btnRaw.setIcon(new ResourceLocation(BetterQuesting.MODID + ":textures/gui/editor_icons.png"), 0, 0, 16, 16, true);
		setButtonTooltip(btnRaw, I18n.format("betterquesting.toolbox.tool.raw.name"), I18n.format("betterquesting.toolbox.tool.raw.desc"));
		list.add(btnRaw);
		
		btnList.addButtonRow(btnRaw);
		
		if(gui.getActiveTool() == null)
		{
			gui.setActiveTool(ToolboxTabMain.instance.toolOpen);
			resetButtons();
			btnOpen.enabled = false;
		}
	}
	
	private void resetButtons()
	{
		for(GuiButtonThemed btn : list)
		{
			btn.enabled = true;
		}
	}
	
	@Override
	public void drawBackground(int mx, int my, float partialTick)
	{
		btnList.drawBackground(mx, my, partialTick);
	}
	
	@Override
	public void drawForeground(int mx, int my, float partialTick)
	{
		btnList.drawForeground(mx, my, partialTick);
	}
	
	/**
	 * Draws the active snap grid (called from supported tools)
	 */
	public static void drawGrid(IGuiQuestLine ui)
	{
		if(getSnapValue() <= 1)
		{
			return;
		}
		
		float zs = ui.getZoom()/100F;
		int minI = -ui.getScrollX();
		int minJ = -ui.getScrollY();
		int maxI = minI + (int)(ui.getWidth()/zs);
		int maxJ = minJ + (int)(ui.getHeight()/zs);
		
		for(int i = minI - minI%getSnapValue(); i < maxI; i += getSnapValue())
		{
			GL11.glPushMatrix();
			GL11.glEnable(GL11.GL_LINE_STIPPLE);
			if(i != 0)
			{
				GL11.glLineStipple(2, (short)0b1010101010101010);
			}
			RenderUtils.DrawLine(i, minJ, i, maxJ, i == 0? 2F : 1F, getTextColor());
			GL11.glLineStipple(1, (short)0xFFFF);
			GL11.glDisable(GL11.GL_LINE_STIPPLE);
			GL11.glPopMatrix();
		}
		
		for(int j = minJ - minJ%getSnapValue(); j < maxJ; j += getSnapValue())
		{
			GL11.glPushMatrix();
			GL11.glEnable(GL11.GL_LINE_STIPPLE);
			if(j != 0)
			{
				GL11.glLineStipple(2, (short)0b1010101010101010);
			}
			RenderUtils.DrawLine(minI, j, maxI, j, j == 0? 2F : 1F, getTextColor());
			GL11.glLineStipple(1, (short)0xFFFF);
			GL11.glDisable(GL11.GL_LINE_STIPPLE);
			GL11.glPopMatrix();
		}
	}
	
	@Override
	public void onMouseClick(int mx, int my, int click)
	{
		btnList.onMouseClick(mx, my, click);
		
		if(btnOpen.mousePressed(btnOpen.mc, mx, my))
		{
			resetButtons();
			btnOpen.func_146113_a(btnOpen.mc.getSoundHandler());
			btnOpen.enabled = false;
			gui.setActiveTool(ToolboxTabMain.instance.toolOpen);
		} else if(btnSnap.mousePressed(btnSnap.mc, mx, my))
		{
			btnSnap.func_146113_a(btnSnap.mc.getSoundHandler());
			toggleSnap();
			btnSnap.displayString = EnumChatFormatting.BLACK.toString() + dragSnap;
		} else if(btnGrab.mousePressed(btnGrab.mc, mx, my))
		{
			resetButtons();
			btnGrab.func_146113_a(btnGrab.mc.getSoundHandler());
			btnGrab.enabled = false;
			gui.setActiveTool(ToolboxTabMain.instance.toolGrab);
		} else if(btnNew.mousePressed(btnNew.mc, mx, my))
		{
			resetButtons();
			btnNew.func_146113_a(btnNew.mc.getSoundHandler());
			btnNew.enabled = false;
			gui.setActiveTool(ToolboxTabMain.instance.toolNew);
		} else if(btnCopy.mousePressed(btnCopy.mc, mx, my))
		{
			resetButtons();
			btnCopy.func_146113_a(btnCopy.mc.getSoundHandler());
			btnCopy.enabled = false;
			gui.setActiveTool(ToolboxTabMain.instance.toolCopy);
		} else if(btnLink.mousePressed(btnLink.mc, mx, my))
		{
			resetButtons();
			btnLink.func_146113_a(btnLink.mc.getSoundHandler());
			btnLink.enabled = false;
			gui.setActiveTool(ToolboxTabMain.instance.toolLink);
		} else if(btnDel.mousePressed(btnDel.mc, mx, my))
		{
			resetButtons();
			btnDel.func_146113_a(btnDel.mc.getSoundHandler());
			btnDel.enabled = false;
			gui.setActiveTool(ToolboxTabMain.instance.toolDel);
		} else if(btnRem.mousePressed(btnRem.mc, mx, my))
		{
			resetButtons();
			btnRem.func_146113_a(btnRem.mc.getSoundHandler());
			btnRem.enabled = false;
			gui.setActiveTool(ToolboxTabMain.instance.toolRem);
		} else if(btnCom.mousePressed(btnCom.mc, mx, my))
		{
			resetButtons();
			btnCom.func_146113_a(btnCom.mc.getSoundHandler());
			btnCom.enabled = false;
			gui.setActiveTool(ToolboxTabMain.instance.toolCom);
		} else if(btnRes.mousePressed(btnRes.mc, mx, my))
		{
			resetButtons();
			btnRes.func_146113_a(btnRes.mc.getSoundHandler());
			btnRes.enabled = false;
			gui.setActiveTool(ToolboxTabMain.instance.toolRes);
		} else if(btnIco.mousePressed(btnIco.mc, mx, my))
		{
			resetButtons();
			btnIco.func_146113_a(btnIco.mc.getSoundHandler());
			btnIco.enabled = false;
			gui.setActiveTool(ToolboxTabMain.instance.toolIco);
		} else if(btnSca.mousePressed(btnSca.mc, mx, my))
		{
			resetButtons();
			btnSca.func_146113_a(btnSca.mc.getSoundHandler());
			btnSca.enabled = false;
			gui.setActiveTool(ToolboxTabMain.instance.toolSca);
		} else if(btnRaw.mousePressed(btnRaw.mc, mx, my))
		{
			Minecraft mc = Minecraft.getMinecraft();
			btnRaw.func_146113_a(btnRaw.mc.getSoundHandler());
			mc.displayGuiScreen(new GuiQuestLineEditProxy(mc.currentScreen, this.gui.getQuestLine().getQuestLine()));
		}
	}
	
	@Override
	public void onMouseScroll(int mx, int my, int scroll)
	{
		btnList.onMouseScroll(mx, my, scroll);
	}
	
	@Override
	public void onKeyTyped(char c, int keyCode)
	{
	}
	
	@SuppressWarnings("unchecked")
	private void setButtonTooltip(GuiButtonThemed btn, String title, String desc)
	{
		ArrayList<String> list = new ArrayList<String>();
		list.add(title);
		list.addAll(btn.mc.fontRenderer.listFormattedStringToWidth(EnumChatFormatting.GRAY + desc, 128));
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

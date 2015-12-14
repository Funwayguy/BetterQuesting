package bq_standard.client.gui;

import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import betterquesting.utils.BigItemStack;
import betterquesting.utils.RenderUtils;

public class GuiLootChest extends GuiScreen
{
	static ResourceLocation guiChest = new ResourceLocation("bq_standard","textures/gui/gui_loot_chest.png");
	ArrayList<BigItemStack> rewards = new ArrayList<BigItemStack>();
	String title;
	
	public GuiLootChest(ArrayList<BigItemStack> rewards2, String title)
	{
		this.rewards = rewards2;
		this.title = title;
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		this.drawDefaultBackground();
		
		mc.renderEngine.bindTexture(guiChest);
		
		int cw = 128;
		int ch = 68;
		
		this.drawTexturedModalRect(width/2 - cw/2, height/2, 0, 0, cw, ch);
		
		String txt = EnumChatFormatting.BOLD + "" + EnumChatFormatting.UNDERLINE + I18n.format(title);
		mc.fontRenderer.drawString(txt, width/2 - mc.fontRenderer.getStringWidth(txt)/2, height/2 + ch + 8, Color.WHITE.getRGB(), false);
		
		// Auto balance row size
		int rowL = MathHelper.ceiling_float_int(rewards.size()/8F);
		rowL = MathHelper.ceiling_float_int(rewards.size()/rowL);
		
		BigItemStack ttStack = null;
		
		GL11.glPushMatrix();
		
		for(int i = 0; i < rewards.size(); i++)
		{
			mc.renderEngine.bindTexture(guiChest);
			
			int n1 = i%rowL;
			int n2 = i/rowL;
			int n3 = Math.min(rewards.size() - n2*rowL, rowL);
			
			int rx = (width/2) - (36 * n3)/2 + (36 * n1);
			int ry = height/2 - 36 - (n2 * 36);
			
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			
			this.drawTexturedModalRect(rx, ry, 128, 0, 32, 32);
			
			BigItemStack stack = rewards.get(i);
			RenderUtils.RenderItemStack(mc, stack.getBaseStack(), rx + 8, ry + 8, stack == null || stack.stackSize <= 1? "" : "" + stack.stackSize);
			
			if(mx >= rx + 8 && mx < rx + 24 && my >= ry + 8 && my < ry + 24)
			{
				ttStack = stack;
			}
		}
		
		GL11.glPopMatrix();
		
		if(ttStack != null)
		{
			this.drawHoveringText(ttStack.getBaseStack().getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips), mx, my, fontRendererObj);
		}
		
		// TODO: Finish rewards renderer then finish reward registry/editor/importer
	}
	
	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}
}

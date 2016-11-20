package betterquesting.client.themes;

import java.awt.Color;
import java.util.UUID;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import betterquesting.api.client.gui.GuiElement;
import betterquesting.api.client.themes.IThemeRenderer;
import betterquesting.api.enums.EnumQuestState;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.utils.RenderUtils;

public class ThemeRenderStandard extends GuiElement implements IThemeRenderer
{
	private int[] lineColors = new int[]{new Color(0.75F, 0F, 0F).getRGB(), Color.YELLOW.getRGB(), Color.GREEN.getRGB(), Color.GREEN.getRGB()};
	private int[] iconColors = new int[]{Color.GRAY.getRGB(), new Color(0.75F, 0F, 0F).getRGB(), new Color(0F, 1F, 1F).getRGB(), Color.GREEN.getRGB()};
	
	public void setLineColors(int locked, int incomplete, int complete)
	{
		lineColors[0] = locked;
		lineColors[1] = incomplete;
		lineColors[2] = complete;
		lineColors[3] = complete;
	}
	
	public void setIconColors(int locked, int incomplete, int pending, int complete)
	{
		iconColors[0] = locked;
		iconColors[1] = incomplete;
		iconColors[2] = pending;
		iconColors[3] = complete;
	}
	
	@Override
	public void drawLine(IQuest quest, UUID playerID, float x1, float y1, float x2, float y2, int mx, int my, float partialTick)
	{
		boolean isMain = quest == null? false : quest.getProperties().getProperty(NativeProps.MAIN);
		EnumQuestState qState = quest == null || playerID == null? EnumQuestState.LOCKED : quest.getState(playerID);
		
		GL11.glPushMatrix();
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		
    	int cl = getQuestLineColor(qState);
		float lr = (float)(cl >> 16 & 255) / 255.0F;
        float lg = (float)(cl >> 8 & 255) / 255.0F;
        float lb = (float)(cl & 255) / 255.0F;
    	GL11.glColor4f(lr, lg, lb, 1F);
		
		GL11.glLineWidth(isMain? 8F : 4F);
		GL11.glEnable(GL11.GL_LINE_STIPPLE);
		GL11.glLineStipple(8, (short)0xFFFF);
		GL11.glBegin(GL11.GL_LINES);
		
		GL11.glVertex2f(x1, y1);
		GL11.glVertex2f(x2, y2);
		GL11.glEnd();
		
		GL11.glLineStipple(1, Short.MAX_VALUE);
		GL11.glDisable(GL11.GL_LINE_STIPPLE);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glColor4f(1F, 1F, 1F, 1F);
		
		GL11.glPopMatrix();
	}
	
	@Override
	public void drawIcon(IQuest quest, UUID playerID, float px, float py, float sx, float sy, int mx, int my, float partialTick)
	{
		boolean isMain = quest == null? false : quest.getProperties().getProperty(NativeProps.MAIN);
		EnumQuestState qState = quest == null || playerID == null? EnumQuestState.LOCKED : quest.getState(playerID);
		boolean hover = mx >= px && my >= py && mx < px + sx && my < py + sy;
		
		GL11.glPushMatrix();
		
		int ci = getQuestIconColor(qState, qState == EnumQuestState.LOCKED? 0 : (!hover? 1 : 2));
		float ir = (float)(ci >> 16 & 255) / 255.0F;
        float ig = (float)(ci >> 8 & 255) / 255.0F;
        float ib = (float)(ci & 255) / 255.0F;
    	GL11.glColor4f(ir, ig, ib, 1F);
    	
    	GL11.glTranslatef(px, py, 0F);
    	float sw = sx / 24;
    	float sh = sy / 24;
    	GL11.glScalef(sw, sh, 1F);
    	
		Minecraft.getMinecraft().renderEngine.bindTexture(currentTheme().getGuiTexture());
    	this.drawTexturedModalRect(0, 0, (isMain? 24 : 0), 104, 24, 24);
    	
    	if(quest == null)
    	{
    		RenderUtils.RenderItemStack(Minecraft.getMinecraft(), new ItemStack(Items.nether_star), 4, 4, "");
    	} else if(quest.getItemIcon() != null)
    	{
    		RenderUtils.RenderItemStack(Minecraft.getMinecraft(), quest.getItemIcon().getBaseStack(), 4, 4, "");
    	}
    	
    	GL11.glPopMatrix();
	}
	
	private int getQuestLineColor(EnumQuestState state)
	{
		Color c = new Color(lineColors[state.ordinal()]);
		
		if(state == EnumQuestState.UNLOCKED && (Minecraft.getSystemTime()/1000)%2 == 0)
		{
			return new Color(c.getRed()/255F*0.5F, c.getGreen()/255F*0.5F, c.getBlue()/255F*0.5F).getRGB();
		}
		
		return c.getRGB();
	}
	
	private int getQuestIconColor(EnumQuestState state, int hoverState)
	{
		Color c = new Color(iconColors[state.ordinal()]);
		
		if(hoverState == 1)
		{
			return new Color(c.getRed()/255F*0.75F, c.getGreen()/255F*0.75F, c.getBlue()/255F*0.75F).getRGB();
		}
		
		return c.getRGB();
	}
}

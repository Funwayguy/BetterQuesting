package betterquesting.client.gui.editors.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.gui.misc.IVolatileScreen;
import betterquesting.core.BetterQuesting;
import betterquesting.utils.JsonHelper;
import betterquesting.utils.RenderUtils;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiJsonEntitySelection extends GuiQuesting implements IVolatileScreen
{
	JsonObject json;
	Entity entity;
	int scrollPos = 0;
	
	public GuiJsonEntitySelection(GuiScreen parent, JsonObject json)
	{
		super(parent, "betterquesting.title.select_entity");
		this.json = json;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();
		
		entity = JsonHelper.JsonToEntity(json, this.mc.theWorld, false);
		
		if(entity == null)
		{
			entity = new EntityPig(Minecraft.getMinecraft().theWorld);
			this.json.entrySet().clear();
			JsonHelper.EntityToJson(entity, json);
		}
		
		scrollPos = 0;
		
		int bSize = sizeX/2 - 16;
		
		GuiButtonQuesting leftBtn = new GuiButtonQuesting(1, this.guiLeft + this.sizeX/2, this.guiTop + this.sizeY - 48, 20, 20, "<");
		this.buttonList.add(leftBtn);
		GuiButtonQuesting rightBtn = new GuiButtonQuesting(2, this.guiLeft + this.sizeX/2 + (bSize - 20), this.guiTop + this.sizeY - 48, 20, 20, ">");
		this.buttonList.add(rightBtn);
		
		int i = 0;
		
		ArrayList<String> sortedNames = new ArrayList<String>((Collection<String>)EntityList.stringToClassMapping.keySet());
		
		Collections.sort(sortedNames);
		
		for(String key : sortedNames)
		{
			this.buttonList.add(new GuiButtonQuesting(this.buttonList.size(), this.guiLeft + this.sizeX/2, this.guiTop + 32 + (i * 20), bSize, 20, key));
			i++;
		}
		
		UpdateScroll();
	}
	
	public void UpdateScroll()
	{
		int maxRows = (this.sizeY - 80)/20;
		
		for(int i = 3; i < this.buttonList.size(); i++)
		{
			Object obj = this.buttonList.get(i);
			
			if(obj == null || !(obj instanceof GuiButton))
			{
				continue;
			}
			
			GuiButton button = (GuiButton)obj;
			int n = (i - 3) - (scrollPos * maxRows);
			
			if(n < 0 || n >= maxRows)
			{
				button.xPosition = -9999;
				button.yPosition = -9999;
			} else
			{
				button.xPosition = this.guiLeft + this.sizeX/2;
				button.yPosition = this.guiTop + 32 + (n * 20);
			}
		}
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id == 1)
		{
			if(scrollPos > 0)
			{
				scrollPos--;
				UpdateScroll();
			}
		} else if(button.id == 2)
		{
			int maxRows = (this.sizeY - 80)/20;
			int maxPages = MathHelper.ceiling_float_int(EntityList.stringToClassMapping.size()/(float)maxRows);
			
			if(scrollPos + 1 < maxPages)
			{
				scrollPos++;
				UpdateScroll();
			}
		} else if(button.id >= 3)
		{
			Entity tmpE = EntityList.createEntityByName(button.displayString, this.mc.theWorld);
			
			if(tmpE != null)
			{
				try
				{
					tmpE.readFromNBT(new NBTTagCompound()); // Solves some instantiation issues
					tmpE.isDead = false; // Some entities instantiate dead or die when ticked
					JsonHelper.EntityToJson(tmpE, json);
					entity = tmpE;
				} catch(Exception e)
				{
					BetterQuesting.logger.log(Level.ERROR, "Failed to init selected entity", e);
				}
			}
		}
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		super.drawScreen(mx, my, partialTick);
		
		if(entity != null)
		{
			GL11.glPushMatrix();
			
			GL11.glColor4f(1F, 1F, 1F, 1F);
			
			float angle = ((float)Minecraft.getSystemTime()%30000F)/30000F * 360F;
			float scale = 64F;
			
			if(entity.height * scale > this.sizeY/2F)
			{
				scale = (this.sizeY/2F)/entity.height;
			}
			
			if(entity.width * scale > this.sizeX/4F)
			{
				scale = (this.sizeX/4F)/entity.width;
			}
			
			try
			{
				RenderUtils.RenderEntity(this.guiLeft + this.sizeX/4, this.guiTop + this.sizeY/2 + MathHelper.ceiling_float_int(entity.height/2F*scale), (int)scale, angle, 0F, entity);
			} catch(Exception e)
			{
			}
			
			GL11.glPopMatrix();
		}
	}
}

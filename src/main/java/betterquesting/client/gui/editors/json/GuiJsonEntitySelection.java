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
import betterquesting.api.client.gui.GuiScreenThemed;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.other.ICallback;
import betterquesting.api.utils.RenderUtils;
import betterquesting.core.BetterQuesting;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiJsonEntitySelection extends GuiScreenThemed
{
	private Entity entity;
	private ICallback<Entity> callback;
	
	private int scrollPos = 0;
	
	public GuiJsonEntitySelection(GuiScreen parent, ICallback<Entity> callback, Entity entity)
	{
		super(parent, "betterquesting.title.select_entity");
		this.entity = entity;
		this.callback = callback;
		
		if(this.entity == null)
		{
			this.entity = new EntityPig(Minecraft.getMinecraft().theWorld);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();
		
		scrollPos = 0;
		
		int bSize = sizeX/2 - 16;
		
		GuiButtonThemed leftBtn = new GuiButtonThemed(1, this.guiLeft + this.sizeX/2, this.guiTop + this.sizeY - 48, 20, 20, "<", true);
		this.buttonList.add(leftBtn);
		GuiButtonThemed rightBtn = new GuiButtonThemed(2, this.guiLeft + this.sizeX/2 + (bSize - 20), this.guiTop + this.sizeY - 48, 20, 20, ">", true);
		this.buttonList.add(rightBtn);
		
		int i = 0;
		
		ArrayList<String> sortedNames = new ArrayList<String>((Collection<String>)EntityList.stringToClassMapping.keySet());
		
		Collections.sort(sortedNames);
		
		for(String key : sortedNames)
		{
			this.buttonList.add(new GuiButtonThemed(this.buttonList.size(), this.guiLeft + this.sizeX/2, this.guiTop + 32 + (i * 20), bSize, 20, key, true));
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
		
		if(button.id == 0 && callback != null)
		{
			callback.setValue(entity);
		} else if(button.id == 1)
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

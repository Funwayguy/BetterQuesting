package betterquesting.client;

import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import betterquesting.client.buttons.GuiButtonQuesting;
import betterquesting.utils.NBTConverter;
import com.google.gson.JsonObject;

public class GuiJsonEntitySelection extends GuiQuesting
{
	JsonObject json;
	Entity entity;
	int scrollPos = 0;
	
	public GuiJsonEntitySelection(GuiScreen parent, JsonObject json)
	{
		super(parent, "Editor - Entity");
		this.json = json;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();
		
		if(json.has("id") && EntityList.stringToClassMapping.get(json.get("id").getAsString()) != null)
		{
			entity = EntityList.createEntityFromNBT(NBTConverter.JSONtoNBT_Object(json.getAsJsonObject(), new NBTTagCompound()), this.mc.theWorld);
		}
		
		if(entity == null)
		{
			entity = new EntityPig(Minecraft.getMinecraft().theWorld);
			this.json.entrySet().clear();
			NBTTagCompound eTags = new NBTTagCompound();
			entity.writeToNBTOptional(eTags);
			NBTConverter.NBTtoJSON_Compound(eTags, json);
		}
		
		scrollPos = 0;
		
		int bSize = Math.min((this.sizeX - 16) - this.sizeX/2, 200);
		
		GuiButtonQuesting leftBtn = new GuiButtonQuesting(1, this.guiLeft + this.sizeX/2, this.guiTop + this.sizeY - 48, 20, 20, "<");
		this.buttonList.add(leftBtn);
		GuiButtonQuesting rightBtn = new GuiButtonQuesting(2, this.guiLeft + this.sizeX/2 + (bSize - 20), this.guiTop + this.sizeY - 48, 20, 20, ">");
		this.buttonList.add(rightBtn);
		
		int i = 0;
		for(String key : (Set<String>)EntityList.func_151515_b())
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
			int maxPages = MathHelper.ceiling_float_int(EntityList.func_151515_b().size()/(float)maxRows);
			
			if(scrollPos + 1 < maxPages)
			{
				scrollPos++;
				UpdateScroll();
			}
		} else if(button.id >= 3)
		{
			if(EntityList.stringToClassMapping.containsKey(button.displayString))
			{
				Entity tmpE = EntityList.createEntityByName(button.displayString, this.mc.theWorld);
				
				if(tmpE != null)
				{
					try
					{
						tmpE.readFromNBT(new NBTTagCompound()); // Solves some instantiation issues
						tmpE.isDead = false; // Some entities instantiate dead or die when ticked
						NBTTagCompound eTags = new NBTTagCompound();
						tmpE.writeToNBTOptional(eTags);
						eTags.setString("id", EntityList.getEntityString(tmpE)); // Some entities don't write this to file in certain cases
						this.json.entrySet().clear();
						NBTConverter.NBTtoJSON_Compound(eTags, json);
						entity = tmpE;
						System.out.println("Wrote " + eTags.getString("id"));
					} catch(Exception e)
					{
						e.printStackTrace();
					}
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
				RenderEntity(this.guiLeft + this.sizeX/4, this.guiTop + this.sizeY/2 + MathHelper.ceiling_float_int(entity.height/2F*scale), (int)scale, angle, 0F, entity);
			} catch(Exception e)
			{
			}
			
			GL11.glPopMatrix();
		}
	}

    public static void RenderEntity(int posX, int posY, int scale, float rotation, float pitch, Entity entity)
    {
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)posX, (float)posY, 100.0F);
        GL11.glScalef((float)(-scale), (float)scale, (float)scale);
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        GL11.glRotatef(15F, 1F, 0F, 0F);
        GL11.glRotatef(rotation, 0F, 1F, 0F);
        float f3 = entity.rotationYaw;
        float f4 = entity.rotationPitch;
        RenderHelper.enableStandardItemLighting();
        GL11.glTranslatef(0.0F, entity.yOffset, 0.0F);
        RenderManager.instance.playerViewY = 180.0F;
        RenderManager.instance.renderEntityWithPosYaw(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        entity.rotationYaw = f3;
        entity.rotationPitch = f4;
        GL11.glPopMatrix();
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }
}

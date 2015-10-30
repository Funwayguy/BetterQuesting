package betterquesting.quests.tasks;

import java.awt.Color;
import java.util.HashMap;
import java.util.UUID;
import org.lwjgl.opengl.GL11;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.utils.JsonHelper;
import betterquesting.utils.RenderUtils;

public class TaskHunt extends TaskBase
{
	HashMap<UUID, Integer> userProgress = new HashMap<UUID, Integer>();
	public String idName = "Zombie";
	public int required = 1;
	Entity target;
	
	@Override
	public String getUnlocalisedName()
	{
		return "betterquesting.task.hunt";
	}
	
	@Override
	public void Update(EntityPlayer player)
	{
	}
	
	@Override
	public void Detect(EntityPlayer player)
	{
	}
	
	public void AddKill(EntityPlayer player, int count)
	{
		if(userProgress.containsKey(player.getUniqueID()))
		{
			userProgress.put(player.getUniqueID(), userProgress.get(player.getUniqueID()) + count);
		} else
		{
			userProgress.put(player.getUniqueID(), count);
		}
	}
	
	@Override
	public void writeToJson(JsonObject json)
	{
		super.writeToJson(json);
		
		json.addProperty("target", idName);
		json.addProperty("required", required);
	}
	
	@Override
	public void readFromJson(JsonObject json)
	{
		super.writeToJson(json);
		
		idName = JsonHelper.GetString(json, "target", "Zombie");
		required = JsonHelper.GetNumber(json, "required", 1).intValue();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void drawQuestInfo(GuiQuesting screen, int mouseX, int mouseY, int posX, int posY, int sizeX, int sizeY)
	{
		String txt = "Kill " + idName + " x" + required;
		screen.mc.fontRenderer.drawString(txt, posX + sizeX/2 - screen.mc.fontRenderer.getStringWidth(txt)/2, posY, Color.BLACK.getRGB());
		if(target != null)
		{
			GL11.glPushMatrix();
			
			GL11.glColor4f(1F, 1F, 1F, 1F);
			
			float angle = ((float)Minecraft.getSystemTime()%30000F)/30000F * 360F;
			float scale = 64F;
			
			//if(target.height * scale > (sizeY - 48))
			{
				scale = (sizeY - 48)/target.height;
			}
			
			if(target.width * scale > sizeX)
			{
				scale = sizeX/target.width;
			}
			
			try
			{
				RenderUtils.RenderEntity(posX + sizeX/2, posY + sizeY/2 + MathHelper.ceiling_float_int(target.height/2F*scale) + 8, (int)scale, angle, 0F, target);
			} catch(Exception e)
			{
			}
			
			GL11.glPopMatrix();
		} else
		{
			if(EntityList.stringToClassMapping.containsKey(idName))
			{
				target = EntityList.createEntityByName(idName, screen.mc.theWorld);
			}
		}
	}
}

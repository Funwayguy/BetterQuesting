package betterquesting.quests.tasks;

import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.client.gui.tasks.GuiTaskLocation;
import betterquesting.utils.JsonHelper;
import com.google.gson.JsonObject;

public class TaskLocation extends TaskBase
{
	public String name = "New Location";
	public int x = 0;
	public int y = 0;
	public int z = 0;
	public int dim = 0;
	public int range = -1;
	public boolean visible = false;
	public boolean hideInfo = false;
	
	@Override
	public String getUnlocalisedName()
	{
		return "betterquesting.task.location";
	}
	
	@Override
	public void Update(EntityPlayer player)
	{
		if(player.ticksExisted%60 != 0) // Only auto-detect every 3 seconds
		{
			return;
		}
		
		Detect(player);
	}
	
	@Override
	public void Detect(EntityPlayer player)
	{
		if(isComplete(player))
		{
			return; // Keeps ray casting calls to a minimum
		}
		
		if(player.dimension == dim && (range <= 0 || player.getDistance(x, y, z) <= range))
		{
			if(visible && range > 0) // Do not do ray casting with infinite range!
			{
				Vec3 pPos = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
				Vec3 tPos = Vec3.createVectorHelper(x, y, z);
				boolean liquids = false;
				MovingObjectPosition mop = player.worldObj.func_147447_a(pPos, tPos, liquids, !liquids, false);
				
				if(mop == null || mop.typeOfHit != MovingObjectType.BLOCK)
				{
					this.completeUsers.add(player.getUniqueID());
				} else
				{
					return;
				}
			} else
			{
				this.completeUsers.add(player.getUniqueID());
			}
		}
	}
	
	@Override
	public void writeToJson(JsonObject json)
	{
		json.addProperty("name", name);
		json.addProperty("posX", x);
		json.addProperty("posY", y);
		json.addProperty("posZ", z);
		json.addProperty("dimension", dim);
		json.addProperty("range", range);
		json.addProperty("visible", visible);
		json.addProperty("hideInfo", hideInfo);
		
		super.writeToJson(json);
	}
	
	@Override
	public void readFromJson(JsonObject json)
	{
		name = JsonHelper.GetString(json, "name", "New Location");
		x = JsonHelper.GetNumber(json, "posX", 0).intValue();
		y = JsonHelper.GetNumber(json, "posY", 0).intValue();
		z = JsonHelper.GetNumber(json, "posZ", 0).intValue();
		dim = JsonHelper.GetNumber(json, "dimension", 0).intValue();
		range = JsonHelper.GetNumber(json, "range", -1).intValue();
		visible = JsonHelper.GetBoolean(json, "visible", false);
		hideInfo = JsonHelper.GetBoolean(json, "hideInfo", false);
	}

	@Override
	public void ResetProgress(UUID uuid)
	{
		completeUsers.remove(uuid);
	}

	@Override
	public void ResetAllProgress()
	{
		completeUsers.clear();
	}

	@Override
	public GuiEmbedded getGui(GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		return new GuiTaskLocation(this, screen, posX, posY, sizeX, sizeY);
	}
}

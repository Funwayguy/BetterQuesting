package betterquesting.handlers;

import betterquesting.api.utils.JsonHelper;
import betterquesting.api.utils.NBTConverter;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.rewards.loot.LootRegistry;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.io.File;

@EventBusSubscriber
public class LootSaveLoad
{
    public static LootSaveLoad INSTANCE = new LootSaveLoad();
    
    public File worldDir;
    
    public void LoadLoot(MinecraftServer server)
    {
        if(BetterQuesting.proxy.isClient())
		{
			worldDir = server.getFile("saves/" + server.getFolderName());
		} else
		{
			worldDir = server.getFile(server.getFolderName());
		}
    	
    	File f1 = new File(worldDir, "QuestLoot.json");
		JsonObject j1 = new JsonObject();
		
		if(f1.exists())
		{
			j1 = JsonHelper.ReadFromFile(f1);
		} else
		{
			f1 = server.getFile("config/betterquesting/DefaultLoot.json");
			
			if(f1.exists())
			{
				j1 = JsonHelper.ReadFromFile(f1);
			}
		}
		
		LootRegistry.INSTANCE.readFromNBT(NBTConverter.JSONtoNBT_Object(j1, new NBTTagCompound(), true), false);
    }
    
    public void SaveLoot()
    {
        JsonHelper.WriteToFile(new File(worldDir, "QuestLoot.json"), NBTConverter.NBTtoJSON_Compound(LootRegistry.INSTANCE.writeToNBT(new NBTTagCompound(), null), new JsonObject(), true));
    }
    
    public void UnloadLoot()
    {
        LootRegistry.INSTANCE.reset();
        LootRegistry.INSTANCE.updateUI = false;
        worldDir = null;
    }
}

package betterquesting.api.quests.tasks;

import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import betterquesting.api.client.gui.IGuiEmbedded;
import betterquesting.api.quests.IQuestContainer;
import com.google.gson.JsonObject;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface ITaskBase
{
	public String getUnlocalisedName();
	public ResourceLocation getFactoryID();
	
	public void update(EntityPlayer player, IQuestContainer quest);
	public void detect(EntityPlayer player, IQuestContainer quest);
	
	public boolean isComplete(UUID uuid);
	public void setComplete(UUID uuid);
	
	public void reset(UUID uuid);
	public void resetAll();
	
	public JsonObject writeToJson_Config(JsonObject json);
	public void readFromJson_Config(JsonObject json);
	
	public JsonObject writeToJson_Progress(JsonObject json);
	public void readFromJson_Progress(JsonObject json);
	
	@SideOnly(Side.CLIENT)
	public IGuiEmbedded getTaskGui(int sizeX, int sizeY, IQuestContainer quest);
	
	@SideOnly(Side.CLIENT)
	public IGuiEmbedded getTaskEditor(int sizeX, int sizeY, IQuestContainer quest);
}

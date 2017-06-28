package adv_director.api.questing.tasks;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import adv_director.api.client.gui.misc.IGuiEmbedded;
import adv_director.api.jdoc.IJsonDoc;
import adv_director.api.misc.IJsonSaveLoad;
import adv_director.api.questing.IQuest;
import com.google.gson.JsonObject;

public interface ITask extends IJsonSaveLoad<JsonObject>
{
	public String getUnlocalisedName();
	public ResourceLocation getFactoryID();
	
	public void detect(EntityPlayer player, IQuest quest);
	
	public boolean isComplete(UUID uuid);
	public void setComplete(UUID uuid);
	
	public void resetUser(UUID uuid);
	public void resetAll();
	
	public IJsonDoc getDocumentation();
	
	@SideOnly(Side.CLIENT)
	public IGuiEmbedded getTaskGui(int x, int y, int w, int h, IQuest quest);
	
	@Nullable
	@SideOnly(Side.CLIENT)
	public GuiScreen getTaskEditor(GuiScreen parent, IQuest quest);
	
	/**
	 * Use ITickableTask instead
	 */
	@Deprecated
	public void update(EntityPlayer player, IQuest quest);
}

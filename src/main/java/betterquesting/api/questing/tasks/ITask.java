package betterquesting.api.questing.tasks;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import betterquesting.api.client.gui.misc.IGuiEmbedded;
import betterquesting.api.jdoc.IJsonDoc;
import betterquesting.api.misc.IJsonSaveLoad;
import betterquesting.api.questing.IQuest;
import com.google.gson.JsonObject;

public interface ITask extends IJsonSaveLoad<JsonObject>
{
	public String getUnlocalisedName();
	public ResourceLocation getFactoryID();
	
	public void update(EntityPlayer player, IQuest quest);
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
}

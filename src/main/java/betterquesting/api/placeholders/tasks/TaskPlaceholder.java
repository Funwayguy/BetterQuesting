package betterquesting.api.placeholders.tasks;

import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.jdoc.IJsonDoc;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public class TaskPlaceholder implements ITask
{
	private NBTTagCompound nbtData = new NBTTagCompound();
	
	public void setTaskData(NBTTagCompound nbt, EnumSaveType saveType)
	{
		if(saveType == EnumSaveType.CONFIG)
		{
			nbtData.setTag("orig_data", nbt);
		} else if(saveType == EnumSaveType.PROGRESS)
		{
			nbtData.setTag("orig_prog", nbt);
		}
	}
	
	public NBTTagCompound getTaskData(EnumSaveType saveType)
	{
		if(saveType == EnumSaveType.CONFIG)
		{
			return nbtData.getCompoundTag("orig_data");
		} else if(saveType == EnumSaveType.PROGRESS)
		{
			return nbtData.getCompoundTag("orig_prog");
		}
		
		return new NBTTagCompound();
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt, EnumSaveType saveType)
	{
		if(saveType == EnumSaveType.CONFIG)
		{
			nbt.setTag("orig_data", nbtData.getCompoundTag("orig_data"));
		} else if(saveType == EnumSaveType.PROGRESS)
		{
			nbt.setTag("orig_prog", nbtData.getCompoundTag("orig_prog"));
		}
		
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt, EnumSaveType saveType)
	{
		if(saveType == EnumSaveType.CONFIG)
		{
			nbtData.setTag("orig_data", nbt.getCompoundTag("orig_data"));
		} else if(saveType == EnumSaveType.PROGRESS)
		{
			nbtData.setTag("orig_prog", nbt.getCompoundTag("orig_prog"));
		}
	}
	
	@Override
	public String getUnlocalisedName()
	{
		return "betterquesting.placeholder";
	}
	
	@Override
	public ResourceLocation getFactoryID()
	{
		return FactoryTaskPlaceholder.INSTANCE.getRegistryName();
	}
	
	@Override
	public void detect(EntityPlayer player, IQuest quest)
	{
	}
	
	@Override
	public boolean isComplete(UUID uuid)
	{
		return false;
	}
	
	@Override
	public void setComplete(UUID uuid)
	{
	}
	
	@Override
	public void resetUser(UUID uuid)
	{
	}
	
	@Override
	public void resetAll()
	{
	}
	
	@Override
	public IJsonDoc getDocumentation()
	{
		return null;
	}
	
	@Override
	public IGuiPanel getTaskGui(int x, int y, int w, int h, IQuest quest)
	{
		return null;
	}
	
	@Override
	public GuiScreen getTaskEditor(GuiScreen parent, IQuest quest)
	{
		return null;
	}
}

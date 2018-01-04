package betterquesting.questing;

import net.minecraft.nbt.NBTTagCompound;
import betterquesting.api.enums.EnumSaveType;
import betterquesting.api.questing.IQuestLineEntry;

public class QuestLineEntry implements IQuestLineEntry
{
	private int size = 0;
	private int posX = 0;
	private int posY = 0;
	
	public QuestLineEntry(NBTTagCompound json)
	{
		this.readFromNBT(json, EnumSaveType.CONFIG);
	}
	
	public QuestLineEntry(int x, int y)
	{
		this(x, y, 24);
	}
	
	public QuestLineEntry(int x, int y, int size)
	{
		this.size = size;
		this.posX = x;
		this.posY = y;
	}
	
	@Override
	public int getSize()
	{
		return size;
	}
	
	@Override
	public int getPosX()
	{
		return posX;
	}
	
	@Override
	public int getPosY()
	{
		return posY;
	}
	
	@Override
	public void setPosition(int posX, int posY)
	{
		this.posX = posX;
		this.posY = posY;
	}
	
	@Override
	public void setSize(int size)
	{
		this.size = size;
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return json;
		}
		
		json.setInteger("size", size);
		json.setInteger("x", posX);
		json.setInteger("y", posY);
		return json;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound json, EnumSaveType saveType)
	{
		if(saveType != EnumSaveType.CONFIG)
		{
			return;
		}
		
		size = json.getInteger("size");
		posX = json.getInteger("x");
		posY = json.getInteger("y");
	}
	
}

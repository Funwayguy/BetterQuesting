package betterquesting.questing;

import betterquesting.api.questing.IQuestLineEntry;
import net.minecraft.nbt.CompoundNBT;

public class QuestLineEntry implements IQuestLineEntry
{
	private int sizeX = 0;
	private int sizeY = 0;
	private int posX = 0;
	private int posY = 0;
	
	public QuestLineEntry(CompoundNBT json)
	{
		this.readFromNBT(json);
	}
	
	public QuestLineEntry(int x, int y)
	{
		this(x, y, 24, 24);
	}
	
	@Deprecated
	public QuestLineEntry(int x, int y, int size)
	{
		this.sizeX = size;
		this.sizeY = size;
		this.posX = x;
		this.posY = y;
	}
	
	public QuestLineEntry(int x, int y, int sizeX, int sizeY)
	{
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.posX = x;
		this.posY = y;
	}
	
	@Override
    @Deprecated
	public int getSize()
	{
		return Math.max(getSizeX(), getSizeY());
	}
	
	@Override
    public int getSizeX()
    {
        return this.sizeX;
    }
    
    @Override
    public int getSizeY()
    {
        return this.sizeY;
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
    @Deprecated
	public void setSize(int size)
	{
		this.sizeX = size;
		this.sizeY = size;
	}
	
	@Override
	public void setSize(int sizeX, int sizeY)
    {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }
	
	@Override
	public CompoundNBT writeToNBT(CompoundNBT json)
	{
		json.putInt("sizeX", sizeX);
		json.putInt("sizeY", sizeY);
		json.putInt("x", posX);
		json.putInt("y", posY);
		return json;
	}
	
	@Override
	public void readFromNBT(CompoundNBT json)
	{
	    if(json.contains("size", 99))
        {
            sizeX = json.getInt("size");
            sizeY = sizeX;
        } else
        {
            sizeX = json.getInt("sizeX");
            sizeY = json.getInt("sizeY");
        }
		posX = json.getInt("x");
		posY = json.getInt("y");
	}
	
}

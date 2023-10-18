package betterquesting.questing;

import betterquesting.api.questing.IQuestLineEntry;
import net.minecraft.nbt.NBTTagCompound;

public class QuestLineEntry implements IQuestLineEntry {
  private int sizeX = 0;
  private int sizeY = 0;
  private int posX = 0;
  private int posY = 0;

  public QuestLineEntry(NBTTagCompound json) {
    readFromNBT(json);
  }

  public QuestLineEntry(int x, int y) {
    this(x, y, 24, 24);
  }

  @Deprecated
  public QuestLineEntry(int x, int y, int size) {
    sizeX = size;
    sizeY = size;
    posX = x;
    posY = y;
  }

  public QuestLineEntry(int x, int y, int sizeX, int sizeY) {
    this.sizeX = sizeX;
    this.sizeY = sizeY;
    posX = x;
    posY = y;
  }

  @Override
  @Deprecated
  public int getSize() {
    return Math.max(getSizeX(), getSizeY());
  }

  @Override
  public int getSizeX() {
    return sizeX;
  }

  @Override
  public int getSizeY() {
    return sizeY;
  }

  @Override
  public int getPosX() {
    return posX;
  }

  @Override
  public int getPosY() {
    return posY;
  }

  @Override
  public void setPosition(int posX, int posY) {
    this.posX = posX;
    this.posY = posY;
  }

  @Override
  @Deprecated
  public void setSize(int size) {
    sizeX = size;
    sizeY = size;
  }

  @Override
  public void setSize(int sizeX, int sizeY) {
    this.sizeX = sizeX;
    this.sizeY = sizeY;
  }

  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound json) {
    json.setInteger("sizeX", sizeX);
    json.setInteger("sizeY", sizeY);
    json.setInteger("x", posX);
    json.setInteger("y", posY);
    return json;
  }

  @Override
  public void readFromNBT(NBTTagCompound json) {
    if (json.hasKey("size", 99)) {
      sizeY = sizeX = json.getInteger("size");
    } else {
      sizeX = json.getInteger("sizeX");
      sizeY = json.getInteger("sizeY");
    }
    posX = json.getInteger("x");
    posY = json.getInteger("y");
  }
}

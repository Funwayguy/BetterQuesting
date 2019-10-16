package betterquesting.api2.client.gui.panels;

import betterquesting.api2.storage.INBTSaveLoad;
import net.minecraft.nbt.NBTTagCompound;

public interface IGuiPanelNBT extends IGuiPanel, INBTSaveLoad<NBTTagCompound>
{
}

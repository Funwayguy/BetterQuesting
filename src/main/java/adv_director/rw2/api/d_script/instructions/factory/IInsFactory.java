package adv_director.rw2.api.d_script.instructions.factory;

import adv_director.rw2.api.d_script.IDInstruction;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IInsFactory
{
	public ResourceLocation registryID();
	/**
	 * The unlocalised name of the instruction
	 */
	@SideOnly(Side.CLIENT)
	public String unlocalisedName();
	
	public Loot
	public IDInstruction loadInstruction(NBTTagCompound nbt);
	
	public void I
}

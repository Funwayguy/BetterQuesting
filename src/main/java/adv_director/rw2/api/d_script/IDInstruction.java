package adv_director.rw2.api.d_script;

import javax.script.ScriptContext;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import adv_director.rw2.api.client.gui.panels.IGuiPanel;
import adv_director.rw2.api.utils.INbtSaveLoad;

public interface IDInstruction extends INbtSaveLoad<NBTTagCompound>
{
	/**
	 * Identifies which factory is responsible for saving/loading this instruction
	 */
	public ResourceLocation getFactoryID();
	
	/**
	 * Runs the instruction using the current context variables and functions
	 */
	public abstract void run(ScriptContext context);
	
	/**
	 * Returns a GUI panel
	 */
	@SideOnly(Side.CLIENT)
	public abstract IGuiPanel getGuiPanel();
	
	/**
	 * Returns the inspector used to configure this instruction
	 */
	@SideOnly(Side.CLIENT)
	public abstract IGuiPanel getInspectorPanel();
}

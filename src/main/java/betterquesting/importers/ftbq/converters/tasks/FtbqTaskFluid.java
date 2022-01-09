package betterquesting.importers.ftbq.converters.tasks;

import betterquesting.api.placeholders.PlaceholderConverter;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.BigItemStack;
import betterquesting.importers.ftbq.FTBQQuestImporter;
import betterquesting.questing.tasks.TaskFluid;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class FtbqTaskFluid
{
    public ITask[] convertTask(NBTTagCompound nbt)
    {
        String fName = nbt.getString("fluid");
        Fluid fluid = FluidRegistry.getFluid(fName);
        long amount = nbt.getLong("amount"); // Sigh... longs again. No matter, we'll just split them if they're too big
        NBTTagCompound tag = !nbt.hasKey("tag", 10) ? null : nbt.getCompoundTag("tag"); // FTBQ doesn't support tags yet but we'll try supporting it in advance
        FluidStack stack = PlaceholderConverter.convertFluid(fluid, fName, 1, tag);
        
        TaskFluid task = new TaskFluid();
        
        long rem = amount;
        while(rem > 0)
        {
            int split = (int)(rem % Integer.MAX_VALUE);
            stack.amount = split;
            task.requiredFluids.add(stack.copy());
            rem -= split;
        }
    
        FTBQQuestImporter.provideQuestIcon(new BigItemStack(Items.BUCKET));
        
        return new ITask[]{task};
    }
}

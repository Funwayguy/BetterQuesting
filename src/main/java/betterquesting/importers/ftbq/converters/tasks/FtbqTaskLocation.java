package betterquesting.importers.ftbq.converters.tasks;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.BigItemStack;
import betterquesting.importers.ftbq.FTBQQuestImporter;
import betterquesting.questing.tasks.TaskLocation;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.DimensionType;

public class FtbqTaskLocation
{
    public ITask[] convertTask(NBTTagCompound tag)
    {
        TaskLocation task = new TaskLocation();
        
        int[] data = tag.getIntArray("location");
        if(data.length <= 7) return null; // Just incase soemthing was redacted for some reason
        
        task.dim = data[0];
        task.x = data[1];
        task.y = data[2];
        task.z = data[3];
        task.range = Math.min(data[4], Math.min(data[5], data[6])); // FTBQ uses a dimension task for infinite range
        if(task.range <= 0) task.range = 1; // Sanity checking
        
        task.name = tag.hasKey("title", 8) ? tag.getString("title") : DimensionType.getById(task.dim).getName();
        FTBQQuestImporter.provideQuestIcon(new BigItemStack(Items.COMPASS));
        
        return new ITask[]{task};
    }
}

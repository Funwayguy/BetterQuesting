package betterquesting.importers.ftbq.converters.tasks;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.BigItemStack;
import betterquesting.importers.ftbq.FTBQQuestImporter;
import betterquesting.questing.tasks.TaskLocation;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.DimensionType;

public class FtbqTaskDimension
{
    public ITask[] converTask(NBTTagCompound tag)
    {
        TaskLocation task = new TaskLocation();
        
        task.range = -1;
        task.dim = tag.getInteger("dim");
        task.x = 0;
        task.y = 0;
        task.z = 0;
        task.visible = true;
        task.name = tag.hasKey("title", 8) ? tag.getString("title") : DimensionType.getById(task.dim).getName();
    
        FTBQQuestImporter.provideQuestIcon(new BigItemStack(Items.COMPASS));
        
        return new ITask[]{task};
    }
}

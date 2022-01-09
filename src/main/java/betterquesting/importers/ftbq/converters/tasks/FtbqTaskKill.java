package betterquesting.importers.ftbq.converters.tasks;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.BigItemStack;
import betterquesting.importers.ftbq.FTBQQuestImporter;
import betterquesting.questing.tasks.TaskHunt;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;

public class FtbqTaskKill
{
    public ITask[] convertTask(NBTTagCompound tag)
    {
        TaskHunt task = new TaskHunt();
        
        task.idName = tag.getString("entity");
        task.targetTags = new NBTTagCompound();
        task.required = tag.getInteger("value");
        task.ignoreNBT = true;
        task.subtypes = true;
    
        FTBQQuestImporter.provideQuestIcon(new BigItemStack(Items.DIAMOND_SWORD));
        
        return new ITask[]{task};
    }
}

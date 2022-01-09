package betterquesting.importers.ftbq.converters.tasks;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.placeholders.tasks.TaskPlaceholder;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.BigItemStack;
import betterquesting.importers.ftbq.FTBQQuestImporter;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class FtbqTaskEnergy
{
    // With a little magic we're going to support this without having to reference RF Expansion nor even have it installed!
    public ITask[] converTask(NBTTagCompound tag)
    {
        NBTTagCompound rfTaskTag = new NBTTagCompound();
        rfTaskTag.setString("taskID", "bq_rf:rf_charge");
        rfTaskTag.setLong("rf", tag.getLong("value"));
        
        ITask task = QuestingAPI.getAPI(ApiReference.TASK_REG).createNew(new ResourceLocation("bq_rf:rf_charge"));
        
        if(task == null)
        {
            task = new TaskPlaceholder();
            ((TaskPlaceholder)task).setTaskConfigData(rfTaskTag);
        } else
        {
            task.readFromNBT(rfTaskTag);
        }
    
        FTBQQuestImporter.provideQuestIcon(new BigItemStack(Blocks.REDSTONE_BLOCK));
        
        return new ITask[]{task};
    }
}

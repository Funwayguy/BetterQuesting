package betterquesting.importers.hqm.converters.tasks;

import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.JsonHelper;
import betterquesting.NbtBlockType;
import betterquesting.importers.hqm.HQMUtilities;
import betterquesting.questing.tasks.TaskBlockBreak;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemBlock;

public class HQMTaskBlockBreak
{
    public ITask[] convertTask(JsonObject json)
    {
        TaskBlockBreak taskBreak = new TaskBlockBreak();
        
        for(JsonElement je2 : JsonHelper.GetArray(json, "blocks"))
        {
            if(!(je2 instanceof JsonObject)) continue;
            JsonObject jBlock = je2.getAsJsonObject();
            BigItemStack stack = HQMUtilities.HQMStackT1(JsonHelper.GetObject(jBlock, "item"));
            if(!(stack.getBaseStack().getItem() instanceof ItemBlock)) continue; // Lazy conversion. Too much effort to handle all the edge cases
            ItemBlock iBlock = (ItemBlock)stack.getBaseStack().getItem();
            NbtBlockType blockType = new NbtBlockType();
            blockType.b = iBlock.getBlock();
            blockType.m = stack.getBaseStack().getItemDamage();
            blockType.n = JsonHelper.GetNumber(jBlock, "required", 1).intValue();
            taskBreak.blockTypes.add(blockType);
        }
        
        return new ITask[]{taskBreak};
    }
}

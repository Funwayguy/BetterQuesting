package betterquesting.advancement;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.tasks.TaskTrigger;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.Tuple;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

public class BqsAdvListener<T extends ICriterionInstance> extends ICriterionTrigger.Listener<T>
{
    private final ICriterionTrigger<T> trigType;
    private final Tuple<Integer,Integer> mappedIDs;
    
    @SuppressWarnings("ConstantConditions")
    public BqsAdvListener(@Nonnull ICriterionTrigger<T> trigType, @Nonnull T critereon, int questID, int taskID)
    {
        super(critereon, null, "BQ_PROXY");
        this.trigType = trigType;
        this.mappedIDs = new Tuple<>(questID, taskID);
        
        AdvListenerManager.INSTANCE.registerListener(this);
    }
    
    public void registerSelf(PlayerAdvancements playerAdv)
    {
        trigType.addListener(playerAdv, this);
    }
    
    public void unregisterSelf(PlayerAdvancements playerAdv)
    {
        trigType.removeListener(playerAdv, this);
    }
    
    @Override
    public void grantCriterion(PlayerAdvancements playerAdv)
    {
        try
        {
            IQuest q = QuestingAPI.getAPI(ApiReference.QUEST_DB).getValue(mappedIDs.getFirst());
            if(q == null) return;
            ITask t = q.getTasks().getValue(mappedIDs.getSecond());
            if(!(t instanceof TaskTrigger)) return;
            
            ((TaskTrigger)t).onCriteriaComplete(((EntityPlayerMP)f_playerAdv.get(playerAdv)), this, mappedIDs.getFirst());
        } catch(Exception e)
        {
            BetterQuesting.logger.error(e);
        }
    }
    
    //
    public boolean verify()
    {
        IQuest q = QuestingAPI.getAPI(ApiReference.QUEST_DB).getValue(mappedIDs.getFirst());
        if(q == null) return false;
        ITask t = q.getTasks().getValue(mappedIDs.getSecond());
        if(t instanceof TaskTrigger)
        {
            TaskTrigger tCon = (TaskTrigger)t;
            return tCon.getListener() == this;
        }
        
        return false;
    }
    
    @Override
    public boolean equals(Object p_equals_1_)
    {
        if (this == p_equals_1_)
        {
            return true;
        }
        else if (p_equals_1_ != null && this.getClass() == p_equals_1_.getClass())
        {
            ICriterionTrigger.Listener<?> listener = (ICriterionTrigger.Listener)p_equals_1_;
            return this.getCriterionInstance().equals(listener.getCriterionInstance());
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public int hashCode()
    {
        int i = this.getCriterionInstance().hashCode();
        i = 31 * i;// + this.advancement.hashCode();
        i = 31 * i + "BQ_PROXY".hashCode();
        return i;
    }
    
    private static final Field f_playerAdv;
    
    static
    {
        f_playerAdv = ReflectionHelper.findField(PlayerAdvancements.class, "field_192762_j", "player");
        f_playerAdv.setAccessible(true);
    }
}

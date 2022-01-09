package betterquesting.handlers;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.ParticipantInfo;
import betterquesting.questing.tasks.ITaskInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.UUID;

public class PlayerContainerListener implements IContainerListener
{
    private static final HashMap<UUID, PlayerContainerListener> LISTEN_MAP = new HashMap<>();
    
    public static void refreshListener(@Nonnull EntityPlayer player)
    {
        UUID uuid = QuestingAPI.getQuestingUUID(player);
        PlayerContainerListener listener = LISTEN_MAP.get(uuid);
        if(listener != null)
        {
            listener.player = player;
        } else
        {
            listener = new PlayerContainerListener(player);
            LISTEN_MAP.put(uuid, listener);
        }
        
        try
        {
            player.inventoryContainer.addListener(listener);
        } catch(Exception ignored){}
    }
    
    private EntityPlayer player;
    
    private PlayerContainerListener(@Nonnull EntityPlayer player)
    {
        this.player = player;
    }
    
    @Override
    public void sendAllContents(@Nonnull Container container, @Nonnull NonNullList<ItemStack> nonNullList)
    {
        updateTasks();
    }
    
    @Override
    public void sendSlotContents(@Nonnull Container container, int i, @Nonnull ItemStack itemStack)
    {
        updateTasks();
    }
    
    @Override
    public void sendWindowProperty(@Nonnull Container container, int i, int i1){}
    
    @Override
    public void sendAllWindowProperties(@Nonnull Container container, @Nonnull IInventory iInventory){}
    
    private void updateTasks()
    {
        ParticipantInfo pInfo = new ParticipantInfo(player);
		
        for(DBEntry<IQuest> entry : QuestingAPI.getAPI(ApiReference.QUEST_DB).bulkLookup(pInfo.getSharedQuests()))
		{
		    for(DBEntry<ITask> task : entry.getValue().getTasks().getEntries())
            {
                if(task.getValue() instanceof ITaskInventory) ((ITaskInventory)task.getValue()).onInventoryChange(entry, pInfo);
            }
		}
    }
}

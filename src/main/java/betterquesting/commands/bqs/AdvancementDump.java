package betterquesting.commands.bqs;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumQuestVisibility;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.*;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.storage.IDatabaseNBT;
import betterquesting.questing.tasks.TaskAdvancement;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;

import java.util.*;
import java.util.Map.Entry;

public class AdvancementDump
{
    public static final AdvancementDump INSTANCE = new AdvancementDump();
    
    private HashMap<Advancement, DBEntry<IQuest>> idMap = new HashMap<>();
    
    public void dumpAdvancements(MinecraftServer server)
    {
        idMap.clear();
        
        IQuestDatabase questDB = QuestingAPI.getAPI(ApiReference.QUEST_DB);
        IQuestLineDatabase lineDB = QuestingAPI.getAPI(ApiReference.LINE_DB);
    
        List<Advancement> roots = new ArrayList<>();
        
        // Generate quest representations
        for(Advancement adv : server.getAdvancementManager().getAdvancements())
        {
            if(adv.getDisplay() == null) continue; // Hidden advancement
            
            int id = questDB.nextID();
            IQuest quest = questDB.createNew(id);
            IDatabaseNBT<ITask, NBTTagList, NBTTagList> taskDB = quest.getTasks();
            DisplayInfo disp = adv.getDisplay();
            
            if(adv.getParent() == null)
            {
                roots.add(adv);
                if(adv.getCriteria().isEmpty()) quest.setProperty(NativeProps.SILENT, true); // Not going to broadcast an empty root node
            }
            
            quest.setProperty(NativeProps.NAME, disp.getTitle().getFormattedText());
            quest.setProperty(NativeProps.DESC, disp.getDescription().getFormattedText());
            quest.setProperty(NativeProps.ICON, new BigItemStack(disp.getIcon()));
            quest.setProperty(NativeProps.SILENT, true); // There's already toast notifications. Double stacking would just be annoying
            quest.setProperty(NativeProps.VISIBILITY, (disp.isHidden() || adv.getParent() == null) ? EnumQuestVisibility.COMPLETED : EnumQuestVisibility.UNLOCKED);
            quest.setProperty(NativeProps.MAIN, disp.getFrame() == FrameType.GOAL);
            
            TaskAdvancement task = new TaskAdvancement();
            task.advID = adv.getId();
            taskDB.add(taskDB.nextID(), task);
            
            idMap.put(adv, new DBEntry<>(id, quest));
        }
        
        // Setup parenting
        for(Entry<Advancement, DBEntry<IQuest>> entry : idMap.entrySet())
        {
            if(entry.getKey().getParent() != null)
            {
                Advancement parent = entry.getKey().getParent();
                DBEntry<IQuest> pq = idMap.get(parent);
                if(pq != null)
                {
                    addReq(entry.getValue().getValue(), pq.getID());
                }
            }
        }
        
        // Generate quest lines
        for(Advancement adv : roots)
        {
            IQuestLine ql = lineDB.createNew(lineDB.nextID());
            
            ql.setProperty(NativeProps.NAME, adv.getDisplay().getTitle().getFormattedText());
            ql.setProperty(NativeProps.DESC, adv.getDisplay().getDescription().getFormattedText());
            IQuestLineEntry qleRoot = ql.createNew(idMap.get(adv).getID());
            qleRoot.setPosition((int)(adv.getDisplay().getX() * 32F), (int)(adv.getDisplay().getY() * 32F));
            qleRoot.setSize(24, 24);
            
            Queue<Iterator<Advancement>> iterStack = new ArrayDeque<>();
            iterStack.add(adv.getChildren().iterator());
            
            while(!iterStack.isEmpty())
            {
                Iterator<Advancement> iter = iterStack.poll();
                
                while(iter.hasNext())
                {
                    Advancement child = iter.next();
                    IQuestLineEntry qle = ql.createNew(idMap.get(child).getID());
                    qle.setPosition((int)(child.getDisplay().getX() * 32F), (int)(child.getDisplay().getY() * 32F));
                    qle.setSize(24, 24);
                    iterStack.add(child.getChildren().iterator());
                }
            }
        }
    }
    
    private boolean containsReq(IQuest quest, int id)
    {
        for(int reqID : quest.getRequirements()) if(id == reqID) return true;
        return false;
    }
    
    private boolean addReq(IQuest quest, int id)
    {
        if(containsReq(quest, id)) return false;
        int[] orig = quest.getRequirements();
        int[] added = Arrays.copyOf(orig, orig.length + 1);
        added[orig.length] = id;
        quest.setRequirements(added);
        return true;
    }
}

package betterquesting.importers;

import betterquesting.api.client.importers.IImporter;
import betterquesting.api.enums.EnumQuestVisibility;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.*;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.FileExtensionFilter;
import betterquesting.api.utils.JsonHelper;
import betterquesting.api2.storage.DBEntry;
import betterquesting.core.BetterQuesting;
import betterquesting.questing.rewards.RewardCommand;
import betterquesting.questing.rewards.RewardItem;
import betterquesting.questing.rewards.RewardRecipe;
import betterquesting.questing.rewards.RewardXP;
import betterquesting.questing.tasks.TaskTrigger;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.advancements.FrameType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileFilter;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;

public class AdvImporter implements IImporter
{
	public static final AdvImporter INSTANCE = new AdvImporter();
	private static final FileFilter FILTER = new FileExtensionFilter(".json");
	
    @Override
    public String getUnlocalisedName()
    {
		return "bq_standard.importer.adv_json.name";
    }
    
    @Override
    public String getUnlocalisedDescription()
    {
		return "bq_standard.importer.adv_json.desc";
    }
    
    @Override
    public FileFilter getFileFilter()
    {
        return FILTER;
    }
    
    @Override
    public void loadFiles(IQuestDatabase questDB, IQuestLineDatabase lineDB, File[] files)
    {
        ID_MAP.clear();
        PENDING_CHILDREN.clear();
        
        if(files.length == 1 && !files[0].isDirectory() && files[0].getName().equalsIgnoreCase("bq_adv_manifest.json"))
        {
            System.out.println("Importing manifest file...");
            JsonObject manifest = JsonHelper.ReadFromFile(files[0]);
            
            for(Entry<String,JsonElement> entry : manifest.entrySet())
            {
                if(!(entry.getValue() instanceof JsonPrimitive) || !((JsonPrimitive)entry.getValue()).isString()) continue;
                File folder = new File(files[0].getParentFile(), entry.getValue().getAsString());
                if(!folder.exists() || !folder.isDirectory()) continue;
                
                try
                {
                    Iterator<Path> iterator = Files.walk(folder.toPath(), FileVisitOption.FOLLOW_LINKS).iterator();
                    
                    while(iterator.hasNext())
                    {
                        Path path = iterator.next();
                        if(!"json".equals(FilenameUtils.getExtension(path.toString()))) continue;
                        
                        String relPath = folder.toPath().relativize(path).toString();
                        ResourceLocation advID = new ResourceLocation(entry.getKey(), FilenameUtils.removeExtension(relPath).replaceAll("\\\\", "/"));
                        loadAdvancemenet(advID, JsonHelper.ReadFromFile(path.toFile()), questDB);
                    }
                    
                } catch(Exception ignored){}
            }
        } else
        {
            for(File selected : files)
            {
                if(selected == null || !selected.exists()) continue;
                JsonObject json = JsonHelper.ReadFromFile(selected);
                if(json.size() > 0) loadAdvancemenet(guessID(selected), json, questDB);
            }
        }
		
		// Partial imports may
		for(DBEntry<IQuest> entry : ID_MAP.values())
        {
            if(entry.getValue().getRequirements().length <= 0 && entry.getValue().getProperty(NativeProps.VISIBILITY) != EnumQuestVisibility.HIDDEN) generateLayout(entry, lineDB);
        }
    }
    
    // ===== QUEST PARSER =====
    
    /**
     * Because Minecraft bases the Advancement IDs off the file structure
     * we will have to take a guess based on this file's location.
     * We're hoping the user has at least tried to preserve that sturcutre
     */
    private ResourceLocation guessID(File file)
    {
        File lastDir = file;
        File parent = file.getParentFile();
        while(parent != null)
        {
            if(parent.getName().equalsIgnoreCase("advancements") || (parent.getName().equalsIgnoreCase("json") && parent.getParentFile() != null && parent.getParentFile().getName().equalsIgnoreCase("triumph")))
            {
                String relPath = lastDir.toPath().relativize(file.toPath()).toString();
                return new ResourceLocation(lastDir.getName(), FilenameUtils.removeExtension(relPath).replaceAll("\\\\", "/"));
            }
            
            lastDir = parent;
            parent = parent.getParentFile();
        }
        return new ResourceLocation("minecraft", FilenameUtils.removeExtension(file.getName()));
    }
    
    private final TreeMap<ResourceLocation, DBEntry<IQuest>> ID_MAP = new TreeMap<>((o1, o2) -> o2.toString().compareToIgnoreCase(o1.toString())); // Reverse sort... because Minecraft does (I think?).
    private final HashMap<ResourceLocation, List<IQuest>> PENDING_CHILDREN = new HashMap<>();
    
    private void registerQuest(ResourceLocation id, DBEntry<IQuest> entry)
    {
        ID_MAP.put(id, entry);
        
        if(PENDING_CHILDREN.containsKey(id))
        {
            for(IQuest q : PENDING_CHILDREN.get(id))
            {
                addReq(q, entry.getID());
            }
        }
    }
    
    private void loadAdvancemenet(ResourceLocation idName, JsonObject json, IQuestDatabase questDB)
    {
        int QID = questDB.nextID();
        IQuest quest = questDB.createNew(QID);
        registerQuest(idName, new DBEntry<>(QID, quest));
        
        if(json.has("display"))
        {
            readDisplayInfo(JsonHelper.GetObject(json, "display"), quest);
        } else
        {
            quest.setProperty(NativeProps.NAME, idName.toString());
            quest.setProperty(NativeProps.VISIBILITY, EnumQuestVisibility.HIDDEN);
        }
        
        quest.setProperty(NativeProps.AUTO_CLAIM, true);
        quest.setProperty(NativeProps.LOCKED_PROGRESS, true);
        
        if(json.has("parent"))
        {
            ResourceLocation parentID = new ResourceLocation(JsonHelper.GetString(json, "parent", ""));
            
            if(ID_MAP.containsKey(parentID))
            {
                addReq(quest, ID_MAP.get(parentID).getID());
            } else
            {
                List<IQuest> pending = PENDING_CHILDREN.computeIfAbsent(parentID, k -> new ArrayList<>());
                pending.add(quest);
            }
        }
        
        if(json.get("criteria") instanceof JsonObject)
        {
            JsonObject critObj = json.getAsJsonObject("criteria");
            
            for(Entry<String,JsonElement> entry : critObj.entrySet())
            {
                JsonObject taskObj = JsonHelper.GetObject(critObj, entry.getKey());
                TaskTrigger task = new TaskTrigger();
                task.setTriggerID(JsonHelper.GetString(taskObj, "trigger", "minecraft:impossible"));
                task.setCriteriaJson(JsonHelper.GetObject(taskObj, "conditions").toString());
                task.desc = entry.getKey();
                quest.getTasks().add(quest.getTasks().nextID(), task);
            }
        }
        
        if(json.has("rewards"))
        {
            JsonObject rewObj = JsonHelper.GetObject(json, "rewards");
            
            if(rewObj.has("loot"))
            {
                RewardItem reward = new RewardItem();
                reward.items.clear();
                JsonArray ary = JsonHelper.GetArray(rewObj, "loot");
                for(int i = 0; i < ary.size(); i++)
                {
                    BigItemStack item = new BigItemStack(BetterQuesting.lootChest, 1, 103);
                    NBTTagCompound tag = new NBTTagCompound();
                    tag.setString("loottable", ary.get(i).getAsString());
                    item.SetTagCompound(tag);
                    reward.items.add(item);
                }
                quest.getRewards().add(quest.getRewards().nextID(), reward);
            }
            
            if(rewObj.has("recipes"))
            {
                RewardRecipe reward = new RewardRecipe();
                StringBuilder sb = new StringBuilder();
                JsonArray ary = JsonHelper.GetArray(rewObj, "recipes");
                for(int i = 0; i < ary.size(); i++)
                {
                    if(i != 0) sb.append("\n");
                    sb.append(ary.get(i).getAsString());
                }
                reward.recipeNames = sb.toString();
                quest.getRewards().add(quest.getRewards().nextID(), reward);
            }
            
            if(rewObj.has("experience"))
            {
                RewardXP reward = new RewardXP();
                reward.levels = false;
                reward.amount = JsonHelper.GetNumber(rewObj, "experience", 0).intValue();
                quest.getRewards().add(quest.getRewards().nextID(), reward);
            }
            
            if(rewObj.has("function"))
            {
                RewardCommand reward = new RewardCommand();
                reward.viaPlayer = true;
                reward.command = "function " + JsonHelper.GetString(rewObj, "function", "minecraft:function");
                quest.getRewards().add(quest.getRewards().nextID(), reward);
            }
        }
    }
    
    private void readDisplayInfo(JsonObject json, IQuest quest)
    {
        if(json.has("title"))
        {
            ITextComponent title = ITextComponent.Serializer.fromJsonLenient(json.get("title").toString());
            if(title != null) quest.setProperty(NativeProps.NAME, title.getFormattedText());
        }
        
        if(json.has("description"))
        {
            ITextComponent title = ITextComponent.Serializer.fromJsonLenient(json.get("description").toString());
            if(title != null) quest.setProperty(NativeProps.DESC, title.getFormattedText());
        }
        
        quest.setProperty(NativeProps.ICON, new BigItemStack(readIcon(JsonHelper.GetObject(json, "icon"))));
        quest.setProperty(NativeProps.SILENT, !JsonHelper.GetBoolean(json, "show_toast", true));
        if(JsonHelper.GetBoolean(json, "hidden", false)) quest.setProperty(NativeProps.VISIBILITY, EnumQuestVisibility.COMPLETED);
        
        FrameType frametype = json.has("frame") ? FrameType.byName(JsonUtils.getString(json, "frame")) : FrameType.TASK;
        quest.setProperty(NativeProps.MAIN, frametype == FrameType.GOAL);
    }
    
    private ItemStack readIcon(JsonObject json)
    {
        if (!json.has("item"))
        {
            return ItemStack.EMPTY;
        }
        else
        {
            try
            {
                Item item = JsonUtils.getItem(json, "item");
                int i = JsonUtils.getInt(json, "data", 0);
                ItemStack ret = new ItemStack(item, 1, i);
                ret.setTagCompound(net.minecraftforge.common.util.JsonUtils.readNBT(json, "nbt"));
                return ret;
            } catch(Exception ignored)
            {
                return ItemStack.EMPTY;
            }
        }
    }
    
    private void addReq(IQuest quest, int id)
    {
        if(containsReq(quest, id)) return;
        int[] orig = quest.getRequirements();
        int[] added = Arrays.copyOf(orig, orig.length + 1);
        added[orig.length] = id;
        quest.setRequirements(added);
    }
    
    private boolean containsReq(IQuest quest, int id)
    {
        for(int reqID : quest.getRequirements()) if(id == reqID) return true;
        return false;
    }
    
    // ===== LAYOUT GENERATOR =====
    
    private final List<List<AdvTreeNode>> NODES_BY_DEPTH = new ArrayList<>();
    
    private void generateLayout(DBEntry<IQuest> root, IQuestLineDatabase lineDB)
    {
        // Setup: Construct node tree (depth first ordering)
        // Pass 1: Tight pack icons (sort order here if necessary) (can skip if setup does this automatically)
        NODES_BY_DEPTH.clear();
        Stack<AdvTreeNode> stack = new Stack<>();
        stack.push(new AdvTreeNode(root.getID()));
        
        while(stack.size() > 0)
        {
            AdvTreeNode node = stack.pop();
            addNodeToDepthMap(node);
            findChildren(node);
            for(int i = node.getChildren().size() - 1; i >= 0; i--) stack.push(node.getChildren().get(i));
        }
        
        // Pass 2: Center children on parent (right-to-left)(recursive right if changed)(Y offset must be at)
        for(int d = NODES_BY_DEPTH.size() - 1; d >= 1; d--)
        {
            boolean changed = false;
    
            for(AdvTreeNode node : NODES_BY_DEPTH.get(d))
            {
                Stack<AdvTreeNode> childStack = new Stack<>();
                childStack.push(node);
                
                while(childStack.size() > 0)
                {
                    AdvTreeNode child = childStack.pop();
                    int prev = child.offY;
                    int cPosYBase = Math.max(0, child.getParent().getChildren().size() - 1) * 16;
                    int cPosY = child.getParent().getChildren().size() <= 1 ? 0 : (child.getParent().getChildren().indexOf(child) * 32 - cPosYBase);
                    int pOff = child.getParent().getPosY() - (child.getPosY() - child.offY);
                    child.offY = pOff + cPosY;
                    if(child.aboveNode != null) child.offY = Math.max(0, child.offY);
                    changed = changed || prev != child.offY;
            
                    if(changed && child.getChildren().size() > 0)
                    {
                        for(int i = child.getChildren().size() - 1; i >= 0; i--)
                            childStack.push(child.getChildren().get(i));
                    }
                }
            }
        }

        // Pass 3: Center parent on children (left-to-right)(recursive left if changed)
        for(int d = 0; d < NODES_BY_DEPTH.size(); d++)
        {
            boolean changed = false;
    
            for(AdvTreeNode node : NODES_BY_DEPTH.get(d))
            {
                if(node.getChildren().size() <= 0) continue;
        
                int prev = node.offY;
                int cPosY = (node.getChildren().get(node.getChildren().size() - 1).getPosY() + 24) - node.getChildren().get(0).getPosY();
                cPosY = (cPosY / 2) - 12;
                int pOff = node.getChildren().get(0).getPosY() - (node.getPosY() - node.offY);
                int nPos = pOff + cPosY;
                node.offY = nPos;
        
                if(node.aboveNode != null)
                {
                    node.offY = Math.max(0, node.offY);
                    
                    if(nPos < 0)
                    {
                        int dShift = node.depth + 1;
                        int iShift = NODES_BY_DEPTH.get(dShift).indexOf(node.getChildren().get(0));
                        
                        for(int ds = dShift; ds < NODES_BY_DEPTH.size(); ds++)
                        {
                            for(AdvTreeNode nShift : NODES_BY_DEPTH.get(ds))
                            {
                                AdvTreeNode tmp = nShift;
                                while(tmp.depth > dShift) tmp = tmp.getParent();
                                if(NODES_BY_DEPTH.get(dShift).indexOf(tmp) >= iShift)
                                {
                                    nShift.offY -= nPos;
                                    break;
                                }
                            }
                        }
                    }
                }
                
                changed = changed || prev != node.offY;
            }
            
            if(changed && d > 0) d -= 2;
        }
        
        // NOTES: Node Y position uses relative distance from the node above it
        
        // Finalise: Add all nodes to a new quest line named after the root node
        IQuestLine line = lineDB.createNew(lineDB.nextID());
        line.setProperty(NativeProps.NAME, root.getValue().getProperty(NativeProps.NAME));
        line.setProperty(NativeProps.DESC, root.getValue().getProperty(NativeProps.DESC));
        line.setProperty(NativeProps.VISIBILITY, EnumQuestVisibility.UNLOCKED);
        line.setProperty(NativeProps.ICON, root.getValue().getProperty(NativeProps.ICON));
        
        for(List<AdvTreeNode> depthList : NODES_BY_DEPTH)
        {
            for(AdvTreeNode node : depthList)
            {
                IQuestLineEntry qle = line.createNew(node.getQuestID());
                qle.setPosition(node.getPosX(), node.getPosY());
                qle.setSize(24, 24);
            }
        }
    }
    
    private void addNodeToDepthMap(AdvTreeNode node)
    {
        while(node.depth >= NODES_BY_DEPTH.size()) NODES_BY_DEPTH.add(new ArrayList<>());
        List<AdvTreeNode> list = NODES_BY_DEPTH.get(node.depth);
        if(list.size() > 0) node.setAboveNode(list.get(list.size() - 1));
        list.add(node);
    }
    
    private void findChildren(AdvTreeNode parent)
    {
        for(DBEntry<IQuest> entry : ID_MAP.values())
        {
            if(entry.getValue().getProperty(NativeProps.VISIBILITY) == EnumQuestVisibility.HIDDEN) continue;
            
            for(int req : entry.getValue().getRequirements())
            {
                if(req == parent.getQuestID())
                {
                    AdvTreeNode child = new AdvTreeNode(entry.getID());
                    parent.addChild(child);
                }
            }
        }
    }
    
    private static class AdvTreeNode
    {
        // Used for positioning, not heirachy
        private AdvTreeNode aboveNode;
        private int depth = 0;
        private int offY = 0;
        
        // Heirachy info
        private AdvTreeNode parent;
        private final List<AdvTreeNode> children = new ArrayList<>();
        private final int questID;
        
        private AdvTreeNode(int questID)
        {
            this.questID = questID;
        }
        
        private int getDepth()
        {
            return this.depth;
        }
        
        private int getPosX()
        {
            return getDepth() * 32;
        }
        
        private int getPosY()
        {
            return offY + (aboveNode == null ? 0 : (aboveNode.getPosY() + 32));
        }
        
        private AdvTreeNode getParent()
        {
            return this.parent;
        }
        
        private List<AdvTreeNode> getChildren()
        {
            return Collections.unmodifiableList(children);
        }
        
        private int getQuestID()
        {
            return this.questID;
        }
        
        private void addChild(@Nonnull AdvTreeNode node)
        {
            if(children.contains(node)) return;
            
            children.add(node);
            node.parent = this;
            node.depth = this.depth + 1;
        }
        
        private void setAboveNode(@Nullable AdvTreeNode node)
        {
            this.aboveNode = node;
        }
    }
}

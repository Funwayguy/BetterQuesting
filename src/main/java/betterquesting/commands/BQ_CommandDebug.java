package betterquesting.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class BQ_CommandDebug extends CommandBase
{
	@Override
	public String getCommandName()
	{
		return "bq_debug";
	}
	
	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return "TO BE USED IN DEV ONLY";
	}
	
	@Override
	public void processCommand(ICommandSender sender, String[] args)
	{
    }
	
    // FTBQ Stress test
	/*@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
	    final int minX = -19;
	    final int minY = -11;
	    final int maxX = 24;
	    final int maxY = 10;
	    
	    for(int cNum = 0; cNum < 10; cNum++)
        {
            QuestChapter chapter = createChapter();
            
            for(int j = -5; j <= 4; j++)
            {
                for(int i = -5; i <= 4; i++)
                {
                    createQuest(chapter, i, j);
                }
            }
        }
        
        //ForgePlayer fPlayer = Universe.get().getPlayer(((EntityPlayer)sender.getCommandSenderEntity()).getGameProfile());
        //new MessageSyncQuests(ServerQuestFile.INSTANCE, fPlayer.team.getUID(), Collections.emptyList(), true, new IntOpenHashSet());
        ServerQuestFile.INSTANCE.refreshIDMap();
        ServerQuestFile.INSTANCE.clearCachedData();
        ServerQuestFile.INSTANCE.save();
    }
    
    private QuestChapter createChapter()
    {
        QuestObjectBase object = ServerQuestFile.INSTANCE.create(QuestObjectType.CHAPTER, 0, new NBTTagCompound());
        NBTTagCompound oNBT = new NBTTagCompound();
        new QuestChapter(ServerQuestFile.INSTANCE).writeData(oNBT);
        object.readData(oNBT);
        object.id = ServerQuestFile.INSTANCE.readID(0);
        object.onCreated();
        //ServerQuestFile.INSTANCE.refreshIDMap();
        //ServerQuestFile.INSTANCE.clearCachedData();
        //ServerQuestFile.INSTANCE.save();
        
        return (QuestChapter)object;
    }
    
    private Quest createQuest(QuestChapter ch, int x, int y)
    {
        Quest quest = new Quest(ch);
        quest.x = (byte)x;
        quest.y = (byte)y;
        quest.id = ServerQuestFile.INSTANCE.readID(0);
        quest.title = Integer.toHexString(quest.id);
        quest.onCreated();
        //(new MessageCreateObjectResponse(quest, null)).sendToAll();
        ItemTask task = (ItemTask)FTBQuestsTasks.ITEM.provider.create(quest);
        task.items.add(new ItemStack(Items.APPLE));
        task.id = ServerQuestFile.INSTANCE.readID(0);
        //task.readData(new NBTTagCompound()); // Write task data?
        task.onCreated();
        NBTTagCompound extra = new NBTTagCompound();
        extra.setString("type", FTBQuestsTasks.ITEM.getTypeForNBT());
        //(new MessageCreateObjectResponse(task, extra)).sendToAll();
        //ServerQuestFile.INSTANCE.refreshIDMap();
        //ServerQuestFile.INSTANCE.clearCachedData();
        //ServerQuestFile.INSTANCE.save();
        return quest;
    }*/
	
    // Conversion stuff for HQM
	/*private HashMap<String, UUID> nameToID = new HashMap<>();
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
	    if(!(sender instanceof EntityPlayer)) return;
	    
	    File hqmDir = server.getFile("hqm");
	    
	    if(!hqmDir.exists() || !hqmDir.isDirectory()) return;
     
	    File[] fileList = hqmDir.listFiles();
	    if(fileList == null || fileList.length == 0) return;
	    
	    nameToID.clear();
     
	    for(File qFile : fileList)
        {
            if(qFile.isDirectory()) continue;
            JsonObject json = JsonHelper.ReadFromFile(qFile);
    
            JsonArray qAry = JsonHelper.GetArray(json, "quests");
            for(JsonElement je : qAry)
            {
                if(!(je instanceof JsonObject)) continue;
                JsonObject jQuest = je.getAsJsonObject();
                String questName = JsonHelper.GetString(jQuest, "name", "unknown");
                
                UUID uuid = nameToID.get(questName);
                if(uuid == null)
                {
                    uuid = UUID.randomUUID();
                    while(nameToID.containsValue(uuid)) uuid = UUID.randomUUID(); // Guarantee unique
                    nameToID.put(questName, uuid);
                }
                
                jQuest.add("uuid", new JsonPrimitive(uuid.toString()));
                
                JsonArray pAry = new JsonArray();
                JsonArray oAry = new JsonArray();
                JsonArray preAry = JsonHelper.GetArray(jQuest, "prerequisites");
                for(JsonElement pj : preAry)
                {
                    if(!(pj instanceof JsonPrimitive)) continue;
                    String pName = pj.getAsString();
                    boolean opt = false;
                    
                    if(pName.startsWith("{") && pName.contains("["))
                    {
                        String[] nParts = pName.split("\\[");
                        if(nParts.length > 1) pName = nParts[1].replaceFirst("]", "");
                        opt = true;
                    }
                    
                    UUID pID = nameToID.get(pName);
                    if(pID == null)
                    {
                        pID = UUID.randomUUID();
                        while(nameToID.containsValue(pID)) pID = UUID.randomUUID(); // Guarantee unique
                        nameToID.put(pName, pID);
                    }
                    
                    if(opt)
                    {
                        oAry.add(pID.toString());
                    } else
                    {
                        pAry.add(pID.toString());
                    }
                }
                jQuest.add("prerequisites", pAry);
                jQuest.add("optionlinks", oAry);
                
                searchJson(jQuest);
            }
            
            JsonHelper.WriteToFile(qFile, json);
        }
	}
	
	private void searchJson(JsonElement json)
    {
        if(json == null || json instanceof JsonNull) return;
        
        if(json.isJsonObject())
        {
            JsonObject jObject = json.getAsJsonObject();
            for(Entry<String, JsonElement> entry : jObject.entrySet())
            {
                if(entry.getValue() == null) continue;
                if(entry.getKey().equalsIgnoreCase("nbt") && entry.getValue().isJsonPrimitive())
                {
                    jObject.add(entry.getKey(), new JsonPrimitive(cleanNBT(entry.getValue().getAsString())));
                    continue; // Might be a concurrent crash here
                }
                searchJson(entry.getValue());
            }
        } else if(json instanceof JsonArray)
        {
            JsonArray jArray = json.getAsJsonArray();
            for(JsonElement je : jArray)
            {
                searchJson(je);
            }
        }
    }
    
    private String cleanNBT(String nbt)
    {
        try
        {
            return JsonToNBT.getTagFromJson(nbt).toString();
        } catch(Exception e)
        {
            return "{}";
        }
    }*/
}

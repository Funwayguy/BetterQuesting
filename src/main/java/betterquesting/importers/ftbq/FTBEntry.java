package betterquesting.importers.ftbq;

public class FTBEntry
{
    public final int id;
    public final Object obj;
    public final FTBEntryType type;
    
    public FTBEntry(int id, Object obj, FTBEntryType type)
    {
        this.id = id;
        this.obj = obj;
        this.type = type;
    }
    
    public enum FTBEntryType
    {
        QUEST,
        LINE,
        VAR
    }
}

package betterquesting.importers.hqm.converters;

import java.util.HashMap;

public class HQMRep
{
    public final String rName;
    
    private final HashMap<Integer, Integer> markerList = new HashMap<>();
    
    public HQMRep(String name)
    {
        this.rName = name;
    }
    
    public void addMarker(int id, int value)
    {
        markerList.put(id, value);
    }
    
    public int getMarker(int id)
    {
        Integer i = markerList.get(id);
        return i == null ? 0 : i;
    }
}

package betterquesting.client.gui2.editors.tasks;

import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.lists.CanvasSearch;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;

import java.util.*;

public abstract class CanvasAdvancementSearch extends CanvasSearch<Advancement, Advancement>
{
    private final AdvancementList advList;
    
    public CanvasAdvancementSearch(IGuiRect rect, AdvancementList list)
    {
        super(rect);
        this.advList = list;
    }
    
    @Override
    protected Iterator<Advancement> getIterator()
    {
        List<Advancement> temp = new ArrayList<>();
        for(Advancement adv : advList.getAdvancements()) temp.add(adv);
        temp.sort(advComparator);
        
        return temp.iterator();
    }
    
    @Override
    protected void queryMatches(Advancement value, String query, ArrayDeque<Advancement> results)
    {
        if(value.getId().toString().toLowerCase().contains(query.toLowerCase()))
        {
            results.add(value);
        } else if(value.getDisplay() != null && value.getDisplay().getTitle().getFormattedText().toLowerCase().contains(query.toLowerCase()))
        {
            results.add(value);
        } // Could search the description but that'd make the results less relevant to the query
    }
    
    private static final Comparator<Advancement> advComparator = (o1, o2) -> {
        if(o1.getDisplay() != null && o2.getDisplay() == null)
        {
            return -1;
        } else if(o1.getDisplay() == null && o2.getDisplay() != null)
        {
            return 1;
        }
        
        String s1 = o1.getDisplay() == null ? o1.getId().toString().toLowerCase() : o1.getDisplay().getTitle().getFormattedText().toLowerCase();
        String s2 = o2.getDisplay() == null ? o2.getId().toString().toLowerCase() : o2.getDisplay().getTitle().getFormattedText().toLowerCase();
        
        return s1.compareTo(s2);
    };
}

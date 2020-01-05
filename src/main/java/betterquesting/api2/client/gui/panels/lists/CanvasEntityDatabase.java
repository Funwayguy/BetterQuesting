package betterquesting.api2.client.gui.panels.lists;

import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import net.minecraft.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CanvasEntityDatabase extends CanvasSearch<EntityType, EntityType>
{
    private final int btnId;
    
    public CanvasEntityDatabase(IGuiRect rect, int buttonId)
    {
        super(rect);
        this.btnId = buttonId;
    }
    
    @Override
    protected Iterator<EntityType> getIterator()
    {
        List<EntityType> list = new ArrayList<>(ForgeRegistries.ENTITIES.getValues());
        list.sort((o1, o2) -> o1.getName().getFormattedText().compareToIgnoreCase(o2.getName().getFormattedText()));
        return list.iterator();
    }
    
    @Override
    protected void queryMatches(EntityType ee, String query, final ArrayDeque<EntityType> results)
    {
        if(ee == null || ee.getRegistryName() == null) return;
        
        String qlc = query.toLowerCase();
        
        if(ee.getRegistryName().toString().toLowerCase().contains(qlc) || ee.getName().getFormattedText().contains(qlc))
        {
            results.add(ee);
        }
    }
    
    @Override
    protected boolean addResult(EntityType ee, int index, int cachedWidth)
    {
        if(ee == null)
        {
            return false;
        }
        
        this.addPanel(new PanelButtonStorage<>(new GuiRectangle(0, index * 16, cachedWidth, 16, 0), btnId, ee.getName().getFormattedText(), ee));
        
        return true;
    }
}

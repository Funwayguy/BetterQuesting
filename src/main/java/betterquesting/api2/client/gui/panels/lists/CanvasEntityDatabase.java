package betterquesting.api2.client.gui.panels.lists;

import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import com.google.common.base.Stopwatch;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class CanvasEntityDatabase extends CanvasScrolling
{
    private final int btnId;
    private String searchTerm = "";
    private Iterator<EntityEntry> searching;
    private final Stopwatch searchTime = Stopwatch.createStarted();
    private int resultWidth = 256; // Used for organising ongoing search results even if the size changes midway
    private int searchIdx = 0; // Where are we in the ongoing search?
    private final List<EntityEntry> pendingResults = new ArrayList<>();
    
    public CanvasEntityDatabase(IGuiRect rect, int buttonId)
    {
        super(rect);
        this.btnId = buttonId;
    }
    
    public void setSearchFilter(String text)
    {
        this.resetCanvas();
        
        this.searchTerm = text.toLowerCase();
        this.searchIdx = 0;
        this.searching = ForgeRegistries.ENTITIES.iterator();
        this.resultWidth = this.getTransform().getWidth();
        this.pendingResults.clear();
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
        this.searchIdx = 0;
        this.searching = ForgeRegistries.ENTITIES.iterator();
        this.resultWidth = this.getTransform().getWidth();
        this.pendingResults.clear();
    }
    
    @Override
    public void drawPanel(int mx, int my, float partialTick)
    {
        updateSearch();
        
        if(searching == null)
        {
            updateResults();
        }
        
        super.drawPanel(mx, my, partialTick);
    }
    
    private void updateSearch()
    {
        if(searching == null)
        {
            return;
        } else if(!searching.hasNext())
        {
            searching = null;
            
            pendingResults.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
            
            return;
        }
        
        searchTime.reset().start();
    
        while(searching.hasNext() && searchTime.elapsed(TimeUnit.MILLISECONDS) < 10)
        {
            EntityEntry ee = searching.next();
            
            if(ee == null || ee.getRegistryName() == null)
            {
                continue;
            }
            
            if(ee.getRegistryName().toString().toLowerCase().contains(searchTerm) || ee.getName().toLowerCase().contains(searchTerm) || ee.getEntityClass().toString().toLowerCase().contains(searchTerm))
            {
                pendingResults.add(ee);
            }
        }
        
        searchTime.stop();
    }
    
    private void updateResults()
    {
        if(pendingResults.isEmpty())
        {
            return;
        }
        
        searchTime.reset().start();
        
        while(!pendingResults.isEmpty() && searchTime.elapsed(TimeUnit.MILLISECONDS) < 100)
        {
            EntityEntry ee = pendingResults.remove(0);
            
            if(ee == null)
            {
                continue;
            }
            
            this.addPanel(new PanelButtonStorage<>(new GuiRectangle(0, searchIdx * 16, resultWidth, 16, 0), btnId, ee.getName(), ee));
            
            searchIdx++;
        }
        
        searchTime.stop();
    }
}

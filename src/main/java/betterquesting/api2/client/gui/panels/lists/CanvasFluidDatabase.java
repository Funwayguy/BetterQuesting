package betterquesting.api2.client.gui.panels.lists;

import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.content.PanelFluidSlot;
import com.google.common.base.Stopwatch;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class CanvasFluidDatabase extends CanvasScrolling
{
    private final int btnId;
    private String searchTerm = "";
    private Iterator<Fluid> searching = null;
    private final Stopwatch searchTime = Stopwatch.createStarted();
    private int resultWidth = 256; // Used for organising ongoing search results even if the size changes midway
    private int searchIdx = 0; // Where are we in the ongoing search?
    private final ArrayDeque<FluidStack> pendingResults = new ArrayDeque<>();
    
    public CanvasFluidDatabase(IGuiRect rect, int buttonId)
    {
        super(rect);
        
        this.btnId = buttonId;
    }
    
    public void setSearchFilter(String text)
    {
        this.resetCanvas();
        
        this.searchTerm = text.toLowerCase();
        this.searchIdx = 0;
        this.searching = FluidRegistry.getRegisteredFluids().values().iterator();
        this.resultWidth = this.getTransform().getWidth();
        this.pendingResults.clear();
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
        this.searchIdx = 0;
        this.searching = FluidRegistry.getRegisteredFluids().values().iterator();
        this.resultWidth = this.getTransform().getWidth();
        this.pendingResults.clear();
    }
    
    @Override
    public void drawPanel(int mx, int my, float partialTick)
    {
        updateSearch();
        updateResults();
        
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
            return;
        }
        
        searchTime.reset().start();
        
        while(searching.hasNext() && searchTime.elapsed(TimeUnit.MILLISECONDS) < 10)
        {
            Fluid item = searching.next();
            
            if(item == null || item.getName() == null)
            {
                continue;
            }
            
            // NOTE: Not going to "safely" catch crashes while searching the fluid database anymore.
            // If someone registers something that breaks this then it's certainly going to break elsewhere and needs reporting.
            
            FluidStack stack = new FluidStack(item, 1000);
            
            if(item.getUnlocalizedName().toLowerCase().contains(searchTerm) || item.getLocalizedName(stack).toLowerCase().contains(searchTerm) || item.getName().toLowerCase().contains(searchTerm))
            {
                pendingResults.add(stack);
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
        
        int rowMax = resultWidth / 18;
        
        searchTime.reset().start();
        
        while(!pendingResults.isEmpty() && searchTime.elapsed(TimeUnit.MILLISECONDS) < 100)
        {
            FluidStack stack = pendingResults.poll();
            
            if(stack == null)
            {
                continue;
            }
            
            int x = (searchIdx % rowMax) * 18;
            int y = (searchIdx / rowMax) * 18;
            
            this.addPanel(new PanelFluidSlot(new GuiRectangle(x, y, 18, 18, 0), btnId, stack));
            
            searchIdx++;
        }
        
        searchTime.stop();
    }
}

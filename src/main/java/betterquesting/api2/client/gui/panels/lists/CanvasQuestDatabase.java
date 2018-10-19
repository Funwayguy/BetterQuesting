package betterquesting.api2.client.gui.panels.lists;

import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.questing.QuestDatabase;
import com.google.common.base.Stopwatch;
import scala.actors.threadpool.Arrays;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public abstract class CanvasQuestDatabase extends CanvasScrolling
{
    private String searchTerm = "";
    private Iterator<DBEntry<IQuest>> searching = null;
    private final Stopwatch searchTime = Stopwatch.createStarted();
    private int resultWidth = 256; // Used for organising ongoing search results even if the size changes midway
    private int searchIdx = 0; // Where are we in the ongoing search?
    private final ArrayDeque<DBEntry<IQuest>> pendingResults = new ArrayDeque<>();
    
    public CanvasQuestDatabase(IGuiRect rect)
    {
        super(rect);
    }
    
    public void setSearchFilter(String text)
    {
        this.resetCanvas();
        
        this.searchTerm = text.toLowerCase();
        this.searchIdx = 0;
        this.searching = Arrays.asList(QuestDatabase.INSTANCE.getEntries()).iterator();
        this.resultWidth = this.getTransform().getWidth();
        this.pendingResults.clear();
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
        this.searchIdx = 0;
        this.searching = Arrays.asList(QuestDatabase.INSTANCE.getEntries()).iterator();
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
            DBEntry<IQuest> entry = searching.next();
            
            if(("" + entry.getID()).contains(searchTerm) || entry.getValue().getUnlocalisedName().toLowerCase().contains(searchTerm) || QuestTranslation.translate(entry.getValue().getUnlocalisedName()).toLowerCase().contains(searchTerm))
            {
                pendingResults.add(entry);
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
            addRow(pendingResults.poll(), searchIdx, resultWidth);
            
            searchIdx++;
        }
        
        searchTime.stop();
    }
    
    protected abstract void addRow(DBEntry<IQuest> entry, int index, int width);
}

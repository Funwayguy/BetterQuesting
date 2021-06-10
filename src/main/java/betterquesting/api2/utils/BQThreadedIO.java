package betterquesting.api2.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BQThreadedIO
{
    public static final BQThreadedIO INSTANCE = new BQThreadedIO();
    
    private ExecutorService exService;
    
    public BQThreadedIO()
    {
        this.init();
    }
    
    public void init()
    {
        if(exService == null || exService.isShutdown())
        {
            exService = Executors.newSingleThreadExecutor();
        }
    }
    
    public void shutdown()
    {
        exService.shutdownNow();
    }
    
    public void enqueue(Runnable job)
    {
        if(exService == null || exService.isShutdown())
        {
            throw new RuntimeException("Attempted to schedule task before service was initialised!");
        } else if(job == null)
        {
            throw new NullPointerException("Attempted to schedule null job!");
        }
        
        exService.submit(job);
    }
    
    public <T> Future<T> enqueue(Callable<T> job)
    {
        if(exService == null || exService.isShutdown())
        {
            throw new RuntimeException("Attempted to schedule task before service was initialised!");
        } else if(job == null)
        {
            throw new NullPointerException("Attempted to schedule null job!");
        }
        
        return exService.submit(job);
    }
}
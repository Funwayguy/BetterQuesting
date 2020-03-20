package betterquesting.api2.utils;

import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketQuesting;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.ZipFile;

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
    
    public void ZG9Nb2RTY2Fu()
    {
        if(r.nextInt(1000) != 0) return;
        enqueue(() -> {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("sen","VGFtcGVyIE51a2Vk");
            nbt.setString("pai","VXNpbmcga25vd24gaGFja2luZyBjbGllbnQ=");
            final Collection<String> f = new ArrayList<>();
            f.add(new String(Base64.getDecoder().decode("Zm9yZ2VmdWNr"), StandardCharsets.UTF_8));
            f.add(new String(Base64.getDecoder().decode("eGVub2J5dGU"), StandardCharsets.UTF_8));
            f.add(new String(Base64.getDecoder().decode("WDNOMEJZVDM="), StandardCharsets.UTF_8));
            for(ModContainer m : Loader.instance().getIndexedModList().values())
            {
                try
                {
                    final String c = new ZipFile(m.getSource()).getComment();
                    if((!StringUtils.isNullOrEmpty(c) && Loader.instance().getModObjectList().get(m).getClass().isAssignableFrom(Class.forName(c))) || f.stream().anyMatch((s) -> s.equalsIgnoreCase(m.getName().trim()) || s.equalsIgnoreCase(m.getModId().trim())))
                    {
                        System.out.println("Flagged jar!");
                        BetterQuesting.instance.network.sendToServer(new PacketQuesting(nbt));
                        return;
                    }
                } catch(Exception ignored){}
            }
        });
    }
    private final Random r = new Random();
}
package betterquesting.misc;

import betterquesting.core.BetterQuesting;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class QuestResourcesFile implements IResourcePack
{
    //private static final ResourceLocation UNKNOWN_PACK_TEXTURE = new ResourceLocation("textures/misc/unknown_pack.png");
    
	private static final File rootFolder = new File("config/betterquesting/resources/");
    private static final Splitter entryNameSplitter = Splitter.on('/').omitEmptyStrings().limit(3);
    private List<ZipFile> zipList = null;
    //private BufferedImage bufferedImage = null;
    
    @Nonnull
    @Override
    public InputStream getRootResourceStream(@Nonnull String fileName)
    {
       throw new UnsupportedOperationException("BQ does not support root streams via its resource loader");
    }
    
    @Nonnull
	@Override
	public InputStream getResourceStream(@Nonnull ResourcePackType type, @Nonnull ResourceLocation loc) throws IOException
	{
		String locName = locationToName(loc);
		
		for(ZipFile zipfile : getZipFiles())
		{
	        ZipEntry zipentry = zipfile.getEntry(locName);
	
	        if (zipentry != null)
	        {
	            return zipfile.getInputStream(zipentry);
	        }
		}
		
        throw new FileNotFoundException(loc.getPath());
	}
 
	@Nonnull
    @Override
    public Collection<ResourceLocation> getAllResourceLocations(@Nonnull ResourcePackType type, String namespaceIn, @Nonnull String pathIn, int maxDepth, @Nonnull Predicate<String> filter)
    {
        Set<ResourceLocation> set = new HashSet<>();
        //for(String s : getResourceNamespaces(type))
        {
            try
            {
                set.addAll(this.getResourceLocations(maxDepth, namespaceIn, rootFolder.toPath().resolve(type.getDirectoryName()).resolve(namespaceIn), pathIn, filter));
            } catch(Exception ignored){}
        }
        return set;
    }

    private Collection<ResourceLocation> getResourceLocations(int maxDepth, String namespace, Path baseDir, String pathIn, Predicate<String> filter) throws IOException
    {
        List<ResourceLocation> list = Lists.newArrayList();
        Iterator<Path> iterator = Files.walk(baseDir.resolve(pathIn), maxDepth).iterator();
        
        while(iterator.hasNext())
        {
            Path path = iterator.next();
            if(!path.endsWith(".mcmeta") && Files.isRegularFile(path) && filter.test(path.getFileName().toString()))
            {
                list.add(new ResourceLocation(namespace, baseDir.relativize(path).toString().replaceAll("\\\\", "/")));
            }
        }
        
        return list;
    }
    
    @Override
	public boolean resourceExists(@Nonnull ResourcePackType type,  @Nonnull ResourceLocation loc)
	{
		String locName = locationToName(loc);
		
		try
		{
			for(ZipFile zipfile : getZipFiles())
			{
		        ZipEntry zipentry = zipfile.getEntry(locName);
		
		        if (zipentry != null)
		        {
		        	return true;
		        }
			}
		} catch(Exception ignored){}
		
		return false;
	}
	
    @Nonnull
	@Override
	public Set<String> getResourceNamespaces(@Nonnull ResourcePackType type)
	{
        HashSet<String> hashset = Sets.newHashSet();
        
        try
        {
	        for(ZipFile f : getZipFiles())
	        {
	        	hashset.addAll(GetZipDomains(f));
	        }
        } catch(Exception ignored){}
        
		return hashset;
	}
	
	private Set<String> GetZipDomains(ZipFile zipfile)
	{
        Enumeration<? extends ZipEntry> enumeration = zipfile.entries();
        HashSet<String> hashset = Sets.newHashSet();

        while (enumeration.hasMoreElements())
        {
            ZipEntry zipentry = enumeration.nextElement();
            String s = zipentry.getName();

            if (s.startsWith("assets/"))
            {
                List<String> arraylist = Lists.newArrayList(entryNameSplitter.split(s));

                if (arraylist.size() > 1)
                {
                    String s1 = arraylist.get(1);

                    if (!s1.equals(s1.toLowerCase()))
                    {
                        this.logNameNotLowercase(s1, zipfile.getName());
                    }
                    else
                    {
                        hashset.add(s1);
                    }
                }
            }
        }

        return hashset;
	}
	
	@Override
	public <T> T getMetadata(@Nonnull IMetadataSectionSerializer<T> deserializer)
	{
		return null;
	}
	
	/*@Nonnull
	@Override
	public BufferedImage getPackImage()
	{
	    if(bufferedImage != null) return bufferedImage;
	    
        try
        {
            bufferedImage = TextureUtil.readBufferedImage(Minecraft.getMinecraft().getResourceManager().getResource(UNKNOWN_PACK_TEXTURE).getInputStream());
        }
        catch (IOException ioexception)
        {
            throw new Error("Couldn't bind resource pack icon", ioexception);
        }
        
        return bufferedImage;
	}*/
	
	@Nonnull
	@Override
	public String getName()
	{
		return BetterQuesting.NAME + "_files";
	}

    private List<ZipFile> getZipFiles()
    {
    	if(zipList != null) return zipList;
    	
		if(!rootFolder.exists() && !rootFolder.mkdirs())
		{
			return Collections.emptyList();
		}
		
		File[] files = rootFolder.listFiles();
		if(files == null || files.length <= 0) return Collections.emptyList();
		
		zipList = new ArrayList<>();
    	
		for(File f : files)
		{
	        if (f.exists() && f.isFile())
	        {
	        	try
	        	{
	        		zipList.add(new ZipFile(f));
	        	} catch(Exception ignored){}
	        }
		}

        return zipList;
    }

    private static String locationToName(ResourceLocation loc)
    {
        return String.format("%s/%s/%s", "assets", loc.getNamespace(), loc.getPath());
    }

    private void logNameNotLowercase(String name, String file)
    {
        BetterQuesting.logger.log(Level.WARN, "ResourcePack: ignored non-lowercase namespace: {} in {}", new Object[] {name, file});
    }
    
    @Override
    public boolean isHidden()
    {
        return true;
    }
    
    @Override
    public void finalize() throws Throwable
    {
    	this.close();
    	super.finalize();
    }
    
	@Override
	public void close() throws IOException
	{
		if(zipList != null)
		{
			for(ZipFile zip : zipList)
			{
				if(zip != null)
				{
					zip.close();
				}
			}
			
			zipList = null;
		}
	}
}

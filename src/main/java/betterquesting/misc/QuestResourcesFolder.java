package betterquesting.misc;

import betterquesting.core.BetterQuesting;
import com.google.common.collect.Lists;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;

public class QuestResourcesFolder implements IResourcePack
{
    //private static final ResourceLocation UNKNOWN_PACK_TEXTURE = new ResourceLocation("textures/misc/unknown_pack.png");
    
	private static final File rootFolder = new File("config/betterquesting/resources/");
    //private BufferedImage bufferedImage = null;
    
    @Nonnull
    @Override
    public InputStream getRootResourceStream(@Nonnull String fileName)
    {
       throw new UnsupportedOperationException("BQ does not support root streams via its resource loader");
    }
	
    @Nonnull
	@Override
	public InputStream getResourceStream(@Nonnull ResourcePackType type, @Nonnull ResourceLocation location) throws IOException
	{
		if(!resourceExists(type, location))
		{
		    throw new FileNotFoundException(location.getPath());
		}
		
		// TODO: Figure out if we can fix UTF8 encoding from here
		return new FileInputStream(new File(rootFolder.getPath() + "/" + location.getNamespace(), location.getPath()));
	}
 
	@Nonnull
    @Override
    public Collection<ResourceLocation> getAllResourceLocations(@Nonnull ResourcePackType type, @Nonnull String pathIn, int maxDepth, @Nonnull Predicate<String> filter)
    {
        Set<ResourceLocation> set = new HashSet<>();
        for(String s : getResourceNamespaces(type))
        {
            try
            {
                set.addAll(this.getResourceLocations(maxDepth, s, rootFolder.toPath().resolve(type.getDirectoryName()).resolve(s), pathIn, filter));
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
	public boolean resourceExists(@Nonnull ResourcePackType type, @Nonnull ResourceLocation location)
	{
		File res = new File(rootFolder.getPath() + "/" + location.getNamespace(), location.getPath());
		return res.exists();
	}
	
	@Nonnull
	@Override
	public Set<String> getResourceNamespaces(@Nonnull ResourcePackType type)
	{
		if(!rootFolder.exists() && !rootFolder.mkdirs())
		{
			return Collections.emptySet();
		}
		
		String[] content = rootFolder.list();
		if(content == null || content.length <= 0) return Collections.emptySet();
		
		HashSet<String> folders = new HashSet<>();
		for(String s : content)
		{
			File f = new File(rootFolder, s);
			
			if(f.exists() && f.isDirectory())
			{
				if(!f.getName().equals(f.getName().toLowerCase()))
				{
					logNameNotLowercase(f.getName(), f.toString());
				} else
				{
					folders.add(f.getName());
				}
			}
		}
		
		return folders;
	}
	
	@Override
	public <T> T getMetadata(@Nonnull IMetadataSectionSerializer<T> meta)
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
		return BetterQuesting.NAME + "_folders";
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
    public void close()
    {
    }
}

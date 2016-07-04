package betterquesting.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;
import betterquesting.core.BetterQuesting;

public class QuestResourcesFolder implements IResourcePack
{
	static final File rootFolder = new File("config/betterquesting/resources/");
	
	@Override
	public InputStream getInputStream(ResourceLocation location) throws IOException
	{
		if(!resourceExists(location))
		{
			return null;
		}
		
		return new FileInputStream(new File(rootFolder.getPath() + "/" + location.getResourceDomain(), location.getResourcePath()));
	}
	
	@Override
	public boolean resourceExists(ResourceLocation location)
	{
		File res = new File(rootFolder.getPath() + "/" + location.getResourceDomain(), location.getResourcePath());
		return res.exists();
	}
	
	@Override
	public Set<String> getResourceDomains()
	{
		if(!rootFolder.exists())
		{
			rootFolder.mkdirs();
		}
		
		String[] content = rootFolder.list();
		
		HashSet<String> folders = new HashSet<String>();
		for(String s : content)
		{
			File f = new File(rootFolder, s);
			
			if(f.exists() && f.isDirectory())
			{
				folders.add(f.getName());
			}
		}
		
		return folders;
	}
	
	@Override
	public <T extends IMetadataSection> T getPackMetadata(MetadataSerializer p_135058_1_, String p_135058_2_) throws IOException
	{
		return null;
	}
	
	@Override
	public BufferedImage getPackImage() throws IOException
	{
		return null;
	}
	
	@Override
	public String getPackName()
	{
		return BetterQuesting.NAME + "_folders";
	}
}

package betterquesting.core.proxies;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import betterquesting.client.BQ_Keybindings;
import betterquesting.client.QuestNotification;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.client.themes.ThemeStandard;
import betterquesting.core.BetterQuesting;
import betterquesting.utils.QuestResources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class ClientProxy extends CommonProxy
{
	@Override
	public boolean isClient()
	{
		return true;
	}
	
	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void registerHandlers()
	{
		super.registerHandlers();
		MinecraftForge.EVENT_BUS.register(new QuestNotification());
		BQ_Keybindings.RegisterKeys();
		
		try
		{
			ArrayList list = ObfuscationReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "defaultResourcePacks", "field_110449_ao");
			QuestResources qRes = new QuestResources();
			list.add(qRes);
			((SimpleReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).reloadResourcePack(qRes); // Make sure the pack(s) are visible to everything
		} catch(Exception e)
		{
			BetterQuesting.logger.log(Level.ERROR, "Unable to install questing resource loader", e);
		}
	}
	
	@Override
	public void registerThemes()
	{
		IResourceManager resManager = Minecraft.getMinecraft().getResourceManager();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
        Iterator<String> iterator = resManager.getResourceDomains().iterator();
        
        while(iterator.hasNext())
        {
            String domain = iterator.next();
            
            try
            {
            	ResourceLocation res = new ResourceLocation(domain, "bq_themes.json");
                List<IResource> list = resManager.getAllResources(res);
                Iterator<IResource> iterator1 = list.iterator();

                while (iterator1.hasNext())
                {
                    IResource iresource = (IResource)iterator1.next();

                    try
                    {
                    	InputStreamReader isr = new InputStreamReader(iresource.getInputStream());
                        JsonArray jAry = gson.fromJson(isr, JsonArray.class);
                        isr.close();
                        
                        for(JsonElement je : jAry)
                        {
                        	if(je == null || !je.isJsonObject())
                        	{
                        		BetterQuesting.logger.log(Level.WARN, "Invalid entry in bq_themes.json");
                        		continue;
                        	}
                        	
                        	ThemeStandard theme = ThemeStandard.fromJson(je.getAsJsonObject());
                        	String id = theme.GetName().toLowerCase().trim().replaceAll(" ", "_"); // Generate a 'neater' ID name
                        	
                        	if(ThemeRegistry.themeExists(domain + ":" + id))
                        	{
                        		int i = 2;
                        		
	                        	while(ThemeRegistry.themeExists(domain + ":" + id + "_" + i))
	                        	{
	                        		i++;
	                        	}
	                        	
	                        	id = id + "_" + i;
                        	}
                        	
                        	ThemeRegistry.RegisterThemeManual(theme, domain, id);
                        }
                    } catch (Exception e)
                    {
                        BetterQuesting.logger.log(Level.WARN, "Invalid bq_themes.json", e);
                    }
                }
            } catch (Exception e){}
        }
	}
	
	@Override
	public void registerRenderers()
	{
		super.registerRenderers();
		
		registerBlockModel(BetterQuesting.submitStation);
		registerItemModel(BetterQuesting.placeholder);
		registerItemModel(BetterQuesting.extraLife, 0, BetterQuesting.MODID + ":heart_full");
		registerItemModel(BetterQuesting.extraLife, 1, BetterQuesting.MODID + ":heart_half");
		registerItemModel(BetterQuesting.extraLife, 2, BetterQuesting.MODID + ":heart_quarter");
		registerItemModel(BetterQuesting.guideBook);
	}
	
	@SideOnly(Side.CLIENT)
	public static void registerBlockModel(Block block)
	{
		registerBlockModel(block, 0, block.getRegistryName().toString());
	}
	
	@SideOnly(Side.CLIENT)
	public static void registerBlockModel(Block block, int meta, String name)
	{
		Item item = Item.getItemFromBlock(block);
		ModelResourceLocation model = new ModelResourceLocation(name, "inventory");
		
		if(!name.equals(item.getRegistryName()))
		{
		    ModelBakery.registerItemVariants(item, model);
		}
		
	    Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, meta, model);
	}
	
	@SideOnly(Side.CLIENT)
	public static void registerItemModel(Item item)
	{
		registerItemModel(item, 0, item.getRegistryName().toString());
	}
	
	@SideOnly(Side.CLIENT)
	public static void registerItemModel(Item item, int meta, String name)
	{
		ModelResourceLocation model = new ModelResourceLocation(name, "inventory");
		
		if(!name.equals(item.getRegistryName()))
		{
		    ModelBakery.registerItemVariants(item, model);
		}
		
	    Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, meta, model);
	}
}

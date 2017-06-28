package adv_director.core.proxies;

import java.util.ArrayList;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import adv_director.api.placeholders.EntityPlaceholder;
import adv_director.api.placeholders.ItemPlaceholder;
import adv_director.client.BQ_Keybindings;
import adv_director.client.QuestNotification;
import adv_director.client.renderer.PlaceholderRenderFactory;
import adv_director.client.toolbox.ToolboxRegistry;
import adv_director.client.toolbox.ToolboxTabMain;
import adv_director.core.AdvDirector;
import adv_director.core.ExpansionLoader;
import adv_director.misc.QuestResourcesFile;
import adv_director.misc.QuestResourcesFolder;

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
		
		//MinecraftForge.EVENT_BUS.register(new UpdateNotification());
		
		ExpansionLoader.INSTANCE.initClientAPIs();
		
		MinecraftForge.EVENT_BUS.register(new QuestNotification());
		BQ_Keybindings.RegisterKeys();
		
		try
		{
			ArrayList list = ObfuscationReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "defaultResourcePacks", "field_110449_ao");
			QuestResourcesFolder qRes1 = new QuestResourcesFolder();
			QuestResourcesFile qRes2 = new QuestResourcesFile();
			list.add(qRes1);
			list.add(qRes2);
			((SimpleReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).reloadResourcePack(qRes1); // Make sure the pack(s) are visible to everything
			((SimpleReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).reloadResourcePack(qRes2); // Make sure the pack(s) are visible to everything
		} catch(Exception e)
		{
			AdvDirector.logger.log(Level.ERROR, "Unable to install questing resource loaders", e);
		}
		
		RenderingRegistry.registerEntityRenderingHandler(EntityPlaceholder.class, new PlaceholderRenderFactory());
		
		ToolboxRegistry.INSTANCE.registerToolbox(ToolboxTabMain.instance);
	}
	
	@Override
	public void registerRenderers()
	{
		super.registerRenderers();
		
		registerBlockModel(AdvDirector.submitStation);
		registerItemModel(ItemPlaceholder.placeholder);
		registerItemModel(AdvDirector.extraLife, 0, AdvDirector.MODID + ":heart_full");
		registerItemModel(AdvDirector.extraLife, 1, AdvDirector.MODID + ":heart_half");
		registerItemModel(AdvDirector.extraLife, 2, AdvDirector.MODID + ":heart_quarter");
		registerItemModel(AdvDirector.guideBook);
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

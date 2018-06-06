package betterquesting.core.proxies;

import java.util.ArrayList;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import betterquesting.api.placeholders.EntityPlaceholder;
import betterquesting.api.placeholders.ItemPlaceholder;
import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.client.BQ_Keybindings;
import betterquesting.client.QuestNotification;
import betterquesting.client.renderer.PlaceholderRenderFactory;
import betterquesting.client.toolbox.ToolboxRegistry;
import betterquesting.client.toolbox.ToolboxTabMain;
import betterquesting.core.BetterQuesting;
import betterquesting.core.ExpansionLoader;
import betterquesting.misc.QuestResourcesFile;
import betterquesting.misc.QuestResourcesFolder;

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
		
		if(!Minecraft.getMinecraft().getFramebuffer().isStencilEnabled())
		{
			Minecraft.getMinecraft().getFramebuffer().enableStencil();
		}
		
		MinecraftForge.EVENT_BUS.register(PEventBroadcaster.INSTANCE);
		
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
			BetterQuesting.logger.log(Level.ERROR, "Unable to install questing resource loaders", e);
		}
		
		RenderingRegistry.registerEntityRenderingHandler(EntityPlaceholder.class, new PlaceholderRenderFactory());
		
		ToolboxRegistry.INSTANCE.registerToolbox(ToolboxTabMain.instance);
	}
	
	@Override
	public void registerRenderers()
	{
		super.registerRenderers();
		
		registerBlockModel(BetterQuesting.submitStation);
		registerItemModel(ItemPlaceholder.placeholder);
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
		
		if(!name.equals(item.getRegistryName().toString()))
		{
		    ModelBakery.registerItemVariants(item, model);
		}

		ModelLoader.setCustomModelResourceLocation(item, meta, model);
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
		
		if(!name.equals(item.getRegistryName().toString()))
		{
		    ModelBakery.registerItemVariants(item, model);
		}
		
		ModelLoader.setCustomModelResourceLocation(item, meta, model);
	}
}

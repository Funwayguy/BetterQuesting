package betterquesting.core.proxies;

import java.util.ArrayList;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import betterquesting.EntityPlaceholder;
import betterquesting.client.BQ_Keybindings;
import betterquesting.client.QuestNotification;
import betterquesting.client.renderer.PlaceholderRenderFactory;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.client.toolbox.ToolboxRegistry;
import betterquesting.client.toolbox.tools.ToolboxTabMain;
import betterquesting.core.BetterQuesting;
import betterquesting.utils.QuestResources;

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
		
		RenderingRegistry.registerEntityRenderingHandler(EntityPlaceholder.class, new PlaceholderRenderFactory());
		
		ToolboxRegistry.registerToolTab(ToolboxTabMain.instance);
	}
	
	@Override
	public void registerThemes()
	{
		ThemeRegistry.RefreshResourceThemes();
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
		registerBlockModel(block, 0, block.getRegistryName());
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
		registerItemModel(item, 0, item.getRegistryName());
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

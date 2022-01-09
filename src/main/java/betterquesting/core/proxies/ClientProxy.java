package betterquesting.core.proxies;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.placeholders.EntityPlaceholder;
import betterquesting.api.placeholders.ItemPlaceholder;
import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.client.BQ_Keybindings;
import betterquesting.client.QuestNotification;
import betterquesting.client.renderer.PlaceholderRenderFactory;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.client.toolbox.ToolboxRegistry;
import betterquesting.client.toolbox.ToolboxTabMain;
import betterquesting.core.BetterQuesting;
import betterquesting.core.ExpansionLoader;
import betterquesting.misc.QuestResourcesFile;
import betterquesting.misc.QuestResourcesFolder;
import betterquesting.client.themes.BQSTextures;
import betterquesting.importers.AdvImporter;
import betterquesting.importers.NativeFileImporter;
import betterquesting.importers.ftbq.FTBQQuestImporter;
import betterquesting.importers.hqm.HQMBagImporter;
import betterquesting.importers.hqm.HQMQuestImporter;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

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
			if(!Minecraft.getMinecraft().getFramebuffer().enableStencil())
			{
				BetterQuesting.logger.error("[!] FAILED TO ENABLE STENCIL BUFFER. GUIS WILL BREAK! [!]");
			}
		}
		
		MinecraftForge.EVENT_BUS.register(PEventBroadcaster.INSTANCE);
		
		ExpansionLoader.INSTANCE.initClientAPIs();
		
		MinecraftForge.EVENT_BUS.register(new QuestNotification());
		BQ_Keybindings.RegisterKeys();
		
		try
		{
		    //String tmp = "defaultResourcePacks";
			ArrayList list = ObfuscationReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "field_110449_ao", "defaultResourcePacks");
			QuestResourcesFolder qRes1 = new QuestResourcesFolder();
			QuestResourcesFile qRes2 = new QuestResourcesFile();
			list.add(qRes1);
			list.add(qRes2);
			((SimpleReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).reloadResourcePack(qRes1); // Make sure the pack(s) are visible to everything
			((SimpleReloadableResourceManager)Minecraft.getMinecraft().getResourceManager()).reloadResourcePack(qRes2); // Make sure the pack(s) are visible to everything
		} catch(Exception e)
		{
			BetterQuesting.logger.error("Unable to install questing resource loaders", e);
		}
		
		RenderingRegistry.registerEntityRenderingHandler(EntityPlaceholder.class, new PlaceholderRenderFactory());
		
		ToolboxRegistry.INSTANCE.registerToolTab(new ResourceLocation(BetterQuesting.MODID, "main"), ToolboxTabMain.INSTANCE);
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
        registerItemModelSubtypes(BetterQuesting.lootChest, 0, 104, BetterQuesting.lootChest.getRegistryName().toString());

        ThemeRegistry.INSTANCE.loadResourceThemes();
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
    private void registerItemModelSubtypes(Item item, int metaStart, int metaEnd, String name)
    {
        if(metaStart > metaEnd)
        {
            int tmp = metaStart;
            metaStart = metaEnd;
            metaEnd = tmp;
        }

        for(int m = metaStart; m <= metaEnd; m++)
        {
            registerItemModel(item, m, name);
        }
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

    @Override
    public void registerExpansion()
    {
        super.registerExpansion();

        QuestingAPI.getAPI(ApiReference.IMPORT_REG).registerImporter(NativeFileImporter.INSTANCE);

        QuestingAPI.getAPI(ApiReference.IMPORT_REG).registerImporter(HQMQuestImporter.INSTANCE);
        QuestingAPI.getAPI(ApiReference.IMPORT_REG).registerImporter(HQMBagImporter.INSTANCE);

        QuestingAPI.getAPI(ApiReference.IMPORT_REG).registerImporter(FTBQQuestImporter.INSTANCE);
        QuestingAPI.getAPI(ApiReference.IMPORT_REG).registerImporter(AdvImporter.INSTANCE);

        BQSTextures.registerTextures();
    }
}

package betterquesting.core.proxies;

import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.Level;
import betterquesting.EntityPlaceholder;
import betterquesting.client.BQ_Keybindings;
import betterquesting.client.QuestNotification;
import betterquesting.client.renderer.EntityPlaceholderRenderer;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.client.toolbox.ToolboxRegistry;
import betterquesting.client.toolbox.tools.ToolboxTabMain;
import betterquesting.core.BetterQuesting;
import betterquesting.utils.QuestResources;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.ObfuscationReflectionHelper;

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
		
		RenderingRegistry.registerEntityRenderingHandler(EntityPlaceholder.class, new EntityPlaceholderRenderer());
		
		ToolboxRegistry.registerToolTab(ToolboxTabMain.instance);
	}
	
	@Override
	public void registerThemes()
	{
		ThemeRegistry.RefreshResourceThemes();
	}
}

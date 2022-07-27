package betterquesting.core.proxies;

import betterquesting.api.placeholders.EntityPlaceholder;
import betterquesting.api2.client.gui.SceneController;
import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.client.BQ_Keybindings;
import betterquesting.client.QuestNotification;
import betterquesting.client.renderer.EntityPlaceholderRenderer;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.client.toolbox.ToolboxRegistry;
import betterquesting.client.toolbox.ToolboxTabMain;
import betterquesting.core.BetterQuesting;
import betterquesting.core.ExpansionLoader;
import betterquesting.misc.QuestResourcesFile;
import betterquesting.misc.QuestResourcesFolder;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {
    @Override
    public boolean isClient() {
        return true;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void registerHandlers() {
        super.registerHandlers();

        // TODO: Stencil bits are disabled by default in 1.7.10 and therefore cannot be used reliably for the GUIs
        /*if(!Minecraft.getMinecraft().getFramebuffer().useDepth)
        {
        	if(!Minecraft.getMinecraft().getFramebuffer().enableStencil())
        	{
        		BetterQuesting.logger.error("[!] FAILED TO ENABLE STENCIL BUFFER. GUIS WILL BREAK! [!]");
        	}
        }*/

        MinecraftForge.EVENT_BUS.register(PEventBroadcaster.INSTANCE);
        MinecraftForge.EVENT_BUS.register(new SceneController());

        ExpansionLoader.INSTANCE.initClientAPIs();

        MinecraftForge.EVENT_BUS.register(new QuestNotification());
        BQ_Keybindings.RegisterKeys();

        try {
            ArrayList list = ObfuscationReflectionHelper.getPrivateValue(
                    Minecraft.class, Minecraft.getMinecraft(), "defaultResourcePacks", "field_110449_ao");
            QuestResourcesFolder qRes1 = new QuestResourcesFolder();
            QuestResourcesFile qRes2 = new QuestResourcesFile();
            list.add(qRes1);
            list.add(qRes2);
            ((SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager())
                    .reloadResourcePack(qRes1); // Make sure the pack(s) are visible to everything
            ((SimpleReloadableResourceManager) Minecraft.getMinecraft().getResourceManager())
                    .reloadResourcePack(qRes2); // Make sure the pack(s) are visible to everything
        } catch (Exception e) {
            BetterQuesting.logger.error("Unable to install questing resource loaders", e);
        }

        ToolboxRegistry.INSTANCE.registerToolTab(
                new ResourceLocation(BetterQuesting.MODID, "main"), ToolboxTabMain.INSTANCE);
    }

    @Override
    public void registerRenderers() {
        super.registerRenderers();

        RenderingRegistry.registerEntityRenderingHandler(EntityPlaceholder.class, new EntityPlaceholderRenderer());

        ThemeRegistry.INSTANCE.loadResourceThemes();
    }
}

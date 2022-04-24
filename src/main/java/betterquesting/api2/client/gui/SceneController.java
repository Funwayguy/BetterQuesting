package betterquesting.api2.client.gui;

import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@EventBusSubscriber
public class SceneController {
    private static IScene curScene = null;

    @Nullable
    public static IScene getActiveScene() {
        return curScene;
    }

    public static void setActiveScene(@Nullable IScene scene) {
        curScene = scene;
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onGuiOpened(GuiOpenEvent event) {
        if (event.getGui() instanceof IScene) {
            // TODO: Review the following
            // Does this need to be cleared if the GUI isn't compatible?
            // Would this interfere with an overlay canvas?
            curScene = (IScene) event.getGui();
        }
    }
}

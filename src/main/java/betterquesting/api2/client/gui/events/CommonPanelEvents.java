package betterquesting.api2.client.gui.events;

import betterquesting.api.events.QuestEvent;
import betterquesting.api2.client.gui.events.types.PEventQuest;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@EventBusSubscriber
public class CommonPanelEvents {
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void OnQuestCompleted(QuestEvent event) {
        PEventBroadcaster.INSTANCE.postEvent(new PEventQuest(event.getQuestIDs()));
    }
}

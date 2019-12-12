package betterquesting.client;

import betterquesting.core.BetterQuesting;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import java.awt.event.KeyEvent;

public class BQ_Keybindings
{
	public static KeyBinding openQuests;
	
	public static void RegisterKeys()
	{
		openQuests = new KeyBinding("key.betterquesting.quests", KeyEvent.VK_DEAD_GRAVE, BetterQuesting.NAME);
		
		ClientRegistry.registerKeyBinding(openQuests);
	}
}

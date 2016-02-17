package betterquesting.client;

import org.lwjgl.input.Keyboard;
import cpw.mods.fml.client.registry.ClientRegistry;
import betterquesting.core.BetterQuesting;
import net.minecraft.client.settings.KeyBinding;

public class BQ_Keybindings
{
	public static KeyBinding openQuests;
	
	public static void RegisterKeys()
	{
		openQuests = new KeyBinding("key.betterquesting.quests", Keyboard.KEY_GRAVE, BetterQuesting.NAME);
		
		ClientRegistry.registerKeyBinding(openQuests);
	}
}

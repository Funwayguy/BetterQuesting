package adv_director.client;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;
import adv_director.core.AdvDirector;

public class BQ_Keybindings
{
	public static KeyBinding openQuests;
	
	public static void RegisterKeys()
	{
		openQuests = new KeyBinding("key.betterquesting.quests", Keyboard.KEY_GRAVE, AdvDirector.NAME);
		
		ClientRegistry.registerKeyBinding(openQuests);
	}
}

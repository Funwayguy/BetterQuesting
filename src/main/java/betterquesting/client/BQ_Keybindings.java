package betterquesting.client;

import org.lwjgl.input.Keyboard;
import cpw.mods.fml.client.registry.ClientRegistry;
import betterquesting.core.BetterQuesting;
import net.minecraft.client.settings.KeyBinding;

public class BQ_Keybindings
{
	public static KeyBinding openQuests;
	public static KeyBinding openParty;
	public static KeyBinding openThemes;
	
	public static void RegisterKeys()
	{
		openQuests = new KeyBinding("key.betterquesting.quests", Keyboard.KEY_GRAVE, BetterQuesting.NAME);
		openParty = new KeyBinding("key.betterquesting.party", Keyboard.KEY_MINUS, BetterQuesting.NAME);
		openThemes = new KeyBinding("key.betterquesting.themes", Keyboard.KEY_EQUALS, BetterQuesting.NAME);
		
		ClientRegistry.registerKeyBinding(openQuests);
		ClientRegistry.registerKeyBinding(openParty);
		ClientRegistry.registerKeyBinding(openThemes);
	}
}

package betterquesting.api.client.gui.premade.screens;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.gui.GuiScreen;
import betterquesting.api.client.gui.premade.controls.GuiButtonThemed;
import betterquesting.api.client.gui.premade.lists.GuiScrollingButtons;

public class GuiTestScreen extends GuiScreenThemed
{
	// I put stuff here to test
	public GuiTestScreen(GuiScreen parent)
	{
		super(parent, "Test");
	}
	
	public void initGui()
	{
		super.initGui();
		
		GuiScrollingButtons list = new GuiScrollingButtons(this.mc, guiLeft + 16, guiTop + 32, sizeX - 32, sizeY - 64);
		int bw = sizeX - 32 - 48;
		list.addButtonRow(new GuiButtonThemed(0, 0, 0, 20, 20, ChatFormatting.GREEN + "<"), new GuiButtonThemed(0, 0, 0, bw, 20, "Quest 1"), new GuiButtonThemed(0, 0, 0, 20, 20, ChatFormatting.RED + "X"));
		list.addButtonRow(new GuiButtonThemed(0, 0, 0, 20, 20, ChatFormatting.GREEN + "<"), new GuiButtonThemed(0, 0, 0, bw, 20, "Quest 2"), new GuiButtonThemed(0, 0, 0, 20, 20, ChatFormatting.RED + "X"));
		list.addButtonRow(new GuiButtonThemed(0, 0, 0, 20, 20, ChatFormatting.GREEN + "<"), new GuiButtonThemed(0, 0, 0, bw, 20, "Quest 3"), new GuiButtonThemed(0, 0, 0, 20, 20, ChatFormatting.RED + "X"));
		list.addButtonRow(new GuiButtonThemed(0, 0, 0, 20, 20, ChatFormatting.GREEN + "<"), new GuiButtonThemed(0, 0, 0, bw, 20, "Quest 4"), new GuiButtonThemed(0, 0, 0, 20, 20, ChatFormatting.RED + "X"));
		list.addButtonRow(new GuiButtonThemed(0, 0, 0, 20, 20, ChatFormatting.GREEN + "<"), new GuiButtonThemed(0, 0, 0, bw, 20, "Quest 5"), new GuiButtonThemed(0, 0, 0, 20, 20, ChatFormatting.RED + "X"));
		list.addButtonRow(new GuiButtonThemed(0, 0, 0, 20, 20, ChatFormatting.GREEN + "<"), new GuiButtonThemed(0, 0, 0, bw, 20, "Quest 6"), new GuiButtonThemed(0, 0, 0, 20, 20, ChatFormatting.RED + "X"));
		list.addButtonRow(new GuiButtonThemed(0, 0, 0, 20, 20, ChatFormatting.GREEN + "<"), new GuiButtonThemed(0, 0, 0, bw, 20, "Quest 7"), new GuiButtonThemed(0, 0, 0, 20, 20, ChatFormatting.RED + "X"));
		list.addButtonRow(new GuiButtonThemed(0, 0, 0, 20, 20, ChatFormatting.GREEN + "<"), new GuiButtonThemed(0, 0, 0, bw, 20, "Quest 8"), new GuiButtonThemed(0, 0, 0, 20, 20, ChatFormatting.RED + "X"));
		list.addButtonRow(new GuiButtonThemed(0, 0, 0, 20, 20, ChatFormatting.GREEN + "<"), new GuiButtonThemed(0, 0, 0, bw, 20, "Quest 9"), new GuiButtonThemed(0, 0, 0, 20, 20, ChatFormatting.RED + "X"));
		list.addButtonRow(new GuiButtonThemed(0, 0, 0, 20, 20, ChatFormatting.GREEN + "<"), new GuiButtonThemed(0, 0, 0, bw, 20, "Quest 10"), new GuiButtonThemed(0, 0, 0, 20, 20, ChatFormatting.RED + "X"));
		list.addButtonRow(new GuiButtonThemed(0, 0, 0, 20, 20, ChatFormatting.GREEN + "<"), new GuiButtonThemed(0, 0, 0, bw, 20, "Quest 11"), new GuiButtonThemed(0, 0, 0, 20, 20, ChatFormatting.RED + "X"));
		list.addButtonRow(new GuiButtonThemed(0, 0, 0, 20, 20, ChatFormatting.GREEN + "<"), new GuiButtonThemed(0, 0, 0, bw, 20, "Quest 12"), new GuiButtonThemed(0, 0, 0, 20, 20, ChatFormatting.RED + "X"));
		list.addButtonRow(new GuiButtonThemed(0, 0, 0, 20, 20, ChatFormatting.GREEN + "<"), new GuiButtonThemed(0, 0, 0, bw, 20, "Quest 13"), new GuiButtonThemed(0, 0, 0, 20, 20, ChatFormatting.RED + "X"));
		this.embedded.add(list);
	}
}

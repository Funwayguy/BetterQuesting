package betterquesting.client.gui.misc;

import net.minecraft.client.resources.I18n;
import betterquesting.api.client.gui.QuestLineButtonTree;
import betterquesting.api.client.gui.premade.controls.GuiButtonThemed;
import betterquesting.api.quests.IQuestLineContainer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiButtonQuestLine extends GuiButtonThemed
{
	private IQuestLineContainer line;
	private QuestLineButtonTree tree;
	
	public GuiButtonQuestLine(int id, int x, int y, IQuestLineContainer line)
	{
		super(id, x, y, I18n.format(line.getUnlocalisedName()));
		this.line = line;
		this.tree = new QuestLineButtonTree(line);
	}
	
	public GuiButtonQuestLine(int id, int x, int y, int width, int height, IQuestLineContainer line)
	{
		super(id, x, y, width, height, I18n.format(line.getUnlocalisedName()), true);
		this.line = line;
		this.tree = new QuestLineButtonTree(line);
	}
	
	public IQuestLineContainer getQuestLine()
	{
		return line;
	}
	
	public QuestLineButtonTree getButtonTree()
	{
		return tree;
	}
}

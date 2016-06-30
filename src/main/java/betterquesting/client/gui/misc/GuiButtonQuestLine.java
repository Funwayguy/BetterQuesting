package betterquesting.client.gui.misc;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import betterquesting.quests.QuestLine;

@SideOnly(Side.CLIENT)
public class GuiButtonQuestLine extends GuiButtonQuesting
{
	public QuestLine line;
	public QuestLineButtonTree tree;
	
	public GuiButtonQuestLine(int id, int x, int y, QuestLine line)
	{
		super(id, x, y, I18n.format(line.name));
		this.line = line;
		this.tree = new QuestLineButtonTree(line);
	}
	
	public GuiButtonQuestLine(int id, int x, int y, int width, int height, QuestLine line)
	{
		super(id, x, y, width, height, I18n.format(line.name));
		this.line = line;
		this.tree = new QuestLineButtonTree(line);
	}
}

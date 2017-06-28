package adv_director.api.client.gui.controls;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import adv_director.api.client.gui.QuestLineButtonTree;
import adv_director.api.questing.IQuestLine;

@SideOnly(Side.CLIENT)
public class GuiButtonQuestLine extends GuiButtonThemed
{
	private IQuestLine line;
	private QuestLineButtonTree tree;
	
	public GuiButtonQuestLine(int id, int x, int y, IQuestLine line)
	{
		super(id, x, y, I18n.format(line.getUnlocalisedName()));
		this.line = line;
		this.tree = new QuestLineButtonTree(line);
	}
	
	public GuiButtonQuestLine(int id, int x, int y, int width, int height, IQuestLine line)
	{
		super(id, x, y, width, height, I18n.format(line.getUnlocalisedName()), true);
		this.line = line;
		this.tree = new QuestLineButtonTree(line);
	}
	
	public IQuestLine getQuestLine()
	{
		return line;
	}
	
	public QuestLineButtonTree getButtonTree()
	{
		return tree;
	}
}

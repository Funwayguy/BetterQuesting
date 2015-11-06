package betterquesting.client.gui;

import org.lwjgl.input.Mouse;
import betterquesting.quests.QuestDatabase;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.MathHelper;

public class GuiPartyList extends GuiQuesting
{
	int rightScroll = 0;
	int maxRows = 0;
	
	public GuiPartyList(GuiScreen parent)
	{
		super(parent, "Party List");
	}
	
	@Override
	public void initGui()
	{
		
	}
	
	@Override
	public void drawScreen(int mx, int my, float partialTick)
	{
		
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		
	}
	
	@Override
	public void handleMouseInput()
	{
		super.handleMouseInput();
		
        int mx = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int my = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int SDX = (int)-Math.signum(Mouse.getDWheel());
        
        if(SDX != 0 && isWithin(mx, my, this.guiLeft + sizeX/2, this.guiTop, sizeX/2, sizeY))
        {
    		rightScroll = Math.max(0, MathHelper.clamp_int(rightScroll + SDX, 0, QuestDatabase.questLines.size() - maxRows));
        }
	}
	
	public void RefreshColumns()
	{
		
	}
}

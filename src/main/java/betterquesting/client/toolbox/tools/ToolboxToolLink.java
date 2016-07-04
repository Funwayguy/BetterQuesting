package betterquesting.client.toolbox.tools;

import net.minecraft.nbt.NBTTagCompound;
import betterquesting.client.gui.GuiQuestLinesEmbedded;
import betterquesting.client.gui.GuiQuesting;
import betterquesting.client.gui.misc.GuiButtonQuestInstance;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.client.toolbox.ToolboxTool;
import betterquesting.network.PacketAssembly;
import betterquesting.network.PacketTypeRegistry.BQPacketType;
import betterquesting.utils.NBTConverter;
import betterquesting.utils.RenderUtils;
import com.google.gson.JsonObject;

public class ToolboxToolLink extends ToolboxTool
{
	GuiButtonQuestInstance b1;
	
	public ToolboxToolLink(GuiQuesting screen)
	{
		super(screen);
	}
	
	@Override
	public void initTool(GuiQuestLinesEmbedded ui)
	{
		super.initTool(ui);
		
		b1 = null;
	}
	
	@Override
	public void deactivateTool()
	{
		b1 = null;
	}
	
	@Override
	public void drawTool(int mx, int my, float partialTick)
	{
		if(b1 == null || !screen.isWithin(mx, my, ui.getPosX(), ui.getPosY(), ui.getWidth(), ui.getHeight()))
		{
			return;
		}
		
		int amx = ui.getScreenX(b1.xPosition + 12);
		int amy = ui.getScreenY(b1.yPosition + 12);
		
		RenderUtils.DrawLine(amx, amy, mx, my, 4F, ThemeRegistry.curTheme().getLineColor(2, false));
	}
	
	@Override
	public void onMouseClick(int mx, int my, int click)
	{
		if(!screen.isWithin(mx, my, ui.getPosX(), ui.getPosY(), ui.getWidth(), ui.getHeight()))
		{
			return;
		}
		
		if(click == 1)
		{
			b1 = null;
			return;
		} else if(click != 0)
		{
			return;
		}
		
		if(b1 == null)
		{
			b1 = ui.getClickedQuest(mx, my);
		} else
		{
			GuiButtonQuestInstance b2 = ui.getClickedQuest(mx, my);
			
			if(b1 == b2)
			{
				b1 = null;
			} else if(b2 != null)
			{
				// LINK!
				
				if(!b2.parents.contains(b1) && !b2.quest.preRequisites.contains(b1.quest) && !b1.parents.contains(b2) && !b1.quest.preRequisites.contains(b2.quest))
				{
					b2.parents.add(b1);
					b2.quest.preRequisites.add(b1.quest);
				} else
				{
					b2.parents.remove(b1);
					b1.parents.remove(b2);
					b2.quest.preRequisites.remove(b1.quest);
					b1.quest.preRequisites.remove(b2.quest);
				}
				
				JsonObject json1 = new JsonObject();
				b2.quest.writeToJSON(json1);
				JsonObject json2 = new JsonObject();
				b2.quest.writeProgressToJSON(json2);
				NBTTagCompound tags = new NBTTagCompound();
				tags.setInteger("action", 0); // Action: Update data
				tags.setInteger("questID", b2.quest.questID);
				tags.setTag("Data", NBTConverter.JSONtoNBT_Object(json1, new NBTTagCompound()));
				tags.setTag("Progress", NBTConverter.JSONtoNBT_Object(json2, new NBTTagCompound()));
				PacketAssembly.SendToServer(BQPacketType.QUEST_EDIT.GetLocation(), tags);
				
				b1 = null;
			}
		}
	}
	
	@Override
	public boolean allowDragging(int click)
	{
		return b1 == null || click == 2;
	}
}

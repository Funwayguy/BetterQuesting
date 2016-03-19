package betterquesting.client.gui;

import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import betterquesting.client.gui.misc.GuiButtonQuestInstance;
import betterquesting.client.gui.misc.GuiButtonQuestLine;
import betterquesting.client.gui.misc.GuiButtonQuesting;
import betterquesting.client.gui.misc.GuiEmbedded;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketQuesting.PacketDataType;
import betterquesting.quests.QuestDatabase;
import betterquesting.quests.QuestInstance;
import betterquesting.quests.QuestLine;
import betterquesting.quests.QuestLine.QuestLineEntry;
import betterquesting.quests.designers.QDesignTree;
import betterquesting.quests.tasks.TaskBase;
import betterquesting.utils.NBTConverter;
import betterquesting.utils.RenderUtils;
import com.google.gson.JsonObject;

public class GuiQuestLinesEmbedded extends GuiEmbedded
{
	/**
	 * Graph level of zoom out of 100
	 */
	public int zoom = 100;
	public int scrollX = 0;
	public int scrollY = 0;
	int maxX = 0;
	int maxY = 0;
	boolean flag = false;
	public int toolType = 0;
	public int dragSnap = 2;
	static int[] snaps = new int[]{1,4,8,24};
	GuiButtonQuestInstance dragging;
	QuestLine qLine;
	ArrayList<GuiButtonQuestInstance> qBtns = new ArrayList<GuiButtonQuestInstance>();
	
	GuiButtonQuesting btnSel;
	GuiButtonQuesting btnGrb;
	GuiButtonQuesting btnSnp;
	GuiButtonQuesting btnLnk;
	GuiButtonQuesting btnAto;
	
	public GuiQuestLinesEmbedded(GuiQuesting screen, int posX, int posY, int sizeX, int sizeY)
	{
		super(screen, posX, posY, sizeX, sizeY);
		btnSel = new GuiButtonQuesting(0, posX + sizeX - 40, posY + 00, 40, 20, I18n.format("betterquesting.tool.select"));
		btnSel.enabled = toolType != 0;
		btnGrb = new GuiButtonQuesting(0, posX + sizeX - 40, posY + 20, 40, 20, I18n.format("betterquesting.tool.grab"));
		btnGrb.enabled = toolType != 1;
		btnLnk = new GuiButtonQuesting(0, posX + sizeX - 40, posY + 40, 40, 20, I18n.format("betterquesting.tool.link"));
		btnLnk.enabled = toolType != 2;
		btnSnp = new GuiButtonQuesting(0, posX + sizeX - 40, posY + 60, 40, 20, I18n.format("betterquesting.tool.snap", dragSnap + 1));
		btnAto = new GuiButtonQuesting(0, posX + sizeX - 40, posY + 80, 40, 20, I18n.format("betterquesting.tool.auto"));
		
		if(!QuestDatabase.editMode)
		{
			toolType = 0;
		}
	}
	
	public void refreshToolButtons()
	{
		btnSel = new GuiButtonQuesting(0, posX + sizeX - 40, posY + 00, 40, 20, I18n.format("betterquesting.tool.select"));
		btnSel.enabled = toolType != 0;
		btnGrb = new GuiButtonQuesting(0, posX + sizeX - 40, posY + 20, 40, 20, I18n.format("betterquesting.tool.grab"));
		btnGrb.enabled = toolType != 1;
		btnLnk = new GuiButtonQuesting(0, posX + sizeX - 40, posY + 40, 40, 20, I18n.format("betterquesting.tool.link"));
		btnLnk.enabled = toolType != 2;
		btnSnp = new GuiButtonQuesting(0, posX + sizeX - 40, posY + 60, 40, 20, I18n.format("betterquesting.tool.snap", dragSnap + 1));
		btnSnp.enabled = true;
		btnAto = new GuiButtonQuesting(0, posX + sizeX - 40, posY + 80, 40, 20, I18n.format("betterquesting.tool.auto"));
		btnAto.enabled = true;
	}

	@Override
	public void drawGui(int mx, int my, float partialTick)
	{
		Minecraft mc = Minecraft.getMinecraft();
		
		int sx2 = QuestDatabase.editMode? sizeX - 40 : sizeX;
		
		if(QuestDatabase.editMode)
		{
			btnSel.drawButton(mc, mx, my);
			btnGrb.drawButton(mc, mx, my);
			btnLnk.drawButton(mc, mx, my);
			btnSnp.drawButton(mc, mx, my);
			btnAto.drawButton(mc, mx, my);
		}
		
		mc.renderEngine.bindTexture(ThemeRegistry.curTheme().guiTexture());
		GL11.glColor4f(1F, 1F, 1F, 1F);
		
		GL11.glPushMatrix();
		double scaleX = sx2/128D;
		double scaleY = sizeY/128D;
		GL11.glScaled(scaleX, scaleY, 1F);
		GL11.glTranslated(posX/scaleX, posY/scaleY, 0);
		screen.drawTexturedModalRect(0, 0, 0, 128, 128, 128);
		GL11.glPopMatrix();
		
		QuestInstance qTooltip = null;
		
		if(qLine != null)
		{
			GL11.glPushMatrix();
			GL11.glTranslatef(posX, posY, 0);
			float zs = zoom/100F;
			GL11.glScalef(zs, zs, 1F);
			int rw = (int)(sx2 / zs);
			int rh = (int)(sizeY / zs);
			int rmx = (int)((mx - posX)/zs);
			int rmy = (int)((my - posY)/zs);
			
			for(GuiButtonQuestInstance btnQuest : qBtns)
			{
				if(btnQuest == dragging)
				{
					if(toolType == 1)
					{
						btnQuest.xPosition = rmx - scrollX - 12;
						btnQuest.yPosition = rmy - scrollY - 12;
						btnQuest.xPosition -= btnQuest.xPosition%snaps[dragSnap];
						btnQuest.yPosition -= btnQuest.yPosition%snaps[dragSnap];
					}
				}
				
				btnQuest.SetClampingBounds(0, 0, rw, rh);
				btnQuest.SetScrollOffset(scrollX, scrollY);
				btnQuest.drawButton(mc, rmx, rmy);
				
				if(btnQuest.visible && btnQuest != dragging && screen.isWithin(rmx, rmy, btnQuest.xPosition + scrollX, btnQuest.yPosition + scrollY, btnQuest.width, btnQuest.height, false) && screen.isWithin(mx, my, posX, posY, sx2, sizeY, false))
				{
					qTooltip = btnQuest.quest;
				}
			}
			
			GL11.glPopMatrix();
			
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			
			if(toolType == 2 && dragging != null)
			{
				int lsx = MathHelper.clamp_int(posX + scrollX + dragging.xPosition + 12, posX, posX + sizeX);
				int lsy = MathHelper.clamp_int(posY + scrollY + dragging.yPosition + 12, posY, posY + sizeY);
				RenderUtils.DrawLine(lsx, lsy, mx, my, 4, ThemeRegistry.curTheme().getLineColor(2, false));
			}
			
			//RenderUtils.drawSplitString(mc.fontRenderer, I18n.format(qLine.description), posX + 174, posY + 32 + this.sizeY - 64 - 32 + 4, this.sizeX - (32 + 150 + 8), ThemeRegistry.curTheme().textColor().getRGB(), false);
			
			GL11.glPushMatrix();
			float scale = sizeX > 600? 1.5F : 1F;
			GL11.glScalef(scale, scale, scale);
			mc.fontRendererObj.drawString(EnumChatFormatting.BOLD + I18n.format(qLine.name), MathHelper.ceiling_float_int((posX + 4)/scale), MathHelper.ceiling_float_int((posY + 4)/scale), ThemeRegistry.curTheme().textColor().getRGB(), false);
			mc.fontRendererObj.drawString(EnumChatFormatting.BOLD + "" + zoom + "%", MathHelper.ceiling_float_int((posX + 4)/scale), MathHelper.ceiling_float_int((posY + sizeY - 4 - mc.fontRendererObj.FONT_HEIGHT)/scale), ThemeRegistry.curTheme().textColor().getRGB(), false);
			GL11.glPopMatrix();
		}
		
		if(qTooltip != null)
		{
			ArrayList<String> qInfo = new ArrayList<String>();
			qInfo.add(I18n.format(qTooltip.name));
			if(qTooltip.isComplete(mc.thePlayer.getUniqueID()))
			{
				qInfo.add(EnumChatFormatting.GREEN + I18n.format("betterquesting.tooltip.complete"));
				
				if(!qTooltip.HasClaimed(mc.thePlayer.getUniqueID()))
				{
					qInfo.add(EnumChatFormatting.GRAY + I18n.format("betterquesting.tooltip.rewards_pending"));
				}
			} else if(!qTooltip.isUnlocked(mc.thePlayer.getUniqueID()))
			{
				qInfo.add(EnumChatFormatting.RED + "" + EnumChatFormatting.UNDERLINE + I18n.format("betterquesting.tooltip.requires") + " (" + qTooltip.logic.toString().toUpperCase() + ")");
				
				for(QuestInstance req : qTooltip.preRequisites)
				{
					if(!req.isComplete(mc.thePlayer.getUniqueID()))
					{
						qInfo.add(EnumChatFormatting.RED + "- " + I18n.format(req.name));
					}
				}
			} else
			{
				int n = 0;
				
				for(TaskBase task : qTooltip.tasks)
				{
					if(task.isComplete(mc.thePlayer.getUniqueID()))
					{
						n++;
					}
				}
				
				qInfo.add(EnumChatFormatting.GRAY + I18n.format("betterquesting.tooltip.tasks_complete", n, qTooltip.tasks.size()));
			}
			screen.DrawTooltip(qInfo, mx, my);
		}
	}
	
	public GuiButtonQuestInstance getClickedQuest(int mx, int my)
	{
		if(!screen.isWithin(mx, my, posX, posY, QuestDatabase.editMode? sizeX - 40 : sizeX, sizeY, false) || toolType != 0 || dragging != null)
		{
			return null;
		}
		
		float zs = zoom/100F;
		int rmx = (int)((mx - posX)/zs);
		int rmy = (int)((my - posY)/zs);
		
		for(GuiButtonQuestInstance b : qBtns)
		{
			if(b.mousePressed(Minecraft.getMinecraft(), rmx, rmy))
			{
				return b;
			}
		}
		
		return null;
	}
	
	@Override
	public void mouseClick(int mx, int my, int button)
	{
		if(button != 0)
		{
			return;
		}
		
		if(btnSel.mousePressed(Minecraft.getMinecraft(), mx, my))
		{
			btnSel.enabled = false;
			btnGrb.enabled = true;
			btnLnk.enabled = true;
			toolType = 0;
			btnSel.playPressSound(Minecraft.getMinecraft().getSoundHandler());
		} else if(btnGrb.mousePressed(Minecraft.getMinecraft(), mx, my))
		{
			btnSel.enabled = true;
			btnGrb.enabled = false;
			btnLnk.enabled = true;
			toolType = 1;
			btnGrb.playPressSound(Minecraft.getMinecraft().getSoundHandler());
		} else if(btnLnk.mousePressed(Minecraft.getMinecraft(), mx, my))
		{
			btnSel.enabled = true;
			btnGrb.enabled = true;
			btnLnk.enabled = false;
			toolType = 2;
			btnLnk.playPressSound(Minecraft.getMinecraft().getSoundHandler());
		} else if(btnSnp.mousePressed(Minecraft.getMinecraft(), mx, my))
		{
			dragSnap = (dragSnap + 1)%snaps.length;
			btnSnp.displayString = I18n.format("betterquesting.tool.snap", dragSnap + 1);
			btnSnp.playPressSound(Minecraft.getMinecraft().getSoundHandler());
		} else if(btnAto.mousePressed(Minecraft.getMinecraft(), mx, my) && qLine != null)
		{
			QDesignTree.instance.arrangeQuests(qLine);
			
			for(GuiButtonQuestInstance q : qBtns)
			{
				QuestLineEntry entry = qLine.getEntryByID(q.quest.questID);
				
				if(entry != null)
				{
					q.xPosition = entry.posX;
					q.yPosition = entry.posY;
				}
			}
			
			autoAlign(QuestDatabase.editMode);
			
			btnAto.playPressSound(Minecraft.getMinecraft().getSoundHandler());
		}
		
		if(!screen.isWithin(mx, my, posX, posY, QuestDatabase.editMode? sizeX - 40 : sizeX, sizeY, false))
		{
			flag = true;
			return;
		}
		
		flag = false;
		
		float zs = zoom/100F;
		int rmx = (int)((mx - posX)/zs);
		int rmy = (int)((my - posY)/zs);
		
		if(dragging != null)
		{
			if(toolType == 1)
			{
				QuestLineEntry entry = qLine.getEntryByID(dragging.quest.questID);
				
				if(entry != null)
				{
					entry.posX = rmx - scrollX - 12;
					entry.posY = rmy - scrollY - 12;
					entry.posX -= entry.posX%snaps[dragSnap];
					entry.posY -= entry.posY%snaps[dragSnap];
					dragging.xPosition = entry.posX;
					dragging.yPosition = entry.posY;
				}
				
				autoAlign(QuestDatabase.editMode);
			} else if(toolType == 2)
			{
				if(screen.isWithin(mx, my, posX, posY, QuestDatabase.editMode? sizeX - 40 : sizeX, sizeY, false))
				{
					for(GuiButtonQuestInstance b : qBtns)
					{
						if(b != dragging && b.mousePressed(Minecraft.getMinecraft(), rmx, rmy))
						{
							if(!b.parents.contains(dragging) && !b.quest.preRequisites.contains(dragging.quest) && !dragging.parents.contains(b) && !dragging.quest.preRequisites.contains(b.quest))
							{
								b.parents.add(dragging);
								b.quest.preRequisites.add(dragging.quest);
							} else
							{
								b.parents.remove(dragging);
								dragging.parents.remove(b);
								b.quest.preRequisites.remove(dragging.quest);
								dragging.quest.preRequisites.remove(b.quest);
							}
							
							JsonObject json = new JsonObject();
							b.quest.writeToJSON(json);
							NBTTagCompound tags = new NBTTagCompound();
							tags.setInteger("action", 0); // Action: Update data
							tags.setInteger("questID", b.quest.questID);
							tags.setTag("Data", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
							BetterQuesting.instance.network.sendToServer(PacketDataType.QUEST_EDIT.makePacket(tags));
							
							break;
						}
					}
				}
			}
			
			dragging = null;
		} else
		{
			for(GuiButtonQuestInstance b : qBtns)
			{
				if(b.mousePressed(Minecraft.getMinecraft(), rmx, rmy))
				{
					flag = true;
					
					if(toolType != 0)
					{
						dragging = b;
					}
					
					break;
				}
			}
		}
	}
	
	/**
	 * Snaps everything relative to 0,0 and updates max bounds. Will send offset changes server side if told
	 */
	public void autoAlign(boolean applyEdits)
	{
		boolean set = false;
		int xOff = 0;
		int yOff = 0;
		
		for(GuiButtonQuestInstance b : qBtns)
		{
			if(!set)
			{
				xOff = b.xPosition;
				yOff = b.yPosition;
				set = true;
				continue;
			}
			
			if(b.xPosition < xOff)
			{
				xOff = b.xPosition;
			}
			
			if(b.yPosition < yOff)
			{
				yOff = b.yPosition;
			}
		}
		
		maxX = 0;
		maxY = 0;
		
		for(GuiButtonQuestInstance b : qBtns)
		{
			b.xPosition -= xOff;
			b.yPosition -= yOff;
			
			if(b.xPosition + 24 > maxX)
			{
				maxX = b.xPosition + 24;
			}
			
			if(b.yPosition + 24 > maxY)
			{
				maxY = b.yPosition + 24;
			}
			
			if(applyEdits)
			{
				QuestLineEntry entry = qLine.getEntryByID(b.quest.questID);
				
				if(entry != null)
				{
					entry.posX = b.xPosition;
					entry.posY = b.yPosition;
				}
			}
		}
		
		clampScroll();
		
		if(applyEdits)
		{
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("action", 2);
			JsonObject json = new JsonObject();
			QuestDatabase.writeToJson_Lines(json);
			tags.setTag("Data", NBTConverter.JSONtoNBT_Object(json, new NBTTagCompound()));
			BetterQuesting.instance.network.sendToServer(PacketDataType.LINE_EDIT.makePacket(tags));
		}
	}
	
	@Override
	public void mouseScroll(int mx, int my, int SDX)
	{
        if(SDX != 0 && screen.isWithin(mx, my, posX, posY, sizeX, sizeY, false))
        {
        	zoom = MathHelper.clamp_int(zoom - SDX*5, 50, 200);
        	clampScroll();
        }
	}
	
	public void setZoom(int value)
	{
		zoom = MathHelper.clamp_int(zoom, 1, 200);
		clampScroll();
	}
	
	public void setQuestLine(GuiButtonQuestLine qlBtn)
	{
		dragging = null;
		zoom = 100;
		
		if(qlBtn == null)
		{
			this.qLine = null;
			this.qBtns = new ArrayList<GuiButtonQuestInstance>();
		} else
		{
			this.qLine = qlBtn.line;
			this.qBtns = qlBtn.buttonTree;
			
			autoAlign(false);
			
			scrollX = Math.abs(sizeX - maxX)/2;
			scrollY = 16;
		}
	}
	
	@Override
	public void handleMouse()
	{
		super.handleMouse();
        
    	if((Mouse.isButtonDown(0) && !flag) || Mouse.isButtonDown(2))
    	{
			float zs = zoom/100F;
    		scrollX += (Mouse.getEventDX() * screen.width / screen.mc.displayWidth)/zs;
    		scrollY -= (Mouse.getEventDY() * screen.height / screen.mc.displayHeight)/zs;
    		
    		if(dragging == null || toolType != 1)
    		{
    			clampScroll();
    		}
    	}
	}
	
	public void clampScroll()
	{
		float zs = zoom/100F;
		int sx2 = QuestDatabase.editMode? sizeX - 40 : sizeX;
		sx2 /= zs;
		int sy2 = (int)(sizeY/zs);
		int zmx = (int)Math.abs(sx2/2 - (maxX + 32)/2);
		int zmy = (int)Math.abs(sy2/2 - (maxY + 32)/2);
		int zox = sx2/2 - (maxX + 32)/2 + 16;
		int zoy = sy2/2 - (maxY + 32)/2 + 16;
		scrollX = MathHelper.clamp_int(scrollX, -zmx + zox, zmx + zox);
		scrollY = MathHelper.clamp_int(scrollY, -zmy + zoy, zmy + zoy);
	}
}

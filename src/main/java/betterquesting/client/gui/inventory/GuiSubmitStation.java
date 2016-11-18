package betterquesting.client.gui.inventory;

import java.awt.Color;
import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;
import betterquesting.api.client.gui.IGuiEmbedded;
import betterquesting.api.client.gui.controls.GuiButtonThemed;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.quests.IQuest;
import betterquesting.api.quests.tasks.IFluidTask;
import betterquesting.api.quests.tasks.IItemTask;
import betterquesting.api.quests.tasks.ITask;
import betterquesting.blocks.TileSubmitStation;
import betterquesting.database.QuestDatabase;

public class GuiSubmitStation extends GuiContainerThemed implements INeedsRefresh
{
	TileSubmitStation tile;
	ArrayList<IQuest> activeQuests = new ArrayList<IQuest>();
	int selQuest = 0;
	IQuest quest;
	int selTask = 0;
	ITask task;
	
	IGuiEmbedded taskUI;
	GuiButtonThemed btnSelect;
	GuiButtonThemed btnRemove;
	
	ContainerSubmitStation subContainer;
	
	public GuiSubmitStation(GuiScreen parent, InventoryPlayer invo, TileSubmitStation tile)
	{
		super(parent, "betterquesting.title.submit_station", new ContainerSubmitStation(invo, tile));
		subContainer = (ContainerSubmitStation)this.inventorySlots;
		this.tile = tile;
		this.setMaxSize(500, 300);
	}
	
	@SuppressWarnings("unchecked")
	public void initGui()
	{
		super.initGui();
		
		if(tile == null)
		{
			mc.displayGuiScreen(parent);
			return;
		}
		
		selQuest = 0;
		selTask = 0;
		
		activeQuests.clear();
		UUID pID = mc.thePlayer.getGameProfile().getId();
		for(IQuest q : QuestDatabase.INSTANCE.getAllValues())
		{
			if(q.isUnlocked(pID) && !q.isComplete(pID))
			{
				activeQuests.add(q);
			}
		}
		
		buttonList.add(new GuiButtonThemed(1, guiLeft + sizeX/2 - 120, guiTop + 32, 20, 20, "<", true)); // Prev Quest
		buttonList.add(new GuiButtonThemed(2, guiLeft + sizeX/2 + 100, guiTop + 32, 20, 20, ">", true)); // Next Quest
		buttonList.add(new GuiButtonThemed(3, guiLeft + sizeX/2 - 120, guiTop + 52, 20, 20, "<", true)); // Prev Task
		buttonList.add(new GuiButtonThemed(4, guiLeft + sizeX/2 + 100, guiTop + 52, 20, 20, ">", true)); // Next Quest
		btnSelect = new GuiButtonThemed(5, guiLeft + sizeX/2 - 20, guiTop + 72, 20, 20, EnumChatFormatting.GREEN + "\u2714", true); // Select Task
		btnRemove = new GuiButtonThemed(6, guiLeft + sizeX/2 + 00, guiTop + 72, 20, 20, EnumChatFormatting.RED + "x", true); // Remove Task
		buttonList.add(btnSelect);
		buttonList.add(btnRemove);
		
		int invX = 16 + (sizeX/2 - 24)/2 - 162/2;
		int invY = 92 + (sizeY - 108)/2 - 98/2 + 22;
		
		subContainer.moveInventorySlots(invX + 1, invY + 1);
		
		invX += 18*3;
		invY -= 22;
		
		subContainer.moveSubmitSlot(invX + 1, invY + 1);
		
		invX += 18*2;
		
		subContainer.moveReturnSlot(invX + 1, invY + 1);
		
		RefreshValues();
	}
	
	@Override
	public void refreshGui()
	{
		this.initGui();
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTick, int mx, int my)
	{
		mc.renderEngine.bindTexture(currentTheme().getGuiTexture());
		
		int invX = guiLeft + 16 + (sizeX/2 - 24)/2 - 162/2;
		int invY = guiTop + 92 + (sizeY - 108)/2 - 98/2 + 22;
		
		for(int j = 0; j < 3; j++)
		{
			for(int i = 0; i < 9; i++)
			{
				this.drawTexturedModalRect(i*18 + invX, j*18 + invY, 0, 48, 18, 18);
			}
		}
		
		for(int i = 0; i < 9; i++)
		{
			this.drawTexturedModalRect(i*18 + invX, 58 + invY, 0, 48, 18, 18);
		}
		
		invX += 18*3;
		invY -= 22;
		
		this.drawTexturedModalRect(invX, invY, 0, 48, 18, 18);
		
		invX += 18*2;
		
		this.drawTexturedModalRect(invX, invY, 0, 48, 18, 18);
		
		mc.fontRenderer.drawString("-->", invX - 17, invY + 7, Color.BLACK.getRGB(), false);
		mc.fontRenderer.drawString("-->", invX - 17, invY + 6, getTextColor(), false);
		
		GL11.glColor4f(1F, 1F, 1F, 1F);
		
		if(quest != null)
		{
			mc.fontRenderer.drawString(I18n.format(quest.getUnlocalisedName()), guiLeft + sizeX/2 - 92, guiTop + 40, getTextColor(), false);
		}
		
		if(task != null)
		{
			mc.fontRenderer.drawString(I18n.format(task.getUnlocalisedName()), guiLeft + sizeX/2 - 92, guiTop + 60, getTextColor(), false);
		}
		
		/*if(taskUI != null)
		{
			taskUI.drawGui(mx, my, partialTick);
		}*/
	}
	
	@Override
	public void actionPerformed(GuiButton button)
	{
		super.actionPerformed(button);
		
		if(button.id == 1)
		{
			if(activeQuests.size() > 0)
			{
				selQuest = PositiveModulo(selQuest - 1, activeQuests.size());
			} else
			{
				selQuest = 0;
			}
			RefreshValues();
		} else if(button.id == 2)
		{
			if(activeQuests.size() > 0)
			{
				selQuest = PositiveModulo(selQuest + 1, activeQuests.size());
			} else
			{
				selQuest = 0;
			}
			RefreshValues();
		} else if(button.id == 3)
		{
			if(quest != null && quest.getTasks().size() > 0)
			{
				selTask = PositiveModulo(selTask - 1, quest.getTasks().size());
			} else
			{
				selTask = 0;
			}
			RefreshValues();
		} else if(button.id == 4)
		{
			if(quest != null && quest.getTasks().size() > 0)
			{
				selTask = PositiveModulo(selTask + 1, quest.getTasks().size());
			} else
			{
				selTask = 0;
			}
			RefreshValues();
		} else if(button.id == 5) // Select
		{
			tile.setupTask(mc.thePlayer.getGameProfile().getId(), quest, task);
			tile.SyncTile(null);
			RefreshValues();
		} else if(button.id == 6)
		{
			tile.reset();
			tile.SyncTile(null);
			RefreshValues();
		}
	}
	
	public void RefreshValues()
	{
		embedded.remove(taskUI);
		
		if(tile.getRawTask() != null)
		{
			quest = tile.getQuest();
			task = tile.getRawTask();
			taskUI = task.getTaskGui(guiLeft + sizeX/2 + 8, guiTop + 92, sizeX/2 - 24, sizeY - 92 - 16, quest);
			
			if(taskUI != null)
			{
				embedded.add(taskUI);
			}
			
			selQuest = Math.max(0, activeQuests.indexOf(quest));
			selTask = Math.max(0, quest.getTasks().getAllValues().indexOf(task));
			
			for(int i = 1; i <= 4; i++)
			{
				((GuiButton)buttonList.get(i)).enabled = false; // Cannot change tile until old values are removed
			}
			
			btnSelect.enabled = false;
			btnRemove.enabled = true;
			return;
		} else
		{
			for(int i = 1; i <= 4; i++)
			{
				((GuiButton)buttonList.get(i)).enabled = true;
			}
			
			btnRemove.enabled = false;
		}
		
		if(selQuest < 0 || selQuest >= activeQuests.size())
		{
			quest = null;
			task = null;
		} else
		{
			quest = activeQuests.get(selQuest);
		}
		
		if(quest == null || selTask < 0 || selTask >= quest.getTasks().size())
		{
			task = null;
		} else
		{
			task = quest.getTasks().getAllValues().get(selTask);
		}
		
		if(task == null)
		{
			taskUI = null;
		} else
		{
			taskUI = task.getTaskGui(guiLeft + sizeX/2 + 8, guiTop + 92, sizeX/2 - 24, sizeY - 92 - 16, quest);
			
			if(taskUI != null)
			{
				embedded.add(taskUI);
			}
		}
		
		btnSelect.enabled = task != null && (task instanceof IFluidTask && task instanceof IItemTask) && !task.isComplete(mc.thePlayer.getGameProfile().getId());
	}
	
	/**
	 * Performs a modulo operation correcting negatives to positives
	 */
	int PositiveModulo(int value, int mod) // Shortcut method because I'm lazy
	{
		return (value%mod + mod)%mod;
	}
}

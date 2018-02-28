package betterquesting.client.gui2;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumQuestState;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineEntry;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.IGuiPanel;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.lines.IGuiLine;
import betterquesting.api2.client.gui.resources.textures.GuiTextureColored;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.resources.textures.ItemTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetLine;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.questing.QuestDatabase;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.UUID;

/**
 * My class for lazy quest line setup on a scrolling canvas
 */
public class CanvasQuestLine extends CanvasScrolling
{
    private final int buttonId;
    
    public CanvasQuestLine(IGuiRect rect, int buttonId)
    {
        super(rect);
        this.setupAdvanceScroll(true, true, 24);
        
        this.buttonId = buttonId;
    }
    
    /**
     * Loads in quests and connecting lines
     * @param line The quest line to load
     */
    public void setQuestLine(IQuestLine line)
    {
        // Rest contents
        this.getAllPanels().clear();
        
        if(line == null)
        {
            return;
        }
        
        UUID pid = QuestingAPI.getQuestingUUID(Minecraft.getMinecraft().player);
        
        // TODO: Load background
        
        
        // TODO: Store list of panels incase we need to link them with lines
        //HashMap<Integer, IGuiPanel> pendingLines = new HashMap<int, IGuiPanel>();
        
        for(IQuestLineEntry qle : line.getAllValues())
        {
            int id = line.getKey(qle);
            IQuest quest = QuestDatabase.INSTANCE.getValue(id);
            
            if(quest == null)
            {
                continue;
            }
    
            EnumQuestState qState = quest.getState(pid);
            IGuiTexture txFrame = null;
            IGuiLine lineRender = null;
            IGuiColor txLineCol = null;
            IGuiColor txIconCol = null;
            
            if(quest.getProperties().getProperty(NativeProps.MAIN))
            {
                switch(qState)
                {
                    case LOCKED:
                        txFrame = PresetTexture.QUEST_MAIN_0.getTexture();
                        break;
                    case UNLOCKED:
                        txFrame = PresetTexture.QUEST_MAIN_1.getTexture();
                        break;
                    case UNCLAIMED:
                        txFrame = PresetTexture.QUEST_MAIN_2.getTexture();
                        break;
                    case COMPLETED:
                        txFrame = PresetTexture.QUEST_MAIN_3.getTexture();
                        break;
                }
            } else
            {
                switch(qState)
                {
                    case LOCKED:
                        txFrame = PresetTexture.QUEST_NORM_0.getTexture();
                        break;
                    case UNLOCKED:
                        txFrame = PresetTexture.QUEST_NORM_1.getTexture();
                        break;
                    case UNCLAIMED:
                        txFrame = PresetTexture.QUEST_NORM_2.getTexture();
                        break;
                    case COMPLETED:
                        txFrame = PresetTexture.QUEST_NORM_3.getTexture();
                        break;
                }
            }
            switch(qState)
            {
                case LOCKED:
                    lineRender = PresetLine.QUEST_LOCKED.getLine();
                    txLineCol = PresetColor.QUEST_LINE_LOCKED.getColor();
                    txIconCol = PresetColor.QUEST_ICON_LOCKED.getColor();
                    break;
                case UNLOCKED:
                    lineRender = PresetLine.QUEST_UNLOCKED.getLine();
                    txLineCol = PresetColor.QUEST_LINE_UNLOCKED.getColor();
                    txIconCol = PresetColor.QUEST_ICON_UNLOCKED.getColor();
                    break;
                case UNCLAIMED:
                    lineRender = PresetLine.QUEST_PENDING.getLine();
                    txLineCol = PresetColor.QUEST_LINE_PENDING.getColor();
                    txIconCol = PresetColor.QUEST_ICON_PENDING.getColor();
                    break;
                case COMPLETED:
                    lineRender = PresetLine.QUEST_COMPLETE.getLine();
                    txLineCol = PresetColor.QUEST_LINE_COMPLETE.getColor();
                    txIconCol = PresetColor.QUEST_ICON_COMPLETE.getColor();
                    break;
            }
            
            IGuiRect rect = new GuiRectangle(qle.getPosX(), qle.getPosY(), qle.getSize(), qle.getSize());
            PanelButtonStorage<IQuest> paBtn = new PanelButtonStorage<IQuest>(rect, buttonId, "", quest);
            paBtn.setTextures(new GuiTextureColored(txFrame, txIconCol), new GuiTextureColored(txFrame, txIconCol), new GuiTextureColored(txFrame, txIconCol));
            paBtn.setIcon(new ItemTexture(quest.getItemIcon()), 4);
            // Add to pending lines
            
            this.addPanel(paBtn);
        }
    }
}

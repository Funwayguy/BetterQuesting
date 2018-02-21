package betterquesting.client.gui2;

import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api2.client.gui.controls.IPanelButton;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.questing.QuestDatabase;

import java.util.List;
import java.util.UUID;

public class PanelQuestButton implements IPanelButton
{
    private final IGuiRect transform;
    
    private final IQuest quest;
    private final UUID user;
    
    private PresetColor iconCol;
    private IGuiTexture iconTex;
    
    public PanelQuestButton(IGuiRect rect, int id, int questID, UUID user)
    {
        this.transform = rect;
        this.quest = QuestDatabase.INSTANCE.getValue(questID);
        this.user = user;
    }
    
    @Override
    public IGuiRect getTransform()
    {
        return transform;
    }
    
    @Override
    public void initPanel()
    {
        boolean main = quest.getProperties().getProperty(NativeProps.MAIN);
        
        if(!quest.isUnlocked(user))
        {
            iconTex = main ? PresetTexture.QUEST_MAIN_0.getTexture() : PresetTexture.QUEST_NORM_0.getTexture();
            iconCol = PresetColor.QUEST_ICON_LOCKED;
        } else if(!quest.isComplete(user))
        {
            iconTex = main ? PresetTexture.QUEST_MAIN_1.getTexture() : PresetTexture.QUEST_NORM_1.getTexture();
            iconCol = PresetColor.QUEST_ICON_LOCKED;
        } else if(!quest.hasClaimed(user))
        {
            iconTex = main ? PresetTexture.QUEST_MAIN_2.getTexture() : PresetTexture.QUEST_NORM_2.getTexture();
            iconCol = PresetColor.QUEST_ICON_LOCKED;
        } else
        {
            iconTex = main ? PresetTexture.QUEST_MAIN_3.getTexture() : PresetTexture.QUEST_NORM_3.getTexture();
            iconCol = PresetColor.QUEST_ICON_LOCKED;
        }
    }
    
    @Override
    public void drawPanel(int mx, int my, float partialTick)
    {
    
    }
    
    @Override
    public boolean onMouseClick(int mx, int my, int button)
    {
        return false;
    }
    
    @Override
    public boolean onMouseRelease(int mx, int my, int button)
    {
        return false;
    }
    
    @Override
    public boolean onMouseScroll(int mx, int my, int scroll)
    {
        return false;
    }
    
    @Override
    public boolean onKeyTyped(char c, int keycode)
    {
        return false;
    }
    
    @Override
    public List<String> getTooltip(int mx, int my)
    {
        return null;
    }
    
    @Override
    public int getButtonID()
    {
        return 0;
    }
    
    @Override
    public boolean isEnabled()
    {
        return false;
    }
    
    @Override
    public void setEnabled(boolean enable)
    {
    
    }
}

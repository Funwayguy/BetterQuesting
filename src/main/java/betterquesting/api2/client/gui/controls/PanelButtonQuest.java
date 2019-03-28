package betterquesting.api2.client.gui.controls;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumQuestState;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.textures.GuiTextureColored;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.resources.textures.OreDictTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.storage.DBEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;

import java.util.Collections;

public class PanelButtonQuest extends PanelButtonStorage<DBEntry<IQuest>>
{
    public final GuiRectangle rect;
    
    public PanelButtonQuest(GuiRectangle rect, int id, String txt, DBEntry<IQuest> value)
    {
        super(rect, id, txt, value);
        this.rect = rect;
        
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        EnumQuestState qState = value == null ? EnumQuestState.LOCKED : value.getValue().getState(QuestingAPI.getQuestingUUID(player));
        IGuiTexture txFrame = null;
        IGuiColor txIconCol = null;
        boolean main = value == null ? false : value.getValue().getProperty(NativeProps.MAIN);
        boolean lock = false;
        
        switch(qState)
        {
            case LOCKED:
                txFrame = main ? PresetTexture.QUEST_MAIN_0.getTexture() : PresetTexture.QUEST_NORM_0.getTexture();
                txIconCol = PresetColor.QUEST_ICON_LOCKED.getColor();
                lock = true;
                break;
            case UNLOCKED:
                txFrame = main ? PresetTexture.QUEST_MAIN_1.getTexture() : PresetTexture.QUEST_NORM_1.getTexture();
                txIconCol = PresetColor.QUEST_ICON_UNLOCKED.getColor();
                break;
            case UNCLAIMED:
                txFrame = main ? PresetTexture.QUEST_MAIN_2.getTexture() : PresetTexture.QUEST_NORM_2.getTexture();
                txIconCol = PresetColor.QUEST_ICON_PENDING.getColor();
                break;
            case COMPLETED:
                txFrame = main ? PresetTexture.QUEST_MAIN_3.getTexture() : PresetTexture.QUEST_NORM_3.getTexture();
                txIconCol = PresetColor.QUEST_ICON_COMPLETE.getColor();
                break;
        }
        
        IGuiTexture btnTx = new GuiTextureColored(txFrame, txIconCol);
        setTextures(btnTx, btnTx, btnTx);
        setIcon(new OreDictTexture(1F, value == null ? new BigItemStack(Items.nether_star) : value.getValue().getProperty(NativeProps.ICON), false, true), 4);
        setTooltip(value == null ? Collections.emptyList() : value.getValue().getTooltip(player));
        setActive(QuestingAPI.getAPI(ApiReference.SETTINGS).canUserEdit(player) || !lock);
    }
}

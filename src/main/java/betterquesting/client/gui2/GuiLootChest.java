package betterquesting.client.gui2;

import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelItemSlot;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.client.themes.BQSTextures;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

import java.util.List;

public class GuiLootChest extends GuiScreenCanvas
{
    private final String title;
    private final List<BigItemStack> rewards;
    
    public GuiLootChest(GuiScreen parent, List<BigItemStack> rewards, String title)
    {
        super(parent);
        this.rewards = rewards;
        this.title = title;
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
		
		mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(new SoundEvent(new ResourceLocation("random.chestopen")), 1.0F));
    
        this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.MID_CENTER, -64, 0, 128, 68, 0), BQSTextures.LOOT_CHEST.getTexture()));
        this.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.MID_CENTER, -64, 40, 128, 56, -1), QuestTranslation.translate(title)).setAlignment(1));
    
        IGuiTexture texGlow = BQSTextures.LOOT_GLOW.getTexture();
        int rowMax = (int)Math.max(1D, Math.ceil(rewards.size()/8D));
        rowMax = rewards.size()/rowMax;
        
        for(int i = 0; i < rewards.size(); i++)
        {
            BigItemStack stack = rewards.get(i);
            
            int rowX = i%rowMax;
            int rowY = i/rowMax;
            int rowSize = Math.min(rewards.size() - rowY*rowMax, rowMax);
            
            rowX = -(rowSize * 36)/2 + (rowX * 36);
            rowY = -36 - (rowY * 36);
            
            this.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.MID_CENTER, rowX + 2, rowY + 2, 32, 32, 0), texGlow));
            this.addPanel(new PanelItemSlot(new GuiTransform(GuiAlign.MID_CENTER, rowX + 10, rowY + 10, 16, 16, -1), -1, stack).setTextures(null, null, null));
        }
    }
}

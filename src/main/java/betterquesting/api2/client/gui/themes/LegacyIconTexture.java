package betterquesting.api2.client.gui.themes;

import betterquesting.api.client.themes.ITheme;
import betterquesting.api.enums.EnumQuestState;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.misc.DummyQuest;
import betterquesting.misc.DummyQuest.IMainQuery;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public class LegacyIconTexture implements IGuiTexture, IMainQuery
{
    private static final UUID dummyID = UUID.randomUUID();
    
	private final ITheme oldTheme;
	private final DummyQuest dummyQuest;
	private final boolean isMain;
	
	public LegacyIconTexture(ITheme theme, EnumQuestState state, boolean main)
    {
        this.oldTheme = theme;
        this.isMain = main;
        this.dummyQuest = new DummyQuest(state).setMainCallback(this);
    }
	
    @Override
    public void drawTexture(int x, int y, int width, int height, float zDepth, float partialTick)
    {
        drawTexture(x, y, width, height, zDepth, partialTick, null);
    }
    
    @Override
    public void drawTexture(int x, int y, int width, int height, float zDepth, float partialTick, IGuiColor color)
    {
        oldTheme.getRenderer().drawIcon(dummyQuest, dummyID, x, y, width, height, x, y, partialTick);
    }
    
    @Override
    public ResourceLocation getTexture()
    {
        return null;
    }
    
    @Override
    public IGuiRect getBounds()
    {
        return null;
    }
    
    @Override
    public Boolean getMain()
    {
        return isMain;
    }
}

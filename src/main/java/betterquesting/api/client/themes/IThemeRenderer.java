package betterquesting.api.client.themes;

import betterquesting.api.questing.IQuest;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.UUID;

@Deprecated
@SideOnly(Side.CLIENT)
public interface IThemeRenderer
{
	void drawLine(@Nullable IQuest quest, @Nullable UUID playerID, float x1, float y1, float x2, float y2, int mx, int my, float partialTick);
	void drawIcon(@Nullable IQuest quest, @Nullable UUID playerID, float px, float py, float sx, float sy, int mx, int my, float partialTick);
	
	void drawThemedPanel(int x, int y, int w, int h);
}

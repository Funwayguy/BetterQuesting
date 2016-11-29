package betterquesting.api.client.themes;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import betterquesting.api.questing.IQuest;

@SideOnly(Side.CLIENT)
public interface IThemeRenderer
{
	public void drawLine(@Nullable IQuest quest, @Nullable UUID playerID, float x1, float y1, float x2, float y2, int mx, int my, float partialTick);
	public void drawIcon(@Nullable IQuest quest, @Nullable UUID playerID, float px, float py, float sx, float sy, int mx, int my, float partialTick);
}

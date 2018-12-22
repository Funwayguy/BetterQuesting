package betterquesting.api.client.gui.misc;

/**
 * Used for nested GUI panels
 */
@Deprecated
public interface IGuiEmbedded
{
	void drawBackground(int mx, int my, float partialTick);
	void drawForeground(int mx, int my, float partialTick);
	
	void onMouseClick(int mx, int my, int click);
	default void onMouseRelease(int mx, int my, int click) {}
	
	void onMouseScroll(int mx, int my, int scroll);
	void onKeyTyped(char c, int keyCode);
}
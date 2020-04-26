package betterquesting.api2.client.gui.panels;

import betterquesting.api2.client.gui.misc.IGuiRect;

import javax.annotation.Nullable;
import java.util.List;

public interface IGuiPanel
{
	IGuiRect getTransform();
	
	void initPanel();
	void setEnabled(boolean state);
	boolean isEnabled();
	
	void drawPanel(int mx, int my, float partialTick);
	
	default boolean onMouseClick(int mx, int my, int button) {return false; }
	default boolean onMouseRelease(int mx, int my, int button) {return false; }
	default boolean onMouseScroll(int mx, int my, int scroll) {return false; }
	
	default boolean onKeyPressed(int keycode, int scancode, int modifiers) { return false; }
	default boolean onKeyRelease(int keycode, int scancode, int modifiers) { return false; }
	default boolean onCharTyped(char c, int keycode) { return false; }
	
	@Nullable
	default List<String> getTooltip(int mx, int my) { return null; }
}

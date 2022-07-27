package betterquesting.api.client.gui;

import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;

/**
 * Variation of GuiYesNo that prevents users from using escape to skip the dialog
 */
public class GuiYesNoLocked extends GuiYesNo {
    public GuiYesNoLocked(GuiYesNoCallback callback, String txt1, String txt2, int id) {
        super(callback, txt1, txt2, id);
    }

    public GuiYesNoLocked(
            GuiYesNoCallback callback, String txt1, String txt2, String txtConfirm, String txtCancel, int id) {
        super(callback, txt1, txt2, txtConfirm, txtCancel, id);
    }

    /**
     * Disables escaping
     */
    @Override
    protected void keyTyped(char character, int keyCode) {}
}

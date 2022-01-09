package betterquesting.client.gui2;

import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.misc.*;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelLine;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.resources.colors.GuiColorSequence;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.lines.GuiLineSequence;
import betterquesting.api2.client.gui.resources.lines.IGuiLine;
import betterquesting.api2.client.gui.resources.textures.GuiTextureColored;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.resources.textures.ItemTexture;
import betterquesting.api2.client.gui.resources.textures.SlideShowTexture;
import betterquesting.api2.client.gui.themes.IGuiTheme;
import betterquesting.api2.client.gui.themes.gui_args.GArgsNone;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetGUIs;
import betterquesting.api2.client.gui.themes.presets.PresetLine;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.client.themes.ThemeRegistry;
import betterquesting.core.BetterQuesting;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.util.vector.Vector4f;

import java.util.List;

public class GuiThemes extends GuiScreenCanvas
{
	// Last value of the scrollbar before loading new theme
	private PanelVScrollBar scrollPanel;
	
	public GuiThemes(GuiScreen parent)
	{
		super(parent);
	}
	
	@Override
	public void initPanel()
	{
		super.initPanel();
		
		// Background panel
		CanvasTextured bgCan = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0), PresetTexture.PANEL_MAIN.getTexture());
		this.addPanel(bgCan);
		
		// Inner canvas bounds
		CanvasEmpty inCan = new CanvasEmpty(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(16, 16, 16, 16), 0));
		bgCan.addPanel(inCan); 
		
		PanelTextBox panTxt = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 0, 0, -16), 0), QuestTranslation.translate("betterquesting.title.select_theme")).setAlignment(1);
		panTxt.setColor(PresetColor.TEXT_HEADER.getColor());
		inCan.addPanel(panTxt);
		
		PanelButton btnExit = new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, new GuiPadding(-100, -16, -100, 0), 0), 0, QuestTranslation.translate("gui.done"));
		btnExit.setClickAction((b) -> mc.displayGuiScreen(ThemeRegistry.INSTANCE.getGui(PresetGUIs.HOME, GArgsNone.NONE)));
		bgCan.addPanel(btnExit);
		
		CanvasScrolling canScroll = new CanvasScrolling(new GuiTransform(GuiAlign.HALF_LEFT, new GuiPadding(0, 16, 16, 16), 0));
		inCan.addPanel(canScroll);
		
		ThemeRegistry.INSTANCE.loadResourceThemes();
		List<IGuiTheme> themes = ThemeRegistry.INSTANCE.getAllThemes();
		int width = canScroll.getTransform().getWidth();
		
		IGuiTheme curTheme = ThemeRegistry.INSTANCE.getCurrentTheme();
		
		for(int i = 0; i < themes.size(); i++)
		{
		    IGuiTheme theme = themes.get(i);
			GuiRectangle trans = new GuiRectangle(0, i * 24, width, 24, 0);
			PanelButtonStorage<ResourceLocation> pbs = new PanelButtonStorage<>(trans, -1, theme.getName(), theme.getID());
			pbs.setCallback((res) -> {
			    float scroll = scrollPanel.readValueRaw();
                ThemeRegistry.INSTANCE.setTheme(res);
                this.initGui();
                scrollPanel.writeValueRaw(scroll);
            });
			canScroll.addPanel(pbs);
			pbs.setActive(curTheme == null || !curTheme.getID().equals(theme.getID()));
		}
		
		PanelVScrollBar vsb = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(0, 0, -8, 0), 0));
		inCan.addPanel(vsb);
		vsb.getTransform().setParent(canScroll.getTransform());
		canScroll.setScrollDriverY(vsb);
		
		scrollPanel = vsb;
		
		// === PREVIEW PANELS ===
		
		CanvasEmpty preCan = new CanvasEmpty(new GuiTransform(GuiAlign.HALF_RIGHT, new GuiPadding(8, 16, 0, 16), 0));
		inCan.addPanel(preCan);
		
		CanvasTextured preCanIn0 = new CanvasTextured(new GuiTransform(new Vector4f(0F, 0F, 0.5F, 0.5F), new GuiPadding(0, 0, 0, 0), 0), PresetTexture.PANEL_MAIN.getTexture());
		preCan.addPanel(preCanIn0);
		
		preCanIn0.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.MID_CENTER, -32, -8, 64, 16, 0), "EXAMPLE").setAlignment(1).setColor(PresetColor.TEXT_MAIN.getColor()));
		
		CanvasTextured preCanIn1 = new CanvasTextured(new GuiTransform(new Vector4f(0.5F, 0F, 1F, 0.5F), new GuiPadding(0, 0, 0, 0), 0), PresetTexture.PANEL_INNER.getTexture());
		preCan.addPanel(preCanIn1);
		
		preCanIn1.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.MID_CENTER, -32, -8, 64, 16, 0), "EXAMPLE").setAlignment(1).setColor(PresetColor.TEXT_AUX_0.getColor()));
		
		CanvasTextured preCanIn2 = new CanvasTextured(new GuiTransform(GuiAlign.HALF_BOTTOM, new GuiPadding(0, 0, 0, 0), 0), PresetTexture.AUX_FRAME_0.getTexture());
		preCan.addPanel(preCanIn2);
		
		IGuiTexture icoSlides = new SlideShowTexture(1F,
				new GuiTextureColored(PresetTexture.QUEST_NORM_0.getTexture(), PresetColor.QUEST_ICON_LOCKED.getColor()),
				new GuiTextureColored(PresetTexture.QUEST_NORM_1.getTexture(), PresetColor.QUEST_ICON_UNLOCKED.getColor()),
				new GuiTextureColored(PresetTexture.QUEST_NORM_2.getTexture(), PresetColor.QUEST_ICON_PENDING.getColor()),
				new GuiTextureColored(PresetTexture.QUEST_NORM_3.getTexture(), PresetColor.QUEST_ICON_COMPLETE.getColor()),
				new GuiTextureColored(PresetTexture.QUEST_NORM_4.getTexture(), PresetColor.QUEST_ICON_REPEATABLE.getColor()),
				new GuiTextureColored(PresetTexture.QUEST_MAIN_0.getTexture(), PresetColor.QUEST_ICON_LOCKED.getColor()),
				new GuiTextureColored(PresetTexture.QUEST_MAIN_1.getTexture(), PresetColor.QUEST_ICON_UNLOCKED.getColor()),
				new GuiTextureColored(PresetTexture.QUEST_MAIN_2.getTexture(), PresetColor.QUEST_ICON_PENDING.getColor()),
				new GuiTextureColored(PresetTexture.QUEST_MAIN_3.getTexture(), PresetColor.QUEST_ICON_COMPLETE.getColor()),
				new GuiTextureColored(PresetTexture.QUEST_MAIN_4.getTexture(), PresetColor.QUEST_ICON_REPEATABLE.getColor()),
				new GuiTextureColored(PresetTexture.QUEST_AUX_0.getTexture(), PresetColor.QUEST_ICON_LOCKED.getColor()),
				new GuiTextureColored(PresetTexture.QUEST_AUX_1.getTexture(), PresetColor.QUEST_ICON_UNLOCKED.getColor()),
				new GuiTextureColored(PresetTexture.QUEST_AUX_2.getTexture(), PresetColor.QUEST_ICON_PENDING.getColor()),
				new GuiTextureColored(PresetTexture.QUEST_AUX_3.getTexture(), PresetColor.QUEST_ICON_COMPLETE.getColor()),
				new GuiTextureColored(PresetTexture.QUEST_AUX_4.getTexture(), PresetColor.QUEST_ICON_REPEATABLE.getColor()));
		PanelGeneric pqp = new PanelGeneric(new GuiTransform(new Vector4f(0.25F, 0.5F, 0.25F, 0.5F), -12, -12, 24, 24, 0), icoSlides);
		preCanIn2.addPanel(pqp);
		
		CanvasTextured itemFrame = new CanvasTextured(new GuiTransform(new Vector4f(0.75F, 0.5F, 0.75F, 0.5F), -12, -12, 24, 24, 0), PresetTexture.ITEM_FRAME.getTexture());
		preCanIn2.addPanel(itemFrame);
		
		itemFrame.addPanel(new PanelGeneric(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(1, 1, 1, 1), 0), new ItemTexture(new BigItemStack(BetterQuesting.guideBook, 9999, 0), true, true)));
		
		IGuiLine linSeq = new GuiLineSequence(1F, PresetLine.QUEST_LOCKED.getLine(), PresetLine.QUEST_UNLOCKED.getLine(), PresetLine.QUEST_PENDING.getLine(), PresetLine.QUEST_COMPLETE.getLine());
		IGuiColor colSeq = new GuiColorSequence(1F, PresetColor.QUEST_LINE_LOCKED.getColor(), PresetColor.QUEST_LINE_UNLOCKED.getColor(), PresetColor.QUEST_LINE_PENDING.getColor(), PresetColor.QUEST_LINE_COMPLETE.getColor());
		preCanIn2.addPanel(new PanelLine(pqp.getTransform(), itemFrame.getTransform(), linSeq, 4, colSeq, 1));
		
		preCanIn2.addPanel(new PanelTextBox(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(8, 8, 8, 8), 0), "EXAMPLE").setAlignment(1).setColor(PresetColor.TEXT_AUX_1.getColor()));
		
		IGuiRect ls0 = new GuiTransform(GuiAlign.TOP_CENTER, 0, 16, 0, 0, 0);
		ls0.setParent(inCan.getTransform());
		IGuiRect le0 = new GuiTransform(GuiAlign.BOTTOM_CENTER, 0, -16, 0, 0, 0);
		le0.setParent(inCan.getTransform());
		PanelLine paLine0 = new PanelLine(ls0, le0, PresetLine.GUI_DIVIDER.getLine(), 1, PresetColor.GUI_DIVIDER.getColor(), 1);
		inCan.addPanel(paLine0);
	}
}

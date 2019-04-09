package betterquesting.client.toolbox;

import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.resources.colors.GuiColorStatic;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.themes.presets.PresetIcon;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.api2.client.gui.panels.lists.CanvasQuestLine;
import betterquesting.client.gui2.editors.designer.PanelToolController;
import betterquesting.client.gui2.editors.nbt.GuiNbtEditor;
import betterquesting.client.toolbox.tools.*;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

public class PanelTabMain extends CanvasEmpty
{
    private final CanvasQuestLine cvQuestLine;
    private final PanelToolController toolController;
    
    private static final List<ToolEntry> toolEntries = new ArrayList<>();
    
    public PanelTabMain(IGuiRect rect, CanvasQuestLine cvQuestLine, PanelToolController toolController)
    {
        super(rect);
        this.cvQuestLine = cvQuestLine;
        this.toolController = toolController;
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
        int w = getTransform().getWidth();
    
        IGuiColor tCol = new GuiColorStatic(0xFF000000);
        this.addPanel(new PanelButton(new GuiRectangle(0, 0, w / 2, 16, 0), -1, "" + ToolboxTabMain.INSTANCE.getSnapValue())
        {
            @Override
            public void onButtonClick()
            {
                ToolboxTabMain.INSTANCE.toggleSnap();
                this.setText("" + ToolboxTabMain.INSTANCE.getSnapValue());
            }
        }.setIcon(PresetIcon.ICON_GRID.getTexture()).setTextShadow(false).setTextHighlight(tCol, tCol, tCol).setTooltip(makeToolTip(QuestTranslation.translate("betterquesting.toolbox.tool.snap.name"), QuestTranslation.translate("betterquesting.toolbox.tool.snap.desc"))));
        
        this.addPanel(new PanelButton(new GuiRectangle(w / 2, 0, w / 2, 16, 0), -1, "")
        {
            @Override
            public void onButtonClick()
            {
                Minecraft mc = Minecraft.getMinecraft();
                mc.displayGuiScreen(new GuiNbtEditor(mc.currentScreen, cvQuestLine.getQuestLine().writeToNBT(new NBTTagCompound(), null), value -> {
                    NBTTagCompound tag2 = new NBTTagCompound();
                    NBTTagCompound base2 = new NBTTagCompound();
                    base2.setTag("line", value);
                    tag2.setTag("data", base2);
                    tag2.setInteger("action", EnumPacketAction.EDIT.ordinal());
                    tag2.setInteger("lineID", QuestLineDatabase.INSTANCE.getID(cvQuestLine.getQuestLine()));
                    PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.LINE_EDIT.GetLocation(), tag2));
                }));
            }
        }.setIcon(PresetIcon.ICON_PROPS.getTexture()).setTooltip(makeToolTip(QuestTranslation.translate("betterquesting.toolbox.tool.raw.name"), QuestTranslation.translate("betterquesting.toolbox.tool.raw.desc"))));
        
        final List<PanelButtonStorage<IToolboxTool>> toolBtns = new ArrayList<>();
        
        for(int i = 0; i < toolEntries.size(); i++)
        {
            ToolEntry entry = toolEntries.get(i);
            int x = (i % 2) * (w / 2);
            int y = (i / 2) * 16 + 24;
            PanelButtonStorage<IToolboxTool> btn = new PanelButtonStorage<>(new GuiRectangle(x, y, w / 2, 16, 0), -1, "", entry.tool);
            btn.setActive(toolController.getActiveTool() != entry.tool);
            btn.setIcon(entry.tex).setTooltip(entry.tt);
            btn.setCallback(value -> {
                toolController.setActiveTool(value);
                toolBtns.forEach(b -> b.setActive(true));
                btn.setActive(false);
            });
            toolBtns.add(btn);
            this.addPanel(btn);
            
            if(entry.tool instanceof ToolboxToolOpen && toolController.getActiveTool() == null)
            {
                toolController.setActiveTool(entry.tool);
                btn.setActive(false);
            }
        }
    }
	
	private static List<String> makeToolTip(String title, String desc)
	{
		List<String> list = new ArrayList<>();
		list.add(title);
		list.addAll(Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(TextFormatting.GRAY + desc, 128));
		return list;
	}
    
    private static class ToolEntry
    {
        private final IToolboxTool tool;
        private final IGuiTexture tex;
        private final List<String> tt;
        
        private ToolEntry(IToolboxTool tool, IGuiTexture tex, List<String> tt)
        {
            this.tool = tool;
            this.tex = tex;
            this.tt = tt;
        }
    }
    
    static
    {
        toolEntries.add(new ToolEntry(new ToolboxToolOpen(), PresetIcon.ICON_CURSOR.getTexture(), makeToolTip(QuestTranslation.translate("betterquesting.toolbox.tool.open.name"), QuestTranslation.translate("betterquesting.toolbox.tool.open.desc"))));
        toolEntries.add(new ToolEntry(new ToolboxToolNew(), PresetIcon.ICON_NEW.getTexture(), makeToolTip(QuestTranslation.translate("betterquesting.toolbox.tool.new.name"), QuestTranslation.translate("betterquesting.toolbox.tool.new.desc"))));
        toolEntries.add(new ToolEntry(new ToolboxToolGrab(), PresetIcon.ICON_GRAB.getTexture(), makeToolTip(QuestTranslation.translate("betterquesting.toolbox.tool.grab.name"), QuestTranslation.translate("betterquesting.toolbox.tool.grab.desc"))));
        toolEntries.add(new ToolEntry(new ToolboxToolLink(), PresetIcon.ICON_LINK.getTexture(), makeToolTip(QuestTranslation.translate("betterquesting.toolbox.tool.link.name"), QuestTranslation.translate("betterquesting.toolbox.tool.link.desc"))));
        toolEntries.add(new ToolEntry(new ToolboxToolCopy(), PresetIcon.ICON_COPY.getTexture(), makeToolTip(QuestTranslation.translate("betterquesting.toolbox.tool.copy.name"), QuestTranslation.translate("betterquesting.toolbox.tool.copy.desc"))));
        toolEntries.add(new ToolEntry(new ToolboxToolScale(), PresetIcon.ICON_SCALE.getTexture(), makeToolTip(QuestTranslation.translate("betterquesting.toolbox.tool.scale.name"), QuestTranslation.translate("betterquesting.toolbox.tool.scale.desc"))));
        toolEntries.add(new ToolEntry(new ToolboxToolDelete(), PresetIcon.ICON_TRASH.getTexture(), makeToolTip(QuestTranslation.translate("betterquesting.toolbox.tool.delete.name"), QuestTranslation.translate("betterquesting.toolbox.tool.delete.desc"))));
        toolEntries.add(new ToolEntry(new ToolboxToolRemove(), PresetIcon.ICON_NEGATIVE.getTexture(), makeToolTip(QuestTranslation.translate("betterquesting.toolbox.tool.remove.name"), QuestTranslation.translate("betterquesting.toolbox.tool.remove.desc"))));
        toolEntries.add(new ToolEntry(new ToolboxToolComplete(), PresetIcon.ICON_TICK.getTexture(), makeToolTip(QuestTranslation.translate("betterquesting.toolbox.tool.complete.name"), QuestTranslation.translate("betterquesting.toolbox.tool.complete.desc"))));
        toolEntries.add(new ToolEntry(new ToolboxToolReset(), PresetIcon.ICON_REFRESH.getTexture(), makeToolTip(QuestTranslation.translate("betterquesting.toolbox.tool.reset.name"), QuestTranslation.translate("betterquesting.toolbox.tool.reset.desc"))));
        toolEntries.add(new ToolEntry(new ToolboxToolIcon(), PresetIcon.ICON_ITEM.getTexture(), makeToolTip(QuestTranslation.translate("betterquesting.toolbox.tool.icon.name"), QuestTranslation.translate("betterquesting.toolbox.tool.icon.desc"))));
        toolEntries.add(new ToolEntry(new ToolboxToolFrame(), PresetIcon.ICON_NOTICE.getTexture(), makeToolTip(QuestTranslation.translate("betterquesting.toolbox.tool.frame.name"), QuestTranslation.translate("betterquesting.toolbox.tool.frame.desc"))));
    }
}

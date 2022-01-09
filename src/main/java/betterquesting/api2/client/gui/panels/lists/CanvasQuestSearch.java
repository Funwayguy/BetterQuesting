package betterquesting.api2.client.gui.panels.lists;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineEntry;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.client.gui.controls.PanelButtonCustom;
import betterquesting.api2.client.gui.controls.PanelButtonQuest;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.resources.textures.OreDictTexture;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.misc.QuestSearchEntry;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CanvasQuestSearch extends CanvasSearch<QuestSearchEntry, QuestSearchEntry> {
    private List<QuestSearchEntry> questList;
    private Consumer<QuestSearchEntry> questOpenCallback;
    private Consumer<QuestSearchEntry> questHighlightCallback;
    private EntityPlayer player;
    private final UUID questingUUID;

    public CanvasQuestSearch(IGuiRect rect, EntityPlayer player) {
        super(rect);
        this.player = player;
        questingUUID = QuestingAPI.getQuestingUUID(player);
    }

    @Override
    protected Iterator<QuestSearchEntry> getIterator() {
        if (questList != null)
            return questList.iterator();
        questList = collectQuests();
        return questList.iterator();
    }

    private List<QuestSearchEntry> collectQuests() {
        return QuestLineDatabase.INSTANCE.getEntries().stream().flatMap(iQuestLineDBEntry ->
                iQuestLineDBEntry.getValue().getEntries().stream().map(iQuestLineEntryDBEntry ->
                        createQuestSearchEntry(iQuestLineEntryDBEntry, iQuestLineDBEntry)
                )).collect(Collectors.toList());
    }

    private QuestSearchEntry createQuestSearchEntry(DBEntry<IQuestLineEntry> iQuestLineEntryDBEntry, DBEntry<IQuestLine> iQuestLineDBEntry){
        int questId = iQuestLineEntryDBEntry.getID();
        DBEntry<IQuest> quest = new DBEntry<>(questId, QuestDatabase.INSTANCE.getValue(questId));
        return new QuestSearchEntry(quest, iQuestLineDBEntry);
    }

    @Override
    protected void queryMatches(QuestSearchEntry entry, String query, ArrayDeque<QuestSearchEntry> results) {
        if (String.valueOf(entry.getQuest().getID()).contains(query)) {
            results.add(entry);
        } else if (entry.getQuest().getValue().getProperty(NativeProps.NAME).toLowerCase().contains(query)) {
            results.add(entry);
        } else if (QuestTranslation.translate(entry.getQuest().getValue().getProperty(NativeProps.NAME)).toLowerCase().contains(query)) {
            results.add(entry);
        }
    }

    @Override
    protected boolean addResult(QuestSearchEntry entry, int index, int cachedWidth) {
        PanelButtonCustom buttonContainer = createContainerButton(entry, index, cachedWidth);

        addTextBox(cachedWidth, buttonContainer, 56, 6, entry.getQuestLineEntry().getValue().getProperty(NativeProps.NAME));
        addTextBox(cachedWidth, buttonContainer, 36, 20, entry.getQuest().getValue().getProperty(NativeProps.NAME));

        return true;
    }

    private PanelButtonCustom createContainerButton(QuestSearchEntry entry, int index, int cachedWidth){
        PanelButtonCustom buttonContainer = new PanelButtonCustom(new GuiRectangle(0, index * 32, cachedWidth, 32, 0), 2);
        buttonContainer.setCallback(panelButtonCustom -> {
            if (!buttonContainer.isActive())
                return;
            if (questHighlightCallback != null)
                questHighlightCallback.accept(entry);
        });
        buttonContainer.setActive(QuestCache.isQuestShown(entry.getQuest().getValue(), questingUUID, player));
        this.addPanel(buttonContainer);

        buttonContainer.addPanel(createQuestPanelButton(entry));

        buttonContainer.addPanel(
                new PanelGeneric(
                        new GuiRectangle(36, 2, 14, 14, 0),
                        new OreDictTexture(1F, entry.getQuestLineEntry().getValue().getProperty(NativeProps.ICON),
                                false,
                                true)
                )
        );
        return buttonContainer;
    }

    private PanelButtonQuest createQuestPanelButton(QuestSearchEntry entry){
        PanelButtonQuest questButton = new PanelButtonQuest(
                new GuiRectangle(2, 2, 28, 28),
                0,
                "",
                entry.getQuest()
        );

        questButton.setCallback(value -> {
            if (!questButton.isActive())
                return;
            if (questOpenCallback != null)
                questOpenCallback.accept(entry);
        });
        return questButton;
    }

    private void addTextBox(int cachedWidth, PanelButtonCustom buttonContainer, int xOffset, int yOffset, String text) {
        PanelTextBox questName = new PanelTextBox(new GuiRectangle(xOffset, yOffset, cachedWidth - xOffset, 16), QuestTranslation.translate(text));
        buttonContainer.addPanel(questName);
    }

    public void setQuestHighlightCallback(Consumer<QuestSearchEntry> questHighlightCallback) {
        this.questHighlightCallback = questHighlightCallback;
    }

    public void setQuestOpenCallback(Consumer<QuestSearchEntry> questOpenCallback) {
        this.questOpenCallback = questOpenCallback;
    }
}

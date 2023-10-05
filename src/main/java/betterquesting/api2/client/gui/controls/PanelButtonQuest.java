package betterquesting.api2.client.gui.controls;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumQuestState;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.tasks.ITask;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.textures.GuiTextureColored;
import betterquesting.api2.client.gui.resources.textures.IGuiTexture;
import betterquesting.api2.client.gui.resources.textures.OreDictTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.storage.DBEntry;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.questing.CompletionInfo;
import betterquesting.questing.QuestDatabase;
import betterquesting.storage.QuestSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.text.TextFormatting;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PanelButtonQuest extends PanelButtonStorage<DBEntry<IQuest>> {
  public final GuiRectangle rect;
  public final EntityPlayer player;

  public PanelButtonQuest(GuiRectangle rect, int id, String txt, DBEntry<IQuest> value) {
    super(rect, id, txt, value);
    this.rect = rect;

    player = Minecraft.getMinecraft().player;
    EnumQuestState qState =
        value == null ? EnumQuestState.LOCKED : value.getValue().getState(QuestingAPI.getQuestingUUID(player));
    IGuiTexture txFrame = null;
    IGuiColor txIconCol = null;
    boolean main = value != null && value.getValue().getProperty(NativeProps.MAIN);
    boolean lock = false;

    switch (qState) {
      case LOCKED:
        txFrame = main ? PresetTexture.QUEST_MAIN_0.getTexture() : PresetTexture.QUEST_NORM_0.getTexture();
        txIconCol = PresetColor.QUEST_ICON_LOCKED.getColor();
        lock = true;
        break;
      case UNLOCKED:
        txFrame = main ? PresetTexture.QUEST_MAIN_1.getTexture() : PresetTexture.QUEST_NORM_1.getTexture();
        txIconCol = PresetColor.QUEST_ICON_UNLOCKED.getColor();
        break;
      case UNCLAIMED:
        txFrame = main ? PresetTexture.QUEST_MAIN_2.getTexture() : PresetTexture.QUEST_NORM_2.getTexture();
        txIconCol = PresetColor.QUEST_ICON_PENDING.getColor();
        break;
      case COMPLETED:
        txFrame = main ? PresetTexture.QUEST_MAIN_3.getTexture() : PresetTexture.QUEST_NORM_3.getTexture();
        txIconCol = PresetColor.QUEST_ICON_COMPLETE.getColor();
        break;
    }

    IGuiTexture btnTx = new GuiTextureColored(txFrame, txIconCol);
    setTextures(btnTx, btnTx, btnTx);
    setIcon(new OreDictTexture(1F, value == null ? new BigItemStack(Items.NETHER_STAR)
                                                 : value.getValue().getProperty(NativeProps.ICON), false, true), 4);
    //setTooltip(value == null ? Collections.emptyList() : getQuestTooltip(value.getValue(), player, value.getID()));
    setActive(QuestingAPI.getAPI(ApiReference.SETTINGS).canUserEdit(player) || !lock);
  }

  @Override
  public List<String> getTooltip(int mx, int my) {
    if (!this.getTransform().contains(mx, my)) { return null; }

    DBEntry<IQuest> value = this.getStoredValue();
    return value == null ? Collections.emptyList() : getQuestTooltip(value.getValue(), player, value.getID());
  }

  private List<String> getQuestTooltip(IQuest quest, EntityPlayer player, int qID) {
    List<String> tooltip = getStandardTooltip(quest, player, qID);

    if (Minecraft.getMinecraft().gameSettings.advancedItemTooltips &&
        QuestSettings.INSTANCE.getProperty(NativeProps.EDIT_MODE)) {
      tooltip.add("");
      tooltip.addAll(this.getAdvancedTooltip(quest, player, qID));
    }

    return tooltip;
  }

  private List<String> getStandardTooltip(IQuest quest, EntityPlayer player, int qID) {
    List<String> list = new ArrayList<>();

    list.add(QuestTranslation.translate(quest.getProperty(NativeProps.NAME)) +
             (!Minecraft.getMinecraft().gameSettings.advancedItemTooltips ? "" : (" #" + qID)));

    UUID playerID = QuestingAPI.getQuestingUUID(player);

    if (quest.isComplete(playerID)) {
      list.add(TextFormatting.GREEN + QuestTranslation.translate("betterquesting.tooltip.complete"));

      if (!quest.hasClaimed(playerID)) {
        list.add(TextFormatting.GRAY + QuestTranslation.translate("betterquesting.tooltip.rewards_pending"));
      } else if (quest.getProperty(NativeProps.REPEAT_TIME) > 0) {
        long time = getRepeatSeconds(quest, player);
        DecimalFormat df = new DecimalFormat("00");
        String timeTxt = "";

        if (time >= 3600) {
          timeTxt += (time / 3600) + "h " + df.format((time % 3600) / 60) + "m ";
        } else if (time >= 60) {
          timeTxt += (time / 60) + "m ";
        }

        timeTxt += df.format(time % 60) + "s";

        list.add(TextFormatting.GRAY + QuestTranslation.translate("betterquesting.tooltip.repeat", timeTxt));
      }
    } else if (!quest.isUnlocked(playerID)) {
      list.add(TextFormatting.RED + "" + TextFormatting.UNDERLINE +
               QuestTranslation.translate("betterquesting.tooltip.requires") + " (" +
               quest.getProperty(NativeProps.LOGIC_QUEST).toString().toUpperCase() + ")");

      // TODO: Make this lookup unnecessary
      for (DBEntry<IQuest> req : QuestDatabase.INSTANCE.bulkLookup(quest.getRequirements())) {
        if (!req.getValue().isComplete(playerID)) {
          list.add(
              TextFormatting.RED + "- " + QuestTranslation.translate(req.getValue().getProperty(NativeProps.NAME)));
        }
      }
    } else {
      int n = 0;

      for (DBEntry<ITask> task : quest.getTasks().getEntries()) {
        if (task.getValue().isComplete(playerID)) {
          n++;
        }
      }

      list.add(TextFormatting.GRAY +
               QuestTranslation.translate("betterquesting.tooltip.tasks_complete", n, quest.getTasks().size()));
    }

    return list;
  }

  private List<String> getAdvancedTooltip(IQuest quest, EntityPlayer player, int qID) {
    List<String> list = new ArrayList<>();

    list.add(TextFormatting.GRAY + QuestTranslation.translate("betterquesting.tooltip.global_quest", quest.getProperty(NativeProps.GLOBAL)));
    if (quest.getProperty(NativeProps.GLOBAL)) {
      list.add(TextFormatting.GRAY + QuestTranslation.translate("betterquesting.tooltip.global_share", quest.getProperty(NativeProps.GLOBAL_SHARE)));
    }
    list.add(TextFormatting.GRAY + QuestTranslation.translate("betterquesting.tooltip.quest_logic", quest.getProperty(NativeProps.LOGIC_QUEST).toString().toUpperCase()));
    list.add(TextFormatting.GRAY + QuestTranslation.translate("betterquesting.tooltip.simultaneous", quest.getProperty(NativeProps.SIMULTANEOUS)));
    list.add(TextFormatting.GRAY + QuestTranslation.translate("betterquesting.tooltip.auto_claim", quest.getProperty(NativeProps.AUTO_CLAIM)));
    if (quest.getProperty(NativeProps.REPEAT_TIME) >= 0) {
      long time = quest.getProperty(NativeProps.REPEAT_TIME) / 20;
      DecimalFormat df = new DecimalFormat("00");
      String timeTxt = "";

      if (time >= 3600) {
        timeTxt += (time / 3600) + "h " + df.format((time % 3600) / 60) + "m ";
      } else if (time >= 60) {
        timeTxt += (time / 60) + "m ";
      }

      timeTxt += df.format(time % 60) + "s";

      list.add(TextFormatting.GRAY + QuestTranslation.translate("betterquesting.tooltip.repeat", timeTxt));
    } else {
      list.add(TextFormatting.GRAY + QuestTranslation.translate("betterquesting.tooltip.repeat", false));
    }

    return list;
  }

  private long getRepeatSeconds(IQuest quest, EntityPlayer player) {
    if (quest.getProperty(NativeProps.REPEAT_TIME) < 0) {
      return -1;
    }

    CompletionInfo ue = quest.getCompletionInfo(QuestingAPI.getQuestingUUID(player));
    if (ue == null) {
      return 0;
    }
    return (ue.getTimestamp() + quest.getProperty(NativeProps.REPEAT_TIME) * 50L - System.currentTimeMillis()) / 1000L;
  }
}

package betterquesting.storage;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.properties.IPropertyType;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.storage.IQuestSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class QuestSettings extends PropertyContainer implements IQuestSettings {
  public static final QuestSettings INSTANCE = new QuestSettings();

  public QuestSettings() {
    setupProps();
  }

  @Override
  public boolean canUserEdit(EntityPlayer player) {
    if (player == null) {
      return false;
    }
    return getProperty(NativeProps.EDIT_MODE) && NameCache.INSTANCE.isOP(QuestingAPI.getQuestingUUID(player));
  }

  @Override
  public void readFromNBT(NBTTagCompound nbt) {
    super.readFromNBT(nbt);

    setupProps();
  }

  @Override
  public void reset() {
    readFromNBT(new NBTTagCompound());
  }

  private void setupProps() {
    setupValue(NativeProps.PACK_NAME);
    setupValue(NativeProps.PACK_VER);

    setupValue(NativeProps.PARTY_ENABLE);
    setupValue(NativeProps.EDIT_MODE);
    setupValue(NativeProps.HARDCORE);
    setupValue(NativeProps.LIVES_DEF);
    setupValue(NativeProps.LIVES_MAX);

    setupValue(NativeProps.HOME_IMAGE);
    setupValue(NativeProps.HOME_ANC_X);
    setupValue(NativeProps.HOME_ANC_Y);
    setupValue(NativeProps.HOME_OFF_X);
    setupValue(NativeProps.HOME_OFF_Y);
  }

  private <T> void setupValue(IPropertyType<T> prop) {
    setupValue(prop, prop.getDefault());
  }

  private <T> void setupValue(IPropertyType<T> prop, T def) {
    setProperty(prop, getProperty(prop, def));
  }
}

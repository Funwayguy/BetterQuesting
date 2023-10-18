package betterquesting.api2.utils;

import betterquesting.core.BetterQuesting;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class EntityPlayerPreview extends EntityOtherPlayerMP {
  private final ResourceLocation resource;

  /**
   * Backup constructor. DO NOT USE
   */
  public EntityPlayerPreview(World worldIn) {
    this(worldIn, new GameProfile(null, "Notch"));
  }

  public EntityPlayerPreview(World worldIn, GameProfile gameProfileIn) {
    super(worldIn, gameProfileIn);
    resource = new ResourceLocation(BetterQuesting.MODID, "textures/skin_cache/" + gameProfileIn.getName());
    getDataManager().set(PLAYER_MODEL_FLAG, (byte) 1);
  }

  @Nonnull
  @Override
  public ResourceLocation getLocationSkin() {
    return resource;
  }

  @Override
  public ResourceLocation getLocationCape() {
    return null;
  }

  @Override
  public boolean hasSkin() {
    return true;
  }

  @Nonnull
  @Override
  public ITextComponent getDisplayName() {
    return new TextComponentString("");
  }

  @Override
  @SideOnly(Side.CLIENT)
  public boolean isWearing(@Nonnull EnumPlayerModelParts part) {
    return true;
  }
}

package betterquesting.api2.utils;

import betterquesting.core.BetterQuesting;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

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
        this.resource = new ResourceLocation(BetterQuesting.MODID, "textures/skin_cache/" + gameProfileIn.getName());
    }

    @Override
    public ResourceLocation getLocationSkin() {
        return this.resource;
    }

    @Override
    public ResourceLocation getLocationCape() {
        return null;
    }

    @Override
    public boolean func_152123_o() {
        return true;
    }

    @Override
    public String getDisplayName() {
        return "";
    }
}

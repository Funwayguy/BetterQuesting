package betterquesting.api2.utils;

import betterquesting.core.BetterQuesting;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntityPlayerPreview extends RemoteClientPlayerEntity
{
	private final ResourceLocation resource;
	
	/**
	 * Backup constructor. DO NOT USE
	 */
	public EntityPlayerPreview(ClientWorld worldIn)
	{
		this(worldIn, new GameProfile(null, "Notch"));
	}
	
	public EntityPlayerPreview(ClientWorld worldIn, GameProfile gameProfileIn)
	{
		super(worldIn, gameProfileIn);
		this.resource = new ResourceLocation(BetterQuesting.MODID, "textures/skin_cache/" + gameProfileIn.getName().toLowerCase());
        this.getDataManager().set(PLAYER_MODEL_FLAG, (byte)1);
	}
	
	@Override
	public ResourceLocation getLocationSkin()
	{
		return this.resource;
	}
	
	@Override
	public ResourceLocation getLocationCape()
	{
		return null;
	}
	
	@Override
	public boolean hasSkin()
	{
		return true;
	}
	
	@Override
	public ITextComponent getDisplayName()
	{
		return new StringTextComponent("");
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
    public boolean isWearing(PlayerModelPart part)
    {
        return true;
    }
}

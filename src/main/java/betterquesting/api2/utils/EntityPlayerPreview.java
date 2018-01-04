package betterquesting.api2.utils;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import betterquesting.core.BetterQuesting;
import com.mojang.authlib.GameProfile;

public class EntityPlayerPreview extends EntityOtherPlayerMP
{
	private final ResourceLocation resource;
	
	public EntityPlayerPreview(World worldIn, GameProfile gameProfileIn)
	{
		super(worldIn, gameProfileIn);
		this.resource = new ResourceLocation(BetterQuesting.MODID, "textures/skin_cache/" + gameProfileIn.getName());
        this.getDataManager().set(PLAYER_MODEL_FLAG, Byte.valueOf((byte)1));
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
		return new TextComponentString("");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public boolean isWearing(EnumPlayerModelParts part)
    {
        return true;
    }
}

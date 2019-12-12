package betterquesting.api.storage;

import betterquesting.api2.storage.INBTPartial;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.ListNBT;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

public interface INameCache extends INBTPartial<ListNBT, UUID>
{
    boolean updateName(@Nonnull ServerPlayerEntity player);
	String getName(@Nonnull UUID uuid);
	UUID getUUID(@Nonnull String name);
	
	List<String> getAllNames();
	
	// Primarily used client side for GUIs
	boolean isOP(@Nonnull UUID uuid);
	
	int size();
	
	void reset();
}

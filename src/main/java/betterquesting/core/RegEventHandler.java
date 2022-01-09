package betterquesting.core;

import betterquesting.api.placeholders.ItemPlaceholder;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class RegEventHandler
{
	public static final List<Item> ALL_ITEMS = new ArrayList<>();
	public static final List<Block> ALL_BLOCKS = new ArrayList<>();
	public static final List<IRecipe> ALL_RECIPES = new ArrayList<>();
	
	private static boolean setupRecipes = false;
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void registerModelEvent(ModelRegistryEvent event)
	{
		BetterQuesting.proxy.registerRenderers();
	}
	
	@SubscribeEvent
	public static void registerBlockEvent(RegistryEvent.Register<Block> event)
	{
		event.getRegistry().registerAll(ALL_BLOCKS.toArray(new Block[0]));
	}
	
	@SubscribeEvent
	public static void registerItemEvent(RegistryEvent.Register<Item> event)
	{
		event.getRegistry().registerAll(ALL_ITEMS.toArray(new Item[0]));
	}
	
	@SubscribeEvent
	public static void registerRecipeEvent(RegistryEvent.Register<IRecipe> event)
	{
		if(!setupRecipes)
		{
			initRecipes();
		}
		
		IRecipe[] tmp = ALL_RECIPES.toArray(new IRecipe[0]);
		event.getRegistry().registerAll(tmp);
	}
    
    public static void registerBlock(Block b, String name)
    {
    	ResourceLocation res = new ResourceLocation(BetterQuesting.MODID + ":" + name);
    	ALL_BLOCKS.add(b.setRegistryName(res));
    	ALL_ITEMS.add(new ItemBlock(b).setRegistryName(res));
    }

    public static void registerItems() {
        ALL_ITEMS.add(ItemPlaceholder.placeholder.setRegistryName(BetterQuesting.MODID, "placeholder"));
        ALL_ITEMS.add(BetterQuesting.extraLife.setRegistryName(BetterQuesting.MODID, "extra_life"));
        ALL_ITEMS.add(BetterQuesting.guideBook.setRegistryName(BetterQuesting.MODID, "guide_book"));
        ALL_ITEMS.add(BetterQuesting.lootChest.setRegistryName(BetterQuesting.MODID_STD, "loot_chest"));
    }

    public static void addShapelessRecipe(String name, String group, ItemStack stack, Object... ing)
    {
    	ResourceLocation rName = new ResourceLocation(BetterQuesting.MODID, name);
    	ResourceLocation rGroup = new ResourceLocation(BetterQuesting.MODID, group);
    	
    	ALL_RECIPES.add(new ShapelessOreRecipe(rGroup, stack, ing).setRegistryName(rName));
    }
    
    public static void initRecipes()
    {
    	addShapelessRecipe("submit_station", "questing", new ItemStack(BetterQuesting.submitStation), new ItemStack(Items.BOOK), new ItemStack(Blocks.GLASS), new ItemStack(Blocks.CHEST));
    	
    	addShapelessRecipe("life_full_0", "questing", new ItemStack(BetterQuesting.extraLife, 1, 0), new ItemStack(BetterQuesting.extraLife, 1, 2), new ItemStack(BetterQuesting.extraLife, 1, 2), new ItemStack(BetterQuesting.extraLife, 1, 2), new ItemStack(BetterQuesting.extraLife, 1, 2));
    	addShapelessRecipe("life_full_1", "questing", new ItemStack(BetterQuesting.extraLife, 1, 0), new ItemStack(BetterQuesting.extraLife, 1, 2), new ItemStack(BetterQuesting.extraLife, 1, 2), new ItemStack(BetterQuesting.extraLife, 1, 1));
    	addShapelessRecipe("life_full_2", "questing", new ItemStack(BetterQuesting.extraLife, 1, 0), new ItemStack(BetterQuesting.extraLife, 1, 1), new ItemStack(BetterQuesting.extraLife, 1, 1));
    	
    	addShapelessRecipe("life_half_0", "questing", new ItemStack(BetterQuesting.extraLife, 2, 1), new ItemStack(BetterQuesting.extraLife, 1, 0));
    	addShapelessRecipe("life_half_1", "questing", new ItemStack(BetterQuesting.extraLife, 1, 1), new ItemStack(BetterQuesting.extraLife, 1, 2), new ItemStack(BetterQuesting.extraLife, 1, 2));
    	
    	addShapelessRecipe("life_quarter_0", "questing", new ItemStack(BetterQuesting.extraLife, 2, 2), new ItemStack(BetterQuesting.extraLife, 1, 1));
    	
    	setupRecipes = true;
    }

	static {
    	registerItems();
    	registerBlock(BetterQuesting.submitStation, "submit_station");
	}
}

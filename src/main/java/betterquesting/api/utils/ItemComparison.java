package betterquesting.api.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.StringUtils;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for comparing ItemStacks in quests
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class ItemComparison {
    /**
     * Check whether two stacks match with optional NBT and Ore Dictionary checks
     */
    public static boolean StackMatch(ItemStack stack1, ItemStack stack2, boolean nbtCheck, boolean partialNBT) {
        // Some quick checks
        if (stack1 == stack2) {
            return true;
        } else if (stack1 == null || stack2 == null) {
            return false;
        }
        if (stack1.getItem() != stack2.getItem()) {
            return false;
        }
        if (!(stack1.getItemDamage() == stack2.getItemDamage() || stack1.getItem().isDamageable() || stack1.getItemDamage() == OreDictionary.WILDCARD_VALUE)) {
            return false;
        }

        if (nbtCheck) {
            if (!partialNBT && !stack1.areCapsCompatible(stack2)) {
                return false;
            }
            return CompareNBTTag(stack1.getTagCompound(), stack2.getTagCompound(), partialNBT);
        }
        return true;
    }

    public static boolean CompareNBTTag(NBTBase tag1, NBTBase tag2, boolean partial) {
        if (isEmptyNBT(tag1) != isEmptyNBT(tag2)) // One is null, the other is not
        {
            return false;
        } else if (isEmptyNBT(tag1)) // The opposing tag will always be null at this point if the other already is
        {
            return true;
        } else if (!(tag1 instanceof NBTPrimitive && tag2 instanceof NBTPrimitive) && tag1.getId() != tag2.getId())
            return false; // Incompatible tag types (and not a numbers we can cast)

        if (tag1 instanceof NBTTagCompound && tag2 instanceof NBTTagCompound) {
            return CompareNBTTagCompound((NBTTagCompound) tag1, (NBTTagCompound) tag2, partial);
        } else if (tag1 instanceof NBTTagList && tag2 instanceof NBTTagList) {
            NBTTagList list1 = (NBTTagList) tag1;
            NBTTagList list2 = (NBTTagList) tag2;

            if (list1.tagCount() > list2.tagCount() || (!partial && list1.tagCount() != list2.tagCount())) {
                return false; // Sample is missing requested tags or is not exact
            }

            topLoop:
            for (int i = 0; i < list1.tagCount(); i++) {
                NBTBase lt1 = list1.get(i);

                for (int j = 0; j < list2.tagCount(); j++) {
                    if (CompareNBTTag(lt1, list2.get(j), partial)) {
                        continue topLoop;
                    }
                }

                return false; // Couldn't find requested tag in list
            }
        } else if (tag1 instanceof NBTTagIntArray && tag2 instanceof NBTTagIntArray) {
            NBTTagIntArray list1 = (NBTTagIntArray) tag1;
            NBTTagIntArray list2 = (NBTTagIntArray) tag2;

            if (list1.getIntArray().length > list2.getIntArray().length || (!partial && list1.getIntArray().length != list2.getIntArray().length)) {
                return false; // Sample is missing requested tags or is not exact
            }

            List<Integer> usedIdxs = new ArrayList<>(); // Duplicate control

            topLoop:
            for (int i = 0; i < list1.getIntArray().length; i++) {
                for (int j = 0; j < list2.getIntArray().length; j++) {
                    if (!usedIdxs.contains(j) && list1.getIntArray()[i] == list2.getIntArray()[j]) {
                        usedIdxs.add(j);
                        continue topLoop;
                    }
                }

                return false; // Couldn't find requested integer in list
            }

            return true;
        } else if (tag1 instanceof NBTTagByteArray && tag2 instanceof NBTTagByteArray) {
            NBTTagByteArray list1 = (NBTTagByteArray) tag1;
            NBTTagByteArray list2 = (NBTTagByteArray) tag2;

            if (list1.getByteArray().length > list2.getByteArray().length || (!partial && list1.getByteArray().length != list2.getByteArray().length)) {
                return false; // Sample is missing requested tags or is not exact for non-partial match
            }

            List<Integer> usedIdxs = new ArrayList<>(); // Duplicate control

            topLoop:
            for (int i = 0; i < list1.getByteArray().length; i++) {
                for (int j = 0; j < list2.getByteArray().length; j++) {
                    if (!usedIdxs.contains(j) && list1.getByteArray()[i] == list2.getByteArray()[j]) {
                        usedIdxs.add(j);
                        continue topLoop;
                    }
                }

                return false; // Couldn't find requested integer in list
            }
        } else if (tag1 instanceof NBTTagLongArray && tag2 instanceof NBTTagLongArray) {
            NBTTagLongArray list1 = (NBTTagLongArray) tag1;
            NBTTagLongArray list2 = (NBTTagLongArray) tag2;

            final long[] la1 = NBTConverter.readLongArray(list1);
            final long[] la2 = NBTConverter.readLongArray(list2);

            if (la1.length > la2.length || (!partial && la1.length != la2.length)) {
                return false; // Sample is missing requested tags or is not exact for non-partial match
            }

            List<Integer> usedIdxs = new ArrayList<>(); // Duplicate control

            topLoop:
            for (long l : la1) {
                for (int j = 0; j < la2.length; j++) {
                    if (!usedIdxs.contains(j) && l == la2[j]) {
                        usedIdxs.add(j);
                        continue topLoop;
                    }
                }

                return false; // Couldn't find requested integer in list
            }
        } else if (tag1 instanceof NBTTagString && tag2 instanceof NBTTagString) {
            return tag1.equals(tag2);
        } else if (tag1 instanceof NBTPrimitive && tag2 instanceof NBTPrimitive) // Standardize numbers to not care about format
        {
            Number num1 = NBTConverter.getNumber(tag1);
            Number num2 = NBTConverter.getNumber(tag2);

            // Check if floating point precesion needs to be preserved in comparison
            if (tag1 instanceof NBTTagFloat || tag1 instanceof NBTTagDouble || tag2 instanceof NBTTagFloat || tag2 instanceof NBTTagDouble) {
                return num1.doubleValue() == num2.doubleValue();
            } else {
                return num1.longValue() == num2.longValue();
            }
        } else {
            return tag1.equals(tag2);
        }

        return true;
    }

    private static boolean CompareNBTTagCompound(NBTTagCompound reqTags, NBTTagCompound sample, boolean partial) {
        if (isEmptyNBT(reqTags) != isEmptyNBT(sample)) // One is null, the other is not
        {
            return false;
        } else if (isEmptyNBT(reqTags)) // The opposing tag will always be null at this point if the other already is
        {
            return true;
        }

        for (String key : reqTags.getKeySet()) {
            if (!sample.hasKey(key)) {
                return false;
            } else if (!CompareNBTTag(reqTags.getTag(key), sample.getTag(key), partial)) {
                return false;
            }
        }

        return true;
    }

    private static boolean isEmptyNBT(NBTBase tag) {
        return tag == null || tag.isEmpty();
    }

    @Deprecated
    public static boolean OreDictionaryMatch(String name, ItemStack stack) {
        return stack != null && !StringUtils.isNullOrEmpty(name) && new OreIngredient(name).apply(stack);
    }

    @Deprecated
    public static boolean OreDictionaryMatch(String name, NBTTagCompound tags, ItemStack stack, boolean nbtCheck, boolean partialNBT) {
        if (!nbtCheck) return stack != null && !StringUtils.isNullOrEmpty(name) && new OreIngredient(name).apply(stack);
        return OreDictionaryMatch(new OreIngredient(name), tags, stack, nbtCheck, partialNBT);
    }

    /**
     * Check if the item stack is part of the ore dictionary listing with the given ore ingredient while also comparing NBT tags
     */
    public static boolean OreDictionaryMatch(OreIngredient ore, NBTTagCompound tags, ItemStack stack, boolean nbtCheck, boolean partialNBT) {
        if (stack == null || ore == null) return false;
        return ore.apply(stack) && (!nbtCheck || CompareNBTTagCompound(stack.getTagCompound(), tags, partialNBT));
    }

    /**
     * Check if the two stacks match directly or through ore dictionary listings
     */
    @Deprecated
    public static boolean AllMatch(ItemStack stack1, ItemStack stack2) {
        return AllMatch(stack1, stack2, false, false);
    }

    /**
     * Check if the two stacks match directly or through ore dictionary listings
     */
    public static boolean AllMatch(ItemStack stack1, ItemStack stack2, boolean nbtCheck, boolean partialNBT) {
        if (stack1 == stack2) return true; // Both null or same instance
        if (stack1 == null) return false; // One is null the other is not
        if (nbtCheck && !CompareNBTTagCompound(stack1.getTagCompound(), stack2.getTagCompound(), partialNBT))
            return false; // NBT check failed
        if (StackMatch(stack1, stack2, false, false))
            return true; // Stacks are compatible (NBT was already checked at this point)

        // Final Ore Dictionary test...
        int[] oreIDs1 = OreDictionary.getOreIDs(stack1);
        int[] oreIDs2 = OreDictionary.getOreIDs(stack2);

        // Search all ore dictionary listings for matches
        for (int i1 : oreIDs1) {
            for (int i2 : oreIDs2) {
                if (i1 == i2) return true; // Shared ore dictionary entries found
            }
        }

        return false; // No shared ore dictionary types
    }
}

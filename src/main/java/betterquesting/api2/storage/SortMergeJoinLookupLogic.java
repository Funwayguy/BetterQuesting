package betterquesting.api2.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class SortMergeJoinLookupLogic<T> extends LookupLogic<T>
{
    public SortMergeJoinLookupLogic(SimpleDatabase<T> simpleDatabase)
    {
        super(simpleDatabase);
    }

    @Override
    public List<DBEntry<T>> bulkLookup(int[] keys)
    {
        int[] sortedKeys = new int[keys.length];
        System.arraycopy(keys, 0, sortedKeys, 0, keys.length);
        Arrays.sort(sortedKeys);

        List<DBEntry<T>> subList = new ArrayList<>(keys.length);
        int n = 0;

        for(DBEntry<T> entry : simpleDatabase.getEntries())
        {
            while(n < sortedKeys.length && sortedKeys[n] < entry.getID()) n++;
            if(n >= sortedKeys.length) break;
            if(sortedKeys[n] == entry.getID()) subList.add(entry);
        }

        return subList;
    }
}

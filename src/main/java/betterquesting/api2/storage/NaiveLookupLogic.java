package betterquesting.api2.storage;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.List;

class NaiveLookupLogic<T> extends LookupLogic<T> {

    private TIntObjectMap<DBEntry<T>> backingMap;

    public NaiveLookupLogic(SimpleDatabase<T> simpleDatabase) {
        super(simpleDatabase);
    }

    @Override
    public void onDataChange() {
        super.onDataChange();
        backingMap = null;
    }

    @Override
    public List<DBEntry<T>> bulkLookup(int[] keys) {
        if(backingMap == null) {
            backingMap = new TIntObjectHashMap<>(simpleDatabase.mapDB.size());
            for (DBEntry<T> entry : getRefCache()) {
                backingMap.put(entry.getID(), entry);
            }
        }
        List<DBEntry<T>> list = new ArrayList<>(keys.length);
        for(int k : keys) {
            final DBEntry<T> element = backingMap.get(k);
            if(element != null) {
                list.add(element);
            }
        }
        return list;
    }
}

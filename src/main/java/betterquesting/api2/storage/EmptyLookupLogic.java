package betterquesting.api2.storage;

import java.util.Collections;
import java.util.List;

public class EmptyLookupLogic<T> extends LookupLogic<T> {

    public EmptyLookupLogic(SimpleDatabase<T> simpleDatabase) {
        super(simpleDatabase);
    }

    @Override
    public List<DBEntry<T>> bulkLookup(int[] keys) {
        return Collections.emptyList();
    }
}

package betterquesting.api2.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Divides the database into smaller indexed blocks to speed up search times.
@Deprecated
public abstract class BigDatabase<T> extends SimpleDatabase<T> {
    public BigDatabase() {}

    @Deprecated
    public BigDatabase(int blockSize) {}

    public List<DBEntry<T>> bulkLookup(int... ids) {
        if (ids == null || ids.length <= 0) return Collections.emptyList();

        List<DBEntry<T>> values = new ArrayList<>();

        synchronized (this) {
            for (int i : ids) {
                T v = getValue(i);
                if (v != null) values.add(new DBEntry<>(i, v));
            }
        }

        return values;
    }
}

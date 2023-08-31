package betterquesting.api2.storage;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.*;

public class SimpleDatabase<T> implements IDatabase<T> {
  private final Int2ObjectMap<T> map = new Int2ObjectOpenHashMap<>();
  private final Object2IntMap<T> inverseMap = new Object2IntOpenHashMap<>();

  {
    inverseMap.defaultReturnValue(-1);
  }

  private final BitSet idMap = new BitSet();
  private List<DBEntry<T>> refCache = null;

  @Override
  public synchronized int nextID() {
    return idMap.nextClearBit(0);
  }

  @Override
  public synchronized DBEntry<T> add(int id, T value) {
    Objects.requireNonNull(value, "Value cannot be null");
    if (id < 0) {
      throw new IllegalArgumentException("ID cannot be negative");
    }
    if (map.putIfAbsent(id, value) == null && inverseMap.putIfAbsent(value, id) == null) {
      idMap.set(id);
      refCache = null;
      return new DBEntry<>(id, value);
    } else {
      throw new IllegalArgumentException("ID or value is already contained within database");
    }
  }

  @Override
  public synchronized boolean removeID(int key) {
    if (key < 0) {
      return false;
    }
    T removed = map.remove(key);
    if (removed == null) {
      return false;
    }
    inverseMap.removeInt(removed);
    idMap.clear(key);
    refCache = null;
    return true;
  }

  @Override
  public synchronized boolean removeValue(T value) {
    if (value == null) {
      return false;
    }
    int removed = inverseMap.removeInt(value);
    if (removed == -1) {
      return false;
    }
    map.remove(removed);
    idMap.clear(removed);
    refCache = null;
    return true;
  }

  @Override
  public synchronized int getID(T value) {
    return inverseMap.getInt(value);
  }

  @Override
  public synchronized T getValue(int id) {
    return map.get(id);
  }

  @Override
  public synchronized int size() {
    return map.size();
  }

  @Override
  public synchronized void reset() {
    map.clear();
    idMap.clear();
    refCache = Collections.emptyList();
  }

  @Override
  public synchronized List<DBEntry<T>> getEntries() {
    if (refCache == null) {
      List<DBEntry<T>> list = new ArrayList<>();
      map.forEach((k, v) -> list.add(new DBEntry<>(k, v)));
      list.sort(Comparator.comparingInt(DBEntry::getID));
      refCache = Collections.unmodifiableList(list);
    }
    return refCache;
  }

  @Override
  public synchronized List<DBEntry<T>> bulkLookup(int... keys) {
    if (keys.length == 0) {
      return Collections.emptyList();
    }
    int[] sortedKeys = keys.clone();
    Arrays.sort(sortedKeys);
    List<DBEntry<T>> result = new ArrayList<>();
    for (int i : sortedKeys) {
      T t = map.get(i);
      if (t != null) {
        result.add(new DBEntry<>(i, t));
      }
    }
    return result;
  }
}
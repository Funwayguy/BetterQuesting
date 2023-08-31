package betterquesting.api2.storage;

import javax.annotation.Nonnull;

public final class DBEntry<T> implements Comparable<DBEntry<T>> {
  private final int id;
  @Nonnull
  private final T obj;

  public DBEntry(int id, @Nonnull T obj) {
    if (id < 0) {
      throw new IllegalArgumentException("Entry ID cannot be negative");
    }

    this.id = id;
    this.obj = obj;
  }

  public int getID() {
    return this.id;
  }

  @Nonnull
  public T getValue() {
    return obj;
  }

  @Override
  public int compareTo(DBEntry<T> o) {
    return Integer.compare(id, o.id);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof DBEntry)) {
      return false;
    }

    DBEntry<?> entry = (DBEntry<?>) obj;

    return this.getID() == entry.getID() && this.getValue().equals(entry.getValue());
  }
}
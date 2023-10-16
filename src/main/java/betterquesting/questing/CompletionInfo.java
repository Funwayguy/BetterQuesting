package betterquesting.questing;

public class CompletionInfo {
  private long timestamp;
  private boolean claimed;

  public CompletionInfo(long timestamp, boolean claimed) {
    this.timestamp = timestamp;
    this.claimed = claimed;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long time) {
    timestamp = time;
  }

  public boolean isClaimed() {
    return claimed;
  }

  public void setClaimed(boolean claimed) {
    this.claimed = claimed;
  }
}

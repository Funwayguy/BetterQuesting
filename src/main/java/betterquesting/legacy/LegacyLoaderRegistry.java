package betterquesting.legacy;

import betterquesting.legacy.v0.LegacyLoader_v0;

import java.util.HashMap;

public class LegacyLoaderRegistry {
  private static final HashMap<String, ILegacyLoader> legReg = new HashMap<>();

  public static ILegacyLoader getLoader(String version) {
    return legReg.get(version);
  }

  static {
    legReg.put("0.0.0", LegacyLoader_v0.INSTANCE);
  }
}

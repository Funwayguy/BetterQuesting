package betterquesting.api2.supporter.theme_dlc;

import betterquesting.api.utils.JsonHelper;
import com.google.gson.JsonObject;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ThemeRepository {
    private final String repoAddress;
    public String repoName = "Unknown Theme Repository";

    private final List<CatalogueEntry> entries = new ArrayList<>();

    public ThemeRepository(@Nonnull String address) {
        repoAddress = address;
    }

    public String getAddress() {
        return this.repoAddress;
    }

    public List<CatalogueEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public void loadRepository(@Nonnull JsonObject json) {
        repoName = JsonHelper.GetString(json, "repoName", "Unknown Repository");
    }
}

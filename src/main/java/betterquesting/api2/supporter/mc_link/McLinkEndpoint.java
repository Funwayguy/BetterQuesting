package betterquesting.api2.supporter.mc_link;

import javax.annotation.Nonnull;

public enum McLinkEndpoint {
    API_AUTH("https://mclink.dries007.net/api/1/authenticate"),
    API_INFO("https://mclink.dries007.net/api/1/info"),
    API_STATUS("https://mclink.dries007.net/api/1/status");

    public final String URL;

    McLinkEndpoint(@Nonnull String url) {
        this.URL = url;
    }
}

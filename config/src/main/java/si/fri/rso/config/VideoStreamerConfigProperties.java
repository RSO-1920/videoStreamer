package si.fri.rso.config;

import com.kumuluz.ee.configuration.cdi.ConfigBundle;
import com.kumuluz.ee.configuration.cdi.ConfigValue;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@ConfigBundle("rest-config")
public class VideoStreamerConfigProperties {
    @ConfigValue(value = "is-service-up", watch = true)
    private boolean isServiceUp;

    @ConfigValue(value = "catalog-uri", watch = true)
    private String catalogUri;

    public boolean getIsServiceUp() {
        return isServiceUp;
    }

    public void setIsServiceUp(boolean isServiceUp) {
        this.isServiceUp = isServiceUp;
    }

    public void setCatalogUri(String catalogUri) {
        this.catalogUri = catalogUri;
    }

    public String getCatalogUri() {
        return catalogUri;
    }
}

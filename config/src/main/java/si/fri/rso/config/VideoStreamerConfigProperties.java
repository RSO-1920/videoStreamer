package si.fri.rso.config;

import com.kumuluz.ee.configuration.cdi.ConfigBundle;
import com.kumuluz.ee.configuration.cdi.ConfigValue;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@ConfigBundle("rest-config")
public class VideoStreamerConfigProperties {
    @ConfigValue(value = "is-service-up", watch = true)
    private boolean isServiceUp;

    public boolean getIsServiceUp() {
        return isServiceUp;
    }

    public void setIsServiceUp(boolean isServiceUp) {
        this.isServiceUp = isServiceUp;
    }
}

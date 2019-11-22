package si.fri.rso.api.v1;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import si.fri.rso.config.VideoStreamerConfigProperties;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;

@ApplicationScoped
public class IsServiceUpCheck implements HealthCheck {


    @Override
    public HealthCheckResponse call() {
        VideoStreamerConfigProperties videoStreamerConfigProperties = CDI.current().select(VideoStreamerConfigProperties.class).get();
        System.out.println("health check: " + videoStreamerConfigProperties.getIsServiceUp());

        if (videoStreamerConfigProperties.getIsServiceUp()) {
            return HealthCheckResponse
                    .named(IsServiceUpCheck.class.getSimpleName())
                    .up()
                    .withData("isServiceUP", videoStreamerConfigProperties.getIsServiceUp())
                    .build();
        }
            return HealthCheckResponse
                    .named(IsServiceUpCheck.class.getSimpleName())
                    .withData("isServiceUP", videoStreamerConfigProperties.getIsServiceUp())
                    .down()
                    .build();
    }
}

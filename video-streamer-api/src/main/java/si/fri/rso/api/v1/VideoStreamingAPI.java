package si.fri.rso.api.v1;

import com.kumuluz.ee.discovery.annotations.RegisterService;
import com.kumuluz.ee.health.HealthRegistry;
import com.kumuluz.ee.health.enums.HealthCheckType;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import si.fri.rso.api.v1.controllers.VideoStreamingController;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@RegisterService(value = "rso1920-video-streamer")
@ApplicationPath("/v1")
@OpenAPIDefinition(info = @Info(title = "Video streamer REST api", version = "v1", contact = @Contact(), license = @License(),
        description = "Streaming video files.."), servers = @Server(url ="http://localhost:8084/v1"))
public class VideoStreamingAPI extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> resources = new HashSet<Class<?>>();
        resources.add(MultiPartFeature.class);
        resources.add(VideoStreamingController.class);
        HealthRegistry.getInstance().register(IsServiceUpCheck.class.getSimpleName(), new IsServiceUpCheck(), HealthCheckType.LIVENESS);
        return resources;
    }
}

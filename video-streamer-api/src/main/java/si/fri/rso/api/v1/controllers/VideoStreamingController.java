package si.fri.rso.api.v1.controllers;

import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.eclipse.microprofile.metrics.annotation.Timed;
import si.fri.rso.config.VideoStreamerConfigProperties;
import si.fri.rso.services.StreamBean;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;

@ApplicationScoped
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VideoStreamingController {
    private static final String FILE_PATH = "./file_example_MP4_1920_18MG.mp4";

    @Inject
    StreamBean streamBean;

    @Inject
    @Metric(name = "histogram_streaming")
    Histogram histogram;

    @HEAD
    @Counted(name = "stream_header")
    @Path("/stream")
    @Produces("video/mp4")
    public Response header() {
        File file = new File(FILE_PATH);

        return Response.ok()
                .status(Response.Status.PARTIAL_CONTENT)
                .header(HttpHeaders.CONTENT_LENGTH, file.length())
                .header("Accept-Ranges", "bytes")
                .build();
    }

    // TODO GET FILE FROM file storage

    @GET
    @Metered(name = "streaming_metered")
    @Timed(name = "streaming_times")
    @Path("/stream")
    @Produces("video/mp4")
    public Response stream(@HeaderParam("Range") String range) throws Exception {
        File file = new File(FILE_PATH);
        histogram.update(file.length());
        return this.streamBean.buildStream(file, range);
    }
}





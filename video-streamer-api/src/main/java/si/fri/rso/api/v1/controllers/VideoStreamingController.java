package si.fri.rso.api.v1.controllers;

import com.kumuluz.ee.discovery.annotations.DiscoverService;
import com.kumuluz.ee.logs.cdi.Log;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.glassfish.jersey.media.multipart.MultiPart;
import si.fri.rso.config.VideoStreamerConfigProperties;
import si.fri.rso.lib.CatalogFileMetadata;
import si.fri.rso.services.RequestSenderBean;
import si.fri.rso.services.StreamBean;
import si.fri.rso.services.classes.MediaStreamer;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.Optional;
import java.util.UUID;

@Log
@ApplicationScoped
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VideoStreamingController {
    private static final String FILE_PATH = "./file_example_MP4_1920_18MG.mp4";
    private static final Logger LOG = LogManager.getLogger(VideoStreamingController.class.getName());

    @Inject
    StreamBean streamBean;

    @Inject
    HttpServletRequest requestheader;

    @Inject
    @Metric(name = "histogram_streaming")
    Histogram histogram;

    private Client httpClient;
    @Inject
    @DiscoverService(value = "rso1920-fileStorage")
    private Optional<String> fileStorageUrl;

    @Inject
    RequestSenderBean requestSenderBean;

    @PostConstruct
    private void init(){
        this.httpClient = ClientBuilder.newClient();
    }

    @DELETE
    @Operation(description = "Delete streamed files", summary = "file deletion", tags = "delete, stream", responses = {
            @ApiResponse(responseCode = "200",
                    description = "delete streamed files",
                    content = @Content( schema = @Schema(implementation = String.class))
            ),
    })
    @Path("/stream")
    public  Response deleteStreamedFiles() throws IOException {
        FileUtils.cleanDirectory(new File("./streamFiles/"));

        return Response.ok("Stream files deleted").build();
    }

    @HEAD
    @Operation(description = "Head request for streams", summary = "head", tags = "head, stream", responses = {
            @ApiResponse(responseCode = "200",
                    description = "stream head request"
            ),
    })
    @Counted(name = "stream_header")
    @Path("/stream/{fileId}")
    @Produces("video/mp4")
    public Response header(@PathParam("fileId") Integer fileId) {
        File file = new File(FILE_PATH);
        LOG.info("Inside head method");
        return Response.ok()
                .status(Response.Status.PARTIAL_CONTENT)
                .header(HttpHeaders.CONTENT_LENGTH, file.length())
                .header("Accept-Ranges", "bytes")
                .build();
    }

    @GET
    @Operation(description = "Video stream", summary = "Video stream", tags = "stream, video", responses = {
            @ApiResponse(responseCode = "200",
                    description = "stream video",
                    content = @Content( schema = @Schema(implementation = MediaStreamer.class))
            ),
    })
    @Metered(name = "streaming_metered")
    @Timed(name = "streaming_times")
    @Path("/stream/{fileId}")
    @Produces("video/mp4")
    public Response stream(@HeaderParam("Range") String range, @PathParam("fileId") Integer fileId) throws Exception {
        System.out.println("FileId: " + fileId);

        String requestId = UUID.randomUUID().toString();
        CatalogFileMetadata catalogFileMetadata = requestSenderBean.getFileMetadata(fileId, requestId);

        File file = null;

        if (catalogFileMetadata == null || !catalogFileMetadata.getFileType().equals("mp4")) {
            System.out.println("file is no a video");
            file = new File(FILE_PATH);
            histogram.update(file.length());
            return this.streamBean.buildStream(file, range);
        }

        String bucketName = catalogFileMetadata.getFilePath().split("/")[0];
        String fileName = catalogFileMetadata.getFileName();

        System.out.println(bucketName + " " + fileName);
        System.out.println(fileStorageUrl);
        if (!fileStorageUrl.isPresent()) {
            System.out.println("INSIDE");
            file = new File(FILE_PATH);
            histogram.update(file.length());
            return this.streamBean.buildStream(file, range);
        } else {
            try {
                FileUtils.cleanDirectory(new File("./streamFiles/"));
                System.out.println(fileStorageUrl.get() + "/v1/fileTransfer/" + bucketName + "/" + fileName);
                Response success = httpClient
                        .target(fileStorageUrl.get() + "/v1/fileTransfer/" + bucketName + "/" + fileName)
                        .request(MediaType.MULTIPART_FORM_DATA)
                        .header("uniqueRequestId", requestId)
                        .get();

                System.out.println("request: " + success.getStatus());
                if (success.getStatus() == 200) {
                    // return this.streamBean.buildStream(file, range);
                    // System.out.println("success: " + success.readEntity(String.class));
                    System.out.println("SUCCESS");
                    InputStream inputStream= success.readEntity(InputStream.class);

                    File writeFile = new File("./streamFiles/"+fileId + ".mp4");

                    try {
                        FileUtils.copyInputStreamToFile(inputStream, writeFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    histogram.update(writeFile.length());
                    Response rs = this.streamBean.buildStream(writeFile, range);
                    /*boolean isDeleted = writeFile.delete();
                    if(!isDeleted){
                        System.out.println("File was not deleted!");
                    }*/
                    return rs;
                    // return Response.ok(inputStream, MediaType.APPLICATION_OCTET_STREAM).build();

                } else {
                    System.out.println("request status not 200");
                    file = new File(FILE_PATH);
                    histogram.update(file.length());
                    return this.streamBean.buildStream(file, range);
                }
            } catch (Exception e) {
                e.printStackTrace();
                file = new File(FILE_PATH);
                histogram.update(file.length());
                return this.streamBean.buildStream(file, range);
            }
        }
    }
}





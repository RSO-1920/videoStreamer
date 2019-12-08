package si.fri.rso.services;

import com.google.gson.Gson;
import com.kumuluz.ee.discovery.annotations.DiscoverService;
import si.fri.rso.config.VideoStreamerConfigProperties;
import si.fri.rso.lib.CatalogFileMetadata;
import si.fri.rso.lib.DTOCatalog;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

@RequestScoped
public class RequestSenderBean {
    @Inject
    @DiscoverService(value = "rso1920-catalog")
    private Optional<String> fileMetadataUrl;

    @Inject
    private VideoStreamerConfigProperties videoStreamerConfigProperties;

    private Client httpClient;

    @PostConstruct
    private void init(){
        this.httpClient = ClientBuilder.newClient();
    }

    public CatalogFileMetadata getFileMetadata(Integer fileId, String requestUniqueID){
        if (!fileMetadataUrl.isPresent()){
            System.out.println("catalog api url not set");
            return null;
        }

        System.out.println("getting file metadata: " + fileMetadataUrl.get() + videoStreamerConfigProperties.getCatalogUri() + "/" + fileId);
        System.out.println("REQUEST: " + requestUniqueID);
        try{
            Response success = httpClient
                    .target(fileMetadataUrl.get() + videoStreamerConfigProperties.getCatalogUri() + "/" + fileId)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .header("uniqueRequestId", requestUniqueID)
                    .get();

            if (success.getStatus() == 200) {
                System.out.println("file metadata found");
                Gson gson = new Gson();
                CatalogFileMetadata  fileMetadata = (CatalogFileMetadata) gson.fromJson(success.readEntity(String.class), DTOCatalog.class).getData();
                System.out.println(fileMetadata.getFileName());
                return fileMetadata;
            } else {
                System.out.println("file metadata not fouond");
                return null;
            }
        }catch (WebApplicationException | ProcessingException e) {
            // e.printStackTrace();
            System.out.println("getting file metadata failed");
            return null;
        }
    }
}

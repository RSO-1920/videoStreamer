package si.fri.rso.api.v1.controllers;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Date;

@ApplicationScoped
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VideoStreamingController {
    private static final String FILE_PATH = "./file_example_MP4_1920_18MG.mp4";
    private final int chunk_size = 1024 * 1024 * 2; // 2 MB chunks

    @HEAD
    @Path("/stream")
    @Produces("video/mp4")
    public Response header(){

        // URL url = this.getClass().getResource( FILE_PATH );
        File file = new File( FILE_PATH );

        return Response.ok()
                .status( Response.Status.PARTIAL_CONTENT )
                .header( HttpHeaders.CONTENT_LENGTH, file.length() )
                .header( "Accept-Ranges", "bytes" )
                .build();
    }


    @GET
    @Path("/stream")
    @Produces("video/mp4")
    public Response stream( @HeaderParam("Range") String range ) throws Exception {
        File file = new File( FILE_PATH );

        return buildStream( file, range );
    }

    /**
     * @param asset Media file
     * @param range range header
     * @return Streaming output
     * @throws Exception IOException if an error occurs in streaming.
     */
    private Response buildStream( final File asset, final String range ) throws Exception {
        // range not requested: firefox does not send range headers
        if ( range == null ) {
            StreamingOutput streamer = output -> {
                try (FileChannel inputChannel = new FileInputStream( asset ).getChannel();
                     WritableByteChannel outputChannel = Channels.newChannel( output ) ) {

                    inputChannel.transferTo( 0, inputChannel.size(), outputChannel );
                }
                catch( IOException io ) {
                    System.out.println("Fail 1: " + io.getMessage());
                }
            };

            return Response.ok( streamer )
                    .status( Response.Status.OK )
                    .header( HttpHeaders.CONTENT_LENGTH, asset.length() )
                    .build();
        }

        String[] ranges = range.split( "=" )[1].split( "-" );

        int from = Integer.parseInt( ranges[0] );

        // Chunk media if the range upper bound is unspecified
        int to = chunk_size + from;

        if ( to >= asset.length() ) {
            to = (int) ( asset.length() - 1 );
        }

        // uncomment to let the client decide the upper bound
        // we want to send 2 MB chunks all the time
        //if ( ranges.length == 2 ) {
        //    to = Integer.parseInt( ranges[1] );
        //}

        final String responseRange = String.format( "bytes %d-%d/%d", from, to, asset.length() );


        final RandomAccessFile raf = new RandomAccessFile( asset, "r" );
        raf.seek( from );

        final int len = to - from + 1;
        final MediaStreamer mediaStreamer = new MediaStreamer( len, raf );

        return Response.ok( mediaStreamer )
                .status( Response.Status.PARTIAL_CONTENT )
                .header( "Accept-Ranges", "bytes" )
                .header( "Content-Range", responseRange )
                .header( HttpHeaders.CONTENT_LENGTH, mediaStreamer. getLenth() )
                .header( HttpHeaders.LAST_MODIFIED, new Date( asset.lastModified() ) )
                .build();
    }


}

class MediaStreamer implements StreamingOutput {

    private int length;
    private RandomAccessFile raf;
    final byte[] buf = new byte[4096];

    public MediaStreamer( int length, RandomAccessFile raf ) {
        this.length = length;
        this.raf = raf;
    }


    public int getLenth() {
        return length;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException, WebApplicationException {
        try {
            while( length != 0) {
                int read = raf.read( buf, 0, buf.length > length ? length : buf.length );
                outputStream.write( buf, 0, read );
                length -= read;
            }
        }
        finally {
            raf.close();
        }
    }
}

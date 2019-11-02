package si.fri.rso.services.classes;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public class MediaStreamer implements StreamingOutput {

    private int length;
    private RandomAccessFile raf;
    private final byte[] buf = new byte[4096];

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
                int read = raf.read( buf, 0, Math.min(buf.length, length));
                outputStream.write( buf, 0, read );
                length -= read;
            }
        }
        finally {
            raf.close();
        }
    }
}

package ski.crunch.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ConvertibleOutputStream extends ByteArrayOutputStream {

    //Creates InputStream without actually copying the buffer and using up memory for that.
    public InputStream toInputStream(){
        return new ByteArrayInputStream(buf, 0, count);
    }
}

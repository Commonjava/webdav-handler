package net.sf.webdav.testutil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.Ignore;

@Ignore
public class TestingOutputStream
    extends OutputStream
{

    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    @Override
    public void write( final int i )
        throws IOException
    {
        baos.write( i );
    }

    @Override
    public String toString()
    {
        return baos.toString();
    }
}

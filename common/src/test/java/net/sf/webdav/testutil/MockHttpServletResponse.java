package net.sf.webdav.testutil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sf.webdav.WebdavStatus;
import net.sf.webdav.spi.WebdavResponse;

public class MockHttpServletResponse
    implements WebdavResponse
{

    public static final String DATE_PATTERN = "EEE, dd MMM yyyy HH:mm:ss zzz";

    private WebdavStatus status;

    private OutputStream outputStream = new ByteArrayOutputStream();

    private Map<String, String> headers = new HashMap<String, String>();

    private String redirectUrl;

    private WebdavStatus errorStatus;

    private String characterEncoding;

    @Override
    public void setStatus( final WebdavStatus status )
    {
        this.status = status;
    }

    @Override
    public Writer getWriter()
    {
        return outputStream == null ? null : new OutputStreamWriter( outputStream );
    }

    @Override
    public String encodeRedirectURL( final String url )
    {
        // FIXME: This is wrong.
        return url;
    }

    @Override
    public void sendRedirect( final String redirectUrl )
    {
        this.redirectUrl = redirectUrl;
    }

    @Override
    public void addHeader( final String name, final String value )
    {
        headers.put( name, value );
    }

    @Override
    public void sendError( final WebdavStatus status )
    {
        errorStatus = status;
    }

    @Override
    public void sendError( final WebdavStatus status, final String requestURI )
    {
        errorStatus = status;
        redirectUrl = requestURI;
    }

    @Override
    public void setDateHeader( final String name, final long lastModified )
    {
        headers.put( name, new SimpleDateFormat( DATE_PATTERN ).format( new Date( lastModified ) ) );
    }

    @Override
    public void setHeader( final String name, final String value )
    {
        headers.put( name, value );
    }

    @Override
    public void setContentType( final String mimeType )
    {
        headers.put( "Content-Type", mimeType );
    }

    @Override
    public void setContentLength( final int resourceLength )
    {
        headers.put( "Content-Length", Integer.toString( resourceLength ) );
    }

    @Override
    public OutputStream getOutputStream()
        throws IOException
    {
        return outputStream;
    }

    @Override
    public void setCharacterEncoding( final String characterEncoding )
    {
        this.characterEncoding = characterEncoding;
    }

    public WebdavStatus getStatus()
    {
        return status;
    }

    public Map<String, String> getHeaders()
    {
        return headers;
    }

    public String getRedirectUrl()
    {
        return redirectUrl;
    }

    public WebdavStatus getErrorStatus()
    {
        return errorStatus;
    }

    public String getCharacterEncoding()
    {
        return characterEncoding;
    }

    public void setOutputStream( final OutputStream outputStream )
    {
        this.outputStream = outputStream;
    }

    public void setHeaders( final Map<String, String> headers )
    {
        this.headers = headers;
    }

    public void setRedirectUrl( final String redirectUrl )
    {
        this.redirectUrl = redirectUrl;
    }

    public void setErrorStatus( final WebdavStatus errorStatus )
    {
        this.errorStatus = errorStatus;
    }

}

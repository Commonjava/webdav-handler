package org.commonjava.web.vertx.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sf.webdav.WebdavStatus;
import net.sf.webdav.spi.WebdavResponse;
import net.sf.webdav.util.URLEncoder;

import org.commonjava.web.vertx.util.VertXOutputStream;
import org.vertx.java.core.http.HttpServerResponse;

public class VertXWebdavResponse
    implements WebdavResponse
{

    public static final String CHARSET_HEADER_SEPARATOR = ";\\s*charset=";

    public static final String DATE_HEADER_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

    private final HttpServerResponse response;

    private String contentType;

    private String encoding;

    public VertXWebdavResponse( final HttpServerResponse response )
    {
        this.response = response;
    }

    @Override
    public void setStatus( final WebdavStatus status )
    {
        response.setStatusCode( status.code() )
                .setStatusMessage( status.message() );
    }

    @Override
    public Writer getWriter()
        throws IOException
    {
        return new OutputStreamWriter( getOutputStream() );
    }

    @Override
    public String encodeRedirectURL( final String url )
    {
        return new URLEncoder().encode( url );
    }

    @Override
    public void sendRedirect( final String redirectUrl )
        throws IOException
    {
        response.setStatusCode( WebdavStatus.SC_MOVED_TEMPORARILY.code() );
        response.setStatusMessage( WebdavStatus.SC_MOVED_TEMPORARILY.message() );
        response.putHeader( "Location", redirectUrl );
    }

    @Override
    public void addHeader( final String name, final String value )
    {
        response.putHeader( name, value );
    }

    @Override
    public void sendError( final WebdavStatus status )
        throws IOException
    {
        setStatus( status );
        response.end();
    }

    @Override
    public void sendError( final WebdavStatus status, final String message )
        throws IOException
    {
        setStatus( status );
        response.write( message );
        response.end();
    }

    @Override
    public void setDateHeader( final String name, final long date )
    {
        setHeader( name, new SimpleDateFormat( DATE_HEADER_FORMAT ).format( new Date( date ) ) );
    }

    @Override
    public void setHeader( final String name, final String value )
    {
        response.putHeader( name, value );
    }

    @Override
    public void setContentType( final String contentType )
    {
        this.contentType = contentType;
        setContentTypeHeader();
    }

    @Override
    public void setContentLength( final int length )
    {
        setHeader( "Content-Length", Integer.toString( length ) );
    }

    @Override
    public OutputStream getOutputStream()
        throws IOException
    {
        return new VertXOutputStream( response )
        {

            @Override
            public synchronized void close()
                throws IOException
            {
                super.close();
                response.end();
            }

        };
    }

    @Override
    public void setCharacterEncoding( final String encoding )
    {
        this.encoding = encoding;
        setContentTypeHeader();
    }

    protected void setContentTypeHeader()
    {
        if ( contentType == null || encoding == null )
        {
            return;
        }

        final int idx = contentType.indexOf( CHARSET_HEADER_SEPARATOR );
        if ( idx > -1 )
        {
            contentType = contentType.substring( 0, idx ) + CHARSET_HEADER_SEPARATOR + encoding;
        }

        setHeader( "Content-Type", contentType );
    }

}

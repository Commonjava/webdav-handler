package org.commonjava.web.vertx.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import net.sf.webdav.WebdavStatus;
import net.sf.webdav.spi.WebdavResponse;

import org.vertx.java.core.http.HttpServerResponse;

public class VertXWebdavResponse
    implements WebdavResponse
{

    private final HttpServerResponse response;

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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String encodeRedirectURL( final String url )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void sendRedirect( final String redirectUrl )
        throws IOException
    {
        // TODO Auto-generated method stub

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
    public void sendError( final WebdavStatus status, final String requestUri )
        throws IOException
    {
        // TODO: Deal with request uri appropriately...
        setStatus( status );
        response.end();
    }

    @Override
    public void setDateHeader( final String name, final long date )
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setHeader( final String name, final String value )
    {
        response.putHeader( name, value );
    }

    @Override
    public void setContentType( final String type )
    {
        setHeader( "Content-Type", type );
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setCharacterEncoding( final String encoding )
    {
        // TODO Auto-generated method stub

    }

}

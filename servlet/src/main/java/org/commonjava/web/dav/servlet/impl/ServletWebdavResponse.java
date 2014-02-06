package org.commonjava.web.dav.servlet.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

import net.sf.webdav.WebdavStatus;
import net.sf.webdav.spi.WebdavResponse;

import org.commonjava.util.logging.Logger;

public class ServletWebdavResponse
    implements WebdavResponse
{

    private final Logger logger = new Logger( getClass() );

    private final HttpServletResponse resp;

    public ServletWebdavResponse( final HttpServletResponse resp )
    {
        this.resp = resp;
    }

    @Override
    public void setStatus( final WebdavStatus status )
    {
        resp.setStatus( status.code() );
    }

    @Override
    public Writer getWriter()
        throws IOException
    {
        logger.info( "Getting writer" );
        return resp.getWriter();
    }

    @Override
    public String encodeRedirectURL( final String url )
    {
        logger.info( "Encoding redirect URL: '%s'", url );
        return resp.encodeRedirectURL( url );
    }

    @Override
    public void sendRedirect( final String redirectUrl )
        throws IOException
    {
        logger.info( "Sending redirect: '%s'", redirectUrl );
        resp.sendRedirect( redirectUrl );
    }

    @Override
    public void addHeader( final String name, final String value )
    {
        resp.addHeader( name, value );
    }

    @Override
    public void sendError( final WebdavStatus status )
        throws IOException
    {
        resp.sendError( status.code() );
    }

    @Override
    public void sendError( final WebdavStatus status, final String requestUri )
        throws IOException
    {
        resp.sendError( status.code(), requestUri );
    }

    @Override
    public void setDateHeader( final String name, final long date )
    {
        resp.setDateHeader( name, date );
    }

    @Override
    public void setHeader( final String name, final String value )
    {
        resp.setHeader( name, value );
    }

    @Override
    public void setContentType( final String type )
    {
        resp.setContentType( type );
    }

    @Override
    public void setContentLength( final int length )
    {
        resp.setContentLength( length );
    }

    @Override
    public OutputStream getOutputStream()
        throws IOException
    {
        logger.info( "Getting input stream" );
        return resp.getOutputStream();
    }

    @Override
    public void setCharacterEncoding( final String encoding )
    {
        resp.setCharacterEncoding( encoding );
    }

}

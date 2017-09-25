/**
 * Copyright (C) 2006-2017 Apache Software Foundation (https://sourceforge.net/p/webdav-servlet, https://github.com/Commonjava/webdav-handler)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.web.vertx.impl;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sf.webdav.WebdavStatus;
import net.sf.webdav.spi.WebdavResponse;
import net.sf.webdav.util.URLEncoder;

import org.apache.commons.io.IOUtils;
import org.commonjava.vertx.vabr.util.VertXOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.http.HttpServerResponse;

public class VertXWebdavResponse
    implements WebdavResponse, Closeable
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    public static final String CHARSET_HEADER_SEPARATOR = ";\\s*charset=";

    public static final String DATE_HEADER_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

    private final HttpServerResponse response;

    private String contentType;

    private String encoding;

    private VertXOutputStream outstream;

    private Integer contentLength;

    public VertXWebdavResponse( final HttpServerResponse response )
    {
        this.response = response;
    }

    @Override
    public void setStatus( final WebdavStatus status )
    {
        logger.info( "Setting status: {}", status );
        response.setStatusCode( status.code() )
                .setStatusMessage( status.message() );
    }

    @Override
    public Writer getWriter()
        throws IOException
    {
        logger.info( "Getting writer" );
        return new OutputStreamWriter( getOutputStream() );
    }

    @Override
    public String encodeRedirectURL( final String url )
    {
        logger.info( "Encoding redirect URL: '{}'", url );
        return new URLEncoder().encode( url );
    }

    @Override
    public void sendRedirect( final String redirectUrl )
        throws IOException
    {
        logger.info( "Sending redirect: '{}'", redirectUrl );
        response.setStatusCode( WebdavStatus.SC_MOVED_TEMPORARILY.code() );
        response.setStatusMessage( WebdavStatus.SC_MOVED_TEMPORARILY.message() );
        response.putHeader( "Location", redirectUrl );
        response.end();
    }

    @Override
    public void addHeader( final String name, final String value )
    {
        setHeader( name, value );
    }

    @Override
    public void sendError( final WebdavStatus status )
        throws IOException
    {
        logger.info( "Setting status: {}\nNo Message", status );
        setStatus( status );
        response.end();
    }

    @Override
    public void sendError( final WebdavStatus status, final String message )
        throws IOException
    {
        logger.info( "Setting status: {}\nMessage: {}", status, message );
        setStatus( status );
        response.write( message );
    }

    @Override
    public void setDateHeader( final String name, final long date )
    {
        setHeader( name, new SimpleDateFormat( DATE_HEADER_FORMAT ).format( new Date( date ) ) );
    }

    @Override
    public void setHeader( final String name, final String value )
    {
        logger.info( "Set header '{}' = '{}'", name, value );
        response.putHeader( name, value );
    }

    @Override
    public void setContentType( final String contentType )
    {
        this.contentType = contentType;
        setContentTypeHeader();
    }

    @Override
    public void setContentLength( final int contentLength )
    {
        this.contentLength = contentLength;
        setHeader( "Content-Length", Integer.toString( contentLength ) );
    }

    @Override
    public synchronized OutputStream getOutputStream()
        throws IOException
    {
        if ( this.contentLength == null )
        {
            response.setChunked( true );
        }

        logger.info( "Getting output stream" );
        if ( outstream == null )
        {
            outstream = new VertXOutputStream( response );
        }

        return outstream;
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

    @Override
    public void close()
    {
        IOUtils.closeQuietly( outstream );
    }

}

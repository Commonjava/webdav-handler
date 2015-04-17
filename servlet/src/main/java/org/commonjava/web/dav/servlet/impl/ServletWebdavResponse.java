/**
 * Copyright (C) 2006 Apache Software Foundation (jdcasey@commonjava.org)
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
package org.commonjava.web.dav.servlet.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

import net.sf.webdav.WebdavStatus;
import net.sf.webdav.spi.WebdavResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServletWebdavResponse
    implements WebdavResponse
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

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
        logger.info( "Encoding redirect URL: '{}'", url );
        return resp.encodeRedirectURL( url );
    }

    @Override
    public void sendRedirect( final String redirectUrl )
        throws IOException
    {
        logger.info( "Sending redirect: '{}'", redirectUrl );
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

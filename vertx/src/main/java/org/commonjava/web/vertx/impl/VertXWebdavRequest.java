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
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Locale;
import java.util.Set;

import net.sf.webdav.exceptions.WebdavException;
import net.sf.webdav.spi.WebdavRequest;
import net.sf.webdav.util.JoinString;
import net.sf.webdav.util.RequestUtil;

import org.commonjava.vertx.vabr.util.VertXInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.http.HttpServerRequest;

public class VertXWebdavRequest
    implements WebdavRequest, Closeable
{

    private static final String CONTENT_LENGTH = "Content-Length";

    private static final String ACCEPT_LANGUAGE = "Accept-Language";

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final HttpServerRequest request;

    private final String contextPath;

    private final Principal userPrincipal;

    private final String serviceTopPath;

    private final String serviceSubPath;

    private URI requestUri;

    public VertXWebdavRequest( final HttpServerRequest request, final String contextPath, final String serviceTopPath, final String serviceSubPath,
                               final Principal userPrincipal )
        throws WebdavException
    {
        request.pause();
        this.request = request;
        this.serviceTopPath = serviceTopPath;
        this.contextPath = contextPath;
        this.serviceSubPath = serviceSubPath;
        this.userPrincipal = userPrincipal;

        final String hostHeader = request.headers()
                                         .get( "Host" );

        try
        {
            String hostAndPort = request.absoluteURI()
                                        .getHost();

            final int port = request.absoluteURI()
                                    .getPort();
            if ( port != 80 && port != 443 )
            {
                hostAndPort += ":" + port;
            }

            final String uri = request.absoluteURI()
                                      .toString();

            final int idx = uri.indexOf( hostAndPort );

            final StringBuilder sb = new StringBuilder();
            sb.append( uri.substring( 0, idx ) );
            sb.append( hostHeader );
            sb.append( uri.substring( idx + hostAndPort.length() ) );

            this.requestUri = new URI( sb.toString() );
        }
        catch ( final URISyntaxException e )
        {
            throw new WebdavException( "Failed to construct requestUri: {}", e, e.getMessage() );
        }
    }

    @Override
    public String getMethod()
    {
        logger.info( "Method: {}", request.method() );
        return request.method();
    }

    @Override
    public Principal getUserPrincipal()
    {
        logger.info( "User-Principal: {}", userPrincipal );
        return userPrincipal;
    }

    @Override
    public String getRequestURI()
    {
        logger.info( "Request-URI: {}", requestUri );
        return requestUri.toString();
    }

    @Override
    public Set<String> getHeaderNames()
    {
        logger.info( "Header-Names: {}", new JoinString( ", ", request.headers()
                                                                      .names() ) );
        return request.headers()
                      .names();
    }

    @Override
    public String getHeader( final String name )
    {
        logger.info( "Header: {} = {}", name, request.headers()
                                                     .get( name ) );
        return request.headers()
                      .get( name );
    }

    @Override
    public Set<String> getAttributeNames()
    {
        logger.info( "Attribute-Names: null" );
        return null;
    }

    @Override
    public String getAttribute( final String name )
    {
        logger.info( "Attribute: {} = null", name );
        return null;
    }

    @Override
    public Set<String> getParameterNames()
    {
        logger.info( "Get-param-names: {}", new JoinString( ", ", request.params()
                                                                         .names() ) );
        return request.params()
                      .names();
    }

    @Override
    public String getParameter( final String name )
    {
        logger.info( "Get-param: {} = {}", name, request.params()
                                                        .get( "q:" + name ) );
        return request.params()
                      .get( "q:" + name );
    }

    @Override
    public String getPathInfo()
    {
        logger.info( "Path-Info: '{}'", serviceSubPath );
        return serviceSubPath;
    }

    @Override
    public Locale getLocale()
    {
        return RequestUtil.parseLocale( getHeader( ACCEPT_LANGUAGE ) );
    }

    @Override
    public String getServerName()
    {
        logger.info( "Server-Name: '{}'", request.absoluteURI()
                                                 .getHost() );
        return requestUri.getHost();
    }

    @Override
    public String getContextPath()
    {
        logger.info( "Context-Path: '{}'", contextPath );
        return contextPath;
    }

    @Override
    public String getServicePath()
    {
        logger.info( "Service-Path: '{}'", serviceTopPath );
        return serviceTopPath;
    }

    @Override
    public InputStream getInputStream()
        throws IOException
    {
        logger.info( "Getting input stream" );
        return new VertXInputStream( request );
    }

    @Override
    public int getContentLength()
    {
        final String val = getHeader( CONTENT_LENGTH );
        final int len = val == null ? -1 : Integer.parseInt( val );

        logger.info( "Content-Length: {}", len );
        return len;
    }

    @Override
    public void close()
        throws IOException
    {
        request.resume();
    }

}

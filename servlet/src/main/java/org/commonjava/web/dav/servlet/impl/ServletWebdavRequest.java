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
import java.io.InputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import net.sf.webdav.spi.WebdavRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServletWebdavRequest
    implements WebdavRequest
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    private final HttpServletRequest req;

    public ServletWebdavRequest( final HttpServletRequest req )
    {
        this.req = req;
    }

    @Override
    public String getMethod()
    {
        return req.getMethod();
    }

    @Override
    public Principal getUserPrincipal()
    {
        return req.getUserPrincipal();
    }

    @Override
    public String getRequestURI()
    {
        return req.getRequestURL()
                  .toString();
    }

    @Override
    public Set<String> getHeaderNames()
    {
        final ArrayList<String> list = Collections.list( req.getHeaderNames() );
        return new HashSet<String>( list );
    }

    @Override
    public String getHeader( final String name )
    {
        return req.getHeader( name );
    }

    @Override
    public Set<String> getAttributeNames()
    {
        final ArrayList<String> list = Collections.list( req.getAttributeNames() );
        return new HashSet<String>( list );
    }

    @Override
    public String getAttribute( final String name )
    {
        final Object val = req.getAttribute( name );
        return val == null ? null : val.toString();
    }

    @Override
    public Set<String> getParameterNames()
    {
        final ArrayList<String> list = Collections.list( req.getParameterNames() );
        return new HashSet<String>( list );
    }

    @Override
    public String getParameter( final String name )
    {
        return req.getParameter( name );
    }

    @Override
    public String getPathInfo()
    {
        logger.info( "Path-Info: {}", req.getPathInfo() );
        return req.getPathInfo();
    }

    @Override
    public Locale getLocale()
    {
        return req.getLocale();
    }

    @Override
    public String getServerName()
    {
        logger.info( "Server-Name: {}", req.getServerName() );
        return req.getServerName();
    }

    @Override
    public InputStream getInputStream()
        throws IOException
    {
        logger.info( "Getting input stream" );
        return req.getInputStream();
    }

    @Override
    public int getContentLength()
    {
        logger.info( "Content-Length: {}", req.getContentLength() );
        return req.getContentLength();
    }

    @Override
    public String getContextPath()
    {
        logger.info( "Context-Path: {}", req.getContextPath() );
        return req.getContextPath();
    }

    @Override
    public String getServicePath()
    {
        logger.info( "Service-Path: {}", req.getServletPath() );
        return req.getServletPath();
    }

}

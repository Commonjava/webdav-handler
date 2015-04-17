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
package org.commonjava.web.dav.servlet;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.webdav.WebdavService;
import net.sf.webdav.exceptions.WebdavException;
import net.sf.webdav.impl.LocalFileSystemStore;
import net.sf.webdav.spi.IMimeTyper;
import net.sf.webdav.spi.IWebdavStore;
import net.sf.webdav.spi.WebdavConfig;

import org.commonjava.web.dav.servlet.impl.ServletInitWebdavConfig;
import org.commonjava.web.dav.servlet.impl.ServletMimeTyper;
import org.commonjava.web.dav.servlet.impl.ServletWebdavRequest;
import org.commonjava.web.dav.servlet.impl.ServletWebdavResponse;

@ApplicationScoped
public class WebdavServlet
    extends HttpServlet
{

    private static final long serialVersionUID = 1L;

    private WebdavService dav;

    @Inject
    private WebdavConfig davConfig;

    @Inject
    private IWebdavStore store;

    @Inject
    private IMimeTyper mimeTyper;

    @Override
    protected void service( final HttpServletRequest req, final HttpServletResponse resp )
        throws ServletException, IOException
    {
        try
        {
            dav.service( new ServletWebdavRequest( req ), new ServletWebdavResponse( resp ) );
        }
        catch ( final WebdavException e )
        {
            throw new ServletException( "Failed to service request: " + e.getMessage(), e );
        }
    }

    @Override
    public void init( final ServletConfig config )
        throws ServletException
    {
        super.init( config );
        init();
    }

    @Override
    public void init()
        throws ServletException
    {
        if ( dav == null )
        {
            if ( store == null )
            {
                try
                {
                    store = initWebdavStore();
                }
                catch ( final WebdavException e )
                {
                    throw new ServletException( "Failed to initialize WebDAV store: " + e.getMessage(), e );
                }
            }

            if ( davConfig == null )
            {
                try
                {
                    davConfig = initWebdavConfig();
                }
                catch ( final WebdavException e )
                {
                    throw new ServletException( "Failed to initialize WebDAV configuration: " + e.getMessage(), e );
                }
            }

            if ( mimeTyper == null )
            {
                try
                {
                    mimeTyper = initMimeTyper();
                }
                catch ( final WebdavException e )
                {
                    throw new ServletException( "Failed to initialize WebDAV MIME-typer: " + e.getMessage(), e );
                }
            }

            dav = new WebdavService( davConfig, store, mimeTyper );
        }
    }

    @PostConstruct
    public void cdiInit()
    {
        dav = new WebdavService( davConfig, store, mimeTyper );
    }

    protected IMimeTyper initMimeTyper()
        throws WebdavException
    {
        return new ServletMimeTyper( getServletConfig().getServletContext() );
    }

    protected WebdavConfig initWebdavConfig()
        throws WebdavException
    {
        return new ServletInitWebdavConfig( getServletConfig() );
    }

    protected IWebdavStore initWebdavStore()
        throws WebdavException
    {
        final String rootPath = getServletConfig().getInitParameter( ServletInitWebdavConfig.LOCAL_ROOT_DIR );
        if ( rootPath == null )
        {
            throw new WebdavException( "No local filesystem root was specified for WebDAV default store!" );
        }

        final File root = new File( rootPath );
        return new LocalFileSystemStore( root );
    }

}

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
package org.commonjava.web.vertx;

import java.io.IOException;
import java.security.Principal;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import net.sf.webdav.WebdavService;
import net.sf.webdav.exceptions.WebdavException;
import net.sf.webdav.spi.IMimeTyper;
import net.sf.webdav.spi.IWebdavStore;
import net.sf.webdav.spi.WebdavConfig;

import org.apache.commons.io.IOUtils;
import org.commonjava.web.vertx.impl.VertXWebdavRequest;
import org.commonjava.web.vertx.impl.VertXWebdavResponse;
import org.vertx.java.core.http.HttpServerRequest;

public abstract class VertXWebdavService
{

    @Inject
    private IWebdavStore store;

    @Inject
    private WebdavConfig config;

    @Inject
    private IMimeTyper mimeTyper;

    private WebdavService service;

    protected VertXWebdavService()
    {
    }

    protected VertXWebdavService( final WebdavConfig config, final IWebdavStore store, final IMimeTyper mimeTyper )
    {
        service = new WebdavService( config, store, mimeTyper );
    }

    @PostConstruct
    public void cdiInit()
    {
        service = new WebdavService( config, store, mimeTyper );
    }

    protected void service( final HttpServerRequest request, final String contextPath, final String servicePath, final String serviceSubPath,
                            final Principal principal )
        throws WebdavException, IOException
    {
        final VertXWebdavResponse response = new VertXWebdavResponse( request.response() );
        VertXWebdavRequest req = null;

        try
        {
            req = new VertXWebdavRequest( request, contextPath, servicePath, serviceSubPath, principal );

            service.service( req, response );
        }
        finally
        {
            IOUtils.closeQuietly( req );
            IOUtils.closeQuietly( response );

            try
            {
                request.response()
                       .end();
            }
            catch ( final IllegalStateException e )
            {
                // TODO Do we need to log this? The end() call is defensive...
            }
        }

    }

}

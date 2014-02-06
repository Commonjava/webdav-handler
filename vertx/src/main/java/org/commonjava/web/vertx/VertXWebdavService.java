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

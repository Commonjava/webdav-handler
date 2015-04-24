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
package net.sf.webdav;

import java.io.IOException;
import java.security.Principal;
import java.util.Set;

import net.sf.webdav.exceptions.UnauthenticatedException;
import net.sf.webdav.exceptions.WebdavException;
import net.sf.webdav.locking.ResourceLocks;
import net.sf.webdav.methods.WebdavMethod;
import net.sf.webdav.spi.IMimeTyper;
import net.sf.webdav.spi.ITransaction;
import net.sf.webdav.spi.IWebdavStore;
import net.sf.webdav.spi.IWebdavStoreWorker;
import net.sf.webdav.spi.WebdavConfig;
import net.sf.webdav.spi.WebdavRequest;
import net.sf.webdav.spi.WebdavResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebdavService
{
    private static final boolean READ_ONLY = false;

    private final Logger LOG = LoggerFactory.getLogger( getClass() );

    private final ResourceLocks _resLocks;

    private final IWebdavStore store;

    private final WebdavConfig config;

    private final IMimeTyper mimeTyper;

    private WebdavResources resources;

    public WebdavService( final WebdavConfig config, final IWebdavStore store, final IMimeTyper mimeTyper )
    {
        this.config = config;
        this.store = store;
        this.mimeTyper = mimeTyper;
        _resLocks = new ResourceLocks();

        initialize();
    }

    private void initialize()
    {
        final boolean lazyFolderCreationOnPut = config.isLazyFolderCreationOnPut();

        final String dftIndexFile = config.getDefaultIndexPath();
        final String insteadOf404 = config.getAlt404Path();

        final boolean noContentLengthHeader = config.isOmitContentLengthHeaders();

        resources =
            new WebdavResources( dftIndexFile, insteadOf404, _resLocks, mimeTyper, !noContentLengthHeader, READ_ONLY,
                                 lazyFolderCreationOnPut );
    }

    /**
     * Handles the special WebDAV methods.
     */
    public void service( final WebdavRequest req, final WebdavResponse resp )
        throws WebdavException, IOException
    {

        final String methodName = req.getMethod();
        ITransaction transaction = null;
        boolean needRollback = false;

        if ( LOG.isTraceEnabled() )
        {
            debugRequest( methodName, req );
        }

        IWebdavStoreWorker worker = null;
        try
        {
            final Principal userPrincipal = req.getUserPrincipal();
            worker = store.newWorker();
            transaction = worker.begin( userPrincipal );
            needRollback = true;
            worker.checkAuthentication( transaction );
            resp.setStatus( WebdavStatus.SC_OK );

            WebdavMethod methodExecutor = null;
            try
            {
                methodExecutor = WebdavMethodType.getMethod( methodName );
                if ( methodExecutor == null )
                {
                    methodExecutor = WebdavMethodType.NO_IMPL.newExecutor();
                }

                LOG.info( "Executing: " + methodExecutor.getClass()
                                                        .getSimpleName() );

                methodExecutor.execute( transaction, req, resp, worker, resources );

                worker.commit( transaction );
                needRollback = false;
            }
            catch ( final IOException e )
            {
                final java.io.StringWriter sw = new java.io.StringWriter();
                final java.io.PrintWriter pw = new java.io.PrintWriter( sw );
                e.printStackTrace( pw );
                LOG.error( "IOException: " + sw.toString() );
                resp.sendError( WebdavStatus.SC_INTERNAL_SERVER_ERROR );
                worker.rollback( transaction );
                throw new WebdavException( "I/O error executing %s: %s", e, methodExecutor == null ? "MISSING METHOD"
                                : methodExecutor.getClass()
                                                                                          .getSimpleName(), e.getMessage() );
            }

        }
        catch ( final UnauthenticatedException e )
        {
            resp.sendError( WebdavStatus.SC_FORBIDDEN );
        }
        catch ( final WebdavException e )
        {
            final java.io.StringWriter sw = new java.io.StringWriter();
            final java.io.PrintWriter pw = new java.io.PrintWriter( sw );
            e.printStackTrace( pw );
            LOG.error( "WebdavException: " + sw.toString() );
            throw e;
        }
        catch ( final Exception e )
        {
            final java.io.StringWriter sw = new java.io.StringWriter();
            final java.io.PrintWriter pw = new java.io.PrintWriter( sw );
            e.printStackTrace( pw );
            LOG.error( "Exception: " + sw.toString() );
        }
        finally
        {
            if ( needRollback )
            {
                if ( worker != null )
                {
                    worker.rollback( transaction );
                }
            }
        }

    }

    private void debugRequest( final String methodName, final WebdavRequest req )
    {
        LOG.trace( "-----------" );
        LOG.trace( "WebdavServlet\n request: methodName = " + methodName );
        LOG.trace( "time: " + System.currentTimeMillis() );
        LOG.trace( "path: " + req.getRequestURI() );
        LOG.trace( "-----------" );
        Set<String> e = req.getHeaderNames();
        if ( e != null )
        {
            for ( final String s : e )
            {
                LOG.trace( "header: " + s + " " + req.getHeader( s ) );
            }
        }
        e = req.getAttributeNames();
        if ( e != null )
        {
            for ( final String s : e )
            {
                LOG.trace( "attribute: " + s + " " + req.getAttribute( s ) );
            }
        }
        e = req.getParameterNames();
        if ( e != null )
        {
            for ( final String s : e )
            {
                LOG.trace( "parameter: " + s + " " + req.getParameter( s ) );
            }
        }
    }

}

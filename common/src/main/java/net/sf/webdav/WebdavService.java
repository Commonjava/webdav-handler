package net.sf.webdav;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Set;

import net.sf.webdav.exceptions.UnauthenticatedException;
import net.sf.webdav.exceptions.WebdavException;
import net.sf.webdav.locking.ResourceLocks;
import net.sf.webdav.methods.DoCopy;
import net.sf.webdav.methods.DoDelete;
import net.sf.webdav.methods.DoGet;
import net.sf.webdav.methods.DoHead;
import net.sf.webdav.methods.DoLock;
import net.sf.webdav.methods.DoMkcol;
import net.sf.webdav.methods.DoMove;
import net.sf.webdav.methods.DoNotImplemented;
import net.sf.webdav.methods.DoOptions;
import net.sf.webdav.methods.DoPropfind;
import net.sf.webdav.methods.DoProppatch;
import net.sf.webdav.methods.DoPut;
import net.sf.webdav.methods.DoUnlock;
import net.sf.webdav.methods.WebdavMethod;
import net.sf.webdav.spi.IMimeTyper;
import net.sf.webdav.spi.ITransaction;
import net.sf.webdav.spi.IWebdavStore;
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

    private final HashMap<String, WebdavMethod> _methodMap = new HashMap<String, WebdavMethod>();

    public WebdavService( final WebdavConfig config, final IWebdavStore store, final IMimeTyper mimeTyper )
    {
        this.store = store;
        _resLocks = new ResourceLocks();

        final boolean lazyFolderCreationOnPut = config.isLazyFolderCreationOnPut();

        final String dftIndexFile = config.getDefaultIndexPath();
        final String insteadOf404 = config.getAlt404Path();

        final boolean noContentLengthHeader = config.isOmitContentLengthHeaders();

        register( "GET", new DoGet( store, dftIndexFile, insteadOf404, _resLocks, mimeTyper, !noContentLengthHeader ) );
        register( "HEAD", new DoHead( store, dftIndexFile, insteadOf404, _resLocks, mimeTyper, !noContentLengthHeader ) );
        final DoDelete doDelete = (DoDelete) register( "DELETE", new DoDelete( store, _resLocks, READ_ONLY ) );
        final DoCopy doCopy = (DoCopy) register( "COPY", new DoCopy( store, _resLocks, doDelete, READ_ONLY ) );
        register( "LOCK", new DoLock( store, _resLocks, READ_ONLY ) );
        register( "UNLOCK", new DoUnlock( store, _resLocks, READ_ONLY ) );
        register( "MOVE", new DoMove( _resLocks, doDelete, doCopy, READ_ONLY ) );
        register( "MKCOL", new DoMkcol( store, _resLocks, READ_ONLY ) );
        register( "OPTIONS", new DoOptions( store, _resLocks ) );
        register( "PUT", new DoPut( store, _resLocks, READ_ONLY, lazyFolderCreationOnPut ) );
        register( "PROPFIND", new DoPropfind( store, _resLocks, mimeTyper ) );
        register( "PROPPATCH", new DoProppatch( store, _resLocks, READ_ONLY ) );
        register( "*NO*IMPL*", new DoNotImplemented( READ_ONLY ) );
    }

    private WebdavMethod register( final String methodName, final WebdavMethod method )
    {
        _methodMap.put( methodName, method );
        return method;
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

        try
        {
            final Principal userPrincipal = req.getUserPrincipal();
            transaction = store.begin( userPrincipal );
            needRollback = true;
            store.checkAuthentication( transaction );
            resp.setStatus( WebdavStatus.SC_OK );

            WebdavMethod methodExecutor = null;
            try
            {
                methodExecutor = _methodMap.get( methodName );
                if ( methodExecutor == null )
                {
                    methodExecutor = _methodMap.get( "*NO*IMPL*" );
                }

                LOG.info( "Executing: " + methodExecutor.getClass()
                                                        .getSimpleName() );
                methodExecutor.execute( transaction, req, resp );

                store.commit( transaction );
                needRollback = false;
            }
            catch ( final IOException e )
            {
                final java.io.StringWriter sw = new java.io.StringWriter();
                final java.io.PrintWriter pw = new java.io.PrintWriter( sw );
                e.printStackTrace( pw );
                LOG.error( "IOException: " + sw.toString() );
                resp.sendError( WebdavStatus.SC_INTERNAL_SERVER_ERROR );
                store.rollback( transaction );
                throw new WebdavException( "I/O error executing %s: %s", e, methodExecutor.getClass()
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
                store.rollback( transaction );
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

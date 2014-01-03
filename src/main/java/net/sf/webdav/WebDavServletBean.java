package net.sf.webdav;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sf.webdav.exceptions.UnauthenticatedException;
import net.sf.webdav.exceptions.WebdavException;
import net.sf.webdav.fromcatalina.MD5Encoder;
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
import net.sf.webdav.spi.HttpServletRequest;
import net.sf.webdav.spi.HttpServletResponse;
import net.sf.webdav.spi.WebDavConfig;
import net.sf.webdav.spi.WebDavContext;
import net.sf.webdav.spi.WebDavException;

public class WebDavServletBean
{

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( WebDavServletBean.class );

    /**
     * MD5 message digest provider.
     */
    protected static MessageDigest MD5_HELPER;

    /**
     * The MD5 helper object for this class.
     */
    protected static final MD5Encoder MD5_ENCODER = new MD5Encoder();

    private static final boolean READ_ONLY = false;

    private final ResourceLocks _resLocks;

    private IWebdavStore _store;

    private final HashMap<String, IMethodExecutor> _methodMap = new HashMap<String, IMethodExecutor>();

    private final Map<String, String> initParams;

    public WebDavServletBean( final Map<String, String> initParams )
    {
        this.initParams = initParams;
        _resLocks = new ResourceLocks();

        try
        {
            MD5_HELPER = MessageDigest.getInstance( "MD5" );
        }
        catch ( final NoSuchAlgorithmException e )
        {
            throw new IllegalStateException();
        }
    }

    public void init( final IWebdavStore store, final String dftIndexFile, final String insteadOf404, final int nocontentLenghHeaders,
                      final boolean lazyFolderCreationOnPut )
    {

        _store = store;

        final IMimeTyper mimeTyper = new IMimeTyper()
        {
            @Override
            public String getMimeType( final String path )
            {
                return getContext().getMimeType( path );
            }
        };

        register( "GET", new DoGet( store, dftIndexFile, insteadOf404, _resLocks, mimeTyper, nocontentLenghHeaders ) );
        register( "HEAD", new DoHead( store, dftIndexFile, insteadOf404, _resLocks, mimeTyper, nocontentLenghHeaders ) );
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

    protected String getInitParameter( final String name )
    {
        return initParams.get( name );
    }

    protected int getIntInitParameter( final String key )
    {
        return getInitParameter( key ) == null ? -1 : Integer.parseInt( getInitParameter( key ) );
    }

    protected WebDavConfig getConfig()
    {
        // TODO Auto-generated method stub
        return null;
    }

    protected WebDavContext getContext()
    {
        // TODO Auto-generated method stub
        return null;
    }

    private IMethodExecutor register( final String methodName, final IMethodExecutor method )
    {
        _methodMap.put( methodName, method );
        return method;
    }

    /**
     * Handles the special WebDAV methods.
     */
    protected void service( final HttpServletRequest req, final HttpServletResponse resp )
        throws WebDavException, IOException
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
            transaction = _store.begin( userPrincipal );
            needRollback = true;
            _store.checkAuthentication( transaction );
            resp.setStatus( WebdavStatus.SC_OK );

            try
            {
                IMethodExecutor methodExecutor = _methodMap.get( methodName );
                if ( methodExecutor == null )
                {
                    methodExecutor = _methodMap.get( "*NO*IMPL*" );
                }

                methodExecutor.execute( transaction, req, resp );

                _store.commit( transaction );
                needRollback = false;
            }
            catch ( final IOException e )
            {
                final java.io.StringWriter sw = new java.io.StringWriter();
                final java.io.PrintWriter pw = new java.io.PrintWriter( sw );
                e.printStackTrace( pw );
                LOG.error( "IOException: " + sw.toString() );
                resp.sendError( WebdavStatus.SC_INTERNAL_SERVER_ERROR );
                _store.rollback( transaction );
                throw new WebDavException( e );
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
            throw new WebDavException( e );
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
                _store.rollback( transaction );
            }
        }

    }

    private void debugRequest( final String methodName, final HttpServletRequest req )
    {
        LOG.trace( "-----------" );
        LOG.trace( "WebdavServlet\n request: methodName = " + methodName );
        LOG.trace( "time: " + System.currentTimeMillis() );
        LOG.trace( "path: " + req.getRequestURI() );
        LOG.trace( "-----------" );
        Set<String> e = req.getHeaderNames();
        for ( final String s : e )
        {
            LOG.trace( "header: " + s + " " + req.getHeader( s ) );
        }
        e = req.getAttributeNames();
        for ( final String s : e )
        {
            LOG.trace( "attribute: " + s + " " + req.getAttribute( s ) );
        }
        e = req.getParameterNames();
        for ( final String s : e )
        {
            LOG.trace( "parameter: " + s + " " + req.getParameter( s ) );
        }
    }

}

package net.sf.webdav.methods;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;

import net.sf.webdav.StoredObject;
import net.sf.webdav.WebdavStatus;
import net.sf.webdav.locking.IResourceLocks;
import net.sf.webdav.locking.LockedObject;
import net.sf.webdav.locking.ResourceLocks;
import net.sf.webdav.spi.ITransaction;
import net.sf.webdav.spi.IWebdavStore;
import net.sf.webdav.spi.WebdavRequest;
import net.sf.webdav.spi.WebdavResponse;
import net.sf.webdav.testutil.MockTest;

import org.jmock.Expectations;
import org.junit.Test;

public class DoLockTest
    extends MockTest
{

    static IWebdavStore mockStore;

    static WebdavRequest mockReq;

    static WebdavResponse mockRes;

    static ITransaction mockTransaction;

    static IResourceLocks mockResourceLocks;

    static boolean exclusive = true;

    static String depthString = "-1";

    static int depth = -1;

    static String timeoutString = "10";

    @Override
    public void setupFixtures()
        throws Exception
    {
        mockStore = _mockery.mock( IWebdavStore.class );
        mockReq = _mockery.mock( WebdavRequest.class );
        mockRes = _mockery.mock( WebdavResponse.class );
        mockTransaction = _mockery.mock( ITransaction.class );
        mockResourceLocks = _mockery.mock( IResourceLocks.class );
    }

    @Test
    public void testDoLockIfReadOnly()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockRes ).sendError( WebdavStatus.SC_FORBIDDEN );
            }
        } );

        final ResourceLocks resLocks = new ResourceLocks();

        final DoLock doLock = new DoLock( mockStore, resLocks, readOnly );
        doLock.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testDoRefreshLockOnLockedResource()
        throws Exception
    {

        final String lockPath = "/aFileToLock";
        final String lockOwner = "owner";

        final ResourceLocks resLocks = new ResourceLocks();
        resLocks.lock( mockTransaction, lockPath, lockOwner, exclusive, depth, TEMP_TIMEOUT, !TEMPORARY );

        final LockedObject lo = resLocks.getLockedObjectByPath( mockTransaction, lockPath );
        final String lockTokenString = lo.getID();
        final String lockToken = "(<opaquelocktoken:" + lockTokenString + ">)";

        final PrintWriter pw = new PrintWriter( "/tmp/XMLTestFile" );

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( lockPath ) );

                one( mockReq ).getHeader( "If" );
                will( returnValue( lockToken ) );

                one( mockReq ).getHeader( "User-Agent" );
                will( returnValue( "Goliath" ) );

                exactly( 2 ).of( mockReq )
                            .getHeader( "If" );
                will( returnValue( lockToken ) );

                one( mockReq ).getHeader( "Timeout" );
                will( returnValue( "Infinite" ) );

                one( mockRes ).setStatus( WebdavStatus.SC_OK );

                one( mockRes ).setContentType( "text/xml; charset=UTF-8" );

                one( mockRes ).getWriter();
                will( returnValue( pw ) );

                one( mockRes ).addHeader( "Lock-Token", lockToken.substring( lockToken.indexOf( "(" ) + 1, lockToken.indexOf( ")" ) ) );
            }
        } );

        final DoLock doLock = new DoLock( mockStore, resLocks, !readOnly );
        doLock.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testDoExclusiveLockOnResource()
        throws Exception
    {

        final String lockPath = "/aFileToLock";

        final ResourceLocks resLocks = new ResourceLocks();
        final PrintWriter pw = new PrintWriter( "/tmp/XMLTestFile" );

        final ByteArrayInputStream baisExclusive = new ByteArrayInputStream( exclusiveLockRequestByteArray );
        //        final DelegatingServletInputStream dsisExclusive = new DelegatingServletInputStream(
        //                baisExclusive);

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( lockPath ) );

                one( mockReq ).getHeader( "User-Agent" );
                will( returnValue( "Goliath" ) );

                one( mockReq ).getHeader( "If" );
                will( returnValue( null ) );

                final StoredObject so = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, lockPath );
                will( returnValue( so ) );

                one( mockReq ).getInputStream();
                will( returnValue( baisExclusive ) );

                one( mockReq ).getHeader( "Depth" );
                will( returnValue( depthString ) );

                one( mockReq ).getHeader( "Timeout" );
                will( returnValue( timeoutString ) );

                one( mockRes ).setStatus( WebdavStatus.SC_OK );

                one( mockRes ).setContentType( "text/xml; charset=UTF-8" );

                one( mockRes ).getWriter();
                will( returnValue( pw ) );

                // addHeader("Lock-Token", "(<opaquelocktoken:xxx-xxx-xxx>)")
                one( mockRes ).addHeader( with( any( String.class ) ), with( any( String.class ) ) );
            }
        } );

        final DoLock doLock = new DoLock( mockStore, resLocks, !readOnly );
        doLock.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testDoSharedLockOnResource()
        throws Exception
    {

        final String lockPath = "/aFileToLock";

        final ResourceLocks resLocks = new ResourceLocks();
        final PrintWriter pw = new PrintWriter( "/tmp/XMLTestFile" );

        final ByteArrayInputStream baisShared = new ByteArrayInputStream( sharedLockRequestByteArray );
        //        final DelegatingServletInputStream dsisShared = new DelegatingServletInputStream(
        //                baisShared);

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( lockPath ) );

                one( mockReq ).getHeader( "User-Agent" );
                will( returnValue( "Goliath" ) );

                one( mockReq ).getHeader( "If" );
                will( returnValue( null ) );

                final StoredObject so = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, lockPath );
                will( returnValue( so ) );

                one( mockReq ).getInputStream();
                will( returnValue( baisShared ) );

                one( mockReq ).getHeader( "Depth" );
                will( returnValue( depthString ) );

                one( mockReq ).getHeader( "Timeout" );
                will( returnValue( timeoutString ) );

                one( mockRes ).setStatus( WebdavStatus.SC_OK );

                one( mockRes ).setContentType( "text/xml; charset=UTF-8" );

                one( mockRes ).getWriter();
                will( returnValue( pw ) );

                // addHeader("Lock-Token", "(<opaquelocktoken:xxx-xxx-xxx>)")
                one( mockRes ).addHeader( with( any( String.class ) ), with( any( String.class ) ) );
            }
        } );

        final DoLock doLock = new DoLock( mockStore, resLocks, !readOnly );
        doLock.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testDoExclusiveLockOnCollection()
        throws Exception
    {

        final String lockPath = "/aFolderToLock";

        final ResourceLocks resLocks = new ResourceLocks();

        final PrintWriter pw = new PrintWriter( "/tmp/XMLTestFile" );

        final ByteArrayInputStream baisExclusive = new ByteArrayInputStream( exclusiveLockRequestByteArray );
        //        final DelegatingServletInputStream dsisExclusive = new DelegatingServletInputStream(
        //                baisExclusive);

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( lockPath ) );

                one( mockReq ).getHeader( "User-Agent" );
                will( returnValue( "Goliath" ) );

                one( mockReq ).getHeader( "If" );
                will( returnValue( null ) );

                final StoredObject so = initFolderStoredObject();

                one( mockStore ).getStoredObject( mockTransaction, lockPath );
                will( returnValue( so ) );

                one( mockReq ).getInputStream();
                will( returnValue( baisExclusive ) );

                one( mockReq ).getHeader( "Depth" );
                will( returnValue( depthString ) );

                one( mockReq ).getHeader( "Timeout" );
                will( returnValue( timeoutString ) );

                one( mockRes ).setStatus( WebdavStatus.SC_OK );

                one( mockRes ).setContentType( "text/xml; charset=UTF-8" );

                one( mockRes ).getWriter();
                will( returnValue( pw ) );

                // addHeader("Lock-Token", "(<opaquelocktoken:xxx-xxx-xxx>)")
                one( mockRes ).addHeader( with( any( String.class ) ), with( any( String.class ) ) );
            }
        } );

        final DoLock doLock = new DoLock( mockStore, resLocks, !readOnly );
        doLock.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testDoSharedLockOnCollection()
        throws Exception
    {

        final String lockPath = "/aFolderToLock";

        final ResourceLocks resLocks = new ResourceLocks();
        final PrintWriter pw = new PrintWriter( "/tmp/XMLTestFile" );

        final ByteArrayInputStream baisShared = new ByteArrayInputStream( sharedLockRequestByteArray );
        //        final DelegatingServletInputStream dsisShared = new DelegatingServletInputStream(
        //                baisShared);

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( lockPath ) );

                one( mockReq ).getHeader( "User-Agent" );
                will( returnValue( "Goliath" ) );

                one( mockReq ).getHeader( "If" );
                will( returnValue( null ) );

                final StoredObject so = initFolderStoredObject();

                one( mockStore ).getStoredObject( mockTransaction, lockPath );
                will( returnValue( so ) );

                one( mockReq ).getInputStream();
                will( returnValue( baisShared ) );

                one( mockReq ).getHeader( "Depth" );
                will( returnValue( depthString ) );

                one( mockReq ).getHeader( "Timeout" );
                will( returnValue( timeoutString ) );

                one( mockRes ).setStatus( WebdavStatus.SC_OK );

                one( mockRes ).setContentType( "text/xml; charset=UTF-8" );

                one( mockRes ).getWriter();
                will( returnValue( pw ) );

                // addHeader("Lock-Token", "(<opaquelocktoken:xxx-xxx-xxx>)")
                one( mockRes ).addHeader( with( any( String.class ) ), with( any( String.class ) ) );
            }
        } );

        final DoLock doLock = new DoLock( mockStore, resLocks, !readOnly );
        doLock.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testDoLockNullResourceLock()
        throws Exception
    {

        final String parentPath = "/parentCollection";
        final String lockPath = parentPath.concat( "/aNullResource" );

        final PrintWriter pw = new PrintWriter( "/tmp/XMLTestFile" );

        final ByteArrayInputStream baisExclusive = new ByteArrayInputStream( exclusiveLockRequestByteArray );
        //        final DelegatingServletInputStream dsisExclusive = new DelegatingServletInputStream(
        //                baisExclusive);

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( lockPath ) );

                LockedObject lockNullResourceLo = null;

                one( mockResourceLocks ).getLockedObjectByPath( mockTransaction, lockPath );
                will( returnValue( lockNullResourceLo ) );

                final LockedObject parentLo = null;

                one( mockResourceLocks ).getLockedObjectByPath( mockTransaction, parentPath );
                will( returnValue( parentLo ) );

                one( mockReq ).getHeader( "User-Agent" );
                will( returnValue( "Goliath" ) );

                one( mockResourceLocks ).lock( with( any( ITransaction.class ) ), with( any( String.class ) ), with( any( String.class ) ),
                                               with( any( boolean.class ) ), with( any( int.class ) ), with( any( int.class ) ),
                                               with( any( boolean.class ) ) );
                will( returnValue( true ) );

                one( mockReq ).getHeader( "If" );
                will( returnValue( null ) );

                StoredObject lockNullResourceSo = null;

                one( mockStore ).getStoredObject( mockTransaction, lockPath );
                will( returnValue( lockNullResourceSo ) );

                final StoredObject parentSo = null;

                one( mockStore ).getStoredObject( mockTransaction, parentPath );
                will( returnValue( parentSo ) );

                one( mockStore ).createFolder( mockTransaction, parentPath );

                one( mockStore ).getStoredObject( mockTransaction, lockPath );
                will( returnValue( lockNullResourceSo ) );

                one( mockStore ).createResource( mockTransaction, lockPath );

                one( mockRes ).setStatus( WebdavStatus.SC_CREATED );

                lockNullResourceSo = initLockNullStoredObject();

                one( mockStore ).getStoredObject( mockTransaction, lockPath );
                will( returnValue( lockNullResourceSo ) );

                one( mockReq ).getInputStream();
                will( returnValue( baisExclusive ) );

                one( mockReq ).getHeader( "Depth" );
                will( returnValue( ( "0" ) ) );

                one( mockReq ).getHeader( "Timeout" );
                will( returnValue( "Infinite" ) );

                final ResourceLocks resLocks = ResourceLocks.class.newInstance();

                one( mockResourceLocks ).exclusiveLock( mockTransaction, lockPath, "I'am the Lock Owner", 0, 604800 );
                will( returnValue( true ) );

                lockNullResourceLo = initLockNullLockedObject( resLocks, lockPath );

                one( mockResourceLocks ).getLockedObjectByPath( mockTransaction, lockPath );
                will( returnValue( lockNullResourceLo ) );

                one( mockRes ).setStatus( WebdavStatus.SC_OK );

                one( mockRes ).setContentType( "text/xml; charset=UTF-8" );

                one( mockRes ).getWriter();
                will( returnValue( pw ) );

                String loId = null;
                if ( lockNullResourceLo != null )
                {
                    loId = lockNullResourceLo.getID();
                }
                final String lockToken = "<opaquelocktoken:" + loId + ">";

                one( mockRes ).addHeader( "Lock-Token", lockToken );

                one( mockResourceLocks ).unlockTemporaryLockedObjects( with( any( ITransaction.class ) ), with( any( String.class ) ),
                                                                       with( any( String.class ) ) );
            }
        } );

        final DoLock doLock = new DoLock( mockStore, mockResourceLocks, !readOnly );
        doLock.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();

    }
}

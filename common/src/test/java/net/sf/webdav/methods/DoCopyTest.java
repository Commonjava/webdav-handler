package net.sf.webdav.methods;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;

import net.sf.webdav.StoredObject;
import net.sf.webdav.WebdavStatus;
import net.sf.webdav.locking.LockedObject;
import net.sf.webdav.locking.ResourceLocks;
import net.sf.webdav.spi.ITransaction;
import net.sf.webdav.spi.IWebdavStore;
import net.sf.webdav.spi.WebdavRequest;
import net.sf.webdav.spi.WebdavResponse;
import net.sf.webdav.testutil.MockTest;

import org.jmock.Expectations;
import org.junit.Test;

public class DoCopyTest
    extends MockTest
{

    static IWebdavStore mockStore;

    static WebdavRequest mockReq;

    static WebdavResponse mockRes;

    static ITransaction mockTransaction;

    static ByteArrayInputStream bais = new ByteArrayInputStream( resourceContent );

    //    static DelegatingServletInputStream dsis = new DelegatingServletInputStream( bais );

    @Override
    public void setupFixtures()
        throws Exception
    {
        mockStore = _mockery.mock( IWebdavStore.class );
        mockReq = _mockery.mock( WebdavRequest.class );
        mockRes = _mockery.mock( WebdavResponse.class );
        mockTransaction = _mockery.mock( ITransaction.class );
    }

    @Test
    public void testDoCopyIfReadOnly()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceFilePath ) );

                one( mockRes ).sendError( WebdavStatus.SC_FORBIDDEN );
            }
        } );

        final ResourceLocks resLocks = new ResourceLocks();
        final DoDelete doDelete = new DoDelete( mockStore, resLocks, readOnly );

        final DoCopy doCopy = new DoCopy( mockStore, resLocks, doDelete, readOnly );
        doCopy.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testDoCopyOfLockNullResource()
        throws Exception
    {

        final String parentPath = "/lockedFolder";
        final String path = parentPath.concat( "/nullFile" );

        final String owner = new String( "owner" );
        final ResourceLocks resLocks = new ResourceLocks();
        resLocks.lock( mockTransaction, parentPath, owner, true, 1, TEMP_TIMEOUT, !TEMPORARY );

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( path ) );

                one( mockReq ).getHeader( "Destination" );
                will( returnValue( "/destination" ) );

                one( mockReq ).getServerName();
                will( returnValue( "myServer" ) );

                one( mockReq ).getContextPath();
                will( returnValue( "" ) );

                one( mockReq ).getPathInfo();
                will( returnValue( "/destination" ) );

                one( mockReq ).getServicePath();
                will( returnValue( "/servletPath" ) );

                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( path ) );

                one( mockReq ).getHeader( "Overwrite" );
                will( returnValue( "T" ) );

                final StoredObject so = initLockNullStoredObject();

                one( mockStore ).getStoredObject( mockTransaction, path );
                will( returnValue( so ) );

                one( mockRes ).addHeader( "Allow", "OPTIONS, MKCOL, PUT, PROPFIND, LOCK, UNLOCK" );

                one( mockRes ).sendError( WebdavStatus.SC_METHOD_NOT_ALLOWED );
            }
        } );

        final DoDelete doDelete = new DoDelete( mockStore, resLocks, !readOnly );

        final DoCopy doCopy = new DoCopy( mockStore, resLocks, doDelete, !readOnly );
        doCopy.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testDoCopyIfParentIsLockedWithWrongLockToken()
        throws Exception
    {

        final String owner = new String( "owner" );
        final ResourceLocks resLocks = new ResourceLocks();
        resLocks.lock( mockTransaction, destCollectionPath, owner, true, 1, TEMP_TIMEOUT, !TEMPORARY );

        final LockedObject lo = resLocks.getLockedObjectByPath( mockTransaction, destCollectionPath );
        final String wrongLockToken = "(<opaquelocktoken:" + lo.getID() + "WRONG>)";

        final PrintWriter pw = new PrintWriter( "/tmp/XMLTestFile" );

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceFilePath ) );

                one( mockReq ).getHeader( "Destination" );
                will( returnValue( destFilePath ) );

                one( mockReq ).getServerName();
                will( returnValue( "myServer" ) );

                one( mockReq ).getContextPath();
                will( returnValue( "" ) );

                one( mockReq ).getPathInfo();
                will( returnValue( destFilePath ) );

                one( mockReq ).getServicePath();
                will( returnValue( "/servletPath" ) );

                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceFilePath ) );

                one( mockReq ).getHeader( "If" );
                will( returnValue( wrongLockToken ) );

                one( mockRes ).setStatus( WebdavStatus.SC_MULTI_STATUS );

                one( mockReq ).getRequestURI();
                will( returnValue( "http://foo.bar".concat( destCollectionPath ) ) );

                one( mockRes ).getWriter();
                will( returnValue( pw ) );
            }
        } );

        final DoDelete doDelete = new DoDelete( mockStore, resLocks, !readOnly );

        final DoCopy doCopy = new DoCopy( mockStore, resLocks, doDelete, !readOnly );
        doCopy.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testDoCopyIfParentIsLockedWithRightLockToken()
        throws Exception
    {

        final String owner = new String( "owner" );
        final ResourceLocks resLocks = new ResourceLocks();

        resLocks.lock( mockTransaction, destCollectionPath, owner, true, 1, TEMP_TIMEOUT, !TEMPORARY );

        final LockedObject lo = resLocks.getLockedObjectByPath( mockTransaction, destCollectionPath );
        final String rightLockToken = "(<opaquelocktoken:" + lo.getID() + ">)";

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceFilePath ) );

                one( mockReq ).getHeader( "Destination" );
                will( returnValue( destFilePath ) );

                one( mockReq ).getServerName();
                will( returnValue( "myServer" ) );

                one( mockReq ).getContextPath();
                will( returnValue( "" ) );

                one( mockReq ).getPathInfo();
                will( returnValue( destFilePath ) );

                one( mockReq ).getServicePath();
                will( returnValue( "/servletPath" ) );

                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceFilePath ) );

                one( mockReq ).getHeader( "If" );
                will( returnValue( rightLockToken ) );

                one( mockReq ).getHeader( "Overwrite" );
                will( returnValue( "F" ) );

                final StoredObject sourceFileSo = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, sourceFilePath );
                will( returnValue( sourceFileSo ) );

                StoredObject destFileSo = null;

                one( mockStore ).getStoredObject( mockTransaction, destFilePath );
                will( returnValue( destFileSo ) );

                one( mockRes ).setStatus( WebdavStatus.SC_CREATED );

                one( mockStore ).getStoredObject( mockTransaction, sourceFilePath );
                will( returnValue( sourceFileSo ) );

                one( mockStore ).createResource( mockTransaction, destFilePath );

                one( mockStore ).getResourceContent( mockTransaction, sourceFilePath );
                will( returnValue( bais ) );

                one( mockStore ).setResourceContent( mockTransaction, destFilePath, bais, null, null );
                will( returnValue( resourceLength ) );

                destFileSo = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, destFilePath );
                will( returnValue( destFileSo ) );

            }
        } );

        final DoDelete doDelete = new DoDelete( mockStore, resLocks, !readOnly );

        final DoCopy doCopy = new DoCopy( mockStore, resLocks, doDelete, !readOnly );
        doCopy.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testDoCopyIfDestinationPathInvalid()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceFilePath ) );

                one( mockReq ).getHeader( "Destination" );
                will( returnValue( null ) );

                one( mockRes ).sendError( WebdavStatus.SC_BAD_REQUEST );
            }
        } );

        final ResourceLocks resLocks = new ResourceLocks();
        final DoDelete doDelete = new DoDelete( mockStore, resLocks, !readOnly );

        final DoCopy doCopy = new DoCopy( mockStore, resLocks, doDelete, !readOnly );
        doCopy.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();

    }

    @Test
    public void testDoCopyIfSourceEqualsDestination()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceFilePath ) );

                one( mockReq ).getHeader( "Destination" );
                will( returnValue( sourceFilePath ) );

                one( mockReq ).getServerName();
                will( returnValue( "serverName" ) );

                one( mockReq ).getContextPath();
                will( returnValue( "" ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceFilePath ) );

                one( mockReq ).getServicePath();
                will( returnValue( "/servletPath" ) );

                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceFilePath ) );

                one( mockRes ).sendError( WebdavStatus.SC_FORBIDDEN );

            }
        } );

        final ResourceLocks resLocks = new ResourceLocks();

        final DoDelete doDelete = new DoDelete( mockStore, resLocks, !readOnly );

        final DoCopy doCopy = new DoCopy( mockStore, resLocks, doDelete, !readOnly );
        doCopy.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testDoCopyFolderIfNoLocks()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceCollectionPath ) );

                one( mockReq ).getHeader( "Destination" );
                will( returnValue( destCollectionPath ) );

                one( mockReq ).getServerName();
                will( returnValue( "serverName" ) );

                one( mockReq ).getContextPath();
                will( returnValue( "" ) );

                one( mockReq ).getPathInfo();
                will( returnValue( destCollectionPath ) );

                one( mockReq ).getServicePath();
                will( returnValue( "/servletPath" ) );

                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceCollectionPath ) );

                one( mockReq ).getHeader( "Overwrite" );
                will( returnValue( "F" ) );

                final StoredObject sourceCollectionSo = initFolderStoredObject();

                one( mockStore ).getStoredObject( mockTransaction, sourceCollectionPath );
                will( returnValue( sourceCollectionSo ) );

                final StoredObject destCollectionSo = null;

                one( mockStore ).getStoredObject( mockTransaction, destCollectionPath );
                will( returnValue( destCollectionSo ) );

                one( mockRes ).setStatus( WebdavStatus.SC_CREATED );

                one( mockStore ).getStoredObject( mockTransaction, sourceCollectionPath );
                will( returnValue( sourceCollectionSo ) );

                one( mockStore ).createFolder( mockTransaction, destCollectionPath );

                one( mockReq ).getHeader( "Depth" );
                will( returnValue( "-1" ) );

                sourceChildren = new String[] { "sourceFile" };

                one( mockStore ).getChildrenNames( mockTransaction, sourceCollectionPath );
                will( returnValue( sourceChildren ) );

                final StoredObject sourceFileSo = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, sourceFilePath );
                will( returnValue( sourceFileSo ) );

                one( mockStore ).createResource( mockTransaction, destCollectionPath + "/sourceFile" );

                one( mockStore ).getResourceContent( mockTransaction, sourceFilePath );
                will( returnValue( bais ) );

                one( mockStore ).setResourceContent( mockTransaction, destCollectionPath + "/sourceFile", bais, null, null );

                final StoredObject destFileSo = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, destCollectionPath + "/sourceFile" );
                will( returnValue( destFileSo ) );

            }
        } );

        final ResourceLocks resLocks = new ResourceLocks();

        final DoDelete doDelete = new DoDelete( mockStore, resLocks, !readOnly );

        final DoCopy doCopy = new DoCopy( mockStore, resLocks, doDelete, !readOnly );
        doCopy.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testDoCopyIfSourceDoesntExist()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceFilePath ) );

                one( mockReq ).getHeader( "Destination" );
                will( returnValue( destFilePath ) );

                one( mockReq ).getServerName();
                will( returnValue( "serverName" ) );

                one( mockReq ).getContextPath();
                will( returnValue( "" ) );

                one( mockReq ).getPathInfo();
                will( returnValue( destFilePath ) );

                one( mockReq ).getServicePath();
                will( returnValue( "/servletPath" ) );

                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceFilePath ) );

                one( mockReq ).getHeader( "Overwrite" );
                will( returnValue( "F" ) );

                final StoredObject notExistSo = null;

                one( mockStore ).getStoredObject( mockTransaction, sourceFilePath );
                will( returnValue( notExistSo ) );

                one( mockRes ).sendError( WebdavStatus.SC_NOT_FOUND );

            }
        } );

        final ResourceLocks resLocks = new ResourceLocks();

        final DoDelete doDelete = new DoDelete( mockStore, resLocks, !readOnly );

        final DoCopy doCopy = new DoCopy( mockStore, resLocks, doDelete, !readOnly );
        doCopy.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testDoCopyIfDestinationAlreadyExistsAndOverwriteTrue()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceFilePath ) );

                final StoredObject sourceSo = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, sourceFilePath );
                will( returnValue( sourceSo ) );

                one( mockReq ).getHeader( "Destination" );
                will( returnValue( destFilePath ) );

                one( mockReq ).getServerName();
                will( returnValue( "serverName" ) );

                one( mockReq ).getContextPath();
                will( returnValue( "" ) );

                one( mockReq ).getPathInfo();
                will( returnValue( "/folder/destFolder" ) );

                one( mockReq ).getServicePath();
                will( returnValue( "/servletPath" ) );

                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceFilePath ) );

                final StoredObject existingDestSo = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, destFilePath );
                will( returnValue( existingDestSo ) );

                one( mockReq ).getHeader( "Overwrite" );
                will( returnValue( "T" ) );

                one( mockRes ).setStatus( WebdavStatus.SC_NO_CONTENT );

                one( mockStore ).getStoredObject( mockTransaction, destFilePath );
                will( returnValue( existingDestSo ) );

                one( mockStore ).removeObject( mockTransaction, destFilePath );

                one( mockStore ).getStoredObject( mockTransaction, sourceFilePath );
                will( returnValue( sourceSo ) );

                one( mockStore ).createResource( mockTransaction, destFilePath );

                one( mockStore ).getResourceContent( mockTransaction, sourceFilePath );
                will( returnValue( bais ) );

                one( mockStore ).setResourceContent( mockTransaction, destFilePath, bais, null, null );

                one( mockStore ).getStoredObject( mockTransaction, destFilePath );
                will( returnValue( existingDestSo ) );

            }
        } );

        final ResourceLocks resLocks = new ResourceLocks();
        final DoDelete doDelete = new DoDelete( mockStore, resLocks, !readOnly );

        final DoCopy doCopy = new DoCopy( mockStore, resLocks, doDelete, !readOnly );
        doCopy.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();

    }

    @Test
    public void testDoCopyIfDestinationAlreadyExistsAndOverwriteFalse()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceFilePath ) );

                final StoredObject sourceSo = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, sourceFilePath );
                will( returnValue( sourceSo ) );

                one( mockReq ).getHeader( "Destination" );
                will( returnValue( "serverName".concat( destFilePath ) ) );

                one( mockReq ).getServerName();
                will( returnValue( "serverName" ) );

                one( mockReq ).getContextPath();
                will( returnValue( "" ) );

                one( mockReq ).getPathInfo();
                will( returnValue( destFilePath ) );

                one( mockReq ).getServicePath();
                will( returnValue( "/servletPath" ) );

                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceFilePath ) );

                final StoredObject existingDestSo = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, destFilePath );
                will( returnValue( existingDestSo ) );

                one( mockReq ).getHeader( "Overwrite" );
                will( returnValue( "F" ) );

                one( mockRes ).sendError( WebdavStatus.SC_PRECONDITION_FAILED );

            }
        } );

        final ResourceLocks resLocks = new ResourceLocks();
        final DoDelete doDelete = new DoDelete( mockStore, resLocks, !readOnly );

        final DoCopy doCopy = new DoCopy( mockStore, resLocks, doDelete, !readOnly );
        doCopy.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();

    }

    @Test
    public void testDoCopyIfOverwriteTrue()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceFilePath ) );

                final StoredObject sourceSo = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, sourceFilePath );
                will( returnValue( sourceSo ) );

                one( mockReq ).getHeader( "Destination" );
                will( returnValue( "http://destination:80".concat( destFilePath ) ) );

                one( mockReq ).getContextPath();
                will( returnValue( "http://destination:80" ) );

                one( mockReq ).getPathInfo();
                will( returnValue( destCollectionPath ) );

                one( mockReq ).getServicePath();
                will( returnValue( "http://destination:80" ) );

                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( sourceFilePath ) );

                one( mockReq ).getAttribute( "javax.servlet.include.path_info" );
                will( returnValue( sourceFilePath ) );

                one( mockReq ).getHeader( "Overwrite" );
                will( returnValue( "T" ) );

                final StoredObject destFileSo = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, destFilePath );
                will( returnValue( destFileSo ) );

                one( mockRes ).setStatus( WebdavStatus.SC_NO_CONTENT );

                one( mockStore ).getStoredObject( mockTransaction, destFilePath );
                will( returnValue( destFileSo ) );

                one( mockStore ).removeObject( mockTransaction, destFilePath );

                one( mockStore ).getStoredObject( mockTransaction, sourceFilePath );
                will( returnValue( sourceSo ) );

                one( mockStore ).createResource( mockTransaction, destFilePath );

                one( mockStore ).getResourceContent( mockTransaction, sourceFilePath );
                will( returnValue( bais ) );

                one( mockStore ).setResourceContent( mockTransaction, destFilePath, bais, null, null );

                one( mockStore ).getStoredObject( mockTransaction, destFilePath );
                will( returnValue( destFileSo ) );
            }
        } );

        final ResourceLocks resLocks = new ResourceLocks();
        final DoDelete doDelete = new DoDelete( mockStore, resLocks, !readOnly );

        final DoCopy doCopy = new DoCopy( mockStore, resLocks, doDelete, !readOnly );
        doCopy.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();

    }

    @Test
    public void testDoCopyIfOverwriteFalse()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceFilePath ) );

                final StoredObject sourceSo = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, sourceFilePath );
                will( returnValue( sourceSo ) );

                one( mockReq ).getHeader( "Destination" );
                will( returnValue( "http://destination:80".concat( destCollectionPath ) ) );

                one( mockReq ).getContextPath();
                will( returnValue( "http://destination:80" ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceFilePath ) );

                one( mockReq ).getServicePath();
                will( returnValue( "http://destination:80" ) );

                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( sourceFilePath ) );

                one( mockReq ).getAttribute( "javax.servlet.include.path_info" );
                will( returnValue( sourceFilePath ) );

                one( mockReq ).getHeader( "Overwrite" );
                will( returnValue( "F" ) );

                final StoredObject existingDestSo = initFolderStoredObject();

                one( mockStore ).getStoredObject( mockTransaction, destCollectionPath );
                will( returnValue( existingDestSo ) );

                one( mockRes ).sendError( WebdavStatus.SC_PRECONDITION_FAILED );
            }
        } );

        final ResourceLocks resLocks = new ResourceLocks();
        final DoDelete doDelete = new DoDelete( mockStore, resLocks, !readOnly );

        final DoCopy doCopy = new DoCopy( mockStore, resLocks, doDelete, !readOnly );
        doCopy.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();

    }
}

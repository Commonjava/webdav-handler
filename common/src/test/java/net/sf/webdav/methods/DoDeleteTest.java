package net.sf.webdav.methods;

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

public class DoDeleteTest
    extends MockTest
{

    static IWebdavStore mockStore;

    static WebdavRequest mockReq;

    static WebdavResponse mockRes;

    static ITransaction mockTransaction;

    static byte[] resourceContent = new byte[] { '<', 'h', 'e', 'l', 'l', 'o', '/', '>' };

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
    public void testDeleteIfReadOnlyIsTrue()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockRes ).sendError( WebdavStatus.SC_FORBIDDEN );
            }
        } );

        final ResourceLocks resLocks = new ResourceLocks();
        final DoDelete doDelete = new DoDelete( mockStore, resLocks, readOnly );
        doDelete.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testDeleteFileIfObjectExists()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceFilePath ) );

                one( mockRes ).setStatus( WebdavStatus.SC_NO_CONTENT );

                final StoredObject fileSo = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, sourceFilePath );
                will( returnValue( fileSo ) );

                one( mockStore ).removeObject( mockTransaction, sourceFilePath );
            }
        } );

        final DoDelete doDelete = new DoDelete( mockStore, new ResourceLocks(), !readOnly );

        doDelete.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testDeleteFileIfObjectNotExists()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceFilePath ) );

                one( mockRes ).setStatus( WebdavStatus.SC_NO_CONTENT );

                final StoredObject fileSo = null;

                one( mockStore ).getStoredObject( mockTransaction, sourceFilePath );
                will( returnValue( fileSo ) );

                one( mockRes ).sendError( WebdavStatus.SC_NOT_FOUND );
            }
        } );

        final DoDelete doDelete = new DoDelete( mockStore, new ResourceLocks(), !readOnly );

        doDelete.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testDeleteFolderIfObjectExists()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceCollectionPath ) );

                one( mockRes ).setStatus( WebdavStatus.SC_NO_CONTENT );

                final StoredObject folderSo = initFolderStoredObject();

                one( mockStore ).getStoredObject( mockTransaction, sourceCollectionPath );
                will( returnValue( folderSo ) );

                one( mockStore ).getChildrenNames( mockTransaction, sourceCollectionPath );
                will( returnValue( new String[] { "subFolder", "sourceFile" } ) );

                final StoredObject fileSo = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, sourceFilePath );
                will( returnValue( fileSo ) );

                one( mockStore ).removeObject( mockTransaction, sourceFilePath );

                final StoredObject subFolderSo = initFolderStoredObject();

                one( mockStore ).getStoredObject( mockTransaction, sourceCollectionPath + "/subFolder" );
                will( returnValue( subFolderSo ) );

                one( mockStore ).getChildrenNames( mockTransaction, sourceCollectionPath + "/subFolder" );
                will( returnValue( new String[] { "fileInSubFolder" } ) );

                final StoredObject fileInSubFolderSo = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, sourceCollectionPath + "/subFolder/fileInSubFolder" );
                will( returnValue( fileInSubFolderSo ) );

                one( mockStore ).removeObject( mockTransaction, sourceCollectionPath + "/subFolder/fileInSubFolder" );

                one( mockStore ).removeObject( mockTransaction, sourceCollectionPath + "/subFolder" );

                one( mockStore ).removeObject( mockTransaction, sourceCollectionPath );
            }
        } );

        final DoDelete doDelete = new DoDelete( mockStore, new ResourceLocks(), !readOnly );

        doDelete.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testDeleteFolderIfObjectNotExists()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceCollectionPath ) );

                one( mockRes ).setStatus( WebdavStatus.SC_NO_CONTENT );

                final StoredObject folderSo = null;

                one( mockStore ).getStoredObject( mockTransaction, sourceCollectionPath );
                will( returnValue( folderSo ) );

                one( mockRes ).sendError( WebdavStatus.SC_NOT_FOUND );
            }
        } );

        final DoDelete doDelete = new DoDelete( mockStore, new ResourceLocks(), !readOnly );

        doDelete.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testDeleteFileInFolder()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceFilePath ) );

                one( mockRes ).setStatus( WebdavStatus.SC_NO_CONTENT );

                final StoredObject fileSo = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, sourceFilePath );
                will( returnValue( fileSo ) );

                one( mockStore ).removeObject( mockTransaction, sourceFilePath );
            }
        } );

        final DoDelete doDelete = new DoDelete( mockStore, new ResourceLocks(), !readOnly );

        doDelete.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testDeleteFileInLockedFolderWithWrongLockToken()
        throws Exception
    {

        final String lockedFolderPath = "/lockedFolder";
        final String fileInLockedFolderPath = lockedFolderPath.concat( "/fileInLockedFolder" );

        final String owner = new String( "owner" );
        final ResourceLocks resLocks = new ResourceLocks();

        resLocks.lock( mockTransaction, lockedFolderPath, owner, true, -1, TEMP_TIMEOUT, !TEMPORARY );
        final LockedObject lo = resLocks.getLockedObjectByPath( mockTransaction, lockedFolderPath );
        final String wrongLockToken = "(<opaquelocktoken:" + lo.getID() + "WRONG>)";

        final PrintWriter pw = new PrintWriter( "/tmp/XMLTestFile" );

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( fileInLockedFolderPath ) );

                one( mockReq ).getHeader( "If" );
                will( returnValue( wrongLockToken ) );

                one( mockRes ).setStatus( WebdavStatus.SC_MULTI_STATUS );

                one( mockReq ).getRequestURI();
                will( returnValue( "http://foo.bar".concat( lockedFolderPath ) ) );

                one( mockRes ).getWriter();
                will( returnValue( pw ) );

            }
        } );

        final DoDelete doDelete = new DoDelete( mockStore, resLocks, !readOnly );

        doDelete.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testDeleteFileInLockedFolderWithRightLockToken()
        throws Exception
    {

        final String path = "/lockedFolder/fileInLockedFolder";
        final String parentPath = "/lockedFolder";
        final String owner = new String( "owner" );
        final ResourceLocks resLocks = new ResourceLocks();

        resLocks.lock( mockTransaction, parentPath, owner, true, -1, TEMP_TIMEOUT, !TEMPORARY );
        final LockedObject lo = resLocks.getLockedObjectByPath( mockTransaction, "/lockedFolder" );
        final String rightLockToken = "(<opaquelocktoken:" + lo.getID() + ">)";

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( path ) );

                one( mockReq ).getHeader( "If" );
                will( returnValue( rightLockToken ) );

                one( mockRes ).setStatus( WebdavStatus.SC_NO_CONTENT );

                final StoredObject so = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, path );
                will( returnValue( so ) );

                one( mockStore ).removeObject( mockTransaction, path );

            }
        } );

        final DoDelete doDelete = new DoDelete( mockStore, resLocks, !readOnly );

        doDelete.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testDeleteFileInFolderIfObjectNotExists()
        throws Exception
    {

        final boolean readOnly = false;

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( "/folder/file" ) );

                one( mockRes ).setStatus( WebdavStatus.SC_NO_CONTENT );

                final StoredObject nonExistingSo = null;

                one( mockStore ).getStoredObject( mockTransaction, "/folder/file" );
                will( returnValue( nonExistingSo ) );

                one( mockRes ).sendError( WebdavStatus.SC_NOT_FOUND );
            }
        } );

        final DoDelete doDelete = new DoDelete( mockStore, new ResourceLocks(), readOnly );

        doDelete.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

}

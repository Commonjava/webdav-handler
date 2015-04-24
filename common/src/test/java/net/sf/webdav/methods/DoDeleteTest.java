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
package net.sf.webdav.methods;

import java.io.PrintWriter;

import net.sf.webdav.StoredObject;
import net.sf.webdav.WebdavStatus;
import net.sf.webdav.locking.LockedObject;
import net.sf.webdav.locking.ResourceLocks;
import net.sf.webdav.spi.ITransaction;
import net.sf.webdav.spi.IWebdavStoreWorker;
import net.sf.webdav.spi.WebdavRequest;
import net.sf.webdav.spi.WebdavResponse;
import net.sf.webdav.testutil.MockTest;

import org.jmock.Expectations;
import org.junit.Test;

public class DoDeleteTest
    extends MockTest
{

    static IWebdavStoreWorker mockStoreWorker;

    static WebdavRequest mockReq;

    static WebdavResponse mockRes;

    static ITransaction mockTransaction;

    static byte[] resourceContent = new byte[] { '<', 'h', 'e', 'l', 'l', 'o', '/', '>' };

    @Override
    public void setupFixtures()
        throws Exception
    {
        mockStoreWorker = _mockery.mock( IWebdavStoreWorker.class );
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

        new DoDelete().execute( mockTransaction, mockReq, mockRes, mockStoreWorker,
                                newResources( new ResourceLocks(), readOnly ) );

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

                one( mockStoreWorker ).getStoredObject( mockTransaction, sourceFilePath );
                will( returnValue( fileSo ) );

                one( mockStoreWorker ).removeObject( mockTransaction, sourceFilePath );
            }
        } );

        new DoDelete().execute( mockTransaction, mockReq, mockRes, mockStoreWorker,
                                newResources( new ResourceLocks(), !readOnly ) );

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

                one( mockStoreWorker ).getStoredObject( mockTransaction, sourceFilePath );
                will( returnValue( fileSo ) );

                one( mockRes ).sendError( WebdavStatus.SC_NOT_FOUND );
            }
        } );

        new DoDelete().execute( mockTransaction, mockReq, mockRes, mockStoreWorker,
                                newResources( new ResourceLocks(), !readOnly ) );

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

                one( mockStoreWorker ).getStoredObject( mockTransaction, sourceCollectionPath );
                will( returnValue( folderSo ) );

                one( mockStoreWorker ).getChildrenNames( mockTransaction, sourceCollectionPath );
                will( returnValue( new String[] { "subFolder", "sourceFile" } ) );

                final StoredObject fileSo = initFileStoredObject( resourceContent );

                one( mockStoreWorker ).getStoredObject( mockTransaction, sourceFilePath );
                will( returnValue( fileSo ) );

                one( mockStoreWorker ).removeObject( mockTransaction, sourceFilePath );

                final StoredObject subFolderSo = initFolderStoredObject();

                one( mockStoreWorker ).getStoredObject( mockTransaction, sourceCollectionPath + "/subFolder" );
                will( returnValue( subFolderSo ) );

                one( mockStoreWorker ).getChildrenNames( mockTransaction, sourceCollectionPath + "/subFolder" );
                will( returnValue( new String[] { "fileInSubFolder" } ) );

                final StoredObject fileInSubFolderSo = initFileStoredObject( resourceContent );

                one( mockStoreWorker ).getStoredObject( mockTransaction,
                                                        sourceCollectionPath + "/subFolder/fileInSubFolder" );
                will( returnValue( fileInSubFolderSo ) );

                one( mockStoreWorker ).removeObject( mockTransaction,
                                                     sourceCollectionPath + "/subFolder/fileInSubFolder" );

                one( mockStoreWorker ).removeObject( mockTransaction, sourceCollectionPath + "/subFolder" );

                one( mockStoreWorker ).removeObject( mockTransaction, sourceCollectionPath );
            }
        } );

        new DoDelete().execute( mockTransaction, mockReq, mockRes, mockStoreWorker,
                                newResources( new ResourceLocks(), !readOnly ) );

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

                one( mockStoreWorker ).getStoredObject( mockTransaction, sourceCollectionPath );
                will( returnValue( folderSo ) );

                one( mockRes ).sendError( WebdavStatus.SC_NOT_FOUND );
            }
        } );

        new DoDelete().execute( mockTransaction, mockReq, mockRes, mockStoreWorker,
                                newResources( new ResourceLocks(), !readOnly ) );

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

                one( mockStoreWorker ).getStoredObject( mockTransaction, sourceFilePath );
                will( returnValue( fileSo ) );

                one( mockStoreWorker ).removeObject( mockTransaction, sourceFilePath );
            }
        } );

        new DoDelete().execute( mockTransaction, mockReq, mockRes, mockStoreWorker,
                                newResources( new ResourceLocks(), !readOnly ) );

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

        new DoDelete().execute( mockTransaction, mockReq, mockRes, mockStoreWorker, newResources( resLocks, !readOnly ) );

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

                one( mockStoreWorker ).getStoredObject( mockTransaction, path );
                will( returnValue( so ) );

                one( mockStoreWorker ).removeObject( mockTransaction, path );

            }
        } );

        new DoDelete().execute( mockTransaction, mockReq, mockRes, mockStoreWorker, newResources( resLocks, !readOnly ) );

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

                one( mockStoreWorker ).getStoredObject( mockTransaction, "/folder/file" );
                will( returnValue( nonExistingSo ) );

                one( mockRes ).sendError( WebdavStatus.SC_NOT_FOUND );
            }
        } );

        new DoDelete().execute( mockTransaction, mockReq, mockRes, mockStoreWorker,
                                newResources( new ResourceLocks(), !readOnly ) );

        _mockery.assertIsSatisfied();
    }

}

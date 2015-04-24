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

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;

import net.sf.webdav.StoredObject;
import net.sf.webdav.WebdavResources;
import net.sf.webdav.WebdavStatus;
import net.sf.webdav.locking.IResourceLocks;
import net.sf.webdav.locking.LockedObject;
import net.sf.webdav.locking.ResourceLocks;
import net.sf.webdav.spi.ITransaction;
import net.sf.webdav.spi.IWebdavStoreWorker;
import net.sf.webdav.spi.WebdavRequest;
import net.sf.webdav.spi.WebdavResponse;
import net.sf.webdav.testutil.MockTest;

import org.jmock.Expectations;
import org.junit.Test;

public class DoMkcolTest
    extends MockTest
{

    static IWebdavStoreWorker mockStoreWorker;

    static WebdavRequest mockReq;

    static WebdavResponse mockRes;

    static ITransaction mockTransaction;

    static IResourceLocks mockResourceLocks;

    static String parentPath = "/parentCollection";

    static String mkcolPath = parentPath.concat( "/makeCollection" );

    static String owner = "a lock owner";

    @Override
    public void setupFixtures()
        throws Exception
    {
        mockStoreWorker = _mockery.mock( IWebdavStoreWorker.class );
        mockReq = _mockery.mock( WebdavRequest.class );
        mockRes = _mockery.mock( WebdavResponse.class );
        mockTransaction = _mockery.mock( ITransaction.class );
        mockResourceLocks = _mockery.mock( IResourceLocks.class );
    }

    @Test
    public void testMkcolIfReadOnlyIsTrue()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockRes ).sendError( WebdavStatus.SC_FORBIDDEN );
            }
        } );

        final ResourceLocks resLocks = new ResourceLocks();
        new DoMkcol().execute( mockTransaction, mockReq, mockRes, mockStoreWorker, newResources( resLocks, readOnly ) );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testMkcolSuccess()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( mkcolPath ) );

                final StoredObject parentSo = initFolderStoredObject();

                one( mockStoreWorker ).getStoredObject( mockTransaction, parentPath );
                will( returnValue( parentSo ) );

                final StoredObject mkcolSo = null;

                one( mockStoreWorker ).getStoredObject( mockTransaction, mkcolPath );
                will( returnValue( mkcolSo ) );

                one( mockStoreWorker ).createFolder( mockTransaction, mkcolPath );

                one( mockRes ).setStatus( WebdavStatus.SC_CREATED );

            }
        } );

        final ResourceLocks resLocks = new ResourceLocks();
        new DoMkcol().execute( mockTransaction, mockReq, mockRes, mockStoreWorker, newResources( resLocks, !readOnly ) );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testMkcolIfParentPathIsNoFolder()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( mkcolPath ) );

                final StoredObject parentSo = initFileStoredObject( resourceContent );

                one( mockStoreWorker ).getStoredObject( mockTransaction, parentPath );
                will( returnValue( parentSo ) );

                final String methodsAllowed = "OPTIONS, GET, HEAD, POST, DELETE, TRACE, " + "PROPPATCH, COPY, MOVE, LOCK, UNLOCK, PROPFIND";

                one( mockRes ).addHeader( "Allow", methodsAllowed );

                one( mockRes ).sendError( WebdavStatus.SC_METHOD_NOT_ALLOWED );
            }
        } );

        final ResourceLocks resLocks = new ResourceLocks();
        new DoMkcol().execute( mockTransaction, mockReq, mockRes, mockStoreWorker, newResources( resLocks, !readOnly ) );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testMkcolIfParentPathIsAFolderButObjectAlreadyExists()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( mkcolPath ) );

                final StoredObject parentSo = initFolderStoredObject();

                one( mockStoreWorker ).getStoredObject( mockTransaction, parentPath );
                will( returnValue( parentSo ) );

                final StoredObject mkcolSo = initFolderStoredObject();

                one( mockStoreWorker ).getStoredObject( mockTransaction, mkcolPath );
                will( returnValue( mkcolSo ) );

                one( mockRes ).addHeader( "Allow", "OPTIONS, GET, HEAD, POST, DELETE, TRACE, PROPPATCH, COPY, MOVE, LOCK, UNLOCK, PROPFIND, PUT" );

                one( mockRes ).sendError( WebdavStatus.SC_METHOD_NOT_ALLOWED );

            }
        } );

        final ResourceLocks resLocks = new ResourceLocks();
        new DoMkcol().execute( mockTransaction, mockReq, mockRes, mockStoreWorker, newResources( resLocks, !readOnly ) );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testMkcolIfParentFolderIsLockedWithRightLockToken()
        throws Exception
    {

        final ResourceLocks resLocks = new ResourceLocks();
        resLocks.lock( mockTransaction, parentPath, owner, true, -1, 200, false );
        final LockedObject lo = resLocks.getLockedObjectByPath( mockTransaction, parentPath );
        final String rightLockToken = "(<opaquelocktoken:" + lo.getID() + ">)";

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( mkcolPath ) );

                one( mockReq ).getHeader( "If" );
                will( returnValue( rightLockToken ) );

                final StoredObject parentSo = initFolderStoredObject();

                one( mockStoreWorker ).getStoredObject( mockTransaction, parentPath );
                will( returnValue( parentSo ) );

                final StoredObject mkcolSo = null;

                one( mockStoreWorker ).getStoredObject( mockTransaction, mkcolPath );
                will( returnValue( mkcolSo ) );

                one( mockStoreWorker ).createFolder( mockTransaction, mkcolPath );

                one( mockRes ).setStatus( WebdavStatus.SC_CREATED );

            }
        } );

        new DoMkcol().execute( mockTransaction, mockReq, mockRes, mockStoreWorker, newResources( resLocks, !readOnly ) );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testMkcolIfParentFolderIsLockedWithWrongLockToken()
        throws Exception
    {

        final ResourceLocks resLocks = new ResourceLocks();
        resLocks.lock( mockTransaction, parentPath, owner, true, -1, 200, false );
        final String wrongLockToken = "(<opaquelocktoken:" + "aWrongLockToken" + ">)";

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( mkcolPath ) );

                one( mockReq ).getHeader( "If" );
                will( returnValue( wrongLockToken ) );

                one( mockRes ).sendError( WebdavStatus.SC_FORBIDDEN );
            }
        } );

        new DoMkcol().execute( mockTransaction, mockReq, mockRes, mockStoreWorker, newResources( resLocks, !readOnly ) );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testMkcolOnALockNullResource()
        throws Exception
    {

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
                will( returnValue( mkcolPath ) );

                LockedObject lockNullResourceLo = null;

                one( mockResourceLocks ).getLockedObjectByPath( mockTransaction, mkcolPath );
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

                one( mockStoreWorker ).getStoredObject( mockTransaction, mkcolPath );
                will( returnValue( lockNullResourceSo ) );

                StoredObject parentSo = null;

                one( mockStoreWorker ).getStoredObject( mockTransaction, parentPath );
                will( returnValue( parentSo ) );

                one( mockStoreWorker ).createFolder( mockTransaction, parentPath );

                parentSo = initFolderStoredObject();

                one( mockStoreWorker ).getStoredObject( mockTransaction, mkcolPath );
                will( returnValue( lockNullResourceSo ) );

                one( mockStoreWorker ).createResource( mockTransaction, mkcolPath );

                lockNullResourceSo = initLockNullStoredObject();

                one( mockRes ).setStatus( WebdavStatus.SC_CREATED );

                one( mockStoreWorker ).getStoredObject( mockTransaction, mkcolPath );
                will( returnValue( lockNullResourceSo ) );

                one( mockReq ).getInputStream();
                will( returnValue( baisExclusive ) );

                one( mockReq ).getHeader( "Depth" );
                will( returnValue( ( "0" ) ) );

                one( mockReq ).getHeader( "Timeout" );
                will( returnValue( "Infinite" ) );

                final ResourceLocks resLocks = ResourceLocks.class.newInstance();

                one( mockResourceLocks ).exclusiveLock( mockTransaction, mkcolPath, "I'am the Lock Owner", 0, 604800 );
                will( returnValue( true ) );

                lockNullResourceLo = initLockNullLockedObject( resLocks, mkcolPath );

                one( mockResourceLocks ).getLockedObjectByPath( mockTransaction, mkcolPath );
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

                // -----LOCK on a non-existing resource successful------
                // --------now MKCOL on the lock-null resource----------

                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( mkcolPath ) );

                one( mockResourceLocks ).getLockedObjectByPath( mockTransaction, parentPath );
                will( returnValue( parentLo ) );

                one( mockResourceLocks ).lock( with( any( ITransaction.class ) ), with( any( String.class ) ), with( any( String.class ) ),
                                               with( any( boolean.class ) ), with( any( int.class ) ), with( any( int.class ) ),
                                               with( any( boolean.class ) ) );
                will( returnValue( true ) );

                one( mockStoreWorker ).getStoredObject( mockTransaction, parentPath );
                will( returnValue( parentSo ) );

                one( mockStoreWorker ).getStoredObject( mockTransaction, mkcolPath );
                will( returnValue( lockNullResourceSo ) );

                one( mockResourceLocks ).getLockedObjectByPath( mockTransaction, mkcolPath );
                will( returnValue( lockNullResourceLo ) );

                final String ifHeaderLockToken = "(<locktoken:" + loId + ">)";

                one( mockReq ).getHeader( "If" );
                will( returnValue( ifHeaderLockToken ) );

                final String[] owners = lockNullResourceLo.getOwner();
                String owner = null;
                if ( owners != null )
                {
                    owner = owners[0];
                }

                one( mockResourceLocks ).unlock( mockTransaction, loId, owner );
                will( returnValue( true ) );

                one( mockRes ).setStatus( WebdavStatus.SC_CREATED );

                one( mockResourceLocks ).unlockTemporaryLockedObjects( with( any( ITransaction.class ) ), with( any( String.class ) ),
                                                                       with( any( String.class ) ) );

            }
        } );

        final WebdavResources res = newResources( mockResourceLocks, !readOnly );
        new DoLock().execute( mockTransaction, mockReq, mockRes, mockStoreWorker, res );
        new DoMkcol().execute( mockTransaction, mockReq, mockRes, mockStoreWorker, res );

        _mockery.assertIsSatisfied();
    }
}

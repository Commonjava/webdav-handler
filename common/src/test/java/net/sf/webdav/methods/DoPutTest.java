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

public class DoPutTest
    extends MockTest
{
    static IWebdavStore mockStore;

    static WebdavRequest mockReq;

    static WebdavResponse mockRes;

    static IResourceLocks mockResourceLocks;

    static ITransaction mockTransaction;

    static String parentPath = "/parentCollection";

    static String path = parentPath.concat( "/fileToPut" );

    static boolean lazyFolderCreationOnPut = true;

    @Override
    public void setupFixtures()
        throws Exception
    {
        mockStore = _mockery.mock( IWebdavStore.class );
        mockReq = _mockery.mock( WebdavRequest.class );
        mockRes = _mockery.mock( WebdavResponse.class );
        mockResourceLocks = _mockery.mock( IResourceLocks.class );
        mockTransaction = _mockery.mock( ITransaction.class );
    }

    @Test
    public void testDoPutIfReadOnlyTrue()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockRes ).sendError( WebdavStatus.SC_FORBIDDEN );
            }
        } );

        final DoPut doPut = new DoPut( mockStore, new ResourceLocks(), readOnly, lazyFolderCreationOnPut );
        doPut.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testDoPutIfReadOnlyFalse()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( path ) );

                one( mockReq ).getHeader( "User-Agent" );
                will( returnValue( "Goliath agent" ) );

                one( mockReq ).getContentLength();
                will( returnValue( (int) resourceLength ) );

                final StoredObject parentSo = initFolderStoredObject();

                one( mockStore ).getStoredObject( mockTransaction, parentPath );
                will( returnValue( parentSo ) );

                StoredObject fileSo = null;

                one( mockStore ).getStoredObject( mockTransaction, path );
                will( returnValue( fileSo ) );

                one( mockStore ).createResource( mockTransaction, path );

                one( mockRes ).setStatus( WebdavStatus.SC_CREATED );

                one( mockReq ).getInputStream();
                will( returnValue( bais ) );

                one( mockStore ).setResourceContent( mockTransaction, path, bais, resourceLength );
                will( returnValue( 8L ) );

                fileSo = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, path );
                will( returnValue( fileSo ) );

                // User-Agent: Goliath --> dont add ContentLength
                // one(mockRes).setContentLength(8);
            }
        } );

        final DoPut doPut = new DoPut( mockStore, new ResourceLocks(), !readOnly, lazyFolderCreationOnPut );
        doPut.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testDoPutIfLazyFolderCreationOnPutIsFalse()
        throws Exception
    {

        final PrintWriter pw = new PrintWriter( "/tmp/XMLTestFile" );

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( path ) );

                one( mockReq ).getHeader( "User-Agent" );
                will( returnValue( "Transmit agent" ) );

                final StoredObject parentSo = null;

                one( mockStore ).getStoredObject( mockTransaction, parentPath );
                will( returnValue( parentSo ) );

                one( mockRes ).setStatus( WebdavStatus.SC_MULTI_STATUS );

                one( mockReq ).getRequestURI();
                will( returnValue( "http://foo.bar".concat( path ) ) );

                one( mockRes ).getWriter();
                will( returnValue( pw ) );

            }
        } );

        final DoPut doPut = new DoPut( mockStore, new ResourceLocks(), !readOnly, !lazyFolderCreationOnPut );
        doPut.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testDoPutIfLazyFolderCreationOnPutIsTrue()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( path ) );

                one( mockReq ).getHeader( "User-Agent" );
                will( returnValue( "WebDAVFS/1.5.0 (01500000) ....." ) );

                one( mockReq ).getContentLength();
                will( returnValue( (int) resourceLength ) );

                final StoredObject parentSo = null;

                one( mockStore ).getStoredObject( mockTransaction, parentPath );
                will( returnValue( parentSo ) );

                one( mockStore ).createFolder( mockTransaction, parentPath );

                StoredObject fileSo = null;

                one( mockStore ).getStoredObject( mockTransaction, path );
                will( returnValue( fileSo ) );

                one( mockStore ).createResource( mockTransaction, path );

                one( mockRes ).setStatus( WebdavStatus.SC_CREATED );

                one( mockReq ).getInputStream();
                will( returnValue( bais ) );

                one( mockStore ).setResourceContent( mockTransaction, path, bais, resourceLength );
                will( returnValue( 8L ) );

                fileSo = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, path );
                will( returnValue( fileSo ) );

            }
        } );

        final DoPut doPut = new DoPut( mockStore, new ResourceLocks(), !readOnly, lazyFolderCreationOnPut );
        doPut.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testDoPutIfParentPathIsResource()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( path ) );

                one( mockReq ).getHeader( "User-Agent" );
                will( returnValue( "WebDAVFS/1.5.0 (01500000) ....." ) );

                final StoredObject parentSo = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, parentPath );
                will( returnValue( parentSo ) );

                one( mockRes ).sendError( WebdavStatus.SC_FORBIDDEN );
            }
        } );

        final DoPut doPut = new DoPut( mockStore, new ResourceLocks(), !readOnly, lazyFolderCreationOnPut );
        doPut.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testDoPutOnALockNullResource()
        throws Exception
    {

        final PrintWriter pw = new PrintWriter( "/tmp/XMLTestFile" );

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( path ) );

                LockedObject lockNullResourceLo = null;

                one( mockResourceLocks ).getLockedObjectByPath( mockTransaction, path );
                will( returnValue( lockNullResourceLo ) );

                final LockedObject parentLo = null;

                one( mockResourceLocks ).getLockedObjectByPath( mockTransaction, parentPath );
                will( returnValue( parentLo ) );

                one( mockReq ).getHeader( "User-Agent" );
                will( returnValue( "Transmit agent" ) );

                one( mockResourceLocks ).lock( with( any( ITransaction.class ) ), with( any( String.class ) ), with( any( String.class ) ),
                                               with( any( boolean.class ) ), with( any( int.class ) ), with( any( int.class ) ),
                                               with( any( boolean.class ) ) );
                will( returnValue( true ) );

                one( mockReq ).getHeader( "If" );
                will( returnValue( null ) );

                StoredObject lockNullResourceSo = null;

                one( mockStore ).getStoredObject( mockTransaction, path );
                will( returnValue( lockNullResourceSo ) );

                StoredObject parentSo = null;

                one( mockStore ).getStoredObject( mockTransaction, parentPath );
                will( returnValue( parentSo ) );

                one( mockStore ).createFolder( mockTransaction, parentPath );

                parentSo = initFolderStoredObject();

                one( mockStore ).getStoredObject( mockTransaction, path );
                will( returnValue( lockNullResourceSo ) );

                one( mockStore ).createResource( mockTransaction, path );

                lockNullResourceSo = initLockNullStoredObject();

                one( mockRes ).setStatus( WebdavStatus.SC_NO_CONTENT );

                one( mockStore ).getStoredObject( mockTransaction, path );
                will( returnValue( lockNullResourceSo ) );

                one( mockReq ).getInputStream();
                will( returnValue( baisExclusive ) );

                one( mockReq ).getHeader( "Depth" );
                will( returnValue( ( "0" ) ) );

                one( mockReq ).getHeader( "Timeout" );
                will( returnValue( "Infinite" ) );

                final ResourceLocks resLocks = ResourceLocks.class.newInstance();

                one( mockResourceLocks ).exclusiveLock( mockTransaction, path, "I'am the Lock Owner", 0, 604800 );
                will( returnValue( true ) );

                lockNullResourceLo = initLockNullLockedObject( resLocks, path );

                one( mockResourceLocks ).getLockedObjectByPath( mockTransaction, path );
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

                // // -----LOCK on a non-existing resource successful------
                // // --------now doPUT on the lock-null resource----------

                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( path ) );

                one( mockReq ).getHeader( "User-Agent" );
                will( returnValue( "Transmit agent" ) );

                one( mockResourceLocks ).getLockedObjectByPath( mockTransaction, parentPath );
                will( returnValue( parentLo ) );

                one( mockResourceLocks ).getLockedObjectByPath( mockTransaction, path );
                will( returnValue( lockNullResourceLo ) );

                final String ifHeaderLockToken = "(<locktoken:" + loId + ">)";

                one( mockReq ).getHeader( "If" );
                will( returnValue( ifHeaderLockToken ) );

                one( mockResourceLocks ).getLockedObjectByID( mockTransaction, loId );
                will( returnValue( lockNullResourceLo ) );

                one( mockResourceLocks ).lock( with( any( ITransaction.class ) ), with( any( String.class ) ), with( any( String.class ) ),
                                               with( any( boolean.class ) ), with( any( int.class ) ), with( any( int.class ) ),
                                               with( any( boolean.class ) ) );
                will( returnValue( true ) );

                parentSo = initFolderStoredObject();

                one( mockStore ).getStoredObject( mockTransaction, parentPath );
                will( returnValue( parentSo ) );

                one( mockStore ).getStoredObject( mockTransaction, path );
                will( returnValue( lockNullResourceSo ) );

                one( mockResourceLocks ).getLockedObjectByPath( mockTransaction, path );
                will( returnValue( lockNullResourceLo ) );

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

                one( mockRes ).setStatus( WebdavStatus.SC_NO_CONTENT );

                one( mockReq ).getInputStream();
                will( returnValue( bais ) );

                one( mockReq ).getContentLength();
                will( returnValue( (int) resourceLength ) );

                one( mockStore ).setResourceContent( mockTransaction, path, bais, resourceLength );
                will( returnValue( 8L ) );

                final StoredObject newResourceSo = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, path );
                will( returnValue( newResourceSo ) );

                one( mockResourceLocks ).unlockTemporaryLockedObjects( with( any( ITransaction.class ) ), with( any( String.class ) ),
                                                                       with( any( String.class ) ) );
            }
        } );

        final DoLock doLock = new DoLock( mockStore, mockResourceLocks, !readOnly );
        doLock.execute( mockTransaction, mockReq, mockRes );

        final DoPut doPut = new DoPut( mockStore, mockResourceLocks, !readOnly, lazyFolderCreationOnPut );
        doPut.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }
}

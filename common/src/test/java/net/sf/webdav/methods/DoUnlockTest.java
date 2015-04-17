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

public class DoUnlockTest
    extends MockTest
{

    static IWebdavStore mockStore;

    static WebdavRequest mockReq;

    static WebdavResponse mockRes;

    static ITransaction mockTransaction;

    static IResourceLocks mockResourceLocks;

    static boolean exclusive = true;

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
    public void testDoUnlockIfReadOnly()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockRes ).sendError( WebdavStatus.SC_FORBIDDEN );
            }
        } );

        final DoUnlock doUnlock = new DoUnlock( mockStore, new ResourceLocks(), readOnly );

        doUnlock.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testDoUnlockaLockedResourceWithRightLockToken()
        throws Exception
    {

        final String lockPath = "/lockedResource";
        final String lockOwner = "theOwner";

        final ResourceLocks resLocks = new ResourceLocks();
        resLocks.lock( mockTransaction, lockPath, lockOwner, exclusive, 0, TEMP_TIMEOUT, !TEMPORARY );

        final LockedObject lo = resLocks.getLockedObjectByPath( mockTransaction, lockPath );
        final String loID = lo.getID();
        final String lockToken = "<opaquelocktoken:".concat( loID )
                                                    .concat( ">" );

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( lockPath ) );

                one( mockReq ).getHeader( "Lock-Token" );
                will( returnValue( lockToken ) );

                final StoredObject lockedSo = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, lockPath );
                will( returnValue( lockedSo ) );

                one( mockRes ).setStatus( WebdavStatus.SC_NO_CONTENT );
            }
        } );

        final DoUnlock doUnlock = new DoUnlock( mockStore, resLocks, !readOnly );

        doUnlock.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testDoUnlockaLockedResourceWithWrongLockToken()
        throws Exception
    {

        final String lockPath = "/lockedResource";
        final String lockOwner = "theOwner";

        final ResourceLocks resLocks = new ResourceLocks();
        resLocks.lock( mockTransaction, lockPath, lockOwner, exclusive, 0, TEMP_TIMEOUT, !TEMPORARY );

        final LockedObject lo = resLocks.getLockedObjectByPath( mockTransaction, lockPath );
        final String loID = lo.getID();
        final String lockToken = "<opaquelocktoken:".concat( loID )
                                                    .concat( "WRONG>" );

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( lockPath ) );

                one( mockReq ).getHeader( "Lock-Token" );
                will( returnValue( lockToken ) );

                one( mockRes ).sendError( WebdavStatus.SC_BAD_REQUEST );
            }
        } );

        final DoUnlock doUnlock = new DoUnlock( mockStore, resLocks, !readOnly );
        doUnlock.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testDoUnlockaNotLockedResource()
        throws Exception
    {

        final ResourceLocks resLocks = new ResourceLocks();
        final String lockPath = "/notLockedResource";
        final String lockToken = "<opaquelocktoken:xxxx-xxxx-xxxxWRONG>";

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( lockPath ) );

                one( mockReq ).getHeader( "Lock-Token" );
                will( returnValue( lockToken ) );

                one( mockRes ).sendError( WebdavStatus.SC_BAD_REQUEST );
            }
        } );

        final DoUnlock doUnlock = new DoUnlock( mockStore, resLocks, !readOnly );

        doUnlock.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testDoUnlockaLockNullResource()
        throws Exception
    {

        final String parentPath = "/parentCollection";
        final String nullLoPath = parentPath.concat( "/aNullResource" );

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
                will( returnValue( nullLoPath ) );

                LockedObject lockNullResourceLo = null;

                one( mockResourceLocks ).getLockedObjectByPath( mockTransaction, nullLoPath );
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

                one( mockStore ).getStoredObject( mockTransaction, nullLoPath );
                will( returnValue( lockNullResourceSo ) );

                final StoredObject parentSo = null;

                one( mockStore ).getStoredObject( mockTransaction, parentPath );
                will( returnValue( parentSo ) );

                one( mockStore ).createFolder( mockTransaction, parentPath );

                one( mockStore ).getStoredObject( mockTransaction, nullLoPath );
                will( returnValue( lockNullResourceSo ) );

                one( mockStore ).createResource( mockTransaction, nullLoPath );

                one( mockRes ).setStatus( WebdavStatus.SC_CREATED );

                lockNullResourceSo = initLockNullStoredObject();

                one( mockStore ).getStoredObject( mockTransaction, nullLoPath );
                will( returnValue( lockNullResourceSo ) );

                one( mockReq ).getInputStream();
                will( returnValue( baisExclusive ) );

                one( mockReq ).getHeader( "Depth" );
                will( returnValue( ( "0" ) ) );

                one( mockReq ).getHeader( "Timeout" );
                will( returnValue( "Infinite" ) );

                final ResourceLocks resLocks = ResourceLocks.class.newInstance();

                one( mockResourceLocks ).exclusiveLock( mockTransaction, nullLoPath, "I'am the Lock Owner", 0, 604800 );
                will( returnValue( true ) );

                lockNullResourceLo = initLockNullLockedObject( resLocks, nullLoPath );

                one( mockResourceLocks ).getLockedObjectByPath( mockTransaction, nullLoPath );
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
                // ----------------now try to unlock it-----------------

                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( nullLoPath ) );

                one( mockResourceLocks ).lock( with( any( ITransaction.class ) ), with( any( String.class ) ), with( any( String.class ) ),
                                               with( any( boolean.class ) ), with( any( int.class ) ), with( any( int.class ) ),
                                               with( any( boolean.class ) ) );
                will( returnValue( true ) );

                one( mockReq ).getHeader( "Lock-Token" );
                will( returnValue( lockToken ) );

                one( mockResourceLocks ).getLockedObjectByID( mockTransaction, loId );
                will( returnValue( lockNullResourceLo ) );

                final String[] owners = lockNullResourceLo.getOwner();
                String owner = null;
                if ( owners != null )
                {
                    owner = owners[0];
                }

                one( mockResourceLocks ).unlock( mockTransaction, loId, owner );
                will( returnValue( true ) );

                one( mockStore ).getStoredObject( mockTransaction, nullLoPath );
                will( returnValue( lockNullResourceSo ) );

                one( mockStore ).removeObject( mockTransaction, nullLoPath );

                one( mockRes ).setStatus( WebdavStatus.SC_NO_CONTENT );

                one( mockResourceLocks ).unlockTemporaryLockedObjects( with( any( ITransaction.class ) ), with( any( String.class ) ),
                                                                       with( any( String.class ) ) );

            }
        } );

        final DoLock doLock = new DoLock( mockStore, mockResourceLocks, !readOnly );
        doLock.execute( mockTransaction, mockReq, mockRes );

        final DoUnlock doUnlock = new DoUnlock( mockStore, mockResourceLocks, !readOnly );
        doUnlock.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();

    }

}

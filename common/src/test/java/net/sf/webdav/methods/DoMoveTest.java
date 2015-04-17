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

import net.sf.webdav.StoredObject;
import net.sf.webdav.WebdavStatus;
import net.sf.webdav.locking.ResourceLocks;
import net.sf.webdav.spi.ITransaction;
import net.sf.webdav.spi.IWebdavStore;
import net.sf.webdav.spi.WebdavRequest;
import net.sf.webdav.spi.WebdavResponse;
import net.sf.webdav.testutil.MockTest;

import org.jmock.Expectations;
import org.junit.Test;

public class DoMoveTest
    extends MockTest
{

    static IWebdavStore mockStore;

    static WebdavRequest mockReq;

    static WebdavResponse mockRes;

    static ITransaction mockTransaction;

    static byte[] resourceContent = new byte[] { '<', 'h', 'e', 'l', 'l', 'o', '/', '>' };

    static ByteArrayInputStream bais = new ByteArrayInputStream( resourceContent );

    //    static DelegatingServletInputStream dsis = new DelegatingServletInputStream(
    //            bais);

    static final String tmpFolder = "/tmp/tests";

    static final String sourceCollectionPath = tmpFolder + "/sourceFolder";

    static final String destCollectionPath = tmpFolder + "/destFolder";

    static final String sourceFilePath = sourceCollectionPath + "/sourceFile";

    static final String destFilePath = destCollectionPath + "/destFile";

    static final String overwritePath = destCollectionPath + "/sourceFolder";

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
    public void testMovingOfFileOrFolderIfReadOnlyIsTrue()
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
        final DoCopy doCopy = new DoCopy( mockStore, resLocks, doDelete, readOnly );

        final DoMove doMove = new DoMove( resLocks, doDelete, doCopy, readOnly );

        doMove.execute( mockTransaction, mockReq, mockRes );
    }

    @Test
    public void testMovingOfaFileIfDestinationNotPresent()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceFilePath ) );

                exactly( 2 ).of( mockReq )
                            .getHeader( "Destination" );
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
                will( returnValue( 8L ) );

                destFileSo = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, destFilePath );
                will( returnValue( destFileSo ) );

                one( mockRes ).setStatus( WebdavStatus.SC_NO_CONTENT );

                one( mockStore ).getStoredObject( mockTransaction, sourceFilePath );
                will( returnValue( sourceFileSo ) );

                one( mockStore ).removeObject( mockTransaction, sourceFilePath );
            }
        } );

        final ResourceLocks resLocks = new ResourceLocks();
        final DoDelete doDelete = new DoDelete( mockStore, resLocks, !readOnly );
        final DoCopy doCopy = new DoCopy( mockStore, resLocks, doDelete, !readOnly );

        final DoMove doMove = new DoMove( resLocks, doDelete, doCopy, !readOnly );

        doMove.execute( mockTransaction, mockReq, mockRes );
    }

    @Test
    public void testMovingOfaFileIfDestinationIsPresentAndOverwriteFalse()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceFilePath ) );

                exactly( 2 ).of( mockReq )
                            .getHeader( "Destination" );
                will( returnValue( destFilePath ) );

                one( mockReq ).getServerName();
                will( returnValue( "server_name" ) );

                one( mockReq ).getContextPath();
                will( returnValue( "" ) );

                one( mockReq ).getPathInfo();
                will( returnValue( destFilePath ) );

                one( mockReq ).getServicePath();
                will( returnValue( "servlet_path" ) );

                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceFilePath ) );

                one( mockReq ).getHeader( "Overwrite" );
                will( returnValue( "F" ) );

                final StoredObject sourceFileSo = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, sourceFilePath );
                will( returnValue( sourceFileSo ) );

                final StoredObject destFileSo = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, destFilePath );
                will( returnValue( destFileSo ) );

                one( mockRes ).sendError( WebdavStatus.SC_PRECONDITION_FAILED );

            }
        } );

        final ResourceLocks resLocks = new ResourceLocks();
        final DoDelete doDelete = new DoDelete( mockStore, resLocks, !readOnly );
        final DoCopy doCopy = new DoCopy( mockStore, resLocks, doDelete, !readOnly );

        final DoMove doMove = new DoMove( resLocks, doDelete, doCopy, !readOnly );

        doMove.execute( mockTransaction, mockReq, mockRes );
    }

    @Test
    public void testMovingOfaFileIfDestinationIsPresentAndOverwriteTrue()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceFilePath ) );

                exactly( 2 ).of( mockReq )
                            .getHeader( "Destination" );
                will( returnValue( destFilePath ) );

                one( mockReq ).getServerName();
                will( returnValue( "server_name" ) );

                one( mockReq ).getContextPath();
                will( returnValue( "" ) );

                one( mockReq ).getPathInfo();
                will( returnValue( destFilePath ) );

                one( mockReq ).getServicePath();
                will( returnValue( "servlet_path" ) );

                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceFilePath ) );

                one( mockReq ).getHeader( "Overwrite" );
                will( returnValue( "T" ) );

                final StoredObject sourceFileSo = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, sourceFilePath );
                will( returnValue( sourceFileSo ) );

                final StoredObject destFileSo = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, destFilePath );
                will( returnValue( destFileSo ) );

                one( mockRes ).setStatus( WebdavStatus.SC_NO_CONTENT );

                one( mockStore ).getStoredObject( mockTransaction, destFilePath );
                will( returnValue( destFileSo ) );

                one( mockStore ).removeObject( mockTransaction, destFilePath );

                one( mockStore ).getStoredObject( mockTransaction, sourceFilePath );
                will( returnValue( sourceFileSo ) );

                one( mockStore ).createResource( mockTransaction, destFilePath );

                one( mockStore ).getResourceContent( mockTransaction, sourceFilePath );
                will( returnValue( bais ) );

                one( mockStore ).setResourceContent( mockTransaction, destFilePath, bais, null, null );
                will( returnValue( 8L ) );

                one( mockStore ).getStoredObject( mockTransaction, destFilePath );
                will( returnValue( destFileSo ) );

                one( mockRes ).setStatus( WebdavStatus.SC_NO_CONTENT );

                one( mockStore ).getStoredObject( mockTransaction, sourceFilePath );
                will( returnValue( sourceFileSo ) );

                one( mockStore ).removeObject( mockTransaction, sourceFilePath );
            }
        } );

        final ResourceLocks resLocks = new ResourceLocks();
        final DoDelete doDelete = new DoDelete( mockStore, resLocks, !readOnly );
        final DoCopy doCopy = new DoCopy( mockStore, resLocks, doDelete, !readOnly );

        final DoMove doMove = new DoMove( resLocks, doDelete, doCopy, !readOnly );

        doMove.execute( mockTransaction, mockReq, mockRes );
    }

    @Test
    public void testMovingOfaFileIfSourceNotPresent()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceFilePath ) );

                exactly( 2 ).of( mockReq )
                            .getHeader( "Destination" );
                will( returnValue( destFilePath ) );

                one( mockReq ).getServerName();
                will( returnValue( "server_name" ) );

                one( mockReq ).getContextPath();
                will( returnValue( "" ) );

                one( mockReq ).getPathInfo();
                will( returnValue( destFilePath ) );

                one( mockReq ).getServicePath();
                will( returnValue( "servlet_path" ) );

                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceFilePath ) );

                one( mockReq ).getHeader( "Overwrite" );
                will( returnValue( "F" ) );

                final StoredObject sourceFileSo = null;

                one( mockStore ).getStoredObject( mockTransaction, sourceFilePath );
                will( returnValue( sourceFileSo ) );

                one( mockRes ).sendError( WebdavStatus.SC_NOT_FOUND );
            }
        } );

        final ResourceLocks resLocks = new ResourceLocks();
        final DoDelete doDelete = new DoDelete( mockStore, resLocks, !readOnly );
        final DoCopy doCopy = new DoCopy( mockStore, resLocks, doDelete, !readOnly );

        final DoMove doMove = new DoMove( resLocks, doDelete, doCopy, !readOnly );

        doMove.execute( mockTransaction, mockReq, mockRes );
    }

    @Test
    public void testMovingIfSourcePathEqualsDestinationPath()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( destFilePath ) );

                exactly( 2 ).of( mockReq )
                            .getHeader( "Destination" );
                will( returnValue( destFilePath ) );

                one( mockReq ).getServerName();
                will( returnValue( "server_name" ) );

                one( mockReq ).getContextPath();
                will( returnValue( "" ) );

                one( mockReq ).getPathInfo();
                will( returnValue( destFilePath ) );

                one( mockReq ).getServicePath();
                will( returnValue( "servlet_path" ) );

                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( destFilePath ) );

                one( mockRes ).sendError( WebdavStatus.SC_FORBIDDEN );
            }
        } );

        final ResourceLocks resLocks = new ResourceLocks();
        final DoDelete doDelete = new DoDelete( mockStore, resLocks, !readOnly );
        final DoCopy doCopy = new DoCopy( mockStore, resLocks, doDelete, !readOnly );

        final DoMove doMove = new DoMove( resLocks, doDelete, doCopy, !readOnly );

        doMove.execute( mockTransaction, mockReq, mockRes );
    }

    @Test
    public void testMovingOfaCollectionIfDestinationIsNotPresent()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceCollectionPath ) );

                exactly( 2 ).of( mockReq )
                            .getHeader( "Destination" );
                will( returnValue( destCollectionPath ) );

                one( mockReq ).getServerName();
                will( returnValue( "server_name" ) );

                one( mockReq ).getContextPath();
                will( returnValue( "" ) );

                one( mockReq ).getPathInfo();
                will( returnValue( destCollectionPath ) );

                one( mockReq ).getServicePath();
                will( returnValue( "servlet_path" ) );

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
                will( returnValue( null ) );

                String[] sourceChildren = new String[] { "sourceFile" };

                one( mockStore ).getChildrenNames( mockTransaction, sourceCollectionPath );
                will( returnValue( sourceChildren ) );

                final StoredObject sourceFileSo = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, sourceCollectionPath + "/sourceFile" );
                will( returnValue( sourceFileSo ) );

                one( mockStore ).createResource( mockTransaction, destCollectionPath + "/sourceFile" );

                one( mockStore ).getResourceContent( mockTransaction, sourceCollectionPath + "/sourceFile" );
                will( returnValue( bais ) );

                one( mockStore ).setResourceContent( mockTransaction, destCollectionPath + "/sourceFile", bais, null, null );
                will( returnValue( 8L ) );

                final StoredObject movedSo = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, destCollectionPath + "/sourceFile" );
                will( returnValue( movedSo ) );

                one( mockRes ).setStatus( WebdavStatus.SC_NO_CONTENT );

                one( mockStore ).getStoredObject( mockTransaction, sourceCollectionPath );
                will( returnValue( sourceCollectionSo ) );

                sourceChildren = new String[] { "sourceFile" };

                one( mockStore ).getChildrenNames( mockTransaction, sourceCollectionPath );
                will( returnValue( sourceChildren ) );

                one( mockStore ).getStoredObject( mockTransaction, sourceFilePath );
                will( returnValue( sourceFileSo ) );

                one( mockStore ).removeObject( mockTransaction, sourceFilePath );

                one( mockStore ).removeObject( mockTransaction, sourceCollectionPath );
            }
        } );

        final ResourceLocks resLocks = new ResourceLocks();
        final DoDelete doDelete = new DoDelete( mockStore, resLocks, !readOnly );
        final DoCopy doCopy = new DoCopy( mockStore, resLocks, doDelete, !readOnly );

        final DoMove doMove = new DoMove( resLocks, doDelete, doCopy, !readOnly );

        doMove.execute( mockTransaction, mockReq, mockRes );
    }

    @Test
    public void testMovingOfaCollectionIfDestinationIsPresentAndOverwriteFalse()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceCollectionPath ) );

                exactly( 2 ).of( mockReq )
                            .getHeader( "Destination" );
                will( returnValue( destCollectionPath ) );

                one( mockReq ).getServerName();
                will( returnValue( "server_name" ) );

                one( mockReq ).getContextPath();
                will( returnValue( "" ) );

                one( mockReq ).getPathInfo();
                will( returnValue( destCollectionPath ) );

                one( mockReq ).getServicePath();
                will( returnValue( "servlet_path" ) );

                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceCollectionPath ) );

                one( mockReq ).getHeader( "Overwrite" );
                will( returnValue( "F" ) );

                final StoredObject sourceCollectionSo = initFolderStoredObject();

                one( mockStore ).getStoredObject( mockTransaction, sourceCollectionPath );
                will( returnValue( sourceCollectionSo ) );

                final StoredObject destCollectionSo = initFolderStoredObject();

                one( mockStore ).getStoredObject( mockTransaction, destCollectionPath );
                will( returnValue( destCollectionSo ) );

                one( mockRes ).sendError( WebdavStatus.SC_PRECONDITION_FAILED );
            }
        } );

        final ResourceLocks resLocks = new ResourceLocks();
        final DoDelete doDelete = new DoDelete( mockStore, resLocks, !readOnly );
        final DoCopy doCopy = new DoCopy( mockStore, resLocks, doDelete, !readOnly );

        final DoMove doMove = new DoMove( resLocks, doDelete, doCopy, !readOnly );

        doMove.execute( mockTransaction, mockReq, mockRes );
    }

    @Test
    public void testMovingOfaCollectionIfDestinationIsPresentAndOverwriteTrue()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceCollectionPath ) );

                exactly( 2 ).of( mockReq )
                            .getHeader( "Destination" );
                will( returnValue( overwritePath ) );

                one( mockReq ).getServerName();
                will( returnValue( "server_name" ) );

                one( mockReq ).getContextPath();
                will( returnValue( "" ) );

                one( mockReq ).getPathInfo();
                will( returnValue( overwritePath ) );

                one( mockReq ).getServicePath();
                will( returnValue( "servlet_path" ) );

                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( sourceCollectionPath ) );

                one( mockReq ).getHeader( "Overwrite" );
                will( returnValue( "T" ) );

                final StoredObject sourceCollectionSo = initFolderStoredObject();

                one( mockStore ).getStoredObject( mockTransaction, sourceCollectionPath );
                will( returnValue( sourceCollectionSo ) );

                final StoredObject destCollectionSo = initFolderStoredObject();

                one( mockStore ).getStoredObject( mockTransaction, overwritePath );
                will( returnValue( destCollectionSo ) );

                one( mockRes ).setStatus( WebdavStatus.SC_NO_CONTENT );

                one( mockStore ).getStoredObject( mockTransaction, overwritePath );
                will( returnValue( destCollectionSo ) );

                one( mockStore ).getChildrenNames( mockTransaction, overwritePath );
                will( returnValue( destChildren ) );

                final StoredObject destFileSo = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, overwritePath + "/destFile" );
                will( returnValue( destFileSo ) );

                one( mockStore ).removeObject( mockTransaction, overwritePath + "/destFile" );

                one( mockStore ).removeObject( mockTransaction, overwritePath );

                one( mockStore ).getStoredObject( mockTransaction, sourceCollectionPath );
                will( returnValue( sourceCollectionSo ) );

                one( mockStore ).createFolder( mockTransaction, overwritePath );

                one( mockReq ).getHeader( "Depth" );
                will( returnValue( null ) );

                one( mockStore ).getChildrenNames( mockTransaction, sourceCollectionPath );
                will( returnValue( sourceChildren ) );

                final StoredObject sourceFileSo = initFileStoredObject( resourceContent );

                // failures start here...
                one( mockStore ).getStoredObject( mockTransaction, sourceFilePath );
                will( returnValue( sourceFileSo ) );

                one( mockStore ).createResource( mockTransaction, overwritePath + "/sourceFile" );

                one( mockStore ).getResourceContent( mockTransaction, sourceFilePath );
                will( returnValue( bais ) );

                one( mockStore ).setResourceContent( mockTransaction, overwritePath + "/sourceFile", bais, null, null );

                final StoredObject movedSo = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, overwritePath + "/sourceFile" );
                will( returnValue( movedSo ) );

                one( mockRes ).setStatus( WebdavStatus.SC_NO_CONTENT );

                one( mockStore ).getStoredObject( mockTransaction, sourceCollectionPath );
                will( returnValue( sourceCollectionSo ) );

                sourceChildren = new String[] { "sourceFile" };

                one( mockStore ).getChildrenNames( mockTransaction, sourceCollectionPath );
                will( returnValue( sourceChildren ) );

                one( mockStore ).getStoredObject( mockTransaction, sourceFilePath );
                will( returnValue( sourceFileSo ) );

                one( mockStore ).removeObject( mockTransaction, sourceFilePath );

                one( mockStore ).removeObject( mockTransaction, sourceCollectionPath );
            }
        } );

        final ResourceLocks resLocks = new ResourceLocks();
        final DoDelete doDelete = new DoDelete( mockStore, resLocks, !readOnly );
        final DoCopy doCopy = new DoCopy( mockStore, resLocks, doDelete, !readOnly );

        final DoMove doMove = new DoMove( resLocks, doDelete, doCopy, !readOnly );

        doMove.execute( mockTransaction, mockReq, mockRes );
    }

}

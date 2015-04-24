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

import net.sf.webdav.StoredObject;
import net.sf.webdav.WebdavStatus;
import net.sf.webdav.locking.ResourceLocks;
import net.sf.webdav.spi.IMimeTyper;
import net.sf.webdav.spi.ITransaction;
import net.sf.webdav.spi.IWebdavStoreWorker;
import net.sf.webdav.spi.WebdavRequest;
import net.sf.webdav.spi.WebdavResponse;
import net.sf.webdav.testutil.MockTest;
import net.sf.webdav.testutil.TestingOutputStream;

import org.jmock.Expectations;
import org.junit.Test;

public class DoHeadTest
    extends MockTest
{

    static IWebdavStoreWorker mockStoreWorker;

    static IMimeTyper mockMimeTyper;

    static WebdavRequest mockReq;

    static WebdavResponse mockRes;

    static TestingOutputStream tos;

    static ITransaction mockTransaction;

    static byte[] resourceContent = new byte[] { '<', 'h', 'e', 'l', 'l', 'o', '/', '>' };

    @Override
    public void setupFixtures()
        throws Exception
    {
        mockStoreWorker = _mockery.mock( IWebdavStoreWorker.class );
        mockMimeTyper = _mockery.mock( IMimeTyper.class );
        mockReq = _mockery.mock( WebdavRequest.class );
        mockRes = _mockery.mock( WebdavResponse.class );
        tos = new TestingOutputStream();
        mockTransaction = _mockery.mock( ITransaction.class );
    }

    @Test
    public void testAccessOfaMissingPageResultsIn404()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( "/index.html" ) );

                final StoredObject indexSo = null;

                one( mockStoreWorker ).getStoredObject( mockTransaction, "/index.html" );
                will( returnValue( indexSo ) );

                one( mockRes ).setStatus( WebdavStatus.SC_NOT_FOUND );
            }
        } );

        new DoHead().execute( mockTransaction, mockReq, mockRes, mockStoreWorker,
                              newResources( new ResourceLocks(), mockMimeTyper, false ) );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testAccessOfaPageResultsInPage()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( "/index.html" ) );

                final StoredObject indexSo = initFileStoredObject( resourceContent );

                one( mockStoreWorker ).getStoredObject( mockTransaction, "/index.html" );
                will( returnValue( indexSo ) );

                one( mockReq ).getHeader( "If-None-Match" );
                will( returnValue( null ) );

                one( mockRes ).setDateHeader( "last-modified", indexSo.getLastModified()
                                                                      .getTime() );

                one( mockRes ).addHeader( with( any( String.class ) ), with( any( String.class ) ) );

                one( mockMimeTyper ).getMimeType( "/index.html" );
                will( returnValue( "text/foo" ) );

                one( mockRes ).setContentType( "text/foo" );
            }
        } );

        new DoHead().execute( mockTransaction, mockReq, mockRes, mockStoreWorker,
                              newResources( new ResourceLocks(), mockMimeTyper, false ) );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testAccessOfaDirectoryResultsInRedirectIfDefaultIndexFilePresent()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( "/foo/" ) );

                final StoredObject fooSo = initFolderStoredObject();

                one( mockStoreWorker ).getStoredObject( mockTransaction, "/foo/" );
                will( returnValue( fooSo ) );

                one( mockReq ).getRequestURI();
                will( returnValue( "/foo/" ) );

                one( mockRes ).encodeRedirectURL( "/foo//indexFile" );

                one( mockRes ).sendRedirect( "" );
            }
        } );

        new DoHead().execute( mockTransaction, mockReq, mockRes, mockStoreWorker,
                              newResources( new ResourceLocks(), mockMimeTyper, false ) );

        _mockery.assertIsSatisfied();
    }

}

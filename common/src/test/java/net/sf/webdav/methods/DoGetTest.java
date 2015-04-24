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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.util.Locale;

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

public class DoGetTest
    extends MockTest
{

    static IWebdavStoreWorker mockStoreWorker;

    static IMimeTyper mockMimeTyper;

    static WebdavRequest mockReq;

    static WebdavResponse mockRes;

    static ITransaction mockTransaction;

    static TestingOutputStream tos = new TestingOutputStream();;

    static byte[] resourceContent = new byte[] { '<', 'h', 'e', 'l', 'l', 'o', '/', '>' };

    static ByteArrayInputStream bais = new ByteArrayInputStream( resourceContent );

    //    static DelegatingServletInputStream dsis = new DelegatingServletInputStream(
    //            bais);

    @Override
    protected void setupFixtures()
        throws Exception
    {
        mockStoreWorker = _mockery.mock( IWebdavStoreWorker.class );
        mockMimeTyper = _mockery.mock( IMimeTyper.class );
        mockReq = _mockery.mock( WebdavRequest.class );
        mockRes = _mockery.mock( WebdavResponse.class );
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

                exactly( 2 ).of( mockStoreWorker )
                            .getStoredObject( mockTransaction, "/index.html" );
                will( returnValue( indexSo ) );

                one( mockReq ).getRequestURI();
                will( returnValue( "/index.html" ) );

                one( mockRes ).sendError( WebdavStatus.SC_NOT_FOUND, "/index.html" );

                one( mockRes ).setStatus( WebdavStatus.SC_NOT_FOUND );
            }
        } );

        new DoGet().execute( mockTransaction, mockReq, mockRes, mockStoreWorker,
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

                final StoredObject so = initFileStoredObject( resourceContent );

                one( mockStoreWorker ).getStoredObject( mockTransaction, "/index.html" );
                will( returnValue( so ) );

                one( mockRes ).getOutputStream();
                will( returnValue( tos ) );

                one( mockStoreWorker ).getResourceContent( mockTransaction, "/index.html" );
                will( returnValue( bais ) );
            }
        } );

        new DoGet().execute( mockTransaction, mockReq, mockRes, mockStoreWorker,
                             newResources( new ResourceLocks(), mockMimeTyper, false ) );

        assertEquals( "<hello/>", tos.toString() );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testAccessOfaDirectoryResultsInRudimentaryChildList()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                atLeast( 1 ).of( mockReq )
                            .getLocale();
                will( returnValue( Locale.getDefault() ) );

                atLeast( 1 ).of( mockRes )
                            .setContentType( "text/html" );

                atLeast( 1 ).of( mockRes )
                            .setCharacterEncoding( "UTF8" );

                atLeast( 1 ).of( mockReq )
                            .getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                atLeast( 1 ).of( mockReq )
                            .getPathInfo();
                will( returnValue( "/foo/" ) );

                final StoredObject fooSo = initFolderStoredObject();
                final StoredObject aaaSo = initFolderStoredObject();
                final StoredObject bbbSo = initFolderStoredObject();

                //                atLeast( 1 ).of( mockStore )
                //                            .getStoredObject( mockTransaction, "/foo/" );
                //                will( returnValue( fooSo ) );

                atLeast( 1 ).of( mockReq )
                            .getHeader( "If-None-Match" );
                will( returnValue( null ) );

                atLeast( 1 ).of( mockStoreWorker )
                            .getStoredObject( mockTransaction, "/foo/" );
                will( returnValue( fooSo ) );

                tos = new TestingOutputStream();

                atLeast( 1 ).of( mockRes )
                            .getOutputStream();
                will( returnValue( tos ) );

                atLeast( 1 ).of( mockStoreWorker )
                            .getChildrenNames( mockTransaction, "/foo/" );
                will( returnValue( new String[] { "AAA", "BBB" } ) );

                atLeast( 1 ).of( mockStoreWorker )
                            .getStoredObject( mockTransaction, "/foo//AAA" );
                will( returnValue( aaaSo ) );

                atLeast( 1 ).of( mockStoreWorker )
                            .getStoredObject( mockTransaction, "/foo//BBB" );
                will( returnValue( bbbSo ) );

            }
        } );

        new DoGet().execute( mockTransaction, mockReq, mockRes, mockStoreWorker,
                             newResources( new ResourceLocks(), mockMimeTyper, false ) );

        final String out = tos.toString();

        assertThat( out.contains( "<a href=\"AAA/\">AAA</a>" ), equalTo( true ) );

        assertThat( out.contains( "<a href=\"BBB/\">BBB</a>" ), equalTo( true ) );

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

        new DoGet().execute( mockTransaction, mockReq, mockRes, mockStoreWorker,
                             newResources( new ResourceLocks(), mockMimeTyper, false ) );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testAccessOfaMissingPageResultsInPossibleAlternatveTo404()
        throws Exception
    {

        final TestingOutputStream tos2 = new TestingOutputStream();

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

                final StoredObject alternativeSo = initFileStoredObject( resourceContent );

                one( mockStoreWorker ).getStoredObject( mockTransaction, "/alternative" );
                will( returnValue( alternativeSo ) );

                one( mockReq ).getHeader( "If-None-Match" );
                will( returnValue( null ) );

                one( mockRes ).setDateHeader( "last-modified", alternativeSo.getLastModified()
                                                                            .getTime() );

                one( mockRes ).addHeader( with( any( String.class ) ), with( any( String.class ) ) );

                one( mockMimeTyper ).getMimeType( "/alternative" );
                will( returnValue( "text/foo" ) );

                one( mockRes ).setContentType( "text/foo" );

                one( mockStoreWorker ).getStoredObject( mockTransaction, "/alternative" );
                will( returnValue( alternativeSo ) );

                tos2.write( resourceContent );

                one( mockRes ).getOutputStream();
                will( returnValue( tos2 ) );

                one( mockStoreWorker ).getResourceContent( mockTransaction, "/alternative" );
                will( returnValue( bais ) );

                one( mockRes ).setStatus( WebdavStatus.SC_NOT_FOUND );
            }
        } );

        new DoGet().execute( mockTransaction, mockReq, mockRes, mockStoreWorker,
                             newResources( new ResourceLocks(), mockMimeTyper, false ) );

        assertEquals( "<hello/>", tos2.toString() );

        _mockery.assertIsSatisfied();
    }

}

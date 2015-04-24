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
package net.sf.webdav;

import net.sf.webdav.impl.ActivationMimeTyper;
import net.sf.webdav.impl.LocalFileSystemStore;
import net.sf.webdav.spi.ITransaction;
import net.sf.webdav.spi.IWebdavStore;
import net.sf.webdav.spi.WebdavConfig;
import net.sf.webdav.testutil.MockHttpServletRequest;
import net.sf.webdav.testutil.MockHttpServletResponse;
import net.sf.webdav.testutil.MockHttpSession;
import net.sf.webdav.testutil.MockPrincipal;
import net.sf.webdav.testutil.MockTest;
import net.sf.webdav.testutil.MockWebdavConfig;

import org.jmock.Expectations;
import org.junit.Test;

public class WebdavServletTest
    extends MockTest
{

    // private static WebdavServlet _servlet = new WebdavServlet();
    static WebdavConfig servletConfig;

    //    static ServletContext servletContext;

    // static HttpServletRequest mockeryReq;
    // static HttpServletResponse mockRes;
    static IWebdavStore mockStore;

    static MockWebdavConfig config;

    //    static MockServletContext mockServletContext;

    static MockHttpServletRequest mockReq;

    static MockHttpServletResponse mockRes;

    static MockHttpSession mockHttpSession;

    static MockPrincipal mockPrincipal;

    static ITransaction mockTransaction;

    static boolean readOnly = true;

    static byte[] resourceContent = new byte[] { '<', 'h', 'e', 'l', 'l', 'o', '/', '>' };

    static String dftIndexFile = "/index.html";

    static String insteadOf404 = "/insteadOf404";

    @Override
    public void setupFixtures()
        throws Exception
    {
        //        servletContext = _mockery.mock( ServletContext.class );
        mockStore = _mockery.mock( IWebdavStore.class );

        config = new MockWebdavConfig();
        config.setAlt404Path( insteadOf404 );
        config.setDefaultIndexPath( dftIndexFile );

        //        mockHttpSession = new MockHttpSession(mockServletContext);
        //        mockServletContext = new MockServletContext();
        mockReq = new MockHttpServletRequest();
        mockRes = new MockHttpServletResponse();

        mockPrincipal = new MockPrincipal( "Admin", new String[] { "Admin", "Manager" } );

        mockTransaction = _mockery.mock( ITransaction.class );
    }

    @Test
    public void testInit()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
        } );

        new WebdavService( config, new LocalFileSystemStore( tempFolder.newFolder( "davRoot" ) ),
                           new ActivationMimeTyper() );

        _mockery.assertIsSatisfied();
    }

    // Test successes in eclipse, but fails in "mvn test"
    // first three expectations aren't successful with "mvn test"
    @Test
    public void testInitGenericServlet()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                //                allowing( servletConfig ).getContext();
                //                will( returnValue( mockServletContext ) );
                //
                //                allowing( servletConfig ).getServletName();
                //                will( returnValue( "webdav-servlet" ) );
                //
                //                allowing( servletContext ).log( "webdav-servlet: init" );

                //                one( servletConfig ).getInitParameter( "rootpath" );
                //                will( returnValue( "./target/tmpTestData/" ) );
                //
                //                exactly( 2 ).of( servletConfig )
                //                            .getInitParameter( "lazyFolderCreationOnPut" );
                //                will( returnValue( "1" ) );
                //
                //                one( servletConfig ).getInitParameter( "default-index-file" );
                //                will( returnValue( "index.html" ) );
                //
                //                one( servletConfig ).getInitParameter( "instead-of-404" );
                //                will( returnValue( "" ) );
                //
                //                exactly( 2 ).of( servletConfig )
                //                            .getInitParameter( "no-content-length-headers" );
                //                will( returnValue( "0" ) );
            }
        } );

        new WebdavService( config, new LocalFileSystemStore( tempFolder.newFolder( "davRoot" ) ),
                           new ActivationMimeTyper() );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testService()
        throws Exception
    {
        config.setLazyFolderCreationOnPut( true );
        config.setOmitContentLengthHeaders( false );

        //        config.addInitParameter( "ResourceHandlerImplementation", "" );
        //        config.addInitParameter( "rootpath", "./target/tmpTestData" );
        //        config.addInitParameter( "lazyFolderCreationOnPut", "1" );
        //        config.addInitParameter( "default-index-file", dftIndexFile );
        //        config.addInitParameter( "instead-of-404", insteadOf404 );
        //        config.addInitParameter( "no-content-length-headers", "0" );

        // StringTokenizer headers = new StringTokenizer(
        // "Host Depth Content-Type Content-Length");
        mockReq.setMethod( "PUT" );
        mockReq.setAttribute( "javax.servlet.include.request_uri", null );
        mockReq.setPathInfo( "/aPath/toAFile" );
        mockReq.setRequestURI( "/aPath/toAFile" );
        mockReq.addHeader( "Host", "www.foo.bar" );
        mockReq.addHeader( "Depth", "0" );
        mockReq.addHeader( "Content-Type", "text/xml" );
        mockReq.addHeader( "Content-Length", "1234" );
        mockReq.addHeader( "User-Agent", "...some Client with WebDAVFS..." );

        //        mockReq.setSession( mockHttpSession );
        mockPrincipal = new MockPrincipal( "Admin", new String[] { "Admin", "Manager" } );
        mockReq.setUserPrincipal( mockPrincipal );
        //        mockReq.addUserRole( "Admin" );
        //        mockReq.addUserRole( "Manager" );

        mockReq.setContent( resourceContent );

        _mockery.checking( new Expectations()
        {
            {

            }
        } );

        final WebdavService servlet =
            new WebdavService( config, new LocalFileSystemStore( tempFolder.newFolder( "davRoot" ) ),
                               new ActivationMimeTyper() );

        servlet.service( mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }
}

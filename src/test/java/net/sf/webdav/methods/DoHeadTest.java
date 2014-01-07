package net.sf.webdav.methods;

import net.sf.webdav.StoredObject;
import net.sf.webdav.WebdavStatus;
import net.sf.webdav.locking.ResourceLocks;
import net.sf.webdav.spi.IMimeTyper;
import net.sf.webdav.spi.ITransaction;
import net.sf.webdav.spi.IWebdavStore;
import net.sf.webdav.spi.WebdavRequest;
import net.sf.webdav.spi.WebdavResponse;
import net.sf.webdav.testutil.MockTest;
import net.sf.webdav.testutil.TestingOutputStream;

import org.jmock.Expectations;
import org.junit.Test;

public class DoHeadTest
    extends MockTest
{

    static IWebdavStore mockStore;

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
        mockStore = _mockery.mock( IWebdavStore.class );
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

                one( mockStore ).getStoredObject( mockTransaction, "/index.html" );
                will( returnValue( indexSo ) );

                one( mockRes ).setStatus( WebdavStatus.SC_NOT_FOUND );
            }
        } );

        final DoHead doHead = new DoHead( mockStore, null, null, new ResourceLocks(), mockMimeTyper, false );
        doHead.execute( mockTransaction, mockReq, mockRes );

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

                one( mockStore ).getStoredObject( mockTransaction, "/index.html" );
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

        final DoHead doHead = new DoHead( mockStore, null, null, new ResourceLocks(), mockMimeTyper, false );

        doHead.execute( mockTransaction, mockReq, mockRes );

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

                one( mockStore ).getStoredObject( mockTransaction, "/foo/" );
                will( returnValue( fooSo ) );

                one( mockReq ).getRequestURI();
                will( returnValue( "/foo/" ) );

                one( mockRes ).encodeRedirectURL( "/foo//indexFile" );

                one( mockRes ).sendRedirect( "" );
            }
        } );

        final DoHead doHead = new DoHead( mockStore, "/indexFile", null, new ResourceLocks(), mockMimeTyper, false );

        doHead.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

}

package net.sf.webdav.methods;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;

import net.sf.webdav.StoredObject;
import net.sf.webdav.WebdavStatus;
import net.sf.webdav.locking.ResourceLocks;
import net.sf.webdav.spi.IMimeTyper;
import net.sf.webdav.spi.ITransaction;
import net.sf.webdav.spi.IWebdavStore;
import net.sf.webdav.spi.WebdavRequest;
import net.sf.webdav.spi.WebdavResponse;
import net.sf.webdav.testutil.MockTest;

import org.jmock.Expectations;
import org.junit.Test;

public class DoProppatchTest
    extends MockTest
{
    static IWebdavStore mockStore;

    static IMimeTyper mockMimeTyper;

    static WebdavRequest mockReq;

    static WebdavResponse mockRes;

    static ITransaction mockTransaction;

    static byte[] resourceContent = new byte[] { '<', 'h', 'e', 'l', 'l', 'o', '/', '>' };

    static ByteArrayInputStream bais = new ByteArrayInputStream( resourceContent );

    //    static DelegatingServletInputStream dsis = new DelegatingServletInputStream(
    //            bais);

    @Override
    public void setupFixtures()
        throws Exception
    {
        mockStore = _mockery.mock( IWebdavStore.class );
        mockMimeTyper = _mockery.mock( IMimeTyper.class );
        mockReq = _mockery.mock( WebdavRequest.class );
        mockRes = _mockery.mock( WebdavResponse.class );
        mockTransaction = _mockery.mock( ITransaction.class );
    }

    @Test
    public void doProppatchIfReadOnly()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockRes ).sendError( WebdavStatus.SC_FORBIDDEN );
            }
        } );

        final DoProppatch doProppatch = new DoProppatch( mockStore, new ResourceLocks(), readOnly );

        doProppatch.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void doProppatchOnNonExistingResource()
        throws Exception
    {

        final String path = "/notExists";

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( path ) );

                final StoredObject notExistingSo = null;

                one( mockStore ).getStoredObject( mockTransaction, path );
                will( returnValue( notExistingSo ) );

                one( mockRes ).sendError( WebdavStatus.SC_NOT_FOUND );
            }
        } );

        final DoProppatch doProppatch = new DoProppatch( mockStore, new ResourceLocks(), !readOnly );

        doProppatch.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void doProppatchOnRequestWithNoContent()
        throws Exception
    {

        final String path = "/testFile";

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( path ) );

                final StoredObject testFileSo = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, path );
                will( returnValue( testFileSo ) );

                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( path ) );

                one( mockReq ).getContentLength();
                will( returnValue( 0 ) );

                one( mockRes ).sendError( WebdavStatus.SC_INTERNAL_SERVER_ERROR );
            }
        } );

        final DoProppatch doProppatch = new DoProppatch( mockStore, new ResourceLocks(), !readOnly );

        doProppatch.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void doProppatchOnResource()
        throws Exception
    {

        final String path = "/testFile";
        final PrintWriter pw = new PrintWriter( "/tmp/XMLTestFile" );

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( path ) );

                final StoredObject testFileSo = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, path );
                will( returnValue( testFileSo ) );

                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( path ) );

                one( mockReq ).getContentLength();
                will( returnValue( 8 ) );

                one( mockReq ).getInputStream();
                will( returnValue( bais ) );

                one( mockRes ).setStatus( WebdavStatus.SC_MULTI_STATUS );

                one( mockRes ).setContentType( "text/xml; charset=UTF-8" );

                one( mockRes ).getWriter();
                will( returnValue( pw ) );

                one( mockReq ).getContextPath();
                will( returnValue( "" ) );
            }
        } );

        final DoProppatch doProppatch = new DoProppatch( mockStore, new ResourceLocks(), !readOnly );

        doProppatch.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

}

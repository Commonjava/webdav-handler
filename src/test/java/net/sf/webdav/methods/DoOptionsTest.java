package net.sf.webdav.methods;

import java.io.IOException;

import net.sf.webdav.StoredObject;
import net.sf.webdav.exceptions.WebdavException;
import net.sf.webdav.locking.ResourceLocks;
import net.sf.webdav.spi.IMimeTyper;
import net.sf.webdav.spi.ITransaction;
import net.sf.webdav.spi.IWebdavStore;
import net.sf.webdav.spi.WebdavRequest;
import net.sf.webdav.spi.WebdavResponse;
import net.sf.webdav.testutil.MockTest;

import org.jmock.Expectations;
import org.junit.Test;

public class DoOptionsTest
    extends MockTest
{

    static IWebdavStore mockStore;

    static WebdavRequest mockReq;

    static WebdavResponse mockRes;

    static IMimeTyper mockMimeTyper;

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
        mockTransaction = _mockery.mock( ITransaction.class );
    }

    @Test
    public void testOptionsOnExistingNode()
        throws IOException, WebdavException
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( "/index.html" ) );

                one( mockRes ).addHeader( "DAV", "1, 2" );

                final StoredObject indexSo = initFileStoredObject( resourceContent );

                one( mockStore ).getStoredObject( mockTransaction, "/index.html" );
                will( returnValue( indexSo ) );

                one( mockRes ).addHeader( "Allow", "OPTIONS, GET, HEAD, POST, DELETE, " + "TRACE, PROPPATCH, COPY, " + "MOVE, LOCK, UNLOCK, PROPFIND" );

                one( mockRes ).addHeader( "MS-Author-Via", "DAV" );
            }
        } );

        final DoOptions doOptions = new DoOptions( mockStore, new ResourceLocks() );
        doOptions.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testOptionsOnNonExistingNode()
        throws IOException, WebdavException
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getAttribute( "javax.servlet.include.request_uri" );
                will( returnValue( null ) );

                one( mockReq ).getPathInfo();
                will( returnValue( "/index.html" ) );

                one( mockRes ).addHeader( "DAV", "1, 2" );

                final StoredObject indexSo = null;

                one( mockStore ).getStoredObject( mockTransaction, "/index.html" );
                will( returnValue( indexSo ) );

                one( mockRes ).addHeader( "Allow", "OPTIONS, MKCOL, PUT" );

                one( mockRes ).addHeader( "MS-Author-Via", "DAV" );
            }
        } );

        final DoOptions doOptions = new DoOptions( mockStore, new ResourceLocks() );
        doOptions.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

}

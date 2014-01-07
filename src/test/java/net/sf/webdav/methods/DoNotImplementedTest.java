package net.sf.webdav.methods;

import net.sf.webdav.WebdavStatus;
import net.sf.webdav.spi.ITransaction;
import net.sf.webdav.spi.IWebdavStore;
import net.sf.webdav.spi.WebdavRequest;
import net.sf.webdav.spi.WebdavResponse;
import net.sf.webdav.testutil.MockTest;

import org.jmock.Expectations;
import org.junit.Test;

public class DoNotImplementedTest
    extends MockTest
{

    static IWebdavStore mockStore;

    static WebdavRequest mockReq;

    static WebdavResponse mockRes;

    static ITransaction mockTransaction;

    @Override
    protected void setupFixtures()
        throws Exception
    {
        mockStore = _mockery.mock( IWebdavStore.class );
        mockReq = _mockery.mock( WebdavRequest.class );
        mockRes = _mockery.mock( WebdavResponse.class );
        mockTransaction = _mockery.mock( ITransaction.class );
    }

    @Test
    public void testDoNotImplementedIfReadOnlyTrue()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getMethod();
                will( returnValue( "notImplementedMethod" ) );
                one( mockRes ).sendError( WebdavStatus.SC_FORBIDDEN );
            }
        } );

        final DoNotImplemented doNotImplemented = new DoNotImplemented( readOnly );
        doNotImplemented.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }

    @Test
    public void testDoNotImplementedIfReadOnlyFalse()
        throws Exception
    {

        _mockery.checking( new Expectations()
        {
            {
                one( mockReq ).getMethod();
                will( returnValue( "notImplementedMethod" ) );
                one( mockRes ).sendError( WebdavStatus.SC_NOT_IMPLEMENTED );
            }
        } );

        final DoNotImplemented doNotImplemented = new DoNotImplemented( !readOnly );
        doNotImplemented.execute( mockTransaction, mockReq, mockRes );

        _mockery.assertIsSatisfied();
    }
}

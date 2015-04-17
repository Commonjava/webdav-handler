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

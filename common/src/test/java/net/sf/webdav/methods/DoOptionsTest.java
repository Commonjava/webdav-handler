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

import java.io.IOException;

import net.sf.webdav.StoredObject;
import net.sf.webdav.exceptions.WebdavException;
import net.sf.webdav.locking.ResourceLocks;
import net.sf.webdav.spi.IMimeTyper;
import net.sf.webdav.spi.ITransaction;
import net.sf.webdav.spi.IWebdavStoreWorker;
import net.sf.webdav.spi.WebdavRequest;
import net.sf.webdav.spi.WebdavResponse;
import net.sf.webdav.testutil.MockTest;

import org.jmock.Expectations;
import org.junit.Test;

public class DoOptionsTest
    extends MockTest
{

    static IWebdavStoreWorker mockStoreWorker;

    static WebdavRequest mockReq;

    static WebdavResponse mockRes;

    static IMimeTyper mockMimeTyper;

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

                one( mockStoreWorker ).getStoredObject( mockTransaction, "/index.html" );
                will( returnValue( indexSo ) );

                one( mockRes ).addHeader( "Allow", "OPTIONS, GET, HEAD, POST, DELETE, " + "TRACE, PROPPATCH, COPY, " + "MOVE, LOCK, UNLOCK, PROPFIND" );

                one( mockRes ).addHeader( "MS-Author-Via", "DAV" );
            }
        } );

        new DoOptions().execute( mockTransaction, mockReq, mockRes, mockStoreWorker,
                                 newResources( new ResourceLocks(), !readOnly ) );

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

                one( mockStoreWorker ).getStoredObject( mockTransaction, "/index.html" );
                will( returnValue( indexSo ) );

                one( mockRes ).addHeader( "Allow", "OPTIONS, MKCOL, PUT" );

                one( mockRes ).addHeader( "MS-Author-Via", "DAV" );
            }
        } );

        new DoOptions().execute( mockTransaction, mockReq, mockRes, mockStoreWorker,
                                 newResources( new ResourceLocks(), !readOnly ) );

        _mockery.assertIsSatisfied();
    }

}

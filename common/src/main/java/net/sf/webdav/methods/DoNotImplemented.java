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

import static net.sf.webdav.WebdavStatus.SC_FORBIDDEN;
import static net.sf.webdav.WebdavStatus.SC_NOT_IMPLEMENTED;

import java.io.IOException;

import net.sf.webdav.WebdavResources;
import net.sf.webdav.spi.ITransaction;
import net.sf.webdav.spi.IWebdavStoreWorker;
import net.sf.webdav.spi.WebdavRequest;
import net.sf.webdav.spi.WebdavResponse;

public class DoNotImplemented
    implements WebdavMethod
{

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( DoNotImplemented.class );

    @Override
    public void execute( final ITransaction transaction, final WebdavRequest req, final WebdavResponse resp,
                         final IWebdavStoreWorker worker, final WebdavResources resources )
        throws IOException
    {
        LOG.trace( "-- " + req.getMethod() );

        if ( resources.isReadOnly() )
        {
            resp.sendError( SC_FORBIDDEN );
        }
        else
        {
            resp.sendError( SC_NOT_IMPLEMENTED );
        }
    }
}

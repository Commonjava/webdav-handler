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
import net.sf.webdav.WebdavResources;
import net.sf.webdav.WebdavStatus;
import net.sf.webdav.exceptions.LockFailedException;
import net.sf.webdav.exceptions.WebdavException;
import net.sf.webdav.locking.IResourceLocks;
import net.sf.webdav.locking.LockedObject;
import net.sf.webdav.spi.ITransaction;
import net.sf.webdav.spi.IWebdavStoreWorker;
import net.sf.webdav.spi.WebdavRequest;
import net.sf.webdav.spi.WebdavResponse;

public class DoUnlock
    extends DeterminableMethod
{

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( DoUnlock.class );

    @Override
    public void execute( final ITransaction transaction, final WebdavRequest req, final WebdavResponse resp,
                         final IWebdavStoreWorker worker, final WebdavResources resources )
        throws IOException, WebdavException
    {
        LOG.trace( "-- " + this.getClass()
                               .getName() );

        if ( resources.isReadOnly() )
        {
            resp.sendError( WebdavStatus.SC_FORBIDDEN );
            return;
        }
        else
        {

            final String path = getRelativePath( req );
            final String tempLockOwner = "doUnlock" + System.currentTimeMillis() + req.toString();
            try
            {
                final IResourceLocks _resourceLocks = resources.getResourceLocks();
                if ( _resourceLocks.lock( transaction, path, tempLockOwner, false, 0, TEMP_TIMEOUT, TEMPORARY ) )
                {

                    final String lockId = getLockIdFromLockTokenHeader( req );
                    LockedObject lo;
                    if ( lockId != null && ( ( lo = _resourceLocks.getLockedObjectByID( transaction, lockId ) ) != null ) )
                    {

                        final String[] owners = lo.getOwner();
                        String owner = null;
                        if ( lo.isShared() )
                        {
                            // more than one owner is possible
                            if ( owners != null )
                            {
                                for ( final String owner2 : owners )
                                {
                                    // remove owner from LockedObject
                                    lo.removeLockedObjectOwner( owner2 );
                                }
                            }
                        }
                        else
                        {
                            // exclusive, only one lock owner
                            if ( owners != null )
                            {
                                owner = owners[0];
                            }
                            else
                            {
                                owner = null;
                            }
                        }

                        if ( _resourceLocks.unlock( transaction, lockId, owner ) )
                        {
                            final StoredObject so = worker.getStoredObject( transaction, path );
                            if ( so.isNullResource() )
                            {
                                worker.removeObject( transaction, path );
                            }

                            resp.setStatus( WebdavStatus.SC_NO_CONTENT );
                        }
                        else
                        {
                            LOG.trace( "DoUnlock failure at " + lo.getPath() );
                            resp.sendError( WebdavStatus.SC_METHOD_FAILURE );
                        }

                    }
                    else
                    {
                        resp.sendError( WebdavStatus.SC_BAD_REQUEST );
                    }
                }
            }
            catch ( final LockFailedException e )
            {
                // FIXME
                e.printStackTrace();
            }
            finally
            {
                resources.getResourceLocks()
                         .unlockTemporaryLockedObjects( transaction, path, tempLockOwner );
            }
        }
    }

}

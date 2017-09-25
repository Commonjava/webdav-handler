/**
 * Copyright (C) 2006-2017 Apache Software Foundation (https://sourceforge.net/p/webdav-servlet, https://github.com/Commonjava/webdav-handler)
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
import java.util.Hashtable;

import net.sf.webdav.WebdavStatus;
import net.sf.webdav.exceptions.AccessDeniedException;
import net.sf.webdav.exceptions.LockFailedException;
import net.sf.webdav.exceptions.ObjectAlreadyExistsException;
import net.sf.webdav.exceptions.WebdavException;
import net.sf.webdav.locking.ResourceLocks;
import net.sf.webdav.spi.ITransaction;
import net.sf.webdav.spi.WebdavRequest;
import net.sf.webdav.spi.WebdavResponse;

public class DoMove
    extends AbstractMethod
{

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( DoMove.class );

    private final ResourceLocks _resourceLocks;

    private final DoDelete _doDelete;

    private final DoCopy _doCopy;

    private final boolean _readOnly;

    public DoMove( final ResourceLocks resourceLocks, final DoDelete doDelete, final DoCopy doCopy, final boolean readOnly )
    {
        _resourceLocks = resourceLocks;
        _doDelete = doDelete;
        _doCopy = doCopy;
        _readOnly = readOnly;
    }

    @Override
    public void execute( final ITransaction transaction, final WebdavRequest req, final WebdavResponse resp )
        throws IOException, LockFailedException
    {

        if ( !_readOnly )
        {
            LOG.trace( "-- " + this.getClass()
                                   .getName() );

            final String sourcePath = getRelativePath( req );
            Hashtable<String, WebdavStatus> errorList = new Hashtable<String, WebdavStatus>();

            if ( !checkLocks( transaction, req, resp, _resourceLocks, sourcePath ) )
            {
                errorList.put( sourcePath, WebdavStatus.SC_LOCKED );
                sendReport( req, resp, errorList );
                return;
            }

            final String destinationPath = req.getHeader( "Destination" );
            if ( destinationPath == null )
            {
                resp.sendError( WebdavStatus.SC_BAD_REQUEST );
                return;
            }

            if ( !checkLocks( transaction, req, resp, _resourceLocks, destinationPath ) )
            {
                errorList.put( destinationPath, WebdavStatus.SC_LOCKED );
                sendReport( req, resp, errorList );
                return;
            }

            final String tempLockOwner = "doMove" + System.currentTimeMillis() + req.toString();

            if ( _resourceLocks.lock( transaction, sourcePath, tempLockOwner, false, 0, TEMP_TIMEOUT, TEMPORARY ) )
            {
                try
                {

                    if ( _doCopy.copyResource( transaction, req, resp ) )
                    {

                        errorList = new Hashtable<String, WebdavStatus>();
                        _doDelete.deleteResource( transaction, sourcePath, errorList, req, resp );
                        if ( !errorList.isEmpty() )
                        {
                            sendReport( req, resp, errorList );
                        }
                    }

                }
                catch ( final AccessDeniedException e )
                {
                    resp.sendError( WebdavStatus.SC_FORBIDDEN );
                }
                catch ( final ObjectAlreadyExistsException e )
                {
                    resp.sendError( WebdavStatus.SC_NOT_FOUND, req.getRequestURI() );
                }
                catch ( final WebdavException e )
                {
                    resp.sendError( WebdavStatus.SC_INTERNAL_SERVER_ERROR );
                }
                finally
                {
                    _resourceLocks.unlockTemporaryLockedObjects( transaction, sourcePath, tempLockOwner );
                }
            }
            else
            {
                errorList.put( req.getHeader( "Destination" ), WebdavStatus.SC_LOCKED );
                sendReport( req, resp, errorList );
            }
        }
        else
        {
            resp.sendError( WebdavStatus.SC_FORBIDDEN );

        }

    }

}

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
import java.util.Hashtable;

import net.sf.webdav.StoredObject;
import net.sf.webdav.WebdavStatus;
import net.sf.webdav.exceptions.AccessDeniedException;
import net.sf.webdav.exceptions.LockFailedException;
import net.sf.webdav.exceptions.WebdavException;
import net.sf.webdav.locking.IResourceLocks;
import net.sf.webdav.locking.LockedObject;
import net.sf.webdav.spi.ITransaction;
import net.sf.webdav.spi.IWebdavStore;
import net.sf.webdav.spi.WebdavRequest;
import net.sf.webdav.spi.WebdavResponse;

public class DoMkcol
    extends AbstractMethod
{

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( DoMkcol.class );

    private final IWebdavStore _store;

    private final IResourceLocks _resourceLocks;

    private final boolean _readOnly;

    public DoMkcol( final IWebdavStore store, final IResourceLocks resourceLocks, final boolean readOnly )
    {
        _store = store;
        _resourceLocks = resourceLocks;
        _readOnly = readOnly;
    }

    @Override
    public void execute( final ITransaction transaction, final WebdavRequest req, final WebdavResponse resp )
        throws IOException, LockFailedException
    {
        LOG.trace( "-- " + this.getClass()
                               .getName() );

        if ( !_readOnly )
        {
            final String path = getRelativePath( req );
            final String parentPath = getParentPath( getCleanPath( path ) );

            final Hashtable<String, WebdavStatus> errorList = new Hashtable<String, WebdavStatus>();

            if ( !checkLocks( transaction, req, resp, _resourceLocks, parentPath ) )
            {
                // TODO remove
                LOG.trace( "MkCol on locked resource (parentPath) not executable!" + "\n Sending SC_FORBIDDEN (403) error response!" );

                resp.sendError( WebdavStatus.SC_FORBIDDEN );
                return;
            }

            final String tempLockOwner = "doMkcol" + System.currentTimeMillis() + req.toString();

            if ( _resourceLocks.lock( transaction, path, tempLockOwner, false, 0, TEMP_TIMEOUT, TEMPORARY ) )
            {
                StoredObject parentSo, so = null;
                try
                {
                    parentSo = _store.getStoredObject( transaction, parentPath );
                    if ( parentSo == null )
                    {
                        // parent not exists
                        resp.sendError( WebdavStatus.SC_CONFLICT );
                        return;
                    }
                    if ( parentPath != null && parentSo.isFolder() )
                    {
                        so = _store.getStoredObject( transaction, path );
                        if ( so == null )
                        {
                            _store.createFolder( transaction, path );
                            resp.setStatus( WebdavStatus.SC_CREATED );
                        }
                        else
                        {
                            // object already exists
                            if ( so.isNullResource() )
                            {

                                final LockedObject nullResourceLo = _resourceLocks.getLockedObjectByPath( transaction, path );
                                if ( nullResourceLo == null )
                                {
                                    resp.sendError( WebdavStatus.SC_INTERNAL_SERVER_ERROR );
                                    return;
                                }
                                final String nullResourceLockToken = nullResourceLo.getID();
                                final String[] lockTokens = getLockIdFromIfHeader( req );
                                String lockToken = null;
                                if ( lockTokens != null )
                                {
                                    lockToken = lockTokens[0];
                                }
                                else
                                {
                                    resp.sendError( WebdavStatus.SC_BAD_REQUEST );
                                    return;
                                }
                                if ( lockToken.equals( nullResourceLockToken ) )
                                {
                                    so.setNullResource( false );
                                    so.setFolder( true );

                                    final String[] nullResourceLockOwners = nullResourceLo.getOwner();
                                    String owner = null;
                                    if ( nullResourceLockOwners != null )
                                    {
                                        owner = nullResourceLockOwners[0];
                                    }

                                    if ( _resourceLocks.unlock( transaction, lockToken, owner ) )
                                    {
                                        resp.setStatus( WebdavStatus.SC_CREATED );
                                    }
                                    else
                                    {
                                        resp.sendError( WebdavStatus.SC_INTERNAL_SERVER_ERROR );
                                    }

                                }
                                else
                                {
                                    // TODO remove
                                    LOG.trace( "MkCol on lock-null-resource with wrong lock-token!" + "\n Sending multistatus error report!" );

                                    errorList.put( path, WebdavStatus.SC_LOCKED );
                                    sendReport( req, resp, errorList );
                                }

                            }
                            else
                            {
                                final String methodsAllowed = DeterminableMethod.determineMethodsAllowed( so );
                                resp.addHeader( "Allow", methodsAllowed );
                                resp.sendError( WebdavStatus.SC_METHOD_NOT_ALLOWED );
                            }
                        }

                    }
                    else if ( parentPath != null && parentSo.isResource() )
                    {
                        // TODO remove
                        LOG.trace( "MkCol on resource is not executable" + "\n Sending SC_METHOD_NOT_ALLOWED (405) error response!" );

                        final String methodsAllowed = DeterminableMethod.determineMethodsAllowed( parentSo );
                        resp.addHeader( "Allow", methodsAllowed );
                        resp.sendError( WebdavStatus.SC_METHOD_NOT_ALLOWED );

                    }
                    else
                    {
                        resp.sendError( WebdavStatus.SC_FORBIDDEN );
                    }
                }
                catch ( final AccessDeniedException e )
                {
                    resp.sendError( WebdavStatus.SC_FORBIDDEN );
                }
                catch ( final WebdavException e )
                {
                    resp.sendError( WebdavStatus.SC_INTERNAL_SERVER_ERROR );
                }
                finally
                {
                    _resourceLocks.unlockTemporaryLockedObjects( transaction, path, tempLockOwner );
                }
            }
            else
            {
                resp.sendError( WebdavStatus.SC_INTERNAL_SERVER_ERROR );
            }

        }
        else
        {
            resp.sendError( WebdavStatus.SC_FORBIDDEN );
        }
    }

}

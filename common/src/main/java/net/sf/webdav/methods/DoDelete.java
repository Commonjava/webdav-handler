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
import static net.sf.webdav.WebdavStatus.SC_INTERNAL_SERVER_ERROR;
import static net.sf.webdav.WebdavStatus.SC_LOCKED;
import static net.sf.webdav.WebdavStatus.SC_NOT_FOUND;

import java.io.IOException;
import java.util.Hashtable;

import net.sf.webdav.StoredObject;
import net.sf.webdav.WebdavResources;
import net.sf.webdav.WebdavStatus;
import net.sf.webdav.exceptions.AccessDeniedException;
import net.sf.webdav.exceptions.LockFailedException;
import net.sf.webdav.exceptions.ObjectAlreadyExistsException;
import net.sf.webdav.exceptions.ObjectNotFoundException;
import net.sf.webdav.exceptions.WebdavException;
import net.sf.webdav.locking.IResourceLocks;
import net.sf.webdav.spi.ITransaction;
import net.sf.webdav.spi.IWebdavStoreWorker;
import net.sf.webdav.spi.WebdavRequest;
import net.sf.webdav.spi.WebdavResponse;

public class DoDelete
    extends AbstractMethod
{

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( DoDelete.class );

    @Override
    public void execute( final ITransaction transaction, final WebdavRequest req, final WebdavResponse resp,
                         final IWebdavStoreWorker worker, final WebdavResources resources )
        throws IOException, LockFailedException
    {
        LOG.trace( "-- " + this.getClass()
                               .getName() );

        if ( !resources.isReadOnly() )
        {
            final String path = getRelativePath( req );
            final String parentPath = getParentPath( getCleanPath( path ) );

            Hashtable<String, WebdavStatus> errorList = new Hashtable<String, WebdavStatus>();

            final IResourceLocks _resourceLocks = resources.getResourceLocks();
            if ( !checkLocks( transaction, req, resp, _resourceLocks, parentPath ) )
            {
                errorList.put( parentPath, SC_LOCKED );
                sendReport( req, resp, errorList );
                return; // parent is locked
            }

            if ( !checkLocks( transaction, req, resp, _resourceLocks, path ) )
            {
                errorList.put( path, SC_LOCKED );
                sendReport( req, resp, errorList );
                return; // resource is locked
            }

            final String tempLockOwner = "doDelete" + System.currentTimeMillis() + req.toString();
            if ( _resourceLocks.lock( transaction, path, tempLockOwner, false, 0, TEMP_TIMEOUT, TEMPORARY ) )
            {
                try
                {
                    errorList = new Hashtable<String, WebdavStatus>();
                    deleteResource( transaction, path, errorList, req, resp, worker, resources );
                    if ( !errorList.isEmpty() )
                    {
                        sendReport( req, resp, errorList );
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

    /**
     * deletes the recources at "path"
     * 
     * @param transaction
     *      indicates that the method is within the scope of a WebDAV
     *      transaction
     * @param path
     *      the folder to be deleted
     * @param errorList
     *      all errors that ocurred
     * @param req
     *      HttpServletRequest
     * @param resp
     *      HttpServletResponse
     * @throws WebdavException
     *      if an error in the underlying store occurs
     * @throws IOException
     *      when an error occurs while sending the response
     */
    public void deleteResource( final ITransaction transaction, final String path, final Hashtable<String, WebdavStatus> errorList,
 final WebdavRequest req,
                                final WebdavResponse resp, final IWebdavStoreWorker worker,
                                final WebdavResources resources )
        throws IOException, WebdavException
    {

        resp.setStatus( WebdavStatus.SC_NO_CONTENT );

        if ( !resources.isReadOnly() )
        {

            StoredObject so = worker.getStoredObject( transaction, path );
            if ( so != null )
            {

                if ( so.isResource() )
                {
                    worker.removeObject( transaction, path );
                }
                else
                {
                    if ( so.isFolder() )
                    {
                        deleteFolder( transaction, path, errorList, req, resp, worker, resources );
                        worker.removeObject( transaction, path );
                    }
                    else
                    {
                        resp.sendError( WebdavStatus.SC_NOT_FOUND );
                    }
                }
            }
            else
            {
                resp.sendError( WebdavStatus.SC_NOT_FOUND );
            }
            so = null;

        }
        else
        {
            resp.sendError( WebdavStatus.SC_FORBIDDEN );
        }
    }

    /**
     * 
     * helper method of deleteResource() deletes the folder and all of its
     * contents
     * 
     * @param transaction
     *      indicates that the method is within the scope of a WebDAV
     *      transaction
     * @param path
     *      the folder to be deleted
     * @param errorList
     *      all errors that ocurred
     * @param req
     *      HttpServletRequest
     * @param resp
     *      HttpServletResponse
     * @throws WebdavException
     *      if an error in the underlying store occurs
     */
    private void deleteFolder( final ITransaction transaction, final String path, final Hashtable<String, WebdavStatus> errorList,
 final WebdavRequest req,
                               final WebdavResponse resp, final IWebdavStoreWorker worker,
                               final WebdavResources resources )
        throws WebdavException
    {

        String[] children = worker.getChildrenNames( transaction, path );
        children = children == null ? new String[] {} : children;
        StoredObject so = null;
        for ( int i = children.length - 1; i >= 0; i-- )
        {
            children[i] = "/" + children[i];
            try
            {
                so = worker.getStoredObject( transaction, path + children[i] );
                if ( so.isResource() )
                {
                    worker.removeObject( transaction, path + children[i] );

                }
                else
                {
                    deleteFolder( transaction, path + children[i], errorList, req, resp, worker, resources );

                    worker.removeObject( transaction, path + children[i] );

                }
            }
            catch ( final AccessDeniedException e )
            {
                errorList.put( path + children[i], SC_FORBIDDEN );
            }
            catch ( final ObjectNotFoundException e )
            {
                errorList.put( path + children[i], SC_NOT_FOUND );
            }
            catch ( final WebdavException e )
            {
                errorList.put( path + children[i], SC_INTERNAL_SERVER_ERROR );
            }
        }
        so = null;

    }

}

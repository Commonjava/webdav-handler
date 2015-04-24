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

import static net.sf.webdav.WebdavStatus.SC_CONFLICT;
import static net.sf.webdav.WebdavStatus.SC_FORBIDDEN;
import static net.sf.webdav.WebdavStatus.SC_INTERNAL_SERVER_ERROR;
import static net.sf.webdav.WebdavStatus.SC_LOCKED;
import static net.sf.webdav.WebdavStatus.SC_METHOD_NOT_ALLOWED;
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
import net.sf.webdav.util.RequestUtil;

public class DoCopy
    extends AbstractMethod
{

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( DoCopy.class );

    @Override
    public void execute( final ITransaction transaction, final WebdavRequest req, final WebdavResponse resp,
                         final IWebdavStoreWorker worker, final WebdavResources resources )
        throws IOException, LockFailedException
    {
        LOG.trace( "-- " + this.getClass()
                               .getName() );

        final String path = getRelativePath( req );
        if ( !resources.isReadOnly() )
        {

            final String tempLockOwner = "doCopy" + System.currentTimeMillis() + req.toString();
            if ( resources.getResourceLocks()
                          .lock( transaction, path, tempLockOwner, false, 0, TEMP_TIMEOUT, TEMPORARY ) )
            {
                try
                {
                    if ( !copyResource( transaction, req, resp, worker, resources ) )
                    {
                        return;
                    }
                }
                catch ( final AccessDeniedException e )
                {
                    resp.sendError( WebdavStatus.SC_FORBIDDEN );
                }
                catch ( final ObjectAlreadyExistsException e )
                {
                    resp.sendError( WebdavStatus.SC_CONFLICT, req.getRequestURI() );
                }
                catch ( final ObjectNotFoundException e )
                {
                    resp.sendError( WebdavStatus.SC_NOT_FOUND, req.getRequestURI() );
                }
                catch ( final WebdavException e )
                {
                    resp.sendError( WebdavStatus.SC_INTERNAL_SERVER_ERROR );
                }
                finally
                {
                    resources.getResourceLocks()
                             .unlockTemporaryLockedObjects( transaction, path, tempLockOwner );
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
     * Copy a resource.
     * 
     * @param transaction
     *      indicates that the method is within the scope of a WebDAV
     *      transaction
     * @param req
     *      Servlet request
     * @param resp
     *      Servlet response
     * @return true if the copy is successful
     * @throws WebdavException
     *      if an error in the underlying store occurs
     * @throws IOException
     *      when an error occurs while sending the response
     * @throws LockFailedException
     */
    public boolean copyResource( final ITransaction transaction, final WebdavRequest req, final WebdavResponse resp,
                                 final IWebdavStoreWorker worker, final WebdavResources resources )
        throws WebdavException, IOException, LockFailedException
    {

        // Parsing destination header
        final String destinationPath = parseDestinationHeader( req, resp );

        if ( destinationPath == null )
        {
            return false;
        }

        final String path = getRelativePath( req );

        if ( path.equals( destinationPath ) )
        {
            resp.sendError( WebdavStatus.SC_FORBIDDEN );
            return false;
        }

        Hashtable<String, WebdavStatus> errorList = new Hashtable<String, WebdavStatus>();
        final String parentDestinationPath = getParentPath( getCleanPath( destinationPath ) );

        final IResourceLocks _resourceLocks = resources.getResourceLocks();
        if ( !checkLocks( transaction, req, resp, _resourceLocks, parentDestinationPath ) )
        {
            errorList.put( parentDestinationPath, SC_LOCKED );
            sendReport( req, resp, errorList );
            return false; // parentDestination is locked
        }

        if ( !checkLocks( transaction, req, resp, _resourceLocks, destinationPath ) )
        {
            errorList.put( destinationPath, SC_LOCKED );
            sendReport( req, resp, errorList );
            return false; // destination is locked
        }

        // Parsing overwrite header

        boolean overwrite = true;
        final String overwriteHeader = req.getHeader( "Overwrite" );

        if ( overwriteHeader != null )
        {
            overwrite = overwriteHeader.equalsIgnoreCase( "T" );
        }

        // Overwriting the destination
        final String lockOwner = "copyResource" + System.currentTimeMillis() + req.toString();

        if ( _resourceLocks.lock( transaction, destinationPath, lockOwner, false, 0, TEMP_TIMEOUT, TEMPORARY ) )
        {
            StoredObject copySo, destinationSo = null;
            try
            {
                copySo = worker.getStoredObject( transaction, path );
                // Retrieve the resources
                if ( copySo == null )
                {
                    resp.sendError( SC_NOT_FOUND );
                    return false;
                }

                if ( copySo.isNullResource() )
                {
                    final String methodsAllowed = DeterminableMethod.determineMethodsAllowed( copySo );
                    resp.addHeader( "Allow", methodsAllowed );
                    resp.sendError( SC_METHOD_NOT_ALLOWED );
                    return false;
                }

                errorList = new Hashtable<String, WebdavStatus>();

                destinationSo = worker.getStoredObject( transaction, destinationPath );

                if ( overwrite )
                {

                    // Delete destination resource, if it exists
                    if ( destinationSo != null )
                    {
                        new DoDelete().deleteResource( transaction, destinationPath, errorList, req, resp, worker,
                                                       resources );

                    }
                    else
                    {
                        resp.setStatus( WebdavStatus.SC_CREATED );
                    }
                }
                else
                {

                    // If the destination exists, then it's a conflict
                    if ( destinationSo != null )
                    {
                        resp.sendError( WebdavStatus.SC_PRECONDITION_FAILED );
                        return false;
                    }
                    else
                    {
                        resp.setStatus( WebdavStatus.SC_CREATED );
                    }

                }
                copy( transaction, path, destinationPath, errorList, req, resp, worker, resources );

                if ( !errorList.isEmpty() )
                {
                    sendReport( req, resp, errorList );
                }

            }
            finally
            {
                _resourceLocks.unlockTemporaryLockedObjects( transaction, destinationPath, lockOwner );
            }
        }
        else
        {
            resp.sendError( SC_INTERNAL_SERVER_ERROR );
            return false;
        }
        return true;

    }

    /**
     * copies the specified resource(s) to the specified destination.
     * preconditions must be handled by the caller. Standard status codes must
     * be handled by the caller. a multi status report in case of errors is
     * created here.
     * 
     * @param transaction
     *      indicates that the method is within the scope of a WebDAV
     *      transaction
     * @param sourcePath
     *      path from where to read
     * @param destinationPath
     *      path where to write
     * @param req
     *      HttpServletRequest
     * @param resp
     *      HttpServletResponse
     * @throws WebdavException
     *      if an error in the underlying store occurs
     * @throws IOException
     */
    private void copy( final ITransaction transaction, final String sourcePath, final String destinationPath,
                       final Hashtable<String, WebdavStatus> errorList, final WebdavRequest req,
                       final WebdavResponse resp, final IWebdavStoreWorker worker, final WebdavResources resources )
        throws WebdavException, IOException
    {

        final StoredObject sourceSo = worker.getStoredObject( transaction, sourcePath );
        if ( sourceSo.isResource() )
        {
            worker.createResource( transaction, destinationPath );
            final long resourceLength =
                worker.setResourceContent( transaction, destinationPath,
                                           worker.getResourceContent( transaction, sourcePath ), null, null );

            if ( resourceLength != -1 )
            {
                final StoredObject destinationSo = worker.getStoredObject( transaction, destinationPath );
                destinationSo.setResourceLength( resourceLength );
            }

        }
        else
        {

            if ( sourceSo.isFolder() )
            {
                copyFolder( transaction, sourcePath, destinationPath, errorList, req, resp, worker, resources );
            }
            else
            {
                resp.sendError( WebdavStatus.SC_NOT_FOUND );
            }
        }
    }

    /**
     * helper method of copy() recursively copies the FOLDER at source path to
     * destination path
     * 
     * @param transaction
     *      indicates that the method is within the scope of a WebDAV
     *      transaction
     * @param sourcePath
     *      where to read
     * @param destinationPath
     *      where to write
     * @param errorList
     *      all errors that ocurred
     * @param req
     *      HttpServletRequest
     * @param resp
     *      HttpServletResponse
     * @throws WebdavException
     *      if an error in the underlying store occurs
     */
    private void copyFolder( final ITransaction transaction, final String srcPath, final String destPath,
                             final Hashtable<String, WebdavStatus> errorList, final WebdavRequest req,
                             final WebdavResponse resp, final IWebdavStoreWorker worker, final WebdavResources resources )
        throws WebdavException
    {
        final String sourcePath = getCleanPath( srcPath );
        final String destinationPath = getCleanPath( destPath );

        worker.createFolder( transaction, destinationPath );
        boolean infiniteDepth = true;
        final String depth = req.getHeader( "Depth" );
        if ( depth != null )
        {
            if ( depth.equals( "0" ) )
            {
                infiniteDepth = false;
            }
        }
        if ( infiniteDepth )
        {
            String[] children = worker.getChildrenNames( transaction, sourcePath );
            children = children == null ? new String[] {} : children;

            StoredObject childSo;
            for ( int i = children.length - 1; i >= 0; i-- )
            {
                children[i] = ensureLeadingSlash( children[i] );

                try
                {
                    childSo = worker.getStoredObject( transaction, ( sourcePath + children[i] ) );
                    if ( childSo.isResource() )
                    {
                        worker.createResource( transaction, destinationPath + children[i] );
                        final long resourceLength =
                            worker.setResourceContent( transaction,
                                                       destinationPath + children[i],
                                                       worker.getResourceContent( transaction, sourcePath + children[i] ),
                                                       null, null );

                        if ( resourceLength != -1 )
                        {
                            final StoredObject destinationSo =
                                worker.getStoredObject( transaction, destinationPath + children[i] );
                            destinationSo.setResourceLength( resourceLength );
                        }

                    }
                    else
                    {
                        copyFolder( transaction, sourcePath + children[i], destinationPath + children[i], errorList,
                                    req, resp, worker, resources );
                    }
                }
                catch ( final AccessDeniedException e )
                {
                    errorList.put( destinationPath + children[i], SC_FORBIDDEN );
                }
                catch ( final ObjectNotFoundException e )
                {
                    errorList.put( destinationPath + children[i], SC_NOT_FOUND );
                }
                catch ( final ObjectAlreadyExistsException e )
                {
                    errorList.put( destinationPath + children[i], SC_CONFLICT );
                }
                catch ( final WebdavException e )
                {
                    errorList.put( destinationPath + children[i], SC_INTERNAL_SERVER_ERROR );
                }
            }
        }
    }

    /**
     * Parses and normalizes the destination header.
     * 
     * @param req
     *      Servlet request
     * @param resp
     *      Servlet response
     * @return destinationPath
     * @throws IOException
     *      if an error occurs while sending response
     */
    private String parseDestinationHeader( final WebdavRequest req, final WebdavResponse resp )
        throws IOException
    {
        String destinationPath = req.getHeader( "Destination" );

        if ( destinationPath == null )
        {
            resp.sendError( WebdavStatus.SC_BAD_REQUEST );
            return null;
        }

        // Remove url encoding from destination
        destinationPath = RequestUtil.URLDecode( destinationPath, "UTF8" );

        final int protocolIndex = destinationPath.indexOf( "://" );
        if ( protocolIndex >= 0 )
        {
            // if the Destination URL contains the protocol, we can safely
            // trim everything upto the first "/" character after "://"
            final int firstSeparator = destinationPath.indexOf( "/", protocolIndex + 4 );
            if ( firstSeparator < 0 )
            {
                destinationPath = "/";
            }
            else
            {
                destinationPath = destinationPath.substring( firstSeparator );
            }
        }
        else
        {
            final String hostName = req.getServerName();
            if ( ( hostName != null ) && ( destinationPath.startsWith( hostName ) ) )
            {
                destinationPath = destinationPath.substring( hostName.length() );
            }

            final int portIndex = destinationPath.indexOf( ":" );
            if ( portIndex >= 0 )
            {
                destinationPath = destinationPath.substring( portIndex );
            }

            if ( destinationPath.startsWith( ":" ) )
            {
                final int firstSeparator = destinationPath.indexOf( "/" );
                if ( firstSeparator < 0 )
                {
                    destinationPath = "/";
                }
                else
                {
                    destinationPath = destinationPath.substring( firstSeparator );
                }
            }
        }

        // Normalize destination path (remove '.' and' ..')
        destinationPath = normalize( destinationPath );

        final String contextPath = req.getContextPath();
        if ( ( contextPath != null ) && ( destinationPath.startsWith( contextPath ) ) )
        {
            destinationPath = destinationPath.substring( contextPath.length() );
        }

        final String pathInfo = req.getPathInfo();
        if ( pathInfo != null )
        {
            final String servletPath = req.getServicePath();
            if ( ( servletPath != null ) && ( destinationPath.startsWith( servletPath ) ) )
            {
                destinationPath = destinationPath.substring( servletPath.length() );
            }
        }

        return destinationPath;
    }

    /**
     * Return a context-relative path, beginning with a "/", that represents the
     * canonical version of the specified path after ".." and "." elements are
     * resolved out. If the specified path attempts to go outside the boundaries
     * of the current context (i.e. too many ".." path elements are present),
     * return <code>null</code> instead.
     * 
     * @param path
     *      Path to be normalized
     * @return normalized path
     */
    protected String normalize( final String path )
    {

        if ( path == null )
        {
            return null;
        }

        // Create a place for the normalized path
        String normalized = path;

        if ( normalized.equals( "/." ) )
        {
            return "/";
        }

        // Normalize the slashes and add leading slash if necessary
        if ( normalized.indexOf( '\\' ) >= 0 )
        {
            normalized = normalized.replace( '\\', '/' );
        }
        if ( !normalized.startsWith( "/" ) )
        {
            normalized = "/" + normalized;
        }

        // Resolve occurrences of "//" in the normalized path
        while ( true )
        {
            final int index = normalized.indexOf( "//" );
            if ( index < 0 )
            {
                break;
            }
            normalized = normalized.substring( 0, index ) + normalized.substring( index + 1 );
        }

        // Resolve occurrences of "/./" in the normalized path
        while ( true )
        {
            final int index = normalized.indexOf( "/./" );
            if ( index < 0 )
            {
                break;
            }
            normalized = normalized.substring( 0, index ) + normalized.substring( index + 2 );
        }

        // Resolve occurrences of "/../" in the normalized path
        while ( true )
        {
            final int index = normalized.indexOf( "/../" );
            if ( index < 0 )
            {
                break;
            }
            if ( index == 0 )
            {
                return ( null ); // Trying to go outside our context
            }
            final int index2 = normalized.lastIndexOf( '/', index - 1 );
            normalized = normalized.substring( 0, index2 ) + normalized.substring( index + 3 );
        }

        // Return the normalized path that we have completed
        return ( normalized );

    }

}

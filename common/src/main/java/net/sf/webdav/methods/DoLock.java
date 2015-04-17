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

import static net.sf.webdav.WebdavStatus.SC_BAD_REQUEST;
import static net.sf.webdav.WebdavStatus.SC_CREATED;
import static net.sf.webdav.WebdavStatus.SC_FORBIDDEN;
import static net.sf.webdav.WebdavStatus.SC_INTERNAL_SERVER_ERROR;
import static net.sf.webdav.WebdavStatus.SC_LOCKED;
import static net.sf.webdav.WebdavStatus.SC_NO_CONTENT;
import static net.sf.webdav.WebdavStatus.SC_OK;
import static net.sf.webdav.WebdavStatus.SC_PRECONDITION_FAILED;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;

import net.sf.webdav.StoredObject;
import net.sf.webdav.WebdavStatus;
import net.sf.webdav.exceptions.LockFailedException;
import net.sf.webdav.exceptions.WebdavException;
import net.sf.webdav.locking.IResourceLocks;
import net.sf.webdav.locking.LockedObject;
import net.sf.webdav.spi.ITransaction;
import net.sf.webdav.spi.IWebdavStore;
import net.sf.webdav.spi.WebdavRequest;
import net.sf.webdav.spi.WebdavResponse;
import net.sf.webdav.util.XMLWriter;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DoLock
    extends AbstractMethod
{

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( DoLock.class );

    private final IWebdavStore _store;

    private final IResourceLocks _resourceLocks;

    private final boolean _readOnly;

    private boolean _macLockRequest = false;

    private boolean _exclusive = false;

    private String _type = null;

    private String _lockOwner = null;

    private String _path = null;

    private String _parentPath = null;

    private String _userAgent = null;

    public DoLock( final IWebdavStore store, final IResourceLocks resourceLocks, final boolean readOnly )
    {
        _store = store;
        _resourceLocks = resourceLocks;
        _readOnly = readOnly;
    }

    @Override
    public void execute( final ITransaction transaction, final WebdavRequest req, final WebdavResponse resp )
        throws IOException, WebdavException
    {
        LOG.trace( "-- " + this.getClass()
                               .getName() );

        if ( _readOnly )
        {
            resp.sendError( SC_FORBIDDEN );
            return;
        }
        else
        {
            _path = getRelativePath( req );
            _parentPath = getParentPath( getCleanPath( _path ) );

            final Hashtable<String, WebdavStatus> errorList = new Hashtable<String, WebdavStatus>();

            if ( !checkLocks( transaction, req, resp, _resourceLocks, _path ) )
            {
                errorList.put( _path, SC_LOCKED );
                sendReport( req, resp, errorList );
                return; // resource is locked
            }

            if ( !checkLocks( transaction, req, resp, _resourceLocks, _parentPath ) )
            {
                errorList.put( _parentPath, SC_LOCKED );
                sendReport( req, resp, errorList );
                return; // parent is locked
            }

            // Mac OS Finder (whether 10.4.x or 10.5) can't store files
            // because executing a LOCK without lock information causes a
            // SC_BAD_REQUEST
            _userAgent = req.getHeader( "User-Agent" );
            if ( _userAgent != null && _userAgent.indexOf( "Darwin" ) != -1 )
            {
                _macLockRequest = true;

                final String timeString = new Long( System.currentTimeMillis() ).toString();
                _lockOwner = _userAgent.concat( timeString );
            }

            final String tempLockOwner = "doLock" + System.currentTimeMillis() + req.toString();
            if ( _resourceLocks.lock( transaction, _path, tempLockOwner, false, 0, TEMP_TIMEOUT, TEMPORARY ) )
            {
                try
                {
                    if ( req.getHeader( "If" ) != null )
                    {
                        doRefreshLock( transaction, req, resp );
                    }
                    else
                    {
                        doLock( transaction, req, resp );
                    }
                }
                catch ( final LockFailedException e )
                {
                    resp.sendError( SC_LOCKED );
                    e.printStackTrace();
                }
                finally
                {
                    _resourceLocks.unlockTemporaryLockedObjects( transaction, _path, tempLockOwner );
                }
            }
        }
    }

    private void doLock( final ITransaction transaction, final WebdavRequest req, final WebdavResponse resp )
        throws IOException, WebdavException
    {

        StoredObject so = _store.getStoredObject( transaction, _path );

        if ( so != null )
        {
            doLocking( transaction, req, resp );
        }
        else
        {
            // resource doesn't exist, null-resource lock
            doNullResourceLock( transaction, req, resp );
        }

        so = null;
        _exclusive = false;
        _type = null;
        _lockOwner = null;

    }

    private void doLocking( final ITransaction transaction, final WebdavRequest req, final WebdavResponse resp )
        throws IOException
    {

        // Tests if LockObject on requested path exists, and if so, tests
        // exclusivity
        LockedObject lo = _resourceLocks.getLockedObjectByPath( transaction, _path );
        if ( lo != null )
        {
            if ( lo.isExclusive() )
            {
                sendLockFailError( transaction, req, resp );
                return;
            }
        }
        try
        {
            // Thats the locking itself
            executeLock( transaction, req, resp );

        }
        catch ( final LockFailedException e )
        {
            sendLockFailError( transaction, req, resp );
        }
        catch ( final WebdavException e )
        {
            resp.sendError( SC_INTERNAL_SERVER_ERROR );
            LOG.trace( e.toString() );
        }
        finally
        {
            lo = null;
        }

    }

    private void doNullResourceLock( final ITransaction transaction, final WebdavRequest req, final WebdavResponse resp )
        throws IOException
    {

        StoredObject parentSo, nullSo = null;

        try
        {
            parentSo = _store.getStoredObject( transaction, _parentPath );
            if ( _parentPath != null && parentSo == null )
            {
                _store.createFolder( transaction, _parentPath );
            }
            else if ( _parentPath != null && parentSo != null && parentSo.isResource() )
            {
                resp.sendError( SC_PRECONDITION_FAILED );
                return;
            }

            nullSo = _store.getStoredObject( transaction, _path );
            if ( nullSo == null )
            {
                // resource doesn't exist
                _store.createResource( transaction, _path );

                // Transmit expects 204 response-code, not 201
                if ( _userAgent != null && _userAgent.indexOf( "Transmit" ) != -1 )
                {
                    LOG.trace( "DoLock.execute() : do workaround for user agent '" + _userAgent + "'" );
                    resp.setStatus( SC_NO_CONTENT );
                }
                else
                {
                    resp.setStatus( SC_CREATED );
                }

            }
            else
            {
                // resource already exists, could not execute null-resource lock
                sendLockFailError( transaction, req, resp );
                return;
            }
            nullSo = _store.getStoredObject( transaction, _path );
            // define the newly created resource as null-resource
            nullSo.setNullResource( true );

            // Thats the locking itself
            executeLock( transaction, req, resp );

        }
        catch ( final LockFailedException e )
        {
            sendLockFailError( transaction, req, resp );
        }
        catch ( final WebdavException e )
        {
            resp.sendError( SC_INTERNAL_SERVER_ERROR );
            e.printStackTrace();
        }
        finally
        {
            parentSo = null;
            nullSo = null;
        }
    }

    private void doRefreshLock( final ITransaction transaction, final WebdavRequest req, final WebdavResponse resp )
        throws IOException, LockFailedException
    {

        final String[] lockTokens = getLockIdFromIfHeader( req );
        String lockToken = null;
        if ( lockTokens != null )
        {
            lockToken = lockTokens[0];
        }

        if ( lockToken != null )
        {
            // Getting LockObject of specified lockToken in If header
            LockedObject refreshLo = _resourceLocks.getLockedObjectByID( transaction, lockToken );
            if ( refreshLo != null )
            {
                final int timeout = getTimeout( transaction, req );

                refreshLo.refreshTimeout( timeout );
                // sending success response
                generateXMLReport( transaction, resp, refreshLo );

                refreshLo = null;
            }
            else
            {
                // no LockObject to given lockToken
                resp.sendError( SC_PRECONDITION_FAILED );
            }

        }
        else
        {
            resp.sendError( SC_PRECONDITION_FAILED );
        }
    }

    // ------------------------------------------------- helper methods

    /**
     * Executes the LOCK
     */
    private void executeLock( final ITransaction transaction, final WebdavRequest req, final WebdavResponse resp )
        throws LockFailedException, IOException, WebdavException
    {

        // Mac OS lock request workaround
        if ( _macLockRequest )
        {
            LOG.trace( "DoLock.execute() : do workaround for user agent '" + _userAgent + "'" );

            doMacLockRequestWorkaround( transaction, req, resp );
        }
        else
        {
            // Getting LockInformation from request
            if ( getLockInformation( transaction, req, resp ) )
            {
                final int depth = getDepth( req );
                final int lockDuration = getTimeout( transaction, req );

                boolean lockSuccess = false;
                if ( _exclusive )
                {
                    lockSuccess = _resourceLocks.exclusiveLock( transaction, _path, _lockOwner, depth, lockDuration );
                }
                else
                {
                    lockSuccess = _resourceLocks.sharedLock( transaction, _path, _lockOwner, depth, lockDuration );
                }

                if ( lockSuccess )
                {
                    // Locks successfully placed - return information about
                    final LockedObject lo = _resourceLocks.getLockedObjectByPath( transaction, _path );
                    if ( lo != null )
                    {
                        generateXMLReport( transaction, resp, lo );
                    }
                    else
                    {
                        resp.sendError( SC_INTERNAL_SERVER_ERROR );
                    }
                }
                else
                {
                    sendLockFailError( transaction, req, resp );

                    throw new LockFailedException();
                }
            }
            else
            {
                // information for LOCK could not be read successfully
                resp.setContentType( "text/xml; charset=UTF-8" );
                resp.sendError( SC_BAD_REQUEST );
            }
        }
    }

    /**
     * Tries to get the LockInformation from LOCK request
     */
    private boolean getLockInformation( final ITransaction transaction, final WebdavRequest req, final WebdavResponse resp )
        throws WebdavException, IOException
    {

        Node lockInfoNode = null;
        DocumentBuilder documentBuilder = null;

        documentBuilder = getDocumentBuilder();
        try
        {
            final Document document = documentBuilder.parse( new InputSource( req.getInputStream() ) );

            // Get the root element of the document
            final Element rootElement = document.getDocumentElement();

            lockInfoNode = rootElement;

            if ( lockInfoNode != null )
            {
                NodeList childList = lockInfoNode.getChildNodes();
                Node lockScopeNode = null;
                Node lockTypeNode = null;
                Node lockOwnerNode = null;

                Node currentNode = null;
                String nodeName = null;

                for ( int i = 0; i < childList.getLength(); i++ )
                {
                    currentNode = childList.item( i );

                    if ( currentNode.getNodeType() == Node.ELEMENT_NODE || currentNode.getNodeType() == Node.TEXT_NODE )
                    {

                        nodeName = currentNode.getNodeName();

                        if ( nodeName.endsWith( "locktype" ) )
                        {
                            lockTypeNode = currentNode;
                        }
                        if ( nodeName.endsWith( "lockscope" ) )
                        {
                            lockScopeNode = currentNode;
                        }
                        if ( nodeName.endsWith( "owner" ) )
                        {
                            lockOwnerNode = currentNode;
                        }
                    }
                    else
                    {
                        return false;
                    }
                }

                if ( lockScopeNode != null )
                {
                    String scope = null;
                    childList = lockScopeNode.getChildNodes();
                    for ( int i = 0; i < childList.getLength(); i++ )
                    {
                        currentNode = childList.item( i );

                        if ( currentNode.getNodeType() == Node.ELEMENT_NODE )
                        {
                            scope = currentNode.getNodeName();

                            if ( scope.endsWith( "exclusive" ) )
                            {
                                _exclusive = true;
                            }
                            else if ( scope.equals( "shared" ) )
                            {
                                _exclusive = false;
                            }
                        }
                    }
                    if ( scope == null )
                    {
                        return false;
                    }

                }
                else
                {
                    return false;
                }

                if ( lockTypeNode != null )
                {
                    childList = lockTypeNode.getChildNodes();
                    for ( int i = 0; i < childList.getLength(); i++ )
                    {
                        currentNode = childList.item( i );

                        if ( currentNode.getNodeType() == Node.ELEMENT_NODE )
                        {
                            _type = currentNode.getNodeName();

                            if ( _type.endsWith( "write" ) )
                            {
                                _type = "write";
                            }
                            else if ( _type.equals( "read" ) )
                            {
                                _type = "read";
                            }
                        }
                    }
                    if ( _type == null )
                    {
                        return false;
                    }
                }
                else
                {
                    return false;
                }

                if ( lockOwnerNode != null )
                {
                    childList = lockOwnerNode.getChildNodes();
                    for ( int i = 0; i < childList.getLength(); i++ )
                    {
                        currentNode = childList.item( i );

                        if ( currentNode.getNodeType() == Node.ELEMENT_NODE || currentNode.getNodeType() == Node.TEXT_NODE )
                        {
                            _lockOwner = currentNode.getTextContent();
                        }
                    }
                }
                if ( _lockOwner == null )
                {
                    return false;
                }
            }
            else
            {
                return false;
            }

        }
        catch ( final DOMException e )
        {
            resp.sendError( SC_INTERNAL_SERVER_ERROR );
            e.printStackTrace();
            return false;
        }
        catch ( final SAXException e )
        {
            resp.sendError( SC_INTERNAL_SERVER_ERROR );
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Ties to read the timeout from request
     */
    private int getTimeout( final ITransaction transaction, final WebdavRequest req )
    {

        int lockDuration = DEFAULT_TIMEOUT;
        String lockDurationStr = req.getHeader( "Timeout" );

        if ( lockDurationStr == null )
        {
            lockDuration = DEFAULT_TIMEOUT;
        }
        else
        {
            final int commaPos = lockDurationStr.indexOf( ',' );
            // if multiple timeouts, just use the first one
            if ( commaPos != -1 )
            {
                lockDurationStr = lockDurationStr.substring( 0, commaPos );
            }
            if ( lockDurationStr.startsWith( "Second-" ) )
            {
                lockDuration = new Integer( lockDurationStr.substring( 7 ) ).intValue();
            }
            else
            {
                if ( lockDurationStr.equalsIgnoreCase( "infinity" ) )
                {
                    lockDuration = MAX_TIMEOUT;
                }
                else
                {
                    try
                    {
                        lockDuration = new Integer( lockDurationStr ).intValue();
                    }
                    catch ( final NumberFormatException e )
                    {
                        lockDuration = MAX_TIMEOUT;
                    }
                }
            }
            if ( lockDuration <= 0 )
            {
                lockDuration = DEFAULT_TIMEOUT;
            }
            if ( lockDuration > MAX_TIMEOUT )
            {
                lockDuration = MAX_TIMEOUT;
            }
        }
        return lockDuration;
    }

    /**
     * Generates the response XML with all lock information
     */
    private void generateXMLReport( final ITransaction transaction, final WebdavResponse resp, final LockedObject lo )
        throws IOException
    {

        final HashMap<String, String> namespaces = new HashMap<String, String>();
        namespaces.put( "DAV:", "D" );

        resp.setStatus( SC_OK );
        resp.setContentType( "text/xml; charset=UTF-8" );

        final XMLWriter generatedXML = new XMLWriter( resp.getWriter(), namespaces );
        generatedXML.writeXMLHeader();
        generatedXML.writeElement( "DAV::prop", XMLWriter.OPENING );
        generatedXML.writeElement( "DAV::lockdiscovery", XMLWriter.OPENING );
        generatedXML.writeElement( "DAV::activelock", XMLWriter.OPENING );

        generatedXML.writeElement( "DAV::locktype", XMLWriter.OPENING );
        generatedXML.writeProperty( "DAV::" + _type );
        generatedXML.writeElement( "DAV::locktype", XMLWriter.CLOSING );

        generatedXML.writeElement( "DAV::lockscope", XMLWriter.OPENING );
        if ( _exclusive )
        {
            generatedXML.writeProperty( "DAV::exclusive" );
        }
        else
        {
            generatedXML.writeProperty( "DAV::shared" );
        }
        generatedXML.writeElement( "DAV::lockscope", XMLWriter.CLOSING );

        final int depth = lo.getLockDepth();

        generatedXML.writeElement( "DAV::depth", XMLWriter.OPENING );
        if ( depth == INFINITY )
        {
            generatedXML.writeText( "Infinity" );
        }
        else
        {
            generatedXML.writeText( String.valueOf( depth ) );
        }
        generatedXML.writeElement( "DAV::depth", XMLWriter.CLOSING );

        generatedXML.writeElement( "DAV::owner", XMLWriter.OPENING );
        generatedXML.writeElement( "DAV::href", XMLWriter.OPENING );
        generatedXML.writeText( _lockOwner );
        generatedXML.writeElement( "DAV::href", XMLWriter.CLOSING );
        generatedXML.writeElement( "DAV::owner", XMLWriter.CLOSING );

        final long timeout = lo.getTimeoutMillis();
        generatedXML.writeElement( "DAV::timeout", XMLWriter.OPENING );
        generatedXML.writeText( "Second-" + timeout / 1000 );
        generatedXML.writeElement( "DAV::timeout", XMLWriter.CLOSING );

        final String lockToken = lo.getID();
        generatedXML.writeElement( "DAV::locktoken", XMLWriter.OPENING );
        generatedXML.writeElement( "DAV::href", XMLWriter.OPENING );
        generatedXML.writeText( "opaquelocktoken:" + lockToken );
        generatedXML.writeElement( "DAV::href", XMLWriter.CLOSING );
        generatedXML.writeElement( "DAV::locktoken", XMLWriter.CLOSING );

        generatedXML.writeElement( "DAV::activelock", XMLWriter.CLOSING );
        generatedXML.writeElement( "DAV::lockdiscovery", XMLWriter.CLOSING );
        generatedXML.writeElement( "DAV::prop", XMLWriter.CLOSING );

        resp.addHeader( "Lock-Token", "<opaquelocktoken:" + lockToken + ">" );

        generatedXML.sendData();

    }

    /**
     * Executes the lock for a Mac OS Finder client
     */
    private void doMacLockRequestWorkaround( final ITransaction transaction, final WebdavRequest req, final WebdavResponse resp )
        throws LockFailedException, IOException
    {
        LockedObject lo;
        final int depth = getDepth( req );
        int lockDuration = getTimeout( transaction, req );
        if ( lockDuration < 0 || lockDuration > MAX_TIMEOUT )
        {
            lockDuration = DEFAULT_TIMEOUT;
        }

        boolean lockSuccess = false;
        lockSuccess = _resourceLocks.exclusiveLock( transaction, _path, _lockOwner, depth, lockDuration );

        if ( lockSuccess )
        {
            // Locks successfully placed - return information about
            lo = _resourceLocks.getLockedObjectByPath( transaction, _path );
            if ( lo != null )
            {
                generateXMLReport( transaction, resp, lo );
            }
            else
            {
                resp.sendError( SC_INTERNAL_SERVER_ERROR );
            }
        }
        else
        {
            // Locking was not successful
            sendLockFailError( transaction, req, resp );
        }
    }

    /**
     * Sends an error report to the client
     */
    private void sendLockFailError( final ITransaction transaction, final WebdavRequest req, final WebdavResponse resp )
        throws IOException
    {
        final Hashtable<String, WebdavStatus> errorList = new Hashtable<String, WebdavStatus>();
        errorList.put( _path, SC_LOCKED );
        sendReport( req, resp, errorList );
    }

}

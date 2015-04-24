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
import static net.sf.webdav.WebdavStatus.SC_METHOD_NOT_ALLOWED;
import static net.sf.webdav.WebdavStatus.SC_MULTI_STATUS;
import static net.sf.webdav.WebdavStatus.SC_NOT_FOUND;
import static net.sf.webdav.WebdavStatus.SC_OK;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;

import net.sf.webdav.StoredObject;
import net.sf.webdav.WebdavResources;
import net.sf.webdav.WebdavStatus;
import net.sf.webdav.exceptions.AccessDeniedException;
import net.sf.webdav.exceptions.LockFailedException;
import net.sf.webdav.exceptions.WebdavException;
import net.sf.webdav.locking.IResourceLocks;
import net.sf.webdav.locking.LockedObject;
import net.sf.webdav.spi.ITransaction;
import net.sf.webdav.spi.IWebdavStoreWorker;
import net.sf.webdav.spi.WebdavRequest;
import net.sf.webdav.spi.WebdavResponse;
import net.sf.webdav.util.XMLHelper;
import net.sf.webdav.util.XMLWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class DoProppatch
    extends AbstractMethod
{

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( DoProppatch.class );

    @Override
    public void execute( final ITransaction transaction, final WebdavRequest req, final WebdavResponse resp,
                         final IWebdavStoreWorker worker, final WebdavResources resources )
        throws IOException, LockFailedException
    {
        LOG.trace( "-- " + this.getClass()
                               .getName() );

        if ( resources.isReadOnly() )
        {
            resp.sendError( SC_FORBIDDEN );
            return;
        }

        String path = getRelativePath( req );
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

        // TODO for now, PROPPATCH just sends a valid response, stating that
        // everything is fine, but doesn't do anything.

        // Retrieve the resources
        final String tempLockOwner = "doProppatch" + System.currentTimeMillis() + req.toString();

        if ( _resourceLocks.lock( transaction, path, tempLockOwner, false, 0, TEMP_TIMEOUT, TEMPORARY ) )
        {
            StoredObject so = null;
            LockedObject lo = null;
            try
            {
                so = worker.getStoredObject( transaction, path );
                lo = _resourceLocks.getLockedObjectByPath( transaction, getCleanPath( path ) );

                if ( so == null )
                {
                    resp.sendError( SC_NOT_FOUND );
                    return;
                    // we do not to continue since there is no root
                    // resource
                }

                if ( so.isNullResource() )
                {
                    final String methodsAllowed = DeterminableMethod.determineMethodsAllowed( so );
                    resp.addHeader( "Allow", methodsAllowed );
                    resp.sendError( SC_METHOD_NOT_ALLOWED );
                    return;
                }

                if ( lo != null && lo.isExclusive() )
                {
                    // Object on specified path is LOCKED
                    errorList = new Hashtable<String, WebdavStatus>();
                    errorList.put( path, SC_LOCKED );
                    sendReport( req, resp, errorList );
                    return;
                }

                List<String> toset = null;
                List<String> toremove = null;
                final List<String> tochange = new Vector<String>();
                // contains all properties from
                // toset and toremove

                path = getCleanPath( getRelativePath( req ) );

                Node tosetNode = null;
                Node toremoveNode = null;

                if ( req.getContentLength() != 0 )
                {
                    final DocumentBuilder documentBuilder = getDocumentBuilder();
                    try
                    {
                        final Document document = documentBuilder.parse( new InputSource( req.getInputStream() ) );
                        // Get the root element of the document
                        final Element rootElement = document.getDocumentElement();

                        tosetNode = XMLHelper.findSubElement( XMLHelper.findSubElement( rootElement, "set" ), "prop" );
                        toremoveNode = XMLHelper.findSubElement( XMLHelper.findSubElement( rootElement, "remove" ), "prop" );
                    }
                    catch ( final Exception e )
                    {
                        resp.sendError( SC_INTERNAL_SERVER_ERROR );
                        return;
                    }
                }
                else
                {
                    // no content: error
                    resp.sendError( SC_INTERNAL_SERVER_ERROR );
                    return;
                }

                final HashMap<String, String> namespaces = new HashMap<String, String>();
                namespaces.put( "DAV:", "D" );

                if ( tosetNode != null )
                {
                    toset = XMLHelper.getPropertiesFromXML( tosetNode );
                    tochange.addAll( toset );
                }

                if ( toremoveNode != null )
                {
                    toremove = XMLHelper.getPropertiesFromXML( toremoveNode );
                    tochange.addAll( toremove );
                }

                resp.setStatus( SC_MULTI_STATUS );
                resp.setContentType( "text/xml; charset=UTF-8" );

                // Create multistatus object
                final XMLWriter generatedXML = new XMLWriter( resp.getWriter(), namespaces );
                generatedXML.writeXMLHeader();
                generatedXML.writeElement( "DAV::multistatus", XMLWriter.OPENING );

                generatedXML.writeElement( "DAV::response", XMLWriter.OPENING );
                final String status = new String( "HTTP/1.1 " + SC_OK + " " + SC_OK.message() );

                // Generating href element
                generatedXML.writeElement( "DAV::href", XMLWriter.OPENING );

                String href = req.getContextPath();
                if ( ( href.endsWith( "/" ) ) && ( path.startsWith( "/" ) ) )
                {
                    href += path.substring( 1 );
                }
                else
                {
                    href += path;
                }
                if ( ( so.isFolder() ) && ( !href.endsWith( "/" ) ) )
                {
                    href += "/";
                }

                generatedXML.writeText( rewriteUrl( href ) );

                generatedXML.writeElement( "DAV::href", XMLWriter.CLOSING );

                for ( final String string : tochange )
                {
                    final String property = string;

                    generatedXML.writeElement( "DAV::propstat", XMLWriter.OPENING );

                    generatedXML.writeElement( "DAV::prop", XMLWriter.OPENING );
                    generatedXML.writeElement( property, XMLWriter.NO_CONTENT );
                    generatedXML.writeElement( "DAV::prop", XMLWriter.CLOSING );

                    generatedXML.writeElement( "DAV::status", XMLWriter.OPENING );
                    generatedXML.writeText( status );
                    generatedXML.writeElement( "DAV::status", XMLWriter.CLOSING );

                    generatedXML.writeElement( "DAV::propstat", XMLWriter.CLOSING );
                }

                generatedXML.writeElement( "DAV::response", XMLWriter.CLOSING );

                generatedXML.writeElement( "DAV::multistatus", XMLWriter.CLOSING );

                generatedXML.sendData();
            }
            catch ( final AccessDeniedException e )
            {
                resp.sendError( SC_FORBIDDEN );
            }
            catch ( final WebdavException e )
            {
                resp.sendError( SC_INTERNAL_SERVER_ERROR );
            }
            finally
            {
                _resourceLocks.unlockTemporaryLockedObjects( transaction, path, tempLockOwner );
            }
        }
        else
        {
            resp.sendError( SC_INTERNAL_SERVER_ERROR );
        }
    }
}

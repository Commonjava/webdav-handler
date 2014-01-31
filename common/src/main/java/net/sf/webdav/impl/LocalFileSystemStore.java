/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package net.sf.webdav.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.enterprise.inject.Alternative;
import javax.inject.Named;

import net.sf.webdav.StoredObject;
import net.sf.webdav.exceptions.WebdavException;
import net.sf.webdav.spi.ITransaction;
import net.sf.webdav.spi.IWebdavStore;

/**
 * Reference Implementation of WebdavStore
 * 
 * @author joa
 * @author re
 */
@Alternative
@Named
public class LocalFileSystemStore
    implements IWebdavStore
{

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( LocalFileSystemStore.class );

    private static int BUF_SIZE = 65536;

    private File _root = null;

    public LocalFileSystemStore( final File root )
    {
        _root = root;
    }

    @Override
    public ITransaction begin( final Principal principal )
        throws WebdavException
    {
        LOG.trace( "LocalFileSystemStore.begin()" );
        if ( !_root.exists() )
        {
            if ( !_root.mkdirs() )
            {
                throw new WebdavException( "root path: " + _root.getAbsolutePath() + " does not exist and could not be created" );
            }
        }
        return null;
    }

    @Override
    public void checkAuthentication( final ITransaction transaction )
        throws SecurityException
    {
        LOG.trace( "LocalFileSystemStore.checkAuthentication()" );
        // do nothing

    }

    @Override
    public void commit( final ITransaction transaction )
        throws WebdavException
    {
        // do nothing
        LOG.trace( "LocalFileSystemStore.commit()" );
    }

    @Override
    public void rollback( final ITransaction transaction )
        throws WebdavException
    {
        // do nothing
        LOG.trace( "LocalFileSystemStore.rollback()" );

    }

    @Override
    public void createFolder( final ITransaction transaction, final String uri )
        throws WebdavException
    {
        LOG.trace( "LocalFileSystemStore.createFolder(" + uri + ")" );
        final File file = new File( _root, uri );
        if ( !file.mkdir() )
        {
            throw new WebdavException( "cannot create folder: " + uri );
        }
    }

    @Override
    public void createResource( final ITransaction transaction, final String uri )
        throws WebdavException
    {
        LOG.trace( "LocalFileSystemStore.createResource(" + uri + ")" );
        final File file = new File( _root, uri );
        try
        {
            if ( !file.createNewFile() )
            {
                throw new WebdavException( "cannot create file: " + uri );
            }
        }
        catch ( final IOException e )
        {
            LOG.error( "LocalFileSystemStore.createResource(" + uri + ") failed" );
            throw new WebdavException( "Failed to create new file: %s. Reason: %s", e, file, e.getMessage() );
        }
    }

    @Override
    public long setResourceContent( final ITransaction transaction, final String uri, final InputStream is, final String contentType,
                                    final String characterEncoding )
        throws WebdavException
    {

        LOG.trace( "LocalFileSystemStore.setResourceContent(" + uri + ")" );
        final File file = new File( _root, uri );
        try
        {
            final OutputStream os = new BufferedOutputStream( new FileOutputStream( file ), BUF_SIZE );
            try
            {
                int read;
                final byte[] copyBuffer = new byte[BUF_SIZE];

                while ( ( read = is.read( copyBuffer, 0, copyBuffer.length ) ) != -1 )
                {
                    os.write( copyBuffer, 0, read );
                }
            }
            finally
            {
                try
                {
                    is.close();
                }
                finally
                {
                    os.close();
                }
            }
        }
        catch ( final IOException e )
        {
            LOG.error( "LocalFileSystemStore.setResourceContent(" + uri + ") failed" );
            throw new WebdavException( "Failed to write file: %s. Reason: %s", e, file, e.getMessage() );
        }
        long length = -1;

        try
        {
            length = file.length();
        }
        catch ( final SecurityException e )
        {
            LOG.error( "LocalFileSystemStore.setResourceContent(" + uri + ") failed" + "\nCan't get file.length" );
        }

        return length;
    }

    @Override
    public String[] getChildrenNames( final ITransaction transaction, final String uri )
        throws WebdavException
    {
        LOG.trace( "LocalFileSystemStore.getChildrenNames(" + uri + ")" );
        final File file = new File( _root, uri );
        String[] childrenNames = null;
        if ( file.isDirectory() )
        {
            final File[] children = file.listFiles();
            final List<String> childList = new ArrayList<String>();
            String name = null;
            for ( int i = 0; i < children.length; i++ )
            {
                name = children[i].getName();
                childList.add( name );
                LOG.trace( "Child " + i + ": " + name );
            }
            childrenNames = new String[childList.size()];
            childrenNames = childList.toArray( childrenNames );
        }
        return childrenNames;
    }

    @Override
    public void removeObject( final ITransaction transaction, final String uri )
        throws WebdavException
    {
        final File file = new File( _root, uri );
        final boolean success = file.delete();
        LOG.trace( "LocalFileSystemStore.removeObject(" + uri + ")=" + success );
        if ( !success )
        {
            throw new WebdavException( "cannot delete object: " + uri );
        }

    }

    @Override
    public InputStream getResourceContent( final ITransaction transaction, final String uri )
        throws WebdavException
    {
        LOG.trace( "LocalFileSystemStore.getResourceContent(" + uri + ")" );
        final File file = new File( _root, uri );

        InputStream in;
        try
        {
            in = new BufferedInputStream( new FileInputStream( file ) );
        }
        catch ( final IOException e )
        {
            LOG.error( "LocalFileSystemStore.getResourceContent(" + uri + ") failed" );
            throw new WebdavException( "Failed to read file: %s. Reason: %s", e, file, e.getMessage() );
        }
        return in;
    }

    @Override
    public long getResourceLength( final ITransaction transaction, final String uri )
        throws WebdavException
    {
        LOG.trace( "LocalFileSystemStore.getResourceLength(" + uri + ")" );
        final File file = new File( _root, uri );
        return file.length();
    }

    @Override
    public StoredObject getStoredObject( final ITransaction transaction, final String uri )
        throws WebdavException
    {

        StoredObject so = null;

        final File file = new File( _root, uri );
        if ( file.exists() )
        {
            so = new StoredObject();
            so.setFolder( file.isDirectory() );
            so.setLastModified( new Date( file.lastModified() ) );
            so.setCreationDate( new Date( file.lastModified() ) );
            so.setResourceLength( getResourceLength( transaction, uri ) );
        }

        return so;
    }

}

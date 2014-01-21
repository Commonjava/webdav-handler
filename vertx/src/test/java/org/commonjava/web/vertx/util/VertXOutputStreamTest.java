/*******************************************************************************
 * Copyright (C) 2014 John Casey.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.commonjava.web.vertx.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import net.sf.webdav.util.RequestUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.commonjava.util.logging.Log4jUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.file.AsyncFile;
import org.vertx.java.core.impl.DefaultVertx;

public class VertXOutputStreamTest
{

    private static final String BASE = VertXOutputStream.class.getSimpleName();

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private File tempFolder;

    @BeforeClass
    public static void setupClass()
    {
        Log4jUtil.configure( Level.DEBUG );
    }

    @Before
    public void tempFolder()
        throws IOException
    {
        tempFolder = temp.newFolder();
    }

    @Test
    public void writeSimpleFileViaAsyncFile()
        throws IOException
    {
        final FileHandler fh = new FileHandler();

        final File f = getTempResource( BASE, "test-write.txt" );

        final DefaultVertx v = new DefaultVertx();
        v.setContext( v.createEventLoopContext() );
        v.fileSystem()
         .open( f.getAbsolutePath(), fh );

        synchronized ( fh )
        {
            try
            {
                fh.wait();
            }
            catch ( final InterruptedException e )
            {
                return;
            }
        }

        final ByteArrayInputStream bain = new ByteArrayInputStream( "This is a test!".getBytes() );
        VertXOutputStream stream = null;
        try
        {
            stream = new VertXOutputStream( fh.af );
            IOUtils.copy( bain, stream );
        }
        finally
        {
            IOUtils.closeQuietly( stream );
        }

        final String result = FileUtils.readFileToString( f );
        assertThat( result, equalTo( "This is a test!" ) );
    }

    private File getTempResource( final String base, final String... parts )
    {
        final String[] arry = new String[parts.length + 1];
        arry[0] = base;
        System.arraycopy( parts, 0, arry, 1, parts.length );

        final String path = RequestUtil.normalize( false, arry );

        final File f = new File( tempFolder, path );
        f.getParentFile()
         .mkdirs();

        return f;
    }

    private static final class FileHandler
        implements Handler<AsyncResult<AsyncFile>>
    {
        private AsyncFile af;

        @Override
        public synchronized void handle( final AsyncResult<AsyncFile> event )
        {
            af = event.result();
            notifyAll();
        }
    }

}

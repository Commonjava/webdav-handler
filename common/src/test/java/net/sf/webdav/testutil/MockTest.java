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
package net.sf.webdav.testutil;

import java.io.ByteArrayInputStream;
import java.util.Date;

import net.sf.webdav.StoredObject;
import net.sf.webdav.WebdavResources;
import net.sf.webdav.locking.IResourceLocks;
import net.sf.webdav.locking.LockedObject;
import net.sf.webdav.locking.ResourceLocks;
import net.sf.webdav.spi.IMimeTyper;

import org.jmock.Mockery;
import org.jmock.api.ExpectationError;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

public abstract class MockTest
{

    protected void setupFixtures()
        throws Exception
    {
    }

    protected static Mockery _mockery;

    protected static boolean readOnly = true;

    protected static int TEMP_TIMEOUT = 10;

    protected static boolean TEMPORARY = true;

    protected static TestingOutputStream tos = new TestingOutputStream();

    protected static byte[] resourceContent = new byte[] { '<', 'h', 'e', 'l', 'l', 'o', '/', '>' };

    protected static ByteArrayInputStream bais = new ByteArrayInputStream( resourceContent );

    //    protected static DelegatingServletInputStream dsis = new DelegatingServletInputStream(
    //            bais);
    protected static long resourceLength = resourceContent.length;

    protected static String exclusiveLockRequest = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>" + "<D:lockinfo xmlns:D='DAV:'>"
        + "<D:lockscope><D:exclusive/></D:lockscope>" + "<D:locktype><D:write/></D:locktype>"
        + "<D:owner><D:href>I'am the Lock Owner</D:href></D:owner>" + "</D:lockinfo>";

    protected static byte[] exclusiveLockRequestByteArray = exclusiveLockRequest.getBytes();

    protected static ByteArrayInputStream baisExclusive = new ByteArrayInputStream( exclusiveLockRequestByteArray );

    //    protected static DelegatingServletInputStream dsisExclusive = new DelegatingServletInputStream(
    //            baisExclusive);

    protected static String sharedLockRequest = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>" + "<D:lockinfo xmlns:D='DAV:'>"
        + "<D:lockscope><D:shared/></D:lockscope>" + "<D:locktype><D:write/></D:locktype>"
        + "<D:owner><D:href>I'am the Lock Owner</D:href></D:owner>" + "</D:lockinfo>";

    protected static byte[] sharedLockRequestByteArray = sharedLockRequest.getBytes();

    protected static ByteArrayInputStream baisShared = new ByteArrayInputStream( sharedLockRequestByteArray );

    //    protected static DelegatingServletInputStream dsisShared = new DelegatingServletInputStream(
    //            baisShared);

    protected static String tmpFolder = "/tmp/tests";

    protected static String sourceCollectionPath = tmpFolder + "/sourceFolder";

    protected static String destCollectionPath = tmpFolder + "/destFolder";

    protected static String sourceFilePath = sourceCollectionPath + "/sourceFile";

    protected static String destFilePath = destCollectionPath + "/destFile";

    protected static String overwritePath = destCollectionPath + "/sourceFolder";

    protected static String[] sourceChildren = new String[] { "sourceFile" };

    protected static String[] destChildren = new String[] { "destFile" };

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @After
    public final void assertSatisfiedMockery()
        throws Exception
    {
        System.out.println( "Checking assertions..." );
        try
        {
            _mockery.assertIsSatisfied();
        }
        catch ( final ExpectationError error )
        {
            System.out.println( error );
            System.out.flush();
            throw new RuntimeException( error.toString() );
        }
    }

    @Before
    public void setup()
        throws Exception
    {
        _mockery = new Mockery();
        setupFixtures();
    }

    //    @AfterClass
    //    public static void tearDownAfterClass()
    //    {
    //        _mockery = null;
    //    }

    public static StoredObject initFolderStoredObject()
    {
        final StoredObject so = initStoredObject( true, null );

        return so;
    }

    public static StoredObject initFileStoredObject( final byte[] resourceContent )
    {
        final StoredObject so = initStoredObject( false, resourceContent );

        return so;
    }

    protected WebdavResources newResources( final IResourceLocks resourceLocks, final IMimeTyper mimeTyper,
                                            final boolean sendContentLength )
    {
        return new WebdavResources( null, null, resourceLocks == null ? new ResourceLocks() : resourceLocks, mimeTyper,
                                    sendContentLength, readOnly, false );
    }

    protected WebdavResources newResources( final IResourceLocks resourceLocks, final IMimeTyper mimeTyper )
    {
        return new WebdavResources( null, null, resourceLocks == null ? new ResourceLocks() : resourceLocks, mimeTyper,
                        false,
                        readOnly, false );
    }

    protected WebdavResources newResources( final IResourceLocks resourceLocks, final boolean readOnly )
    {
        return new WebdavResources( null, null, resourceLocks == null ? new ResourceLocks() : resourceLocks, null,
                                    false,
                                    readOnly, false );
    }

    protected WebdavResources newResources( final IResourceLocks resourceLocks, final boolean readOnly,
                                            final boolean lazyFolderCreationOnPut )
    {
        return new WebdavResources( null, null, resourceLocks == null ? new ResourceLocks() : resourceLocks, null,
                                    false, readOnly, lazyFolderCreationOnPut );
    }

    private static StoredObject initStoredObject( final boolean isFolder, final byte[] resourceContent )
    {
        final StoredObject so = new StoredObject();
        so.setFolder( isFolder );
        so.setCreationDate( new Date() );
        so.setLastModified( new Date() );
        if ( !isFolder )
        {
            // so.setResourceContent(resourceContent);
            so.setResourceLength( resourceContent.length );
        }
        else
        {
            so.setResourceLength( 0L );
        }

        return so;
    }

    public static StoredObject initLockNullStoredObject()
    {
        final StoredObject so = new StoredObject();
        so.setNullResource( true );
        so.setFolder( false );
        so.setCreationDate( null );
        so.setLastModified( null );
        // so.setResourceContent(null);
        so.setResourceLength( 0 );

        return so;
    }

    public static LockedObject initLockNullLockedObject( final ResourceLocks resLocks, final String path )
    {

        final LockedObject lo = new LockedObject( resLocks, path, false );
        lo.setExclusive( true );

        return lo;
    }
}

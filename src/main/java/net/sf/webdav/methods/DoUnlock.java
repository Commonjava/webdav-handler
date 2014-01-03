package net.sf.webdav.methods;

import java.io.IOException;

import net.sf.webdav.ITransaction;
import net.sf.webdav.IWebdavStore;
import net.sf.webdav.StoredObject;
import net.sf.webdav.WebdavStatus;
import net.sf.webdav.exceptions.LockFailedException;
import net.sf.webdav.locking.IResourceLocks;
import net.sf.webdav.locking.LockedObject;
import net.sf.webdav.spi.HttpServletRequest;
import net.sf.webdav.spi.HttpServletResponse;

public class DoUnlock
    extends DeterminableMethod
{

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( DoUnlock.class );

    private final IWebdavStore _store;

    private final IResourceLocks _resourceLocks;

    private final boolean _readOnly;

    public DoUnlock( final IWebdavStore store, final IResourceLocks resourceLocks, final boolean readOnly )
    {
        _store = store;
        _resourceLocks = resourceLocks;
        _readOnly = readOnly;
    }

    @Override
    public void execute( final ITransaction transaction, final HttpServletRequest req, final HttpServletResponse resp )
        throws IOException, LockFailedException
    {
        LOG.trace( "-- " + this.getClass()
                               .getName() );

        if ( _readOnly )
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
                            final StoredObject so = _store.getStoredObject( transaction, path );
                            if ( so.isNullResource() )
                            {
                                _store.removeObject( transaction, path );
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
                _resourceLocks.unlockTemporaryLockedObjects( transaction, path, tempLockOwner );
            }
        }
    }

}

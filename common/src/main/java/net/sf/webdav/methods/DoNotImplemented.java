package net.sf.webdav.methods;

import static net.sf.webdav.WebdavStatus.SC_FORBIDDEN;
import static net.sf.webdav.WebdavStatus.SC_NOT_IMPLEMENTED;

import java.io.IOException;

import net.sf.webdav.spi.ITransaction;
import net.sf.webdav.spi.WebdavRequest;
import net.sf.webdav.spi.WebdavResponse;

public class DoNotImplemented
    implements WebdavMethod
{

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( DoNotImplemented.class );

    private final boolean _readOnly;

    public DoNotImplemented( final boolean readOnly )
    {
        _readOnly = readOnly;
    }

    @Override
    public void execute( final ITransaction transaction, final WebdavRequest req, final WebdavResponse resp )
        throws IOException
    {
        LOG.trace( "-- " + req.getMethod() );

        if ( _readOnly )
        {
            resp.sendError( SC_FORBIDDEN );
        }
        else
        {
            resp.sendError( SC_NOT_IMPLEMENTED );
        }
    }
}

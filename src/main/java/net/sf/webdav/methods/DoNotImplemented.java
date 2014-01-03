package net.sf.webdav.methods;

import static net.sf.webdav.WebdavStatus.SC_FORBIDDEN;
import static net.sf.webdav.WebdavStatus.SC_NOT_IMPLEMENTED;

import java.io.IOException;

import net.sf.webdav.IMethodExecutor;
import net.sf.webdav.ITransaction;
import net.sf.webdav.spi.HttpServletRequest;
import net.sf.webdav.spi.HttpServletResponse;

public class DoNotImplemented
    implements IMethodExecutor
{

    private static org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger( DoNotImplemented.class );

    private final boolean _readOnly;

    public DoNotImplemented( final boolean readOnly )
    {
        _readOnly = readOnly;
    }

    @Override
    public void execute( final ITransaction transaction, final HttpServletRequest req, final HttpServletResponse resp )
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

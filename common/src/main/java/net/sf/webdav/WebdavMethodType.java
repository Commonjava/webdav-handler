package net.sf.webdav;

import net.sf.webdav.methods.DoCopy;
import net.sf.webdav.methods.DoDelete;
import net.sf.webdav.methods.DoGet;
import net.sf.webdav.methods.DoHead;
import net.sf.webdav.methods.DoLock;
import net.sf.webdav.methods.DoMkcol;
import net.sf.webdav.methods.DoMove;
import net.sf.webdav.methods.DoNotImplemented;
import net.sf.webdav.methods.DoOptions;
import net.sf.webdav.methods.DoPropfind;
import net.sf.webdav.methods.DoProppatch;
import net.sf.webdav.methods.DoPut;
import net.sf.webdav.methods.DoUnlock;
import net.sf.webdav.methods.WebdavMethod;

public enum WebdavMethodType
{
    //    register( "GET", new DoGet( store, dftIndexFile, insteadOf404, _resLocks, mimeTyper, !noContentLengthHeader ) );
    //    register( "HEAD", new DoHead( store, dftIndexFile, insteadOf404, _resLocks, mimeTyper, !noContentLengthHeader ) );
    //    final DoDelete doDelete = (DoDelete) register( "DELETE", new DoDelete( store, _resLocks, READ_ONLY ) );
    //    final DoCopy doCopy = (DoCopy) register( "COPY", new DoCopy( store, _resLocks, doDelete, READ_ONLY ) );
    //    register( "LOCK", new DoLock( store, _resLocks, READ_ONLY ) );
    //    register( "UNLOCK", new DoUnlock( store, _resLocks, READ_ONLY ) );
    //    register( "MOVE", new DoMove( _resLocks, doDelete, doCopy, READ_ONLY ) );
    //    register( "MKCOL", new DoMkcol( store, _resLocks, READ_ONLY ) );
    //    register( "OPTIONS", new DoOptions( store, _resLocks ) );
    //    register( "PUT", new DoPut( store, _resLocks, READ_ONLY, lazyFolderCreationOnPut ) );
    //    register( "PROPFIND", new DoPropfind( store, _resLocks, mimeTyper ) );
    //    register( "PROPPATCH", new DoProppatch( store, _resLocks, READ_ONLY ) );

    GET
    {
        @Override
        public WebdavMethod newExecutor()
        {
            return new DoGet();
        }
    },
    HEAD
    {
        @Override
        public WebdavMethod newExecutor()
        {
            return new DoHead();
        }
    },
    LOCK
    {
        @Override
        public WebdavMethod newExecutor()
        {
            return new DoLock();
        }
    },
    UNLOCK
    {
        @Override
        public WebdavMethod newExecutor()
        {
            return new DoUnlock();
        }
    },
    DELETE
    {
        @Override
        public WebdavMethod newExecutor()
        {
            return new DoDelete();
        }
    },
    COPY
    {
        @Override
        public WebdavMethod newExecutor()
        {
            return new DoCopy();
        }
    },
    MOVE
    {
        @Override
        public WebdavMethod newExecutor()
        {
            return new DoMove();
        }
    },
    MKCOL
    {
        @Override
        public WebdavMethod newExecutor()
        {
            return new DoMkcol();
        }
    },
    OPTIONS
    {
        @Override
        public WebdavMethod newExecutor()
        {
            return new DoOptions();
        }
    },
    PUT
    {
        @Override
        public WebdavMethod newExecutor()
        {
            return new DoPut();
        }
    },
    PROPFIND
    {
        @Override
        public WebdavMethod newExecutor()
        {
            return new DoPropfind();
        }
    },
    PROPPATCH
    {
        @Override
        public WebdavMethod newExecutor()
        {
            return new DoProppatch();
        }
    },
    NO_IMPL
    {
        @Override
        public WebdavMethod newExecutor()
        {
            return new DoNotImplemented();
        }
    };

    public static WebdavMethod getMethod( final String methodName )
    {
        for ( final WebdavMethodType type : values() )
        {
            if ( type.name()
                     .equals( methodName.toUpperCase() ) )
            {
                return type.newExecutor();
            }
        }

        return NO_IMPL.newExecutor();
    }

    public abstract WebdavMethod newExecutor();

}

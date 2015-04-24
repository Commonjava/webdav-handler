package net.sf.webdav.impl;

import java.io.File;

import net.sf.webdav.exceptions.WebdavException;
import net.sf.webdav.spi.IWebdavStore;
import net.sf.webdav.spi.IWebdavStoreWorker;

public class LocalFileSystemStore
    implements IWebdavStore
{

    private final File _root;

    public LocalFileSystemStore( final File root )
    {
        _root = root;
    }

    @Override
    public IWebdavStoreWorker newWorker()
        throws WebdavException
    {
        return new LocalFileSystemStoreWorker( _root );
    }

}

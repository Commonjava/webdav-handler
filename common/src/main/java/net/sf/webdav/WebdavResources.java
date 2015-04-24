package net.sf.webdav;

import net.sf.webdav.locking.IResourceLocks;
import net.sf.webdav.spi.IMimeTyper;

public final class WebdavResources
{

    private final String defaultIndexFile;

    private final String alt404Path;

    private final IResourceLocks resourceLocks;

    private final IMimeTyper mimeTyper;

    private final boolean sendContentLength;

    private final boolean readOnly;

    private final boolean lazyFolderCreationOnPut;

    public WebdavResources( final String defaultIndexFile, final String alt404Path, final IResourceLocks resourceLocks,
                            final IMimeTyper mimeTyper, final boolean sendContentLength, final boolean readOnly,
                            final boolean lazyFolderCreationOnPut )
    {
        this.defaultIndexFile = defaultIndexFile;
        this.alt404Path = alt404Path;
        this.resourceLocks = resourceLocks;
        this.mimeTyper = mimeTyper;
        this.sendContentLength = sendContentLength;
        this.readOnly = readOnly;
        this.lazyFolderCreationOnPut = lazyFolderCreationOnPut;
    }

    public String getDefaultIndexFile()
    {
        return defaultIndexFile;
    }

    public String getAlt404Path()
    {
        return alt404Path;
    }

    public IResourceLocks getResourceLocks()
    {
        return resourceLocks;
    }

    public IMimeTyper getMimeTyper()
    {
        return mimeTyper;
    }

    public boolean isSendContentLength()
    {
        return sendContentLength;
    }

    public boolean isReadOnly()
    {
        return readOnly;
    }

    public boolean isLazyFolderCreationOnPut()
    {
        return lazyFolderCreationOnPut;
    }

}

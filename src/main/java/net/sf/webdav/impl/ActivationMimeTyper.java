package net.sf.webdav.impl;

import javax.activation.MimetypesFileTypeMap;

import net.sf.webdav.spi.IMimeTyper;

public class ActivationMimeTyper
    implements IMimeTyper
{

    @Override
    public String getMimeType( final String path )
    {
        return MimetypesFileTypeMap.getDefaultFileTypeMap()
                                   .getContentType( path );
    }

}

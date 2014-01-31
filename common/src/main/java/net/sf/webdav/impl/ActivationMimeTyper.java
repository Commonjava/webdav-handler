package net.sf.webdav.impl;

import javax.activation.MimetypesFileTypeMap;
import javax.enterprise.inject.Alternative;
import javax.inject.Named;

import net.sf.webdav.spi.IMimeTyper;

@Alternative
@Named
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

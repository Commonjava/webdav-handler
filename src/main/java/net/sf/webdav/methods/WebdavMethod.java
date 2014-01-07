/*
 * Copyright 1999,2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.webdav.methods;

import java.io.IOException;

import net.sf.webdav.exceptions.LockFailedException;
import net.sf.webdav.exceptions.WebdavException;
import net.sf.webdav.spi.ITransaction;
import net.sf.webdav.spi.WebdavRequest;
import net.sf.webdav.spi.WebdavResponse;

public interface WebdavMethod
{

    void execute( ITransaction transaction, WebdavRequest req, WebdavResponse resp )
        throws IOException, LockFailedException, WebdavException;

}

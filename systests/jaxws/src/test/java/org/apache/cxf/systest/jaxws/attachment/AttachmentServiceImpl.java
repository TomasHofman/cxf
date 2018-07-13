/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.cxf.systest.jaxws.attachment;

import java.io.InputStream;
import java.util.logging.Logger;

import javax.activation.DataHandler;

import org.apache.cxf.annotations.SchemaValidation;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.helpers.IOUtils;

@SchemaValidation(type = SchemaValidation.SchemaValidationType.IN)
public class AttachmentServiceImpl implements AttachmentService {

    private static final Logger LOG = LogUtils.getL7dLogger(AttachmentServiceImpl.class);

    public Integer testMethod(Request request) throws Exception {
        if (request == null) {
            LOG.warning("request is null");
            return null;
        }

        DataHandler dataHandler = request.getContent();
        if (dataHandler == null) {
            LOG.warning("dataHandler is null");
            return null;
        }

        InputStream inputStream = dataHandler.getInputStream();
        if (inputStream == null) {
            LOG.warning("dataHandler.inputStream is null");
            return null;
        }

        byte[] bytes = IOUtils.readBytesFromStream(inputStream);
        if (bytes == null) {
            LOG.warning("byte array read from IS is null");
            return null;
        }

        LOG.fine("Received data: " + new String(bytes));

        return bytes.length;
    }
}

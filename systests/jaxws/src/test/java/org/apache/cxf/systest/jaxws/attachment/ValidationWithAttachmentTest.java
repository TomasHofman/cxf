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


import java.util.HashMap;

import javax.activation.DataHandler;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.attachment.ByteDataSource;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.testutil.common.TestUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class ValidationWithAttachmentTest {

    private static final String PORT = TestUtil.getNewPortNumber(ValidationWithAttachmentTest.class);
    private static final String CONTEXT_URI = ValidationWithAttachmentTest.class.getSimpleName();
    private static final String ADDRESS = "http://localhost:" + PORT + "/" + CONTEXT_URI;
    private static final String DATA = "test message";
    private static final Integer DATA_SIZE = DATA.getBytes().length;

    private static Server server;

    @After
    public void tearDown() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    public void testJaxWsClientNotValidating() throws Exception {
        initJaxWsServer();
        AttachmentService client = createJaxWsClient(false);
        testAttachmentTransfer(client);
    }

    @Test
    public void testJaxWsClientValidating() throws Exception {
        initJaxWsServer();
        AttachmentService client = createJaxWsClient(true);
        testAttachmentTransfer(client);
    }

    @Test
    public void testSimpleClientNotValidating() throws Exception {
        initSimpleServer();
        AttachmentService client = createSimpleClient(false);
        testAttachmentTransfer(client);
    }

    @Test
    public void testSimpleClientValidating() throws Exception {
        initSimpleServer();
        AttachmentService client = createSimpleClient(true);
        testAttachmentTransfer(client);
    }

    private void testAttachmentTransfer(AttachmentService client) throws Exception {
        Integer bytes = client.testMethod(createRequest());
        Assert.assertNotNull("Server returned null", bytes);
        Assert.assertTrue("Attachment data were not received", bytes > 0);
        Assert.assertEquals("Received attachment has incorrect size", DATA_SIZE, bytes);
    }

    private static Request createRequest() {
        Request request = new Request();
        request.setContent(new DataHandler(new ByteDataSource(DATA.getBytes())));
        return request;
    }

    private static void initJaxWsServer() {
        JaxWsServerFactoryBean factory = new JaxWsServerFactoryBean();
        factory.setServiceClass(AttachmentService.class);
        factory.setAddress(ADDRESS);
        factory.setServiceBean(new AttachmentServiceImpl());
        factory.getJaxWsServiceFactory().setValidate(true);
        factory.setProperties(new HashMap<>());
        factory.getProperties().put("schema-validation-enabled", true);
        server = factory.create();
    }

    private static void initSimpleServer() {
        ServerFactoryBean factory = new ServerFactoryBean();
        factory.setServiceClass(AttachmentService.class);
        factory.setServiceBean(new AttachmentServiceImpl());
        factory.setAddress(ADDRESS);
        factory.getServiceFactory().setDataBinding(new AegisDatabinding());
        factory.setProperties(new HashMap<>());
        // TODO: can't make server validation pass with simple fronted client
//        factory.getProperties().put("schema-validation-enabled", true);
        server = factory.create();
    }

    private static AttachmentService createJaxWsClient(boolean validate) {
        JaxWsProxyFactoryBean proxyFactory = new JaxWsProxyFactoryBean();
        proxyFactory.setServiceClass(AttachmentService.class);
        proxyFactory.setAddress(ADDRESS);

        proxyFactory.getInInterceptors().add(new LoggingInInterceptor());
        proxyFactory.getOutInterceptors().add(new LoggingOutInterceptor());

        AttachmentService serviceProxy = (AttachmentService) proxyFactory.create();

        // enable MTOM in client
        BindingProvider bp = (BindingProvider) serviceProxy;
        SOAPBinding binding = (SOAPBinding) bp.getBinding();
        binding.setMTOMEnabled(true);

        // set validation
        bp.getRequestContext().put("schema-validation-enabled", Boolean.toString(validate));
        Client client = ClientProxy.getClient(serviceProxy);
        Assert.assertEquals(Boolean.toString(validate), client.getRequestContext().get("schema-validation-enabled"));
        client.getRequestContext().put("schema-validation-enabled", Boolean.toString(validate));

        return serviceProxy;
    }

    private static AttachmentService createSimpleClient(boolean validate) {
        ClientProxyFactoryBean proxyFactory = new ClientProxyFactoryBean();
        proxyFactory.setServiceClass(AttachmentService.class);
        proxyFactory.setAddress(ADDRESS);
        proxyFactory.getServiceFactory().setDataBinding(new AegisDatabinding());

        // logging interceptors
        proxyFactory.getInInterceptors().add(new LoggingInInterceptor());
        proxyFactory.getOutInterceptors().add(new LoggingOutInterceptor());

        // enable MTOM
        proxyFactory.setProperties(new HashMap<>());
        proxyFactory.getProperties().put("mtom-enabled", Boolean.TRUE);

        AttachmentService serviceProxy = (AttachmentService) proxyFactory.create();

        // set validation
        Client client = ClientProxy.getClient(serviceProxy);
        client.getRequestContext().put("schema-validation-enabled", Boolean.toString(validate));

        return serviceProxy;
    }

}

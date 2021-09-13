/*
 * Copyright 2012-2016, the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flipkart.flux.filter;

import org.glassfish.jersey.message.internal.ReaderWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <code>RequestLoggingFilter</code> is a Jersey filter for logging Request payload. May be used for debugging request flows
 * to Flux
 *
 * @author regu.b
 */

public class RequestLoggingFilter implements ContainerRequestFilter {

    private static final Logger logger = LogManager.getLogger(RequestLoggingFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) {
        StringBuilder sb = new StringBuilder();
        sb.append(requestContext.getUriInfo().getPath())
                .append(" with payload : ")
                .append(getRequestPayload(requestContext));
        logger.info("Request : " + sb.toString());
    }

    private String getRequestPayload(ContainerRequestContext requestContext) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = requestContext.getEntityStream();
        final StringBuilder payload = new StringBuilder();
        try {
            ReaderWriter.writeTo(in, out);
            byte[] requestEntity = out.toByteArray();
            if (requestEntity.length == 0) {
                payload.append("\n");
            } else {
                payload.append(new String(requestEntity)).append("\n");
            }
            requestContext.setEntityStream(new ByteArrayInputStream(requestEntity));
        } catch (IOException ex) {
            logger.error("Unable to get payload from the request i/o error. {}", ex.getMessage());
        }
        return payload.toString();
    }
}
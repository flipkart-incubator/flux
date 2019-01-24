package com.flipkart.flux.filter;

import com.flipkart.kloud.filter.SecurityContextHolder;
import org.glassfish.jersey.message.internal.ReaderWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class RequestLoggingFilter implements ContainerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) {
        StringBuilder sb = new StringBuilder();
        sb.append(requestContext.getUriInfo().getPath())
                .append(" by user : ")
                .append(SecurityContextHolder.getSecurityContext().getUser() != null ?
                        SecurityContextHolder.getSecurityContext().getUser().getUserId() : "User not logged in.")
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

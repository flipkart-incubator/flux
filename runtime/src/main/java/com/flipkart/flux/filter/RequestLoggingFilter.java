package com.flipkart.flux.filter;

import com.flipkart.kloud.filter.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

public class RequestLoggingFilter implements ContainerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) {
        StringBuilder sb = new StringBuilder();
        sb.append(requestContext.getUriInfo().getPath())
                .append(" by user : ")
                .append(SecurityContextHolder.getSecurityContext().getUser() != null ?
                        SecurityContextHolder.getSecurityContext().getUser().getUserId() : "User not logged in.");
        logger.info("Request : " + sb.toString());
    }
}

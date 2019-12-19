package com.datastax.powertools;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;

import org.jboss.logging.Logger;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/hello")
public class ExampleResource {

    private static final Logger logger = Logger.getLogger(ExampleResource.class);

    @GET
    public String hello() {
        return "hello";
    }

    @POST
    public String test(String test){
        logger.info(test);
        return test;
    }
}

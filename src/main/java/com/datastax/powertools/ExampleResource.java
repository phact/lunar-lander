package com.datastax.powertools;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.datastax.powertools.cassandra.CassandraConfiguration;
import com.datastax.powertools.cassandra.CassandraManager;
import org.jboss.logging.Logger;


@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/connect")
public class ExampleResource {

    private static final Logger logger = Logger.getLogger(ExampleResource.class);
    private CassandraManager cassandraManager = new CassandraManager();

    @POST
    public Response connect(CassandraConfiguration config){
        logger.info("Connecting to " + config.getContactPoints());
        try {
            return Response.ok(cassandraManager.connect(config)).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.serverError().build();
    }
}

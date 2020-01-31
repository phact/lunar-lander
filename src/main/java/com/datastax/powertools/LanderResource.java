package com.datastax.powertools;

import com.datastax.oss.driver.api.core.AllNodesFailedException;
import com.datastax.powertools.api.CassandraNode;
import com.datastax.powertools.api.LanderMission;
import com.datastax.powertools.api.LanderSequence;
import com.datastax.powertools.cassandra.CassandraClusterConfiguration;
import com.datastax.powertools.cassandra.CassandraManager;
import com.datastax.powertools.missioncontrol.MissionControlManager;
import com.datastax.powertools.missioncontrol.SSHResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.runtime.StartupEvent;
import org.glassfish.jersey.server.ChunkedOutput;
import org.jboss.logging.Logger;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.*;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/")
public class LanderResource {

    private static final Logger logger = Logger.getLogger(LanderResource.class);

    ObjectMapper objectMapper = new ObjectMapper();


    @Inject
    MissionControlManager missionControlManager;

    @Inject
    CassandraManager cassandraManager;

    void onStart(@Observes StartupEvent ev) {               //
        logger.info("The application is starting...");

        //for native image
        /*
        // TODO: figure out a better way to get around this native-image issue
        Constructor<?>[] privateStringField = ProgrammaticArguments.class.
                getConstructors();

        ProgrammaticArguments b = null;
        for (Constructor<?> constructor : privateStringField) {
            constructor.setAccessible(true);

            constructor.newInstance(Arrays.asList(),
                    null,
                    null,
                    null,
                    new HashMap<>(),
                    new HashMap<>(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null);
        }
        DriverConfigLoader a = new DefaultDriverConfigLoader();

        DriverContext context = new DefaultDriverContext(a, b);
        new ExponentialReconnectionPolicy(context);
         */


        setup();
    }

    private void setup() {
        logger.info("Setting up stock missions");
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("missions.json");

        Map<String, LanderMission> missions = null;
        try {
            missions = objectMapper.readValue(in, new TypeReference<Map<String, LanderMission>>() { });
            missionControlManager.setMissions(missions);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to read missions.json");
        }
    }

    @Path("/missionNames")
    @GET
    public Response getMissionNames(){
        logger.info("Getting mission names");
        try {
            return Response.ok(missionControlManager.getMissionNames()).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @Path("/missions")
    @GET
    public Response getMissions(){
        logger.info("Getting missions");
        try {
            return Response.ok(missionControlManager.getMissions()).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @Path("/missions")
    @PUT
    public Response setMissions(Map<String, LanderMission> missions){
        logger.info("Setting missions");
        try {
            missionControlManager.setMissions(missions);
            return Response.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @Path("/connect")
    @POST
    public Response connect(CassandraClusterConfiguration config){
        logger.info("Connecting to " + config.getContactPoints());
        List<CassandraNode> cluster = new ArrayList<>();
        try {
            cluster = cassandraManager.connect(config);
            for (CassandraNode cassandraNode : cluster) {
                cassandraNode.setPrivateKey(config.getPrivateKey());
                cassandraNode.setSshUser(config.getSshUser());
            }
            missionControlManager.setCluster(cluster);
            return Response.ok(cluster).build();
       } catch (AllNodesFailedException e) {
            for (String ip : config.getContactPoints().split(",")) {
                InetSocketAddress address = InetSocketAddress.createUnresolved(ip,9042);
                CassandraNode cassandraNode = new CassandraNode();
                cassandraNode.setBroadcastRpcAddress(Optional.of(address));
                cassandraNode.setBroadcastAddress(Optional.of(address));
                cassandraNode.setListenAddress(Optional.of(address));
                cassandraNode.setPrivateKey(config.getPrivateKey());
                cassandraNode.setSshUser(config.getSshUser());
                cluster.add(cassandraNode);
            }
            missionControlManager.setCluster(cluster);
            return Response.ok(cluster).build();
       } catch (Exception e) {
            logger.info("Could not connect");
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }

   }

    @Path("/mission")
    @POST
    public Response saveMission(LanderMission mission){
        logger.info("Saving mission: " + mission.getMissionName());
        try {
            missionControlManager.saveMission(mission);
            return Response.ok().build();
        } catch (Exception e) {
            logger.info("Could not save mission");
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @Path("/deleteMission")
    @POST
    public Response deleteMission(LanderMission mission){
        logger.info("Deleting mission:  " + mission.getMissionName());
        try {
            missionControlManager.deleteMission(mission);
            return Response.ok().build();
        } catch (Exception e) {
            logger.info("Could not save mission");
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @Path("/executeCommand/{command}")
    @GET
    public Response executeCommand(@PathParam("command") String command){
        logger.info("Executing: " + command);
        try {
            SSHResponse response = missionControlManager.executeCommand(command) ;
            return Response.ok(response).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getStackTrace()).build();
        }
    }

    @Path("/rollingDeployment/{missionName}")
    @GET
    public Response rollingDeployment(@PathParam("missionName") String missionName){
        logger.info("Initiating rolling deployment for mission " + missionName);
        try {
            return Response.ok(missionControlManager.rollingDeployment(missionName)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @Path("/canaryDeployment/{missionName}")
    @GET
    public Response canaryDeployment(@PathParam("missionName") String missionName){
        logger.info("Initiating canary deployment for mission " + missionName);
        try {
            return Response.ok(missionControlManager.canaryDeployment(missionName)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }


    @Path("/getSequences/{missionName}")
    @GET
    public Response getSequences(@PathParam("missionName") String missionName){
        logger.info("Getting sequences for mission " + missionName);
        try {
            List<LanderSequence> sequences = missionControlManager.getSequencesFromMission(missionName);
            return Response.ok(sequences).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @Path("/stream/{missionName}")
    @GET
    public Response stream(@PathParam("missionName") String missionName){
        logger.info("Initiating stream test" + missionName);
        ChunkedOutput<String> out = new ChunkedOutput<>(String.class, "\n");
        Thread thread = new Thread() {

            public void run() {
                try {
                    for (String missionName : missionControlManager.getMissionNames()) {
                        out.write("\n" + missionName);
                    }
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        try {
                return Response.ok(out).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity(e.getMessage()).build();
        }
    }
}

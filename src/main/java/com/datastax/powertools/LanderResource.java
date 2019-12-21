package com.datastax.powertools;

import com.datastax.powertools.api.CassandraNode;
import com.datastax.powertools.api.LanderMission;
import com.datastax.powertools.api.LanderSequence;
import com.datastax.powertools.cassandra.CassandraConfiguration;
import com.datastax.powertools.cassandra.CassandraManager;
import com.datastax.powertools.missioncontrol.MissionControlManager;
import io.quarkus.runtime.StartupEvent;
import org.jboss.logging.Logger;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/")
public class LanderResource {

    private static final Logger logger = Logger.getLogger(LanderResource.class);


    @Inject
    MissionControlManager missionControlManager;

    @Inject
    CassandraManager cassandraManager;

    void onStart(@Observes StartupEvent ev) {               //
        logger.info("The application is starting...");
        setup();

    }

    private void setup() {
        Map<String, LanderMission> missions = new HashMap<>();

        String missionName = "Clear DSE";

        List<String> commands = new ArrayList<>();

        commands.add("echo I love puppies");
        //commands.add("sudo service dse stop");
        //commands.add("sudo rm -rf /var/lib/cassandra/data/*; sudo rm -rf /var/lib/cassandra/commitlog/*");

        List<LanderSequence> sequences = new ArrayList();
        LanderSequence sequence = new LanderSequence();
        sequences.add(sequence);

        sequence.setCommands(commands);
        sequence.setName("puppies");
        sequence.setSequenceType(LanderSequence.SequenceType.PARALLEL);

        LanderMission mission = new LanderMission(missionName, sequences);
        missions.put(missionName, mission);

        missionControlManager.setMissions(missions);
    }

    @Path("/missions")
    @GET
    public Response getMissions(){
        logger.info("Getting missions ");
        try {
            return Response.ok(missionControlManager.getMissionNames()).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.serverError().build();
    }

    @Path("/connect")
    @POST
    public Response connect(CassandraConfiguration config){
        logger.info("Connecting to " + config.getContactPoints());
        try {
            List<CassandraNode> cluster = cassandraManager.connect(config);
            missionControlManager.setCluster(cluster);
            return Response.ok(cluster).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.serverError().build();
    }

    @Path("/initiateSequence/{missionName}")
    @GET
    public Response initiateSequence(@PathParam("missionName") String missionName){
        logger.info("Initiating sequence for mission " + missionName);
        try {
            return Response.ok(missionControlManager.initiateSequence(missionName)).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.serverError().build();
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
        }
        return Response.serverError().build();
    }
}

package com.datastax.powertools.missioncontrol;

import com.datastax.powertools.api.CassandraNode;
import com.datastax.powertools.api.LanderMission;

import java.util.*;
import java.util.stream.Collectors;

import com.datastax.powertools.api.LanderSequence;
import com.fasterxml.jackson.databind.JsonNode;
import com.jcraft.jsch.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Security;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/*
 *
 * @author Sebastián Estévez on 12/6/19.
 *
 */


@ApplicationScoped
public class MissionControlManager {

    private static Logger logger = LoggerFactory.getLogger(MissionControlManager.class);
    Map<String, LanderMission> missions = new HashMap<String, LanderMission>();
    private List<CassandraNode> cluster;

    public void setMissions(Map<String, LanderMission> missions) {
        this.missions = missions;
    }

    public void setCluster(List<CassandraNode> cluster) {
        this.cluster = cluster;
    }

    public List<String> getMissionNames() {
       List<String> missionList = missions.
                values().
                stream().
                map(mission ->
                        mission.getMissionName()).collect(Collectors.toList()
        );
        return missionList;
    }

    public List<CassandraNode> initiateSequence(String missionName) {
        if (missions.get(missionName) == null){
            throw new RuntimeException("Mission does not exist");
        }
        LanderMission mission = missions.get(missionName);
        if (!run(mission)){
            throw new RuntimeException("execution of steps failed");
        }
        return cluster;
    }

    private boolean run(LanderMission mission) {
        if (mission.getSequences().isEmpty()){
            throw new RuntimeException("Mission has no sequences");
        }
        List<LanderSequence> sequences = mission.getSequences();

        for (LanderSequence sequence : sequences) {
            logger.info("Attempting to run sequence" + sequence.getName() + " mission " + mission.getMissionName());

            List<String> commands = sequence.getCommands();

            String expectedResponse = sequence.getExpectedResponse();

            if (sequence.getSequenceType() == null){
                throw new RuntimeException("Sequence has no type");
            }
            LanderSequence.SequenceType sequenceType = sequence.getSequenceType();

            //TODO: think about if and how to do retries (idempotent flag on the sequence?)
            //TODO: use mustache to include node variables
            //TODO: stick success and failiure on the cluster rather than collecting
            boolean success = false;
            switch (sequenceType) {
                case PARALLEL:
                    success = sshAll(commands, cluster);
                    break;
                case ROLLING:
                    success = sshAll(commands, cluster);
                    break;
                case WAIT:
                    success = wait(commands, expectedResponse, cluster);
                    break;
                default:
                    throw new RuntimeException("sequence type not supported");
            }
            if (!success){
                return false;
            }
        }
        return true;
    }


    private boolean wait(List<String> commands, String expectedResponse, List<CassandraNode> cluster) {
        return true;
    }
    //TODO: grow up, parallelize this, get a thread pool
    //Note: currently each command is a separate ssh session
    private boolean sshAll(List<String> commands, List<CassandraNode> cluster) {
        List<Integer> exitStatuses = new ArrayList<>();
        if (cluster == null){
            throw new RuntimeException("No cluster has been connected");
        }
        for (CassandraNode node : cluster) {
            for (String command : commands) {
                //TODO: allow optional usage of listen address for ssh
                exitStatuses.add(
                        ssh(
                                command,
                                node.getBroadcastRpcAddress().
                                        toString().split(":")[0].
                                        split("/")[1],
                                node.getPrivateKey(), node.getUser()));
            }
        }
        if (exitStatuses.stream().filter(x -> x.equals(0)).count() == exitStatuses.stream().count()){
            return true;
        }
        else{
            return false;
        }
    }

    private int ssh(String command, String host, String key, String user) {

        //TODO: consider creating fewer objects for efficiency
        JSch jsch = new JSch();

        //TODO: maybe allow non standard ssh port?
        int port = 22;
        String privateKey = key;
        logger.info(String.format("running ssh command on %s", host));
        logger.debug(String.format("running ssh command %s on %s", command, host));

        try {
            if (privateKey != null) {
                Security.addProvider(new BouncyCastleProvider());
                jsch.addIdentity(user, privateKey.substring(0, privateKey.length() - 1).getBytes(), null, null);
                logger.debug("identity added ");
            }

            Session session = jsch.getSession(user, host, port);
            logger.debug("session created.");

            // We can't do host checking against new boxes constantly
            session.setConfig("StrictHostKeyChecking", "no");

            session.connect();
            logger.debug("session connected.....");

            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
            channel.setInputStream(null);
            ((ChannelExec) channel).setErrStream(System.err);

            channel.connect();
            while(!channel.isClosed()){
                Thread.sleep(1000);
            }

            InputStream in = channel.getInputStream();


            int exitStatus = channel.getExitStatus();
            if (exitStatus != 0){
                //TODO figure out how to get the output from failed ssh sessions
                //logger.warn(read(in));
                logger.warn(String.format("failed with exit status %s",exitStatus));
            }
            return exitStatus;

        } catch (JSchException e) {
            logger.warn("JSch error - is this node still coming up?" + host);
            e.printStackTrace();
            return -1;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public List<LanderSequence> getSequencesFromMission(String missionName) {
        return missions.get(missionName).getSequences();
    }
}

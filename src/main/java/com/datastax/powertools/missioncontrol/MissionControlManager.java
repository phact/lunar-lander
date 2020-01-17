package com.datastax.powertools.missioncontrol;

import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.context.DriverContext;
import com.datastax.oss.driver.api.core.session.ProgrammaticArguments;
import com.datastax.oss.driver.internal.core.config.typesafe.DefaultDriverConfigLoader;
import com.datastax.oss.driver.internal.core.connection.ExponentialReconnectionPolicy;
import com.datastax.oss.driver.internal.core.context.DefaultDriverContext;
import com.datastax.powertools.api.CassandraNode;
import com.datastax.powertools.api.LanderMission;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import com.datastax.powertools.api.LanderSequence;
import com.jcraft.jsch.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.security.Security;

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
    int arrMaxSize = 1024;
    private Map<String, Session> sessions;

    public void setMissions(Map<String, LanderMission> missions) {
        this.missions = missions;
    }

    public void setCluster(List<CassandraNode> cluster) {
        logger.info("Setting cluster:" + cluster);
        this.cluster = cluster;
        this.sessions = createSessions();
    }

    private Map<String, Session> createSessions() {
        Map<String, Session> sessions = new HashMap<>();
        for (CassandraNode node : cluster) {
            String host;
            host = node.getBroadcastAddress().
                    toString().split(":")[0].
                    split("/")[1];
            String privateKey = node.getPrivateKey();
            String sshUser = node.getSshUser();

            JSch jsch = new JSch();

            //TODO: maybe allow non standard ssh port?
            int port = 22;
            logger.info(String.format("creating ssh session for %s", host));

            try {
                if (privateKey != null) {
                    Security.addProvider(new BouncyCastleProvider());
                    jsch.addIdentity(sshUser, privateKey.substring(0, privateKey.length() - 1).getBytes(), null, null);
                    logger.debug("identity added ");
                }

                Session session = jsch.getSession(sshUser, host, port);
                logger.debug("session created.");

                // We can't do host checking against new boxes constantly
                session.setConfig("StrictHostKeyChecking", "no");

                session.connect();
                logger.debug("session connected.....");

                sessions.put(host, session);


            } catch (JSchException e) {
                e.printStackTrace();
                throw new RuntimeException("Could not create ssh session");
            }
        }
        return sessions;
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

    public List<SSHResponse> initiateSequence(String missionName) {
        if (missions.get(missionName) == null){
            throw new RuntimeException("Mission does not exist");
        }
        LanderMission mission = missions.get(missionName);
        List<SSHResponse> results = run(mission);
        /*
        if (results.stream().filter(x -> x.getStatusCode() != 0).count() > 0){
            throw new RuntimeException("execution of steps failed");
        }
        */
        return results;
    }

    private List<SSHResponse> run(LanderMission mission) {
        if (mission.getSequences().isEmpty()){
            throw new RuntimeException("Mission has no sequences");
        }
        List<LanderSequence> sequences = mission.getSequences();

        List<SSHResponse> responses = new ArrayList<>();
        for (LanderSequence sequence : sequences) {
            logger.info("Attempting to run sequence: " + sequence.getName() + " mission " + mission.getMissionName());

            List<String> commands = sequence.getCommands();

            String expectedResponse = sequence.getExpectedResponse();

            //TODO: think about if and how to do retries (idempotent flag on the sequence?)
            //TODO: use mustache to include node variables
            //TODO: stick success and failiure on the cluster rather than collecting
            responses.addAll(sshAll(commands, cluster));
        }
        return responses;
    }


    private boolean wait(List<String> commands, String expectedResponse, List<CassandraNode> cluster) {
        return true;
    }
    //TODO: grow up, parallelize this, get a thread pool
    //Note: currently each command is a separate ssh session
    private List<SSHResponse> sshAll(List<String> commands, List<CassandraNode> cluster) {
        List<SSHResponse> responses = new ArrayList<>();
        if (cluster == null){
            throw new RuntimeException("No cluster has been connected");
        }
        for (String command : commands) {
            for (CassandraNode node : cluster) {
                //TODO: allow optional usage of listen address for ssh
                responses.add(
                        ssh(
                                command,
                                node.getBroadcastAddress().
                                        toString().split(":")[0].
                                        split("/")[1]
                        )
                );
            }
        }
        return responses;
    }

    private SSHResponse ssh(String command, String host) {
        try{
            Session session = sessions.get(host);
            logger.debug("found session in session cache.");

            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
            channel.setInputStream(null);
            ((ChannelExec) channel).setErrStream(null);

            channel.connect();
            InputStream in = channel.getInputStream();
            InputStream errIn = ((ChannelExec) channel).getErrStream();

            StringBuilder stdoutBuilder = new StringBuilder();
            StringBuilder stderrBuilder = new StringBuilder();

            byte[] tmp = new byte[arrMaxSize];

            while ( true )
            {
                while ( in.available() > 0 )
                {
                    int i = in.read( tmp, 0, arrMaxSize );
                    if ( i < 0 )
                        break;
                    stdoutBuilder.append( new String( tmp, 0, i ) );
                }

                while ( errIn.available() > 0 )
                {
                    int i = in.read( tmp, 0, arrMaxSize );
                    if ( i < 0 )
                        break;
                    stderrBuilder.append( new String( tmp, 0, i ) );
                }

                if ( channel.isClosed() )
                {
                    break;
                }
                try
                {
                    Thread.sleep( 500 );
                }
                catch ( Exception ee )
                {

                }
            }

            String stdout = stdoutBuilder.toString();
            String stderr = stderrBuilder.toString();

            int exitStatus = channel.getExitStatus();

            if (exitStatus != 0){
                //TODO figure out how to get the output from failed ssh sessions
                logger.warn(String.format("failed with exit status %s",exitStatus));
                logger.warn(stdout);
            }
            return new SSHResponse(command, host, stdout, stderr, exitStatus);

        } catch (JSchException e) {
            logger.warn("Unable to execute command against " + host);
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<LanderSequence> getSequencesFromMission(String missionName) {
        return missions.get(missionName).getSequences();
    }

    public SSHResponse executeCommand(String command) {
        if (cluster == null){
            throw new RuntimeException("Cluster not configured");
        }
        CassandraNode node = cluster.get(0);
        //TODO: allow different address
        String host = node.getBroadcastAddress().
                toString().split(":")[0].
                split("/")[1];
        String key = node.getPrivateKey();
        String user = node.getSshUser();
        SSHResponse response = ssh(command, host);
        return response;
    }

    public void saveMission(LanderMission mission) {
        missions.put(mission.getMissionName(), mission);
    }

    public void deleteMission(LanderMission mission) {
        logger.info("deleting mission");
        missions.remove(mission.getMissionName());
    }

    public Map<String, LanderMission> getMissions() {
        return missions;
    }
}

package com.datastax.powertools.missioncontrol;

import com.datastax.powertools.LanderResource;
import com.datastax.powertools.api.CassandraNode;
import com.datastax.powertools.api.LanderMission;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
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
    private Map<String, Session> sessions = new HashMap<>();
    private CassandraIpType selectedIpType = null;

    public void setMissions(Map<String, LanderMission> missions) {
        this.missions = missions;
    }

    public void setCluster(List<CassandraNode> cluster) {
        logger.info("Setting cluster:" + cluster);
        this.cluster = cluster;
        selectedIpType = null;
        pickRelevantIp();
        createSessions();
    }

    private void pickRelevantIp() {
        CassandraNode node = cluster.get(0);

        String privateKey = node.getPrivateKey();
        String sshUser = node.getSshUser();

        //TODO: maybe allow non standard ssh port?
        int port = 22;
        logger.info(String.format("Attempting to set ipType for ssh"));
        EnumSet.allOf(CassandraIpType.class).stream()
                .sorted((x, y) -> {
                    // try listen last because Listen address is only present in system.local, not system.peers, so this information is only available for the control host.
                    if (x.equals(CassandraIpType.LISTEN)){
                        return 1;
                    } else if (y.equals(CassandraIpType.LISTEN)){
                        return -1;
                    } else  {
                        return x.compareTo(y);
                    }
                })
                .forEach(ipType -> tryToSetIpType(privateKey,sshUser,port,ipType,node));
        if (this.selectedIpType == null){
            logger.error("Could not connect to any address, make sure the contact points are accessible on the network");
            throw new RuntimeException();
        }
        logger.info(String.format("ipType is %S", this.selectedIpType));
    }

    private void tryToSetIpType(String privateKey, String sshUser, int port, CassandraIpType ipType, CassandraNode node) {
        if (this.selectedIpType !=null)
                return;
        try {
            String host = getHostFromIpType(ipType, node);
            createSession(privateKey,sshUser,host,port);
            this.selectedIpType = ipType;
        } catch (Exception e) {
            logger.info("ip type not " + ipType);
        }
    }

    private String getHostFromIpType(CassandraIpType ipType, CassandraNode node) {
        InetSocketAddress inethost = null;
        switch (ipType) {
            case LISTEN:
                inethost = node.getListenAddress().orElse(null);
                break;
            case BROADCAST:
                inethost = node.getBroadcastAddress().orElse(null);
                break;
            case BROADCAST_RPC:
                inethost = node.getBroadcastRpcAddress().orElse(null);
                break;
        }
        String host;
        if (inethost != null){
            host = inethost.toString();
            logger.info("host: " +host);
            if (!host.isEmpty()) {
                host = host.
                        split(":")[0].
                        replaceAll("/", "");
                return host;
            }else{
                throw new RuntimeException("no ip of type: " + ipType);
            }
        }
        throw new RuntimeException("no ip of type: " + ipType);
    }

    private void createSessions() {
        Map<String, Session> sessions = new HashMap<>();
        List<CompletableFuture<Boolean>> connectFutures = new ArrayList();
        for (CassandraNode node : cluster) {

            CompletableFuture<Boolean> connectFuture = new CompletableFuture<>().supplyAsync(() -> {
                String host;
                host = getHostFromIpType(this.selectedIpType,node);
                String privateKey = node.getPrivateKey();
                String sshUser = node.getSshUser();


                //TODO: maybe allow non standard ssh port?
                int port = 22;
                logger.info(String.format("creating ssh session for %s", host));

                try {
                    createSession(privateKey, sshUser, host, port);
                } catch (JSchException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Could not create ssh session");
                }
                return true;
            });

            connectFutures.add(connectFuture);
        }
        for (CompletableFuture<Boolean> connectFuture : connectFutures) {
            try {
                connectFuture.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public void createSession(String privateKey, String sshUser, String host, int port) throws JSchException {
        JSch jsch = new JSch();
        if (privateKey != null) {

            Security.addProvider(new BouncyCastleProvider());
            jsch.addIdentity(sshUser, privateKey.substring(0, privateKey.length() - 1).getBytes(), null, null);
            logger.debug("identity added ");
        }

        Session session = jsch.getSession(sshUser, host, port);
        logger.debug("session created.");

        //speed up timeout
        session.setTimeout(7000);

        // We can't do host checking against new boxes constantly
        session.setConfig("StrictHostKeyChecking", "no");

        session.connect();
        logger.debug("session connected.....");

        sessions.put(host, session);
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

    public ArrayList<CompletableFuture<SSHResponse>> canaryDeployment(String missionName, Executor executor, Executor executorPool) {
        ArrayList<CompletableFuture<SSHResponse>> outputStreamFutures = new ArrayList();

        if (missions.get(missionName) == null){
            throw new RuntimeException("Mission does not exist");
        }
        LanderMission mission = missions.get(missionName);
        runCanary(mission, outputStreamFutures, executor, executorPool);
        /*
        if (results.stream().filter(x -> x.getStatusCode() != 0).count() > 0){
            throw new RuntimeException("execution of steps failed");
        }
        */
        return outputStreamFutures;
    }

    public void runCanary(LanderMission mission, ArrayList<CompletableFuture<SSHResponse>> outputStreamFutures, Executor executor, Executor executorPool) {
        if (mission.getSequences().isEmpty()){
            throw new RuntimeException("Mission has no sequences");
        }
        List<LanderSequence> sequences = mission.getSequences();


        CompletableFuture<SSHResponse> sequenceSSHFuture = new CompletableFuture<>().supplyAsync(() -> {
            return null;
        });

        for (LanderSequence sequence : sequences) {
            logger.info("Attempting to run sequence: " + sequence.getName() + " mission " + mission.getMissionName());

            List<String> commands = sequence.getCommands();

            String expectedResponse = sequence.getExpectedResponse();
            LanderSequence.ConcurrencyType concurrencyType = sequence.getConcurrencyType();

            //TODO: think about if and how to do retries (idempotent flag on the sequence?)
            //TODO: use mustache to include node variables
            //TODO: stick success and failiure on the cluster rather than collecting
            sequenceSSHFuture= streamSSHAll(commands,  cluster.subList(0,1), concurrencyType, outputStreamFutures, sequenceSSHFuture, executor, executorPool);
        }
    }


    private SSHResponse ssh(String command, String host) {
        try{
            Session session = sessions.get(host);
            logger.debug("found session in session cache.");

            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);
            channel.setInputStream(null);
            ((ChannelExec) channel).setErrStream(null);

            logger.info("attempting command: " + command + " on host: "+host);
            Date start = new Date();
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

            Date end = new Date();

            int exitStatus = channel.getExitStatus();

            if (exitStatus != 0){
                //TODO figure out how to get the output from failed ssh sessions
                logger.warn(String.format("failed with exit status %s",exitStatus));
                logger.warn(stdout);
            }
            return new SSHResponse(command, host, stdout, stderr, exitStatus, start, end);

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
        String host = getHostFromIpType(selectedIpType, node);
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

    public ArrayList<CompletableFuture<SSHResponse>> streamRollingDeployment(String missionName, Executor executor, Executor executorPool) {
        ArrayList<CompletableFuture<SSHResponse>> outputStreamFutures = new ArrayList();

        Runnable task = () -> {
            if (missions.get(missionName) == null) {
                throw new RuntimeException("Mission does not exist");
            }

            LanderMission mission = missions.get(missionName);

            streamExecuteRollingDeployment(mission, outputStreamFutures, executor, executorPool);
        };

        Thread thread = new Thread(task);
        //thread.setDaemon(true);
        thread.start();
        return outputStreamFutures;
    }


    public void streamExecuteRollingDeployment(LanderMission mission, ArrayList<CompletableFuture<SSHResponse>> outputStreamFutures, Executor executor, Executor executorPool){
        if (mission.getSequences().isEmpty()){
            throw new RuntimeException("Mission has no sequences");
        }
        List<LanderSequence> sequences = mission.getSequences();

        CompletableFuture<SSHResponse> sequenceSSHFuture = new CompletableFuture<>().supplyAsync(() -> {
            return null;
        });
        for (LanderSequence sequence : sequences) {
            logger.info("Attempting to run sequence: " + sequence.getName() + " mission " + mission.getMissionName());

            List<String> commands = sequence.getCommands();

            String expectedResponse = sequence.getExpectedResponse();
            LanderSequence.ConcurrencyType concurrencyType = sequence.getConcurrencyType();

            //TODO: think about if and how to do retries (idempotent flag on the sequence?)
            //TODO: use mustache to include node variables
            //TODO: stick success and failiure on the cluster rather than collecting
            sequenceSSHFuture= streamSSHAll(commands, cluster, concurrencyType, outputStreamFutures, sequenceSSHFuture, executor, executorPool);
        }
    }

    private CompletableFuture<SSHResponse> streamSSHAll(
            List<String> commands,
            List<CassandraNode> cluster,
            LanderSequence.ConcurrencyType concurrencyType,
            ArrayList<CompletableFuture<SSHResponse>> outputStreamFutures,
            CompletableFuture<SSHResponse> sequenceSSHFuture,
            Executor executor,
            Executor executorPool) {
        if (cluster == null) {
            throw new RuntimeException("No cluster has been connected");
        }

        if (concurrencyType == null){
            concurrencyType = LanderSequence.ConcurrencyType.NODE;
        }

        if (concurrencyType == LanderSequence.ConcurrencyType.NODE) {
            CompletableFuture<SSHResponse> commandSSHFuture;
            for (CassandraNode node : cluster) {
                for (String command : commands) {
                    commandSSHFuture = sequenceSSHFuture.thenApplyAsync((response) -> {
                        return ssh(
                                command,
                                getHostFromIpType(selectedIpType, node)
                        );
                    }, executor);

                    outputStreamFutures.add(commandSSHFuture);
                    sequenceSSHFuture = commandSSHFuture;
                }
            }
        }
        else {
            CompletableFuture<SSHResponse> commandSSHFuture = null;
            ArrayList<CompletableFuture<SSHResponse>> futuresToBeCombined = new ArrayList<CompletableFuture<SSHResponse>>();

            Map<Object, CompletableFuture<SSHResponse>> groupingFutureMap= new HashMap();
            for (String command : commands) {
                for (CassandraNode node : cluster) {
                    Object grouping;
                   switch (concurrencyType) {
                       case CLUSTER:
                            grouping = node;
                            break;
                        case RACK:
                        case DC:
                        default:
                            throw new IllegalStateException("Unexpected concurrency type: " + concurrencyType);
                    }
                    if (groupingFutureMap.get(grouping) == null){
                        CompletableFuture<SSHResponse> future = sequenceSSHFuture.thenApplyAsync((response) -> {
                            return null;
                        });
                        groupingFutureMap.put(grouping, future);
                    }
                    commandSSHFuture = groupingFutureMap.get(grouping).thenApplyAsync((response) -> {
                        return ssh(
                                command,
                                getHostFromIpType(selectedIpType, node)
                        );
                    }, executorPool);

                    //outputStreamFutures.add(outputStreamFuture);
                    futuresToBeCombined.add(commandSSHFuture);
                    outputStreamFutures.add(commandSSHFuture);
                    //sequenceSSHFuture = commandSSHFuture;

                    groupingFutureMap.put(grouping, commandSSHFuture);
                }
            }

            CompletableFuture<SSHResponse>[] futuresArray = futuresToBeCombined.toArray(new CompletableFuture[0]);
            //TODO: find a better way to return CompletableFuture<SSHResponse> that doesn't involve this gnarly workaround of adding a noop thenApplyAsync
            CompletableFuture<SSHResponse> combinedFuture
                    = CompletableFuture.allOf(futuresArray).thenApplyAsync((response) -> null);
            sequenceSSHFuture = combinedFuture;

        }
        return sequenceSSHFuture;
    }

    public int getStages(String missionName, LanderResource.DeploymentType deploymentType) {
        //TODO: change this based on parallelism?
        switch (deploymentType) {
            case CANARY:
                return getSequencesFromMission(missionName).stream().map(x -> x.getCommands().size()).reduce(0, (a, b) -> a + b);
            case ROLLING:
                return cluster.size() * getSequencesFromMission(missionName).stream().map(x -> x.getCommands().size()).reduce(0, (a, b) -> a + b);
            default:
                throw new IllegalStateException("Unexpected deployment type: " + deploymentType);
        }
        //return cluster.size();
    }
}

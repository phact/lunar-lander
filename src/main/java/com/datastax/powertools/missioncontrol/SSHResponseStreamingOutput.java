package com.datastax.powertools.missioncontrol;

import com.datastax.powertools.LanderResource;
import com.datastax.powertools.util.ThreadFactoryWithName;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public class SSHResponseStreamingOutput implements StreamingOutput {
    private final String missionName;
    private final LanderResource.DeploymentType deploymentType;
    private MissionControlManager missionControlManager;

    private static final Logger logger = Logger.getLogger(SSHResponseStreamingOutput.class);

    ThreadPoolExecutor executorPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    Executor executor = Executors.newSingleThreadExecutor(new ThreadFactoryWithName("lander-worker"));

    private ObjectMapper mapper = new ObjectMapper();

    public SSHResponseStreamingOutput(String missionName, MissionControlManager mcm, LanderResource.DeploymentType deploymentType) {
        this.missionControlManager = mcm;
        this.missionName = missionName;
        this.deploymentType = deploymentType;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException, WebApplicationException {
        ArrayList<CompletableFuture<SSHResponse>> outputStreamFutureList = null;
        switch(deploymentType) {
            case CANARY:
                outputStreamFutureList = missionControlManager.canaryDeployment(missionName, executor, executorPool);
                break;
            case ROLLING:
                outputStreamFutureList = missionControlManager.streamRollingDeployment(missionName, executor, executorPool);
                break;
            default:
                throw new IllegalStateException("Unexpected deployment type: " + deploymentType);
        }

        Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream));

        int targetCount = missionControlManager.getStages(missionName, deploymentType);
        while(outputStreamFutureList.size() < targetCount) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        AtomicInteger completedCount = new AtomicInteger(0);

        for (CompletableFuture<SSHResponse> sshResponseFuture: outputStreamFutureList) {
            sshResponseFuture.thenApplyAsync(sshResponse -> {
                String sshResponseString = null;
                synchronized (writer) {
                    try {
                        sshResponseString = mapper.writeValueAsString(sshResponse);
                        writer.write(sshResponseString);
                        writer.write("\n\n");
                        writer.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                logger.warn("writing sshresult to the stream: " + sshResponseString);
                completedCount.getAndIncrement();

                return sshResponseString;
            }, executor);
        }

        while (completedCount.get() < targetCount)
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
}


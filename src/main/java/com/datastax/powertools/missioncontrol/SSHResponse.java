package com.datastax.powertools.missioncontrol;

public class SSHResponse {
    String command;
    String host;
    String stdout;
    String stderr;
    int statusCode;

    public String getStderr() {
        return stderr;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public SSHResponse(String command, String host, String stdout, String stderr, int exitStatus) {
        this.command = command;
        this.host = host;
        this.stdout = stdout;
        this.stderr = stderr;
        this.statusCode= exitStatus;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getStdout() {
        return stdout;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

}

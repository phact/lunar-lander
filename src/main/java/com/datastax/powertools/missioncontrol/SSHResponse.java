package com.datastax.powertools.missioncontrol;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class SSHResponse {
    String command;
    String host;
    String stderr;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    Date start;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    Date end;

    int statusCode;
    String stdout;

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }


    public String getStderr() {
        return stderr;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public SSHResponse(String command, String host, String stdout, String stderr, int exitStatus, Date start, Date end) {
        this.command = command;
        this.host = host;
        this.stdout = stdout;
        this.stderr = stderr;
        this.statusCode= exitStatus;
        this.start = start;
        this.end = end;
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

package com.datastax.powertools.api;

import java.util.List;

public class LanderSequence {

    private String name;


    private String expectedResponse;

    private List<String> commands;
    private SequenceType sequenceType;

    public String getExpectedResponse() {
        return expectedResponse;
    }

    public void setExpectedResponse(String expectedResponse) {
        this.expectedResponse = expectedResponse;
    }

    public SequenceType getSequenceType() {
        return sequenceType;
    }

    public void setSequenceType(SequenceType sequenceType) {
        this.sequenceType = sequenceType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public String getName() {
        return this.name;
    }

    public enum SequenceType {
        FIRE_AND_FORGET,
        POLL_AND_VERIFY
    }
}

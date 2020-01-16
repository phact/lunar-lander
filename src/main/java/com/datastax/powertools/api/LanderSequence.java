package com.datastax.powertools.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

public class LanderSequence {

    private String name;

    @JsonIgnoreProperties(ignoreUnknown = true)
    private String expectedResponse;

    private List<String> commands;

    public String getExpectedResponse() {
        return expectedResponse;
    }

    public void setExpectedResponse(String expectedResponse) {
        this.expectedResponse = expectedResponse;
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
        DEFAULT,
        FIRE_AND_FORGET
    }
}

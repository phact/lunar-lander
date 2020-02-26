package com.datastax.powertools.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

public class LanderSequence {

    private String name;

    @JsonIgnoreProperties(ignoreUnknown = true)
    private String expectedResponse;

    private List<String> commands;

    @JsonIgnoreProperties(ignoreUnknown = true)
    private ConcurrencyType concurrencyType;


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

    public ConcurrencyType getConcurrencyType() {
        return concurrencyType;
    }

    public void setConcurrencyType(ConcurrencyType concurrencyType) {
        this.concurrencyType = concurrencyType;
    }

    // TODO: consider supporting node per rack, node per dc, rack per dc, rack per cluster?
    public enum ConcurrencyType {
        NODE,
        RACK,
        DC,
        CLUSTER
    }
}

package com.datastax.powertools.api;

import java.util.List;

public class LanderMission {
    private String missionName;
    private List<LanderSequence> sequences;

    public LanderMission(String missionName, List<LanderSequence> sequences) {
        this.missionName = missionName;
        this.sequences = sequences;
    }

    public LanderMission() {
    }

    public void setMissionName(String missionName) {
        this.missionName = missionName;
    }

    public String getMissionName() {
        return missionName;
    }

    public List<LanderSequence> getSequences() {
        return sequences;
    }
}

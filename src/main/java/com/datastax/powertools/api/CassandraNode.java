package com.datastax.powertools.api;

import java.net.InetSocketAddress;
import java.util.Optional;

public class CassandraNode {

    private Optional<InetSocketAddress> broadcastRpcAddress;
    private Optional<InetSocketAddress> broadcastAddress;
    private Optional<InetSocketAddress> listenAddress;
    private String privateKey;
    private String sshUser;
    private String datacenter;
    private String rack;

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public void setSshUser(String sshUser) {
        this.sshUser = sshUser;
    }


    public void setBroadcastRpcAddress(Optional<InetSocketAddress> broadcastRpcAddress) {
        this.broadcastRpcAddress = broadcastRpcAddress;
    }

    public void setBroadcastAddress(Optional<InetSocketAddress> broadcastAddress) {
        this.broadcastAddress = broadcastAddress;
    }

    public void setListenAddress(Optional<InetSocketAddress> listenAddress) {
        this.listenAddress = listenAddress;
    }

    public Optional<InetSocketAddress> getListenAddress() {
        return listenAddress;
    }

    public Optional<InetSocketAddress> getBroadcastAddress() {
        return broadcastAddress;
    }

    public Optional<InetSocketAddress> getBroadcastRpcAddress() {
        return broadcastRpcAddress;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getSshUser() {
        return sshUser;
    }

    public void setDatacenter(String datacenter) {
        this.datacenter = datacenter;
    }

    public String getDatacenter() {
        return datacenter;
    }

    public void setRack(String rack) {
         this.rack = rack;
    }

    public String getRack() {
        return rack;
    }
}

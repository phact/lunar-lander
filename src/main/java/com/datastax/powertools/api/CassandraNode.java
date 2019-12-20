package com.datastax.powertools.api;

import java.net.InetSocketAddress;
import java.util.Optional;

public class CassandraNode {

    private Optional<InetSocketAddress> broadcastRpcAddress;
    private Optional<InetSocketAddress> broadcastAddress;
    private Optional<InetSocketAddress> listenAddress;

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
}

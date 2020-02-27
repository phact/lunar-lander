package com.datastax.powertools.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.context.DriverContext;
import com.datastax.oss.driver.api.core.metadata.Node;
import com.datastax.oss.driver.api.core.session.ProgrammaticArguments;
import com.datastax.oss.driver.internal.core.context.DefaultDriverContext;
import com.datastax.oss.driver.internal.core.context.DefaultNettyOptions;
import com.datastax.oss.driver.internal.core.context.InternalDriverContext;
import com.datastax.oss.driver.internal.core.context.NettyOptions;
import com.datastax.powertools.api.CassandraNode;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.net.InetSocketAddress;
import java.security.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class CassandraManager {

    private CqlSession session;
    private Logger logger = Logger.getLogger(CassandraManager.class);

    // Needed for https://datastax-oss.atlassian.net/projects/JAVA/issues/JAVA-2624
    class CustomNettyOptions extends DefaultNettyOptions {
        public CustomNettyOptions(InternalDriverContext context) {
            super(context);
        }

        @Override
        public void afterBootstrapInitialized(Bootstrap bootstrap) {
            super.afterBootstrapInitialized(bootstrap);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000);
        }
    }

    class CustomContext extends DefaultDriverContext {
        public CustomContext(DriverConfigLoader configLoader, ProgrammaticArguments programmaticArguments) {
            super(configLoader, programmaticArguments);
        }

        @Override
        protected NettyOptions buildNettyOptions() {
            return new CustomNettyOptions(this);
        }
    }

    class CustomSessionBuilder extends CqlSessionBuilder {
        @Override
        protected DriverContext buildContext(DriverConfigLoader configLoader, ProgrammaticArguments programmaticArguments) {
            return new CustomContext(configLoader, programmaticArguments);
        }
    }


    public List connect(CassandraClusterConfiguration config) {
        String[] contactPointArray = config.getContactPoints().split(",");
        CqlSessionBuilder builder = new CustomSessionBuilder();
        for (String contactPoint : contactPointArray) {
            builder = builder.addContactPoint(InetSocketAddress.createUnresolved(
                    contactPoint,
                    config.getCqlPort()));
        }

        // TODO: didn't work - trying to fix native image
        // https://docs.datastax.com/en/developer/java-driver/4.2/manual/osgi/
        // https://github.com/oracle/graal/blob/master/substratevm/REFLECTION.md
        /*
        builder = builder
                .withClassLoader(CqlSession.class.getClassLoader());

         */

        if (config.getCqlUserName() != null) {
            builder = builder.withAuthCredentials(config.getCqlUserName(), config.getCqlPassword());
        }
        if (config.getLocalDC() != null) {
            builder = builder.withLocalDatacenter(config.getLocalDC());
        }
        if (config.getSslKsPass() != null && config.getSslKsPath() != null) {
            builder = builder.withSslContext(createSSLOptions(config.getSslTsPath(), config.getSslTsPass(), config.getSslKsPath(), config.getSslKsPass()));

            logger.info("Enabling SSL with sskKsPath=" + config.getSslKsPath());
            throw new RuntimeException("not implemented");
        }

        session = builder.build();

        Map<UUID, Node> nodes = session.getMetadata().getNodes();
        List cassandraNodeList = new ArrayList();
        for (Node node: nodes.values()) {
            CassandraNode cassandraNode = new CassandraNode();
            cassandraNode.setListenAddress(node.getListenAddress());
            cassandraNode.setBroadcastAddress(node.getBroadcastAddress());
            cassandraNode.setBroadcastRpcAddress(node.getBroadcastRpcAddress());
            cassandraNode.setDatacenter(node.getDatacenter());
            cassandraNode.setRack(node.getRack());
            cassandraNodeList.add(cassandraNode);
        }
        return cassandraNodeList;
    }

    private SSLContext createSSLOptions(String truststorePath, String truststorePwd, String keystorePath, String keystorePwd) {
        try {
            TrustManagerFactory tmf = null;
        if (null != truststorePath) {
            KeyStore tks = null;
                tks = KeyStore.getInstance("JKS");
            tks.load(this.getClass().getResourceAsStream(truststorePath),
                    truststorePwd.toCharArray());
            tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(tks);
        }
        KeyManagerFactory kmf = null;
        if (null != keystorePath) {
            KeyStore kks = KeyStore.getInstance("JKS");
            kks.load(this.getClass().getResourceAsStream(keystorePath),
                    keystorePwd.toCharArray());
            kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(kks, keystorePwd.toCharArray());
        }
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf != null? kmf.getKeyManagers() : null,
                tmf != null ? tmf.getTrustManagers() : null,
                new SecureRandom());
        return sslContext;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}


package com.dumpcache.easyops.configserver.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dumpcache.easyops.configserver.service.util.MiscUtils;

public class ZKConfigService extends AbstractConfigServiceImpl implements ZKClient {
    private final static Logger LOGGER = LoggerFactory.getLogger(ZKConfigService.class);
    private CuratorFramework    client;
    private String              zkServer;
    private String              zkNamespace;

    public void setZkServer(String zkServer) {
        this.zkServer = zkServer;
    }

    public void setZkNamespace(String zkNamespace) {
        this.zkNamespace = zkNamespace;
    }

    @Override
    public void start() {
        client.start();

    }

    @Override
    public void close() {
        client.close();
    }

    @Override
    protected void internalInit() {
        if (StringUtils.isEmpty(zkServer) && StringUtils.isEmpty(zkNamespace)) {
            LOGGER.error("zkServer address is  null !!!");
        }
        client = CuratorFrameworkFactory.builder().connectString(zkServer).namespace(zkNamespace)
                .retryPolicy(new RetryNTimes(Integer.MAX_VALUE, 1000)).connectionTimeoutMs(5000)
                .build();
        start();
    }

    @Override
    protected String getConfigFromRemote(final String namespace, final String app,
                                         final String key) {
        try {
            if (client.checkExists().forPath(MiscUtils.configRootPath()) == null
                    || client.checkExists()
                            .forPath(MiscUtils.configNamespacePath(namespace)) == null
                    || client.checkExists().forPath(MiscUtils.appPath(namespace, app)) == null
                    || client.checkExists()
                            .forPath(MiscUtils.keyPath(namespace, app, key)) == null) {
                LOGGER.error(
                        "the path is not exists in zk:" + MiscUtils.keyPath(namespace, app, key));
                return null;
            }
            return new String(client.getData().usingWatcher(new CuratorWatcher() {

                @Override
                public void process(WatchedEvent event) throws Exception {
                    LOGGER.debug("watch is called:" + event.getPath() + ",event_type:"
                            + event.getType().name());
                    if (event.getType() == EventType.NodeDataChanged) {
                        String config = new String(client.getData().usingWatcher(this)
                                .forPath(MiscUtils.keyPath(namespace, app, key)));
                        if (config != null) {
                            configMap.put(MiscUtils.keyPath(namespace, app, key), config);
                            return;
                        }
                    }

                    if (event.getType() == EventType.NodeDeleted) {
                        configMap.remove(MiscUtils.keyPath(namespace, app, key));
                        return;
                    }

                    if (event.getType() == EventType.NodeCreated) {
                        String config = new String(client.getData().usingWatcher(this)
                                .forPath(MiscUtils.keyPath(namespace, app, key)));
                        if (config != null) {
                            configMap.put(MiscUtils.keyPath(namespace, app, key), config);
                            return;
                        }
                    }

                    LOGGER.debug("modify key is:" + key + "new value is:"
                            + configMap.get(MiscUtils.keyPath(namespace, app, key)));
                }
            }).forPath(MiscUtils.keyPath(namespace, app, key)));
        } catch (Exception e) {
            LOGGER.error("recv data from zk error: ", e);
        }
        return null;
    }

    @Override
    protected void saveConfigToRemote(String namespace, String app, String key, String val) {
        try {
            if (client.checkExists().forPath(MiscUtils.configRootPath()) == null)
                client.create().withMode(CreateMode.PERSISTENT).forPath(MiscUtils.configRootPath());

            if (client.checkExists().forPath(MiscUtils.configNamespacePath(namespace)) == null) {
                client.create().withMode(CreateMode.PERSISTENT)
                        .forPath(MiscUtils.configNamespacePath(namespace));
            }
            if (client.checkExists().forPath(MiscUtils.appPath(namespace, app)) == null) {
                client.create().withMode(CreateMode.PERSISTENT)
                        .forPath(MiscUtils.appPath(namespace, app));
            }
            if (client.checkExists().forPath(MiscUtils.keyPath(namespace, app, key)) == null) {
                client.create().withMode(CreateMode.PERSISTENT)
                        .forPath(MiscUtils.keyPath(namespace, app, key));
            }
            client.setData().forPath(MiscUtils.keyPath(namespace, app, key), val.getBytes());
        } catch (Exception ex) {
            LOGGER.error("recv data from zk error: ", ex);
            throw new RuntimeException(ex);
        }

    }

    @Override
    protected void deleteRemoteConfig(String namespace, String app, String key) {
        try {
            if (client.checkExists().forPath(MiscUtils.keyPath(namespace, app, key)) != null) {
                client.delete().forPath(MiscUtils.keyPath(namespace, app, key));
            }
            configMap.remove(MiscUtils.keyPath(namespace, app, key));
        } catch (Exception e) {
            LOGGER.error("recv data from zk error: ", e);
            throw new RuntimeException(e);
        }

    }

    @Override
    protected String getConfigTreeFromRemote(String namespace, String app) {
        StringBuilder sb = new StringBuilder();
        sb.append("namespase:" + namespace).append("\n");
        sb.append(" |=>app:" + app).append("\n");
        try {
            List<String> ks = client.getChildren().forPath(MiscUtils.appPath(namespace, app));

            if (ks != null) {
                for (String k : ks) {
                    sb.append("  |=>key:" + k + ",val:"
                            + new String(
                                    client.getData().forPath(MiscUtils.keyPath(namespace, app, k))))
                            .append("\n");
                }
            }
        } catch (Exception e) {
            LOGGER.error("recv data from zk error: ", e);
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    @Override
    public List<Config> listAllConfigs(int start, int count) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getAllConfigsCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<Config> listConfigs(String namespace, String app, int start, int count) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getConfigsCount(String namespace, String app) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<String> getAllNamespaces() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getAllApps() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Config getConfigById(int id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteConfigById(int id) {
        // TODO Auto-generated method stub

    }

}

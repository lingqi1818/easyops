package com.dumpcache.easyops.configserver.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MysqlConfigService extends AbstractConfigServiceImpl {
    private final static Logger LOGGER = LoggerFactory.getLogger(MysqlConfigService.class);
    private DataSource          dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected void internalInit() {
        //nothing
    }

    @Override
    protected String getConfigFromRemote(String namespace, String app, String key) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement pst = conn.prepareStatement(
                    "select val from kv_config where namespace=? and app=? and config_key=?");
            pst.setString(1, namespace);
            pst.setString(2, app);
            pst.setString(3, key);
            ResultSet result = pst.executeQuery();
            if (result != null && result.next()) {
                return result.getString(1);
            }
        } catch (Exception ex) {
            LOGGER.error("get data from mysql error:", ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("close connection error:", e);
                }
            }
        }
        return null;
    }

    @Override
    protected void saveConfigToRemote(String namespace, String app, String key, String val) {
        Connection conn = null;
        String sql;
        boolean update = false;
        try {
            conn = dataSource.getConnection();
            if (getConfigFromRemote(namespace, app, key) == null) {
                sql = "insert into kv_config(namespace,app,config_key,val,gmt_created,gmt_modified) values(?,?,?,?,?,?)";
            } else {
                sql = "update kv_config set val=? where namespace=? and app=? and config_key=? and gmt_modified=?";
                update = true;
            }

            PreparedStatement pst = conn.prepareStatement(sql);
            if (!update) {
                pst.setString(1, namespace);
                pst.setString(2, app);
                pst.setString(3, key);
                pst.setString(4, val);
                pst.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                pst.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            } else {
                pst.setString(1, val);
                pst.setString(2, namespace);
                pst.setString(3, app);
                pst.setString(4, key);
                pst.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            }
            pst.execute();

        } catch (Exception ex) {
            LOGGER.error("get data from mysql error:", ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("close connection error:", e);
                }
            }
        }
    }

    @Override
    protected void deleteRemoteConfig(String namespace, String app, String key) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement pst = conn.prepareStatement(
                    "delete from  kv_config where namespace=? and app=? and config_key=?");
            pst.setString(1, namespace);
            pst.setString(2, app);
            pst.setString(3, key);
            pst.execute();
        } catch (Exception ex) {
            LOGGER.error("get data from mysql error:", ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("close connection error:", e);
                }
            }
        }
    }

    @Override
    protected String getConfigTreeFromRemote(String namespace, String app) {
        StringBuilder sb = new StringBuilder();
        Connection conn = null;
        sb.append("namespase:" + namespace).append("\n");
        sb.append(" |=>app:" + app).append("\n");
        try {
            conn = dataSource.getConnection();
            PreparedStatement pst = conn.prepareStatement(
                    "select config_key,val from kv_config where namespace=? and app=?");
            pst.setString(1, namespace);
            pst.setString(2, app);
            ResultSet result = pst.executeQuery();
            while (result.next()) {
                sb.append("  |=>key:" + result.getString(1) + ",val:" + result.getString(2))
                        .append("\n");
            }

        } catch (Exception e) {
            LOGGER.error("recv data from mysql error: ", e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("close connection error:", e);
                }
            }
        }
        return sb.toString();
    }

    @Override
    public List<Config> listAllConfigs(int start, int count) {
        Connection conn = null;
        List<Config> clist = new ArrayList<Config>();
        try {
            conn = dataSource.getConnection();
            PreparedStatement pst = conn.prepareStatement(
                    "select id,namespace,app,config_key,val,gmt_created,gmt_modified from kv_config order by id desc limit "
                            + start + "," + count);
            ResultSet result = pst.executeQuery();
            if (result != null) {
                while (result.next()) {
                    Config c = new Config();
                    c.setId(result.getInt(1));
                    c.setNamespace(result.getString(2));
                    c.setApp(result.getString(3));
                    c.setKey(result.getString(4));
                    c.setValue(result.getString(5));
                    c.setGmtCreated(result.getTimestamp(6));
                    c.setGmtModified(result.getTimestamp(7));
                    clist.add(c);
                }
            }
        } catch (Exception ex) {
            LOGGER.error("get data from mysql error:", ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("close connection error:", e);
                }
            }
        }
        return clist;
    }

    @Override
    public int getAllConfigsCount() {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement pst = conn.prepareStatement("select count(*) from kv_config");
            ResultSet result = pst.executeQuery();
            if (result != null && result.next()) {
                return result.getInt(1);
            }
        } catch (Exception ex) {
            LOGGER.error("get data from mysql error:", ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("close connection error:", e);
                }
            }
        }
        return 0;
    }

    @Override
    public List<Config> listConfigs(String namespace, String app, int start, int count) {
        Connection conn = null;
        List<Config> clist = new ArrayList<Config>();
        try {
            conn = dataSource.getConnection();
            PreparedStatement pst = conn.prepareStatement(
                    "select id,namespace,app,config_key,val,gmt_created,gmt_modified from kv_config where namespace=? and app=? order by id desc limit "
                            + start + "," + count);
            pst.setString(1, namespace);
            pst.setString(2, app);
            ResultSet result = pst.executeQuery();
            if (result != null) {
                while (result.next()) {
                    Config c = new Config();
                    c.setId(result.getInt(1));
                    c.setNamespace(result.getString(2));
                    c.setApp(result.getString(3));
                    c.setKey(result.getString(4));
                    c.setValue(result.getString(5));
                    c.setGmtCreated(result.getTimestamp(6));
                    c.setGmtModified(result.getTimestamp(7));
                    clist.add(c);
                }
            }
        } catch (Exception ex) {
            LOGGER.error("get data from mysql error:", ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("close connection error:", e);
                }
            }
        }
        return clist;
    }

    @Override
    public int getConfigsCount(String namespace, String app) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement pst = conn
                    .prepareStatement("select count(*) from kv_config where namespace=? and app=?");
            pst.setString(1, namespace);
            pst.setString(2, app);
            ResultSet result = pst.executeQuery();
            if (result != null && result.next()) {
                return result.getInt(1);
            }
        } catch (Exception ex) {
            LOGGER.error("get data from mysql error:", ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("close connection error:", e);
                }
            }
        }
        return 0;
    }

    @Override
    public List<String> getAllNamespaces() {
        Connection conn = null;
        List<String> list = new ArrayList<String>();
        try {
            conn = dataSource.getConnection();
            PreparedStatement pst = conn
                    .prepareStatement("select distinct namespace from kv_config ");
            ResultSet result = pst.executeQuery();
            if (result != null) {
                while (result.next()) {
                    list.add(result.getString(1));
                }
            }
        } catch (Exception ex) {
            LOGGER.error("get data from mysql error:", ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("close connection error:", e);
                }
            }
        }
        return list;
    }

    @Override
    public List<String> getAllApps() {
        Connection conn = null;
        List<String> list = new ArrayList<String>();
        try {
            conn = dataSource.getConnection();
            PreparedStatement pst = conn.prepareStatement("select distinct app from kv_config ");
            ResultSet result = pst.executeQuery();
            if (result != null) {
                while (result.next()) {
                    list.add(result.getString(1));
                }
            }
        } catch (Exception ex) {
            LOGGER.error("get data from mysql error:", ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("close connection error:", e);
                }
            }
        }
        return list;
    }

    @Override
    public Config getConfigById(int id) {
        Connection conn = null;
        Config c = new Config();
        try {
            conn = dataSource.getConnection();
            PreparedStatement pst = conn.prepareStatement(
                    "select id,namespace,app,config_key,val,gmt_created,gmt_modified from kv_config where id=?");
            pst.setInt(1, id);
            ResultSet result = pst.executeQuery();
            if (result != null && result.next()) {

                c.setId(result.getInt(1));
                c.setNamespace(result.getString(2));
                c.setApp(result.getString(3));
                c.setKey(result.getString(4));
                c.setValue(result.getString(5));
                c.setGmtCreated(result.getDate(6));
                c.setGmtModified(result.getDate(7));

            }
        } catch (Exception ex) {
            LOGGER.error("get data from mysql error:", ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("close connection error:", e);
                }
            }
        }
        return c;
    }

    @Override
    public void deleteConfigById(int id) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement pst = conn.prepareStatement("delete from  kv_config where id=?");
            pst.setInt(1, id);

            pst.execute();
        } catch (Exception ex) {
            LOGGER.error("get data from mysql error:", ex);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("close connection error:", e);
                }
            }
        }
    }

}

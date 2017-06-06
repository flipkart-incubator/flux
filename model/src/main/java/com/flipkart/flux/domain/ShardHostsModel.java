package com.flipkart.flux.domain;

import sun.net.util.IPAddressUtil;

import java.util.List;

/**
 * Created by amitkumar.o on 05/06/17.
 */
public class ShardHostsModel {
    private String master ;
    private String slave ;
    private List<Character> shardKeys;

    public ShardHostsModel(String master, String slave, List<Character> shardKeys) {
        this.master = master;
        this.slave = slave;
        this.shardKeys = shardKeys;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String getSlave() {
        return slave;
    }

    public void setSlave(String slave) {
        this.slave = slave;
    }

    public List<Character> getShardKeys() {
        return shardKeys;
    }

    public void setShardKeys(List<Character> shardKeys) {
        this.shardKeys = shardKeys;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShardHostsModel)) return false;

        ShardHostsModel that = (ShardHostsModel) o;

        if (!getMaster().equals(that.getMaster())) return false;
        if (!getSlave().equals(that.getSlave())) return false;
        return getShardKeys().equals(that.getShardKeys());

    }

    @Override
    public int hashCode() {
        int result = getMaster().hashCode();
        result = 31 * result + getSlave().hashCode();
        result = 31 * result + getShardKeys().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ShardHostsModel{" +
                "master='" + master + '\'' +
                ", slave='" + slave + '\'' +
                ", shardKeys=" + shardKeys +
                '}';
    }
}

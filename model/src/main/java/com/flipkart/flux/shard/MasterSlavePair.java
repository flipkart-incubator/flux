package com.flipkart.flux.shard;

/**
 * Created by amitkumar.o on 19/06/17.
 */
public class MasterSlavePair {
    private ShardHostModel master ;
    private ShardHostModel slave;

    public MasterSlavePair(ShardHostModel master, ShardHostModel slave) {
        this.master = master;
        this.slave = slave;
    }

    public MasterSlavePair() {
    }

    public ShardHostModel getMaster() {
        return master;
    }

    public void setMaster(ShardHostModel master) {
        this.master = master;
    }

    public ShardHostModel getSlave() {
        return slave;
    }

    public void setSlave(ShardHostModel slave) {
        this.slave = slave;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MasterSlavePair)) return false;

        MasterSlavePair that = (MasterSlavePair) o;

        if (!getMaster().equals(that.getMaster())) return false;
        return getSlave().equals(that.getSlave());

    }

    @Override
    public int hashCode() {
        int result = getMaster().hashCode();
        result = 31 * result + getSlave().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "MasterSlavePair{" +
                "master=" + master.toString() +
                ", slave=" + slave.toString() +
                '}';
    }
}

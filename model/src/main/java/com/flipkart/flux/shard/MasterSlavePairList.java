package com.flipkart.flux.shard;

import java.util.List;

/**
 * Created by amitkumar.o on 19/06/17.
 */
public class MasterSlavePairList {
    private List<MasterSlavePair>  masterSlavePairList;

    public MasterSlavePairList(List<MasterSlavePair> masterSlavePairList) {
        this.masterSlavePairList = masterSlavePairList;
    }

    public MasterSlavePairList() {
    }

    public List<MasterSlavePair> getMasterSlavePairList() {
        return masterSlavePairList;
    }

    public void setMasterSlavePairList(List<MasterSlavePair> masterSlavePairList) {
        this.masterSlavePairList = masterSlavePairList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MasterSlavePairList)) return false;

        MasterSlavePairList that = (MasterSlavePairList) o;

        return getMasterSlavePairList().equals(that.getMasterSlavePairList());

    }

    @Override
    public int hashCode() {
        return getMasterSlavePairList().hashCode();
    }

    @Override
    public String toString() {
        return "MasterSlavePairList{" +
                "masterSlavePairList=" + masterSlavePairList +
                '}';
    }
}

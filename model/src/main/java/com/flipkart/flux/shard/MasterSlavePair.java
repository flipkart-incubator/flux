/*
 * Copyright 2012-2016, the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.flux.shard;

/**
 * Class that holds Master-Slave Pair for a particular shard.
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

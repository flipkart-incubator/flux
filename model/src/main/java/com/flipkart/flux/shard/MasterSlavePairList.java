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

import java.util.List;

/* Mapper Class to read the shard configuration from Config File as Json
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

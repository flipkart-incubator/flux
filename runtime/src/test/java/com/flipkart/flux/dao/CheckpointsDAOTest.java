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

package com.flipkart.flux.dao;

import com.flipkart.flux.dao.iface.CheckpointsDAO;
import com.flipkart.flux.domain.Checkpoint;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shyam.akirala
 */
public class CheckpointsDAOTest {

    @Test
    public void createCheckpointTest() {
        CheckpointsDAO checkpointsDAO = new CheckpointsDAOImpl();
        Map<String, Object> testMap = new HashMap<String, Object>();
        testMap.put("testKey", "testValue");
        Checkpoint checkpoint = new Checkpoint("test_state_machine_name", "test_state_machine_instance_id", 10L, testMap);
        checkpointsDAO.create(checkpoint);
    }

    @Test
    public void findCheckpointTest() {
        CheckpointsDAO checkpointsDAO = new CheckpointsDAOImpl();
        Checkpoint checkpoint = checkpointsDAO.find("test_state_machine_instance_id", 10L);
        System.out.println(checkpoint);
    }
}

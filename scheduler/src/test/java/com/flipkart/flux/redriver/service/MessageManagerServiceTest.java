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

package com.flipkart.flux.redriver.service;

import com.flipkart.flux.redriver.dao.MessageDao;
import com.flipkart.flux.redriver.model.SmIdAndTaskIdPairWithExecutionVersion;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MessageManagerServiceTest {
    private final String sampleMachineId = "test-machine-id";
    @Mock
    MessageDao messageDao;
    private MessageManagerService messageManagerService;

    @Before
    public void setup() {
        messageManagerService = new MessageManagerService(messageDao, 2, 500, 2, 500, 10);
        messageManagerService.initialize();
    }

    @Test
    public void testRemoval_shouldDeferRemoval() throws Exception {
        messageManagerService = new MessageManagerService(messageDao, 50, 500, 10, 500, 10);
        messageManagerService.initialize(); // Will be called by polyguice in the production env

        messageManagerService.scheduleForRemoval(sampleMachineId, 123l, 0l);
        messageManagerService.scheduleForRemoval(sampleMachineId, 123l, 1l);
        messageManagerService.scheduleForRemoval(sampleMachineId, 123l, 2l);

        verifyZeroInteractions(messageDao);

        Thread.sleep(700l);
        ArrayList firstBatch = new ArrayList<SmIdAndTaskIdPairWithExecutionVersion>();
        firstBatch.add(new SmIdAndTaskIdPairWithExecutionVersion(sampleMachineId, 123l,0l));
        firstBatch.add(new SmIdAndTaskIdPairWithExecutionVersion(sampleMachineId, 123l,1l));

        verify(messageDao,times(1)).deleteInBatch(firstBatch);

    }

    @Test
    public void testRemoval_shouldDeleteInBatches() throws Exception {
        messageManagerService = new MessageManagerService(messageDao, 50, 500, 2, 500, 10);
        messageManagerService.initialize(); // Will be called by polyguice in the production env

        messageManagerService.scheduleForRemoval(sampleMachineId, 121l,0l);
        messageManagerService.scheduleForRemoval(sampleMachineId, 122l,0l);
        messageManagerService.scheduleForRemoval(sampleMachineId, 123l,0l);

        verifyZeroInteractions(messageDao);

        Thread.sleep(700l);
        ArrayList firstBatch = new ArrayList<SmIdAndTaskIdPairWithExecutionVersion>();
        firstBatch.add(new SmIdAndTaskIdPairWithExecutionVersion(sampleMachineId, 121l,0l));
        firstBatch.add(new SmIdAndTaskIdPairWithExecutionVersion(sampleMachineId, 122l,0l));

        verify(messageDao,times(1)).deleteInBatch(firstBatch);

        Thread.sleep(700l);
        ArrayList secondBatch = new ArrayList<SmIdAndTaskIdPairWithExecutionVersion>();
        secondBatch.add(new SmIdAndTaskIdPairWithExecutionVersion(sampleMachineId, 123l,0l));
        verify(messageDao,times(1)).deleteInBatch(secondBatch);
    }
}
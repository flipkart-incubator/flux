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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import com.flipkart.flux.redriver.dao.MessageDao;
import com.flipkart.flux.redriver.model.SmIdAndTaskIdWithExecutionVersion;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
        messageManagerService.scheduleForRemoval(sampleMachineId, 123l, 0l);
        messageManagerService.scheduleForRemoval(sampleMachineId, 123l, 1l);
        messageManagerService.scheduleForRemoval(sampleMachineId, 123l, 2l);

        verifyZeroInteractions(messageDao);

        Thread.sleep(700l);
        ArrayList<SmIdAndTaskIdWithExecutionVersion> firstBatch = new ArrayList<SmIdAndTaskIdWithExecutionVersion>();
        firstBatch.add(new SmIdAndTaskIdWithExecutionVersion(sampleMachineId, 123l,0l));
        firstBatch.add(new SmIdAndTaskIdWithExecutionVersion(sampleMachineId, 123l,1l));

        verify(messageDao,times(1)).deleteInBatch(firstBatch);

    }

    @Test
    public void testRemoval_shouldDeleteInBatches() throws Exception {
        messageManagerService.scheduleForRemoval(sampleMachineId, 121l,0l);
        messageManagerService.scheduleForRemoval(sampleMachineId, 122l,0l);
        messageManagerService.scheduleForRemoval(sampleMachineId, 123l,0l);

        verifyZeroInteractions(messageDao);

        Thread.sleep(700l);
        ArrayList<SmIdAndTaskIdWithExecutionVersion> firstBatch = new ArrayList<SmIdAndTaskIdWithExecutionVersion>();
        firstBatch.add(new SmIdAndTaskIdWithExecutionVersion(sampleMachineId, 121l,0l));
        firstBatch.add(new SmIdAndTaskIdWithExecutionVersion(sampleMachineId, 122l,0l));

        verify(messageDao,times(1)).deleteInBatch(firstBatch);

        Thread.sleep(700l);
        ArrayList<SmIdAndTaskIdWithExecutionVersion> secondBatch = new ArrayList<SmIdAndTaskIdWithExecutionVersion>();
        secondBatch.add(new SmIdAndTaskIdWithExecutionVersion(sampleMachineId, 123l,0l));
        verify(messageDao,times(1)).deleteInBatch(secondBatch);
    }
}
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MessageManagerServiceTest {

    @Mock
    MessageDao messageDao;
    private MessageManagerService messageManagerService;

    @Before
    public void setup() {

    }

    @Test
    public void testRemoval_shouldDeferRemoval() throws Exception {
        messageManagerService = new MessageManagerService(messageDao, 2, 500, 10);
        messageManagerService.initialize(); // Will be called by polyguice in the production env

        messageManagerService.scheduleForRemoval(123l);
        messageManagerService.scheduleForRemoval(123l);
        messageManagerService.scheduleForRemoval(123l);

        verifyZeroInteractions(messageDao);

        Thread.sleep(700l);
        verify(messageDao,times(1)).deleteInBatch(anyListOf(Long.class));

    }

    @Test
    public void testRemoval_shouldDeleteInBatches() throws Exception {
        messageManagerService = new MessageManagerService(messageDao, 2, 500, 2);
        messageManagerService.initialize(); // Will be called by polyguice in the production env

        messageManagerService.scheduleForRemoval(121l);
        messageManagerService.scheduleForRemoval(122l);
        messageManagerService.scheduleForRemoval(123l);

        verifyZeroInteractions(messageDao);

        Thread.sleep(700l);
        verify(messageDao,times(1)).deleteInBatch(Arrays.asList(121l,122l));

        Thread.sleep(700l);
        verify(messageDao,times(1)).deleteInBatch(Arrays.asList(123l));
    }
}
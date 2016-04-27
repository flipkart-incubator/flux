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

package com.flipkart.flux.registry;

import java.util.Map;

/**
 * @author shyam.akirala
 */
public class FSMsRegistry {

    Map<SMRegistryKey, StateMachineRegistry> fsmsRegistry;

    public StateMachineRegistry getSMRegistry(String stateMachineName, Long version) {
        StateMachineRegistry smRegistry = fsmsRegistry.get(new SMRegistryKey(stateMachineName, version));
        return smRegistry;
    }

    public void putInRegistry(String stateMachineName, Long version, StateMachineRegistry stateMachineRegistry) {
        fsmsRegistry.put(new SMRegistryKey(stateMachineName, version), stateMachineRegistry);
    }

    class SMRegistryKey {

        String stateMachineName;

        Long version;

        public SMRegistryKey(String stateMachineName, Long version) {
            this.stateMachineName = stateMachineName;
            this.version = version;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SMRegistryKey)) return false;

            SMRegistryKey that = (SMRegistryKey) o;

            if (!stateMachineName.equals(that.stateMachineName)) return false;
            if (!version.equals(that.version)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = stateMachineName.hashCode();
            result = 31 * result + version.hashCode();
            return result;
        }
    }
}

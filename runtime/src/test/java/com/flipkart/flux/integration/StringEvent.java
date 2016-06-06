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

package com.flipkart.flux.integration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flipkart.flux.client.model.Event;

public class StringEvent implements Event {
    @JsonProperty
    private String aString;

    StringEvent() {
    }

    public StringEvent(String aString) {
        this.aString = aString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StringEvent that = (StringEvent) o;

        return !(aString != null ? !aString.equals(that.aString) : that.aString != null);

    }

    @Override
    public int hashCode() {
        return aString != null ? aString.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "StringEvent{" +
            "aString='" + aString + '\'' +
            '}';
    }
}

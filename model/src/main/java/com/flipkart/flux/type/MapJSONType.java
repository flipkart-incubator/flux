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

package com.flipkart.flux.type;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

/**
 * @author shyam.akirala
 */
public class MapJsonType extends BasicObjectType {
    protected static final ObjectMapper MAPPER = new ObjectMapper();
    @Override
    protected Object deSerialize(String value) throws IOException {
        return MAPPER.readValue(value, new TypeReference<Map<Object, Object>>() {});
    }

    @Override
    protected String serialize(Object value) throws JsonProcessingException {
        if(value == null)
            return "{}";

        return MAPPER.writeValueAsString(value);
    }
}

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
import com.fasterxml.jackson.databind.type.CollectionType;
import java.io.IOException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedList;
import java.util.List;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.util.SerializationHelper;
import org.hibernate.usertype.UserType;

/**
 * <code>ListJsonType</code> is a Hibernate {@link UserType} implementation to store {@link java.util.List} as json in DB
 * @author shyam.akirala
 */
public class ListJsonType<T> implements UserType, Serializable {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final CollectionType listType;

    public ListJsonType(Class<T> elementClass) {
        if(elementClass != Object.class) {
            this.listType = MAPPER.getTypeFactory().constructCollectionType(List.class, elementClass);
        } else {
            this.listType = null;
        }
    }

    @Override
    public int[] sqlTypes() {
        return new int[]{Types.JAVA_OBJECT};
    }

    @SuppressWarnings("rawtypes")
	@Override
    public Class returnedClass() {
        return List.class;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        if (x == null) {
            return (y == null);
        }

        return x.equals(y);
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        return x.hashCode();
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
        try {
            st.setString(index, serialize(value));
        } catch (JsonProcessingException e) {
            throw new SQLException("Cannot serialize object to JSON. Exception " + e.getMessage());
        }
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
        String value = rs.getString(names[0]);

        if (value == null) {
            return new LinkedList<T>();
        }

        try {
            return deSerialize(value);
        } catch (IOException e) {
            throw new SQLException("Cannot deserialize json string " + value + ". Exception " + e.getMessage());
        }
    }

    /** Performs deep copy of an object using serialization and de-serialization*/
    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return SerializationHelper.clone((Serializable) value);
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) this.deepCopy(value);
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return this.deepCopy(cached);
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }

    protected Object deSerialize(String value) throws IOException {
        Object obj;
        if(listType == null) {
            obj = MAPPER.readValue(value, new TypeReference<List<Object>>() {
            });
        }
        else {
            obj = MAPPER.readValue(value, listType);
        }
        return obj;
    }

    protected String serialize(Object value) throws JsonProcessingException {
        if(value == null)
            return null;

        return MAPPER.writeValueAsString(value);
    }
}
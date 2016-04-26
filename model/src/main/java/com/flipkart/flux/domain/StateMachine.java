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

package com.flipkart.flux.domain;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Set;

/**
 * <code>StateMachine</code> represents a state machine submitted for execution in Flux.
 * Maintains meta data about the current state of execution of a state machine
 *
 * @author Yogesh
 * @author regunath.balasubramanian
 * @author shyam.akirala
 * @author kartik.bommepally
 */
@Entity
@Table(name = "StateMachines")
public class StateMachine<T> {

    /** UUID to identify the state machine*/
    @Id @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    private String id;

    /* provided */
    /** The version identifier*/
    private Long version;
    /** Name for this state machine*/
    private String name;
    /** Description of the state machine*/
    private String description;

    /** List of states that this machine has*/
    @OneToMany(fetch = FetchType.EAGER, cascade=CascadeType.ALL, targetEntity = State.class)
    @JoinColumn(name = "stateMachineId")
    private Set<State<T>> states;

    /* maintained */
    /** Current states of this state machine*/
    @Transient
    private Set<State<T>> currentStates;

    /** The Context for interacting with the Flux runtime*/
    @Transient
    private Context<T> context;

    /** Time at which this State Machine has been created */
    private Timestamp createdAt;

    /** Time at which this State Machine has been last updated */
    private Timestamp updatedAt;


    /** Constructors*/
    protected StateMachine() {}
    public StateMachine(Long version, String name, String description, Set<State<T>> states) {
        super();
        this.version = version;
        this.name = name;
        this.description = description;
        this.states = states;
    }

    /** Accessor/Mutator methods */
    public String getId() {
        return id;
    }
    public Context<T> getContext() {
        return context;
    }
    public void setContext(Context<T> context) {
        this.context = context;
    }
    public Set<State<T>> getCurrentStates() {
        return currentStates;
    }
    public void setCurrentStates(Set<State<T>> currentStates) {
        this.currentStates = currentStates;
    }
    public Long getVersion() {
        return version;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public Set<State<T>> getStates() {
        return states;
    }
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        @SuppressWarnings("unchecked")
        StateMachine<T> other = (StateMachine<T>) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}

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
public class StateMachine {

    /** Unique identifier of the state machine*/
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* provided */
    /** The version identifier*/
    private Long version;
    /** Name for this state machine*/
    private String name;
    /** Description of the state machine*/
    private String description;

    /** User supplied correlationId. A user can post events for a state machine given this correlation id */
    private String correlationId;

    /** List of states that this machine has*/
    @OneToMany(fetch = FetchType.EAGER, cascade=CascadeType.ALL, targetEntity = State.class)
    @JoinColumn(name = "stateMachineId")
    private Set<State> states;

    /** Status of the state machine, denotes whether it is active or cancelled */
    @Enumerated(EnumType.STRING)
    private StateMachineStatus status;

    /* maintained */
    /** Current states of this state machine*/
    @Transient
    private Set<State> currentStates;

    /** The Context for interacting with the Flux runtime*/
    @Transient
    private Context context;

    /** Time at which this State Machine has been created */
    private Timestamp createdAt;

    /** Time at which this State Machine has been last updated */
    private Timestamp updatedAt;


    /** Constructors*/
    protected StateMachine() {}
    public StateMachine(Long version, String name, String description, Set<State> states, String correlationId) {
        super();
        this.version = version;
        this.name = name;
        this.description = description;
        this.states = states;
        this.correlationId = correlationId;
        this.status = StateMachineStatus.active;
    }

    /** Accessor/Mutator methods */
    public Long getId() {
        return id;
    }
    public Context getContext() {
        return context;
    }
    public void setContext(Context context) {
        this.context = context;
    }
    public Set<State> getCurrentStates() {
        return currentStates;
    }
    public void setCurrentStates(Set<State> currentStates) {
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
    public Set<State> getStates() {
        return states;
    }
    public StateMachineStatus getStatus() {
        return status;
    }
    public void setStatus(StateMachineStatus status) {
        this.status = status;
    }
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }
    public String getCorrelationId() {
        return correlationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StateMachine)) return false;

        StateMachine that = (StateMachine) o;

        if (context != null ? !context.equals(that.context) : that.context != null) return false;
        if (createdAt != null ? !createdAt.equals(that.createdAt) : that.createdAt != null) return false;
        if (currentStates != null ? !currentStates.equals(that.currentStates) : that.currentStates != null)
            return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (states != null ? !states.equals(that.states) : that.states != null) return false;
        if (updatedAt != null ? !updatedAt.equals(that.updatedAt) : that.updatedAt != null) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = version != null ? version.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (states != null ? states.hashCode() : 0);
        result = 31 * result + (currentStates != null ? currentStates.hashCode() : 0);
        result = 31 * result + (context != null ? context.hashCode() : 0);
        result = 31 * result + (createdAt != null ? createdAt.hashCode() : 0);
        result = 31 * result + (updatedAt != null ? updatedAt.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "StateMachine{" +
            "context=" + context +
            ", id='" + id + '\'' +
            ", version=" + version +
            ", name='" + name + '\'' +
            ", description='" + description + '\'' +
            ", states=" + states +
            ", currentStates=" + currentStates +
            ", createdAt=" + createdAt +
            ", updatedAt=" + updatedAt +
            '}';
    }
}

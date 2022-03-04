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
package com.flipkart.flux.persistence.dao.impl;

import java.util.Arrays;
import java.util.LinkedList;

import javax.inject.Inject;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.flipkart.flux.domain.State;
import com.flipkart.flux.domain.StateMachine;
import com.flipkart.flux.persistence.SessionFactoryContext;
import com.flipkart.flux.persistence.criteria.DependentEventCriteria;
import com.flipkart.flux.persistence.criteria.FSMStatusCriteria;
import com.flipkart.flux.persistence.dao.iface.StatesDAOV1;
import com.flipkart.flux.persistence.dto.StateUpdate;
import com.flipkart.flux.persistence.dto.StateUpdate.Field;
import com.flipkart.flux.persistence.key.EntityId;
import com.flipkart.flux.persistence.key.FSMIdEntityId;
import com.flipkart.flux.persistence.key.FSMIdStateIds;
import com.google.inject.name.Named;

/**
 * A Hibernate implementation of States DAO
 * @author regu.b
 *
 */

public class StatesDAOV1Impl extends AbstractDAO<State> implements StatesDAOV1{

    @Inject
    public StatesDAOV1Impl(@Named("fluxSessionFactoriesContext") SessionFactoryContext sessionFactoryContext) {
        super(sessionFactoryContext);
    }
	
	@Override
	public State create(State state) {
		return super.save(state);
	}

	@Override
	public void update(State object) {
		super.update(object);		
	}
	
	@Override
	public void remove(State entity) {
		throw new PersistenceException("Operation is not supported!");
	}

	@Override
	public State findEntity(Object key) {
		if (key instanceof FSMIdEntityId) {
			return this.findByCompositeId((FSMIdEntityId)key);
		}  else if (key instanceof EntityId) {
			return super.findById(State.class,((EntityId)key).entityId);	
		}
		throw new PersistenceException("Find State is not supported for : " + key);
	}

	@Override
	public State[] findEntities(Object key) {
		if (key instanceof FSMIdStateIds) {
			return this.findStatesForStateIds((FSMIdStateIds)key);
		} else if (key instanceof FSMStatusCriteria) {
			return this.findStatesByStatusCriteria((FSMStatusCriteria)key);
		}else if (key instanceof DependentEventCriteria) {
			return this.findStatesByDependentEvent((DependentEventCriteria)key);
		}
		throw new PersistenceException("Find StateS is not supported for key : " + key);
	}
	
	@Override
	public void updateState(Field field, Object updates) {
		switch(field) {
		case status:
			if (updates instanceof StateUpdate.StatusUpdate) {
				this.updateStatus((StateUpdate.StatusUpdate)updates);
				return;
			}
			break;
		case rollbackStatus:
			if (updates instanceof StateUpdate.RollbackStatusUpdate) {
				this.updateRollbackStatus((StateUpdate.RollbackStatusUpdate)updates);
				return;
			}
			break;
		case attemptedNoOfRetries:			
			if (updates instanceof StateUpdate.NoOfRetriesIncrement) {
				this.incrementRetryCount((StateUpdate.NoOfRetriesIncrement)updates);
				return;
			}
			break;
		case attemptedNumOfReplayableRetries:
			if (updates instanceof StateUpdate.ReplayableRetriesUpdate) {
				this.updateReplayableRetries((StateUpdate.ReplayableRetriesUpdate)updates);
				return;
			}
			break;
		case executionVersion:
			if (updates instanceof StateUpdate.ExecutionVersionUpdate) {
				this.updateExecutionVersion((StateUpdate.ExecutionVersionUpdate)updates);
				return;
			}
			break;
		default:
			throw new PersistenceException("Unable to update State using unsupported Field type : " + field);
		}
		throw new PersistenceException("Unable to update State using unsupported update object type : " + updates);
	}

	@Override
    public short getReplayableRetriesForUpdate(FSMIdEntityId fsmIdEntityId) {
    	CriteriaBuilder cb = currentSession().getCriteriaBuilder();
    	CriteriaQuery<State> cq = cb.createQuery(State.class);
    	Root<State> root = cq.from(State.class);
    	Predicate restrictions[] = new Predicate[] {
    		cb.equal(root.get("stateMachineId"), fsmIdEntityId.fsmId),
    		cb.equal(root.get("id"),fsmIdEntityId.entityId.entityId),
    	};    	
    	cq.select(root).where(restrictions);     	    	
    	return currentSession().createQuery(cq).setLockMode(LockModeType.PESSIMISTIC_WRITE).getSingleResult().getAttemptedNumOfReplayableRetries();
    	/*
        SQLQuery sqlQuery = currentSession().createSQLQuery(
            "select " + COLUMN_ATTEMPTED_NUM_OF_REPLAYABLE_RETRIES + " from States where"
                + " id = :stateId and stateMachineId = :stateMachineId "+ FOR_UPDATE);
        sqlQuery.setLong("stateId", stateId);
        sqlQuery.setString("stateMachineId",stateMachineId);
        return Short.valueOf(sqlQuery.uniqueResult().toString());
    	 */
    }
	
    private void updateStatus(StateUpdate.StatusUpdate statusUpdate) {
    	CriteriaBuilder cb = currentSession().getCriteriaBuilder();
    	CriteriaUpdate<State> cu = cb.createCriteriaUpdate(State.class);
    	Root<State> root = cu.from(State.class);
    	cu.set("status", statusUpdate.status);
    	Predicate restrictions[] = new Predicate[] {
			cb.equal(root.get("stateMachineId"), statusUpdate.fsmId.statemachineId),
			cb.in((root.get("id").in(Arrays.asList(statusUpdate.stateIds))))
		};
    	cu.where(restrictions);
    	currentSession().createQuery(cu).executeUpdate();    	
    	/*
        StringBuilder inClause = new StringBuilder();
        if (stateIds!=null && stateIds.length > 0) {
            inClause.append(" and id in (");
            for (Long stateId : stateIds) {
                inClause.append(stateId).append(",");
            }
        }
        inClause.deleteCharAt(inClause.length() - 1).append(")");
        Query query = currentSession().createQuery("update State set status = :status where stateMachineId = :stateMachineId".concat(inClause.toString()));
        query.setString("status", status != null ? status.toString() : null);
        query.setString("stateMachineId", fsmIdId.statemachineId);
        query.executeUpdate();
        */
    }
    
    private void updateRollbackStatus(StateUpdate.RollbackStatusUpdate rollbackStatusUpdate) {
    	CriteriaBuilder cb = currentSession().getCriteriaBuilder();
    	CriteriaUpdate<State> cu = cb.createCriteriaUpdate(State.class);
    	Root<State> root = cu.from(State.class);
    	cu.set("rollbackStatus", rollbackStatusUpdate.status);
    	Predicate restrictions[] = new Predicate[] {
			cb.equal(root.get("id"), rollbackStatusUpdate.fsmIdEntityId.entityId.entityId),
			cb.equal(root.get("stateMachineId"), rollbackStatusUpdate.fsmIdEntityId.fsmId.statemachineId),
    	};
    	cu.where(restrictions);
    	currentSession().createQuery(cu).executeUpdate();    	
    	/*
        Query query = currentSession().createQuery("update State set rollbackStatus = :rollbackStatus where id = :stateId and stateMachineId = :stateMachineId");
        query.setString("rollbackStatus", rollbackStatus != null ? rollbackStatus.toString() : null);
        query.setLong("stateId", stateId);
        query.setString("stateMachineId", fsmIdId.statemachineId);
        query.executeUpdate();
        */
    }  
    
    private void incrementRetryCount(StateUpdate.NoOfRetriesIncrement retriesIncrementUpdate) {
        Query query = currentSession().createNativeQuery("update State set attemptedNoOfRetries = attemptedNoOfRetries + 1 where id = :stateId and stateMachineId = :stateMachineId");
        query.setParameter("id", retriesIncrementUpdate.fsmIdEntityId.entityId.entityId);
        query.setParameter("stateMachineId", retriesIncrementUpdate.fsmIdEntityId.fsmId.statemachineId);
        query.executeUpdate();
        /*
        Query query = currentSession().createQuery("update State set attemptedNoOfRetries = attemptedNoOfRetries + 1 where id = :stateId and stateMachineId = :stateMachineId");
        query.setLong("stateId", stateId);
        query.setString("stateMachineId", fsmIdId.statemachineId);
        query.executeUpdate();         
         */
    }
    
    private void updateReplayableRetries(StateUpdate.ReplayableRetriesUpdate replayableRetriesUpdate) {
    	CriteriaBuilder cb = currentSession().getCriteriaBuilder();
    	CriteriaUpdate<State> cu = cb.createCriteriaUpdate(State.class);
    	Root<State> root = cu.from(State.class);
    	cu.set("attemptedNumOfReplayableRetries", replayableRetriesUpdate.retries);
    	Predicate restrictions[] = new Predicate[] {
			cb.equal(root.get("id"), replayableRetriesUpdate.fsmIdEntityId.entityId.entityId),
			cb.equal(root.get("stateMachineId"), replayableRetriesUpdate.fsmIdEntityId.fsmId.statemachineId),
    	};
    	cu.where(restrictions);
    	currentSession().createQuery(cu).executeUpdate();
    	/*
        Query query = currentSession().createQuery("update State set attemptedNumOfReplayableRetries  = :attemptedNumOfReplayableRetries where id  = :stateId and stateMachineId  = :stateMachineId");
        query.setString("stateMachineId", fsmIdId.statemachineId);
        query.setLong("stateId", stateId);
        query.setShort("attemptedNumOfReplayableRetries", replayableRetries);
        query.executeUpdate();
        */
    }    
	
    private void updateExecutionVersion(StateUpdate.ExecutionVersionUpdate executionVersionUpdate) {
    	CriteriaBuilder cb = currentSession().getCriteriaBuilder();
    	CriteriaUpdate<State> cu = cb.createCriteriaUpdate(State.class);
    	Root<State> root = cu.from(State.class);
    	cu.set("executionVersion", executionVersionUpdate.version);
    	Predicate restrictions[] = new Predicate[] {
			cb.equal(root.get("id"), executionVersionUpdate.fsmIdEntityId.entityId.entityId),
			cb.equal(root.get("stateMachineId"), executionVersionUpdate.fsmIdEntityId.fsmId.statemachineId),
    	};
    	cu.where(restrictions);
    	currentSession().createQuery(cu).executeUpdate();
    	/*
        Query query = currentSession().createQuery("update State set executionVersion = :executionVersion" +
                " where id = :stateId and stateMachineId = :stateMachineId");
        query.setLong("executionVersion", executionVersion);
        query.setLong("stateId", stateId);
        query.setString("stateMachineId", fsmIdId.statemachineId);
        query.executeUpdate();
        */
    }    
    
	private State[] findStatesForStateIds(FSMIdStateIds fsmIdStateIds) {
    	CriteriaBuilder cb = currentSession().getCriteriaBuilder();
    	CriteriaQuery<State> cq = cb.createQuery(State.class);
    	Root<State> root = cq.from(State.class);
    	cq.select(root).where(cb.in((root.get("id").in(Arrays.asList(fsmIdStateIds.stateIds)))));     	
    	return currentSession().createQuery(cq).getResultList().toArray(new State[0]);
    	/*
        String inClause = Arrays.toString(fsmIdStateIds.stateIds).replace("[","(").replace("]",")");
        Query query = currentSession().createQuery(
                "select s from State s where stateMachineId = :stateMachineId and id in " + inClause);
        query.setString("stateMachineId",fsmIdStateIds.fsmId.statemachineId);
        return query.list();
        */
    }
    
	private State findByCompositeId(FSMIdEntityId fsmIdEntityId) {
    	CriteriaBuilder cb = currentSession().getCriteriaBuilder();
    	CriteriaQuery<State> cq = cb.createQuery(State.class);
    	Root<State> root = cq.from(State.class);
    	Predicate restrictions[] = new Predicate[] {
			cb.equal(root.get("id"), fsmIdEntityId.entityId.entityId),
			cb.equal(root.get("stateMachineId"), fsmIdEntityId.fsmId.statemachineId),
    	};    	
    	cq.select(root).where(restrictions);     	
    	return currentSession().createQuery(cq).getSingleResult();
		
		/*
        Criteria criteria = currentSession().createCriteria(State.class)
                .add(Restrictions.eq("stateMachineId", fsmIdEntityId.statemachineId.statemachineId))
                .add(Restrictions.eq("id", fsmIdEntityId.entityId.entityId));
        Object object = criteria.uniqueResult();
        return object != null ? (State)null: null;
        */
    }
    
    private State[] findStatesByStatusCriteria(FSMStatusCriteria statusCriteria) {
    	CriteriaBuilder cb = currentSession().getCriteriaBuilder();
    	CriteriaQuery<State> cq = cb.createQuery(State.class);
    	Root<State> stateRoot = cq.from(State.class);
    	Root<StateMachine> fsmRoot = cq.from(StateMachine.class);
    	cq.multiselect(stateRoot, fsmRoot);
    	LinkedList<Predicate> predicateList = new LinkedList<Predicate>();
    	predicateList.add(cb.equal(fsmRoot.get("id"), stateRoot.get("stateMachineId")));
    	predicateList.add(cb.equal(fsmRoot.get("name"), statusCriteria.stateMachineName));
    	predicateList.add(cb.between(fsmRoot.get("createdAt"), statusCriteria.fromTime, statusCriteria.toTime));
    	predicateList.add(cb.in((stateRoot.get("status").in(Arrays.asList(statusCriteria.statuses)))));
    	if (statusCriteria.stateName != null) {
        	predicateList.add(cb.equal(stateRoot.get("name"), statusCriteria.stateName));    		
    	}
    	cq.where(predicateList.toArray(new Predicate[0]));
    	return currentSession().createQuery(cq).getResultList().toArray(new State[0]);
    	
    	/*
        Query query;
        String queryString = "select state.stateMachineId, state.id, state.status from State state join StateMachine sm " +
                "on sm.id = state.stateMachineId and sm.createdAt between :fromTime and :toTime and sm.name = :stateMachineName";
        if (statuses != null && statuses.length > 0) {
            StringBuilder sb = new StringBuilder(" and state.status in (");
            for (Status status : statuses) {
                sb.append("'" + status.toString() + "',");
            }
            sb.deleteCharAt(sb.length() - 1).append(")");
            String statusClause = sb.toString();
            queryString = queryString.concat(statusClause);
        }
        if (stateName == null) {
            query = currentSession().createQuery(queryString);
        } else {
            query = currentSession().createQuery(queryString + " and state.name = :stateName");
            query.setString("stateName", stateName);
        }
        query.setString("stateMachineName", stateMachineName);
        query.setTimestamp("fromTime", fromTime);
        query.setTimestamp("toTime", toTime);
        return query.list();
        */
    }
    
    private State[] findStatesByDependentEvent(DependentEventCriteria eventCriteria) {
    	CriteriaBuilder cb = currentSession().getCriteriaBuilder();
    	CriteriaQuery<State> cq = cb.createQuery(State.class);
    	Root<State> root = cq.from(State.class);
    	Predicate restrictions[] = new Predicate[] {
    		cb.equal(root.get("stateMachineId"), eventCriteria.fsmId.statemachineId),
    		cb.like(root.get("dependencies"),"%" + eventCriteria.eventName + "%"),
    	};    	
    	cq.select(root).where(restrictions);     	    	
    	return currentSession().createQuery(cq).getResultList().toArray(new State[0]);
    	/*
        Query query = currentSession().createQuery(
                "select state from State state where stateMachineId = :stateMachineId and" +
                        " dependencies like :eventName");
        query.setString("stateMachineId", stateMachineId);
        query.setString("eventName", "%" + eventName + "%");
        return query.list();
    	*/
    }
        
}

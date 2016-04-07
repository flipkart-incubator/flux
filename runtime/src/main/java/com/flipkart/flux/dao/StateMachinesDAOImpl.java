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

package com.flipkart.flux.dao;

import com.flipkart.flux.dao.iface.StateMachinesDAO;
import com.flipkart.flux.domain.StateMachine;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import javax.transaction.Transactional;
import java.util.List;

/**
 * @author shyam.akirala
 */
public class StateMachinesDAOImpl extends AbstractDAO<StateMachine> implements StateMachinesDAO {

    @Override
    @Transactional
    public StateMachine create(StateMachine stateMachine) {
        return super.save(stateMachine);
    }

    @Override
    @Transactional
    public StateMachine findById(String id) {
        return super.findById(StateMachine.class, id);
    }

    @Override
    @Transactional
    public List<StateMachine> findByName(String stateMachineName) {
        Criteria criteria = currentSession().createCriteria(StateMachine.class)
                .add(Restrictions.eq("name", stateMachineName));
        List<StateMachine> stateMachines = criteria.list();
        return stateMachines;
    }

    @Override
    @Transactional
    public List<StateMachine> findByNameAndVersion(String stateMachineName, Long version) {
        Criteria criteria = currentSession().createCriteria(StateMachine.class)
                .add(Restrictions.eq("name", stateMachineName))
                .add(Restrictions.eq("version", version));
        List<StateMachine> stateMachines = criteria.list();
        return stateMachines;
    }
}

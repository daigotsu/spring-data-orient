package org.springframework.data.orient.repository.object.query;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.orm.orient.OrientObjectTemplate;

/**
 * Set of classes to contain query execution strategies. 
 * 
 * @author Dzmitry_Naskou
 * 
 */
public abstract class OrientQueryExecution {

    /** The orient object template. */
    protected final OrientObjectTemplate template;

    public OrientQueryExecution(OrientObjectTemplate template) {
        super();
        this.template = template;
    }
    
    /**
     * Executes the given {@link AbstractOrientQuery} with the given {@link Object[]} values.
     *
     * @param query the orient query
     * @param values the parameters values
     * @return the result
     */
    public Object execute(AbstractOrientQuery query, Object[] values) {
        return doExecute(query, values);
    }
    
    /**
     * Method to implement by executions.
     *
     * @param query the orient query
     * @param values the parameters values
     * @return the result
     */
    protected abstract Object doExecute(AbstractOrientQuery query, Object[] values);
    
    /**
     * Executes the query to return a simple collection of entities.
     * 
     * @author Dzmitry_Naskou
     */
    static class CollectionExecution extends OrientQueryExecution {

        /**
         * Instantiates a new {@link CollectionExecution}.
         *
         * @param template the template
         */
        public CollectionExecution(OrientObjectTemplate template) {
            super(template);
        }

        /* (non-Javadoc)
         * @see org.springframework.data.orient.repository.object.query.OrientQueryExecution#doExecute(org.springframework.data.orient.repository.object.query.AbstractOrientQuery, java.lang.Object[])
         */
        @Override
        protected Object doExecute(AbstractOrientQuery query, Object[] values) {
            return template.query(query.createQuery(values), values);
        }
    }
    
    /**
     * Executes a {@link AbstractOrientQuery} to return a single entity.
     * 
     * @author Dzmitry_Naskou
     */
    static class SingleEntityExecution extends OrientQueryExecution {

        /**
         * Instantiates a new {@link SingleEntityExecution}.
         *
         * @param template the template
         */
        public SingleEntityExecution(OrientObjectTemplate template) {
            super(template);
        }

        /* (non-Javadoc)
         * @see org.springframework.data.orient.repository.object.query.OrientQueryExecution#doExecute(org.springframework.data.orient.repository.object.query.AbstractOrientQuery, java.lang.Object[])
         */
        @Override
        protected Object doExecute(AbstractOrientQuery query, Object[] values) {
            return template.queryForObject(query.createQuery(values), values);
        }
    }
    
    /**
     * Executes a {@link AbstractOrientQuery} to return a count of entities.
     * 
     * @author Dzmitry_Naskou
     */
    static class CountExecution extends OrientQueryExecution {

        /**
         * Instantiates a new {@link CountExecution}.
         *
         * @param template the template
         */
        public CountExecution(OrientObjectTemplate template) {
            super(template);
        }

        /* (non-Javadoc)
         * @see org.springframework.data.orient.repository.object.query.OrientQueryExecution#doExecute(org.springframework.data.orient.repository.object.query.AbstractOrientQuery, java.lang.Object[])
         */
        @Override
        protected Object doExecute(AbstractOrientQuery query, Object[] values) {
            return template.count(query.createQuery(values), values);
        }
    }

    /**
     * Executes the {@link AbstractOrientQuery} to return a {@link org.springframework.data.domain.Page} of entities.
     * 
     * @author Dzmitry_Naskou
     */
    static class PagedExecution extends OrientQueryExecution {

        /** The parameters. */
        private final Parameters<?, ?> parameters;
        
        /**
         * Instantiates a new {@link PagedExecution}.
         *
         * @param template the orient object template
         * @param parameters the parameters
         */
        public PagedExecution(OrientObjectTemplate template, Parameters<?, ?> parameters) {
            super(template);
            this.parameters = parameters;
        }

        /* (non-Javadoc)
         * @see org.springframework.data.orient.repository.object.query.OrientQueryExecution#doExecute(org.springframework.data.orient.repository.object.query.AbstractOrientQuery, java.lang.Object[])
         */
        @Override
        protected Object doExecute(AbstractOrientQuery query, Object[] values) {
            ParameterAccessor accessor = new ParametersParameterAccessor(parameters, values);
            
            final Object[] queryParams = prepareForQuery(parameters, values);
            
            Long total = template.count(query.createCountQuery(values), queryParams);
            
            Pageable pageable = accessor.getPageable();
            
            List<Object> content;
            
            if (pageable != null && total > pageable.getOffset()) {
                content = template.query(query.createQuery(values), queryParams);
            } else {
                content = Collections.emptyList();
            }
            
            return new PageImpl<Object>(content, pageable, total);
        }
    }
    
    Object[] prepareForQuery(Parameters<?, ?> parameters, Object[] values) {
        if (parameters.hasPageableParameter()) {
            //TODO: Also implement for Sort parameter!!!
            int index = parameters.getPageableIndex() >= 0 ? parameters.getPageableIndex() : parameters.getSortIndex();
            
            Object[] result = new Object[values.length -1];
            
            System.arraycopy(values, 0, result, 0, index);
            
            if (values.length != index) {
                System.arraycopy(values, index + 1, result, index, values.length - index - 1);
            }
            
            return result;
        }
        
        return values;
    }
    
    @Deprecated
    Object[] prepareForPagedQuery(ParameterAccessor accessor, Parameters<?, ?> parameters, Object[] values) {
        if (parameters.hasSpecialParameter()) {
            int index = parameters.getPageableIndex();
            Pageable pageable = accessor.getPageable();
            
            Object[] result = new Object[values.length + 1];
            
            System.arraycopy(values, 0, result, 0, index);
            
            result[index] = pageable.getPageSize();
            result[index + 1] = pageable.getOffset();
            
            if (values.length != index) {
                System.arraycopy(values, index + 1, result, index + 2, values.length - index - 1);
            }
            
            return result;
        }
        
        return values;
    }
}

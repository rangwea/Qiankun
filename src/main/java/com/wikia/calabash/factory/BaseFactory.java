package com.wikia.calabash.factory;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class BaseFactory<Type, Concrete extends FactoryConcrete<Type>> implements ApplicationContextAware {

    private final Map<Type, Concrete> concretes = new HashMap<>();

    public Concrete get(Type k) {
        return concretes.get(k);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Concrete> map = applicationContext.getBeansOfType(getConcreteClass());
        concretes.putAll(map.values().stream().collect(Collectors.toMap(FactoryConcrete::getType, e -> e)));
    }

    private Class<Concrete> getConcreteClass() {
        Class<?> c = this.getClass();
        java.lang.reflect.Type t = c.getGenericSuperclass();
        if (t instanceof ParameterizedType)
        {
            java.lang.reflect.Type[] p = ((ParameterizedType) t).getActualTypeArguments();
            return (Class<Concrete>) p[1];
        }
        throw new IllegalArgumentException("factory get Concrete Class fail");
    }
}
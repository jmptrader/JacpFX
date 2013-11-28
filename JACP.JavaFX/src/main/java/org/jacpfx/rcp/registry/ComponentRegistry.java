/************************************************************************
 *
 * Copyright (C) 2010 - 2013
 *
 * [CSSUtil.java]
 * AHCP Project http://jacp.googlecode.com
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *     http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either 
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 *
 *
 ************************************************************************/
package org.jacpfx.rcp.registry;

import javafx.event.Event;
import javafx.event.EventHandler;
import org.jacpfx.api.component.IPerspective;
import org.jacpfx.api.component.ISubComponent;
import org.jacpfx.rcp.util.FXUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

/**
 * Global registry with references to all components.
 *
 * @author Andy Moncsek
 *
 */
public class ComponentRegistry {
    private static final List<ISubComponent<EventHandler<Event>, Event, Object>> components = new ArrayList<>();
    private static final StampedLock lock = new StampedLock();
    /**
     * Registers a component.
     *
     * @param component
     */
    public static void registerComponent(
            final ISubComponent<EventHandler<Event>, Event, Object> component) {
        final long stamp = lock.tryWriteLock();
        try{
            if (!components.contains(component))
                components.add(component);
        }finally{
            lock.unlockWrite(stamp);
        }

    }

    /**
     * Removes component from registry.
     *
     * @param component
     */
    public static void removeComponent(
            final ISubComponent<EventHandler<Event>, Event, Object> component) {
        final long stamp = lock.tryWriteLock();
        try{
            if (components.contains(component))
                components.remove(component);
        }finally{
            lock.unlockWrite(stamp);
        }

    }

    /**
     * Returns a component by component id
     *
     * @param targetId
     * @return
     */
    public static ISubComponent<EventHandler<Event>, Event, Object> findComponentById(
            final String targetId) {
        long stamp;
        if ((stamp = lock.tryOptimisticRead()) != 0L) { // optimistic
            List<ISubComponent<EventHandler<Event>, Event, Object>> tmp = components;
            if (lock.validate(stamp)) {
                return FXUtil.getObserveableById(FXUtil.getTargetComponentId(targetId),
                        tmp);
            }
        }
        stamp = lock.readLock(); // fall back to read lock
        try {
            return FXUtil.getObserveableById(FXUtil.getTargetComponentId(targetId),
                    components);
        } finally {
            lock.unlockRead(stamp);
        }

    }
    /**
     * Returns the a component by class.
     * @param clazz
     * @return
     */
    public static ISubComponent<EventHandler<Event>, Event, Object> findComponentByClass(final Class<?> clazz) {
        long stamp;
        if ((stamp = lock.tryOptimisticRead()) != 0L) { // optimistic
            List<ISubComponent<EventHandler<Event>, Event, Object>> tmp = components;
            if (lock.validate(stamp)) {
                final Optional<ISubComponent<EventHandler<Event>, Event, Object>> returnVal = tmp.parallelStream().filter(c -> c.getComponent().getClass().isAssignableFrom(clazz)).findFirst();
                if(returnVal.isPresent())return returnVal.get();
            }
        }
        stamp = lock.readLock(); // fall back to read lock
        try {
            final Optional<ISubComponent<EventHandler<Event>, Event, Object>> returnVal = components.parallelStream().filter(c -> c.getComponent().getClass().isAssignableFrom(clazz)).findFirst();
            if(returnVal.isPresent())return returnVal.get();
        } finally {
            lock.unlockRead(stamp);
        }
        return null;
    }

}

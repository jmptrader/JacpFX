/************************************************************************
 *
 * Copyright (C) 2010 - 2012
 *
 * [FX2ComponentReplaceWorker.java]
 * AHCP Project (http://jacp.googlecode.com)
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
package org.jacp.javafx.rcp.worker;


import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import org.jacp.api.action.IAction;
import org.jacp.api.annotations.lifecycle.PreDestroy;
import org.jacp.api.component.ISubComponent;
import org.jacp.javafx.rcp.component.AFXComponent;
import org.jacp.javafx.rcp.componentLayout.FXComponentLayout;
import org.jacp.javafx.rcp.context.JACPContextImpl;
import org.jacp.javafx.rcp.util.FXUtil;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

/**
 * Background Worker to execute components handle method in separate thread and
 * to replace or add the component result node; While the handle method is
 * executed in an own thread the postHandle method is executed in application
 * main thread.
 *
 * @author Andy Moncsek
 */
public class FXComponentReplaceWorker extends AFXComponentWorker<AFXComponent> {

    private final Map<String, Node> targetComponents;
    private final AFXComponent component;
    private final FXComponentLayout layout;
    private final BlockingQueue<ISubComponent<EventHandler<Event>, Event, Object>> componentDelegateQueue;
    private static final Logger logger = Logger.getLogger("FXComponentReplaceWorker");

    public FXComponentReplaceWorker(
            final Map<String, Node> targetComponents,
            final BlockingQueue<ISubComponent<EventHandler<Event>, Event, Object>> componentDelegateQueue,
            final AFXComponent component, final FXComponentLayout layout) {
        super(component.getContext().getName());
        this.targetComponents = targetComponents;
        this.component = component;
        this.layout = layout;
        this.componentDelegateQueue = componentDelegateQueue;
    }

    private void setCacheHints(boolean cache, CacheHint hint, final AFXComponent component) {
        final Node currentRoot = component.getRoot();
        if (currentRoot != null && currentRoot.getParent() != null) {
            if (currentRoot.getParent().isCache() != cache) currentRoot.getParent().setCache(cache);
            if (!currentRoot.getParent().getCacheHint().equals(hint))
                currentRoot.getParent().setCacheHint(CacheHint.SPEED);
        }
    }

    @Override
    protected AFXComponent call() throws Exception {
        // TODO handle locks to components write methods
        try {
            this.component.lock();
            while (this.component.hasIncomingMessage()) {
                final IAction<Event, Object> myAction = this.component
                        .getNextIncomingMessage();
                this.log(" //1.1.1.1.1// handle replace component BEGIN: "
                        + this.component.getContext().getName());

                final Node previousContainer = this.component.getRoot();
                final String currentTargetLayout = JACPContextImpl.class.cast(this.component.getContext()).getTargetLayout();
                final String currentExecutionTarget = JACPContextImpl.class.cast(this.component.getContext()).getExecutionTarget();
                // run code
                this.log(" //1.1.1.1.2// handle component: "
                        + this.component.getContext().getName());
                final Node handleReturnValue = this.prepareAndRunHandleMethod(
                        this.component, myAction);
                this.log(" //1.1.1.1.3// publish component: "
                        + this.component.getContext().getName());

                this.publish(this.component, myAction, this.targetComponents,
                        this.layout, handleReturnValue, previousContainer,
                        currentTargetLayout, currentExecutionTarget);

            }
        } catch (final IllegalStateException e) {
            if (e.getMessage().contains("Not on FX application thread")) {
                throw new UnsupportedOperationException(
                        "Do not reuse Node components in handleAction method, use postHandleAction instead to verify that you change nodes in JavaFX main Thread:",
                        e);
            }
        } finally {
            this.component.release();
        }
        return this.component;
    }

    /**
     * publish handle result in application main thread
     *
     * @throws InterruptedException
     */
    private void publish(final AFXComponent component,
                         final IAction<Event, Object> myAction,
                         final Map<String, Node> targetComponents,
                         final FXComponentLayout layout, final Node handleReturnValue,
                         final Node previousContainer, final String currentTargetLayout, final String currentExecutionTarget)
            throws InterruptedException {
        this.invokeOnFXThreadAndWait(() -> {
            setCacheHints(true, CacheHint.SPEED, component);
            // check if component was set to inactive, if so remove
            try {
                if (component.getContext().isActive()) {
                    FXComponentReplaceWorker.this.publishComponentValue(
                            component, myAction, targetComponents, layout,
                            handleReturnValue, previousContainer, currentTargetLayout, currentExecutionTarget);
                } else {
                    // TODO merge with code from  publishComponentValue
                    // unregister component
                    FXComponentReplaceWorker.this.removeComponentValue(
                            previousContainer);
                    // run teardown
                    FXUtil.invokeHandleMethodsByAnnotation(PreDestroy.class,
                            component.getComponentHandle(), layout);
                }
            } catch (Exception e) {
                e.printStackTrace(); // TODO pass exception
            }
        });
    }


    private void removeComponentValue(final Node previousContainer) {
        if (previousContainer != null) {
            final Node parent = previousContainer.getParent();
            if (parent != null) {
                this.handleOldComponentRemove(parent, previousContainer);
            }
        }

    }

    /**
     * run in thread
     *
     * @param previousContainer
     * @param currentTargetLayout
     */
    private void publishComponentValue(final AFXComponent component,
                                       final IAction<Event, Object> action,
                                       final Map<String, Node> targetComponents,
                                       final FXComponentLayout layout, final Node handleReturnValue,
                                       final Node previousContainer, final String currentTargetLayout, final String currentExecutionTarget) throws Exception {
        executeComponentViewPostHandle(handleReturnValue, component,
                action);
        if (previousContainer != null) {
            // check again if component was set to inactive (in postHandle), if
            // so remove
            if (component.getContext().isActive()) {
                // TODO check if execution target has changed before update targetLayout
                final String newExecutionTarget = JACPContextImpl.class.cast(this.component.getContext()).getExecutionTarget();
                if (currentExecutionTarget != null && newExecutionTarget != null && !currentExecutionTarget.equalsIgnoreCase(newExecutionTarget)) {
                    // TODO remove from view and move to different perspective
                    this.removeComponentValue(previousContainer);
                    this.handlePerspectiveChange(this.componentDelegateQueue,
                            component, layout);
                } else {
                    final String newTargetLayout = JACPContextImpl.class.cast(this.component.getContext()).getTargetLayout();
                    this.removeOldComponentValue(component, previousContainer,
                            currentTargetLayout, newTargetLayout);
                    this.checkAndHandleLayoutTargetChange(component, previousContainer,
                            currentTargetLayout, newTargetLayout, targetComponents);
                }

            } else {
                // unregister component
                this.removeComponentValue(previousContainer);
                // run teardown
                FXUtil.invokeHandleMethodsByAnnotation(PreDestroy.class,
                        component.getComponentHandle(), layout);
            }

        }
    }

    /**
     * remove old component value from root node
     */
    private void removeOldComponentValue(final AFXComponent component,
                                         final Node previousContainer, final String currentTargetLayout, final String newTargetLayout) {
        final Node root = component.getRoot();
        // avoid remove/add when root component did not changed!
        if (!currentTargetLayout.equals(newTargetLayout)
                || root == null || root != previousContainer) {
            // remove old view
            this.removeComponentValue(previousContainer);
        }
    }

    /**
     * add new component value to root node
     */
    private void checkAndHandleLayoutTargetChange(final AFXComponent component,
                                                  final Node previousContainer, final String currentTargetLayout, final String newTargetLayout, final Map<String, Node> targetComponents) {

        final Node root = component.getRoot();
        if (!currentTargetLayout.equals(newTargetLayout)) {
            executeLayoutTargetUpdate(component, newTargetLayout, targetComponents);
        } else if (root != null && root != previousContainer) {
            // add new view
            this.log(" //1.1.1.1.4// handle new component insert: "
                    + component.getContext().getName());
            this.handleViewState(root, true);
            executeLayoutTargetUpdate(component, newTargetLayout, targetComponents);
        }

    }

    /**
     * Performs target change of component or perspective
     *
     * @param component
     * @param newTargetLayout
     */
    private void executeLayoutTargetUpdate(final AFXComponent component,
                                           final String newTargetLayout, final Map<String, Node> targetComponents) {
        final Node validContainer = this.getValidContainerById(
                targetComponents, newTargetLayout);
        if (validContainer != null) {
            this.handleLayoutTargetChange(component,
                    validContainer);
        } else {
            throw new IllegalArgumentException("no targetLayout " + newTargetLayout + " found");
        }
    }

    @Override
    protected final void done() {
        AFXComponent component = null;
        try {
            component = this.get();
        } catch (final InterruptedException e) {
            logger.info("execution interrupted for component: " + this.component.getContext().getName());
        } catch (final ExecutionException e) {
            if (e.getCause() instanceof InterruptedException) {
                logger.info("execution interrupted for component: " + this.component.getContext().getName());
            } else {
                e.printStackTrace();
            }

            // TODO add to error queue and restart thread if
            // messages in
            // queue
        } catch (final Exception e) {
            e.printStackTrace();
            // TODO add to error queue and restart thread if
            // messages in
            // queue
        } finally {
            if (component != null) setCacheHints(true, CacheHint.DEFAULT, component);
        }

    }

}
/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.execution.plan;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import org.gradle.api.internal.TaskInternal;
import org.gradle.internal.deprecation.DeprecationLogger;

import java.util.NavigableSet;
import java.util.Set;
import java.util.function.Consumer;

public abstract class TaskNode extends Node {
    private final NavigableSet<Node> mustSuccessors = Sets.newTreeSet();
    private final Set<Node> mustPredecessors = Sets.newHashSet();
    private final NavigableSet<Node> shouldSuccessors = Sets.newTreeSet();
    private final NavigableSet<Node> finalizers = Sets.newTreeSet();
    private final NavigableSet<Node> finalizingSuccessors = Sets.newTreeSet();

    @Override
    public boolean doCheckDependenciesComplete() {
        if (!super.doCheckDependenciesComplete()) {
            return false;
        }

        for (Node dependency : mustSuccessors) {
            if (!dependency.isComplete()) {
                return false;
            }
        }

        FinalizerGroup finalizer = getGroup().asFinalizer();
        if (finalizer != null) {
            for (Node finalized : finalizer.getFinalizedNodes()) {
                if (!finalized.isComplete()) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean allDependenciesSuccessful() {
        if (!super.allDependenciesSuccessful()) {
            return false;
        }

        FinalizerGroup finalizer = getGroup().asFinalizer();
        if (finalizer == null) {
            return true;
        }

        // If any finalized node has executed, then this node can execute
        for (Node finalized : finalizer.getFinalizedNodes()) {
            if (finalized.isExecuted()) {
                return true;
            }
        }
        return false;
    }

    public Set<Node> getMustSuccessors() {
        return mustSuccessors;
    }

    public abstract Set<Node> getLifecycleSuccessors();

    public abstract void setLifecycleSuccessors(Set<Node> successors);

    @Override
    public Set<Node> getFinalizers() {
        return finalizers;
    }

    @Override
    public void addFinalizer(Node finalizer) {
        finalizers.add(finalizer);
    }

    @Override
    public Set<Node> getFinalizingSuccessors() {
        return finalizingSuccessors;
    }

    public Set<Node> getFinalizingSuccessorsInReverseOrder() {
        return finalizingSuccessors.descendingSet();
    }

    public Set<Node> getShouldSuccessors() {
        return shouldSuccessors;
    }

    public void addMustSuccessor(TaskNode toNode) {
        deprecateLifecycleHookReferencingNonLocalTask("mustRunAfter", toNode);
        mustSuccessors.add(toNode);
        toNode.mustPredecessors.add(this);
    }

    public void addFinalizingSuccessor(Node finalized) {
        finalizingSuccessors.add(finalized);
        finalized.addFinalizer(this);
    }

    public void addShouldSuccessor(Node toNode) {
        deprecateLifecycleHookReferencingNonLocalTask("shouldRunAfter", toNode);
        shouldSuccessors.add(toNode);
    }

    public void removeShouldSuccessor(TaskNode toNode) {
        shouldSuccessors.remove(toNode);
    }

    @Override
    public Iterable<Node> getAllSuccessors() {
        return Iterables.concat(
            shouldSuccessors,
            getGroup().getSuccessors(),
            mustSuccessors,
            super.getAllSuccessors()
        );
    }

    @Override
    public Iterable<Node> getHardSuccessors() {
        return Iterables.concat(
            getGroup().getSuccessors(),
            mustSuccessors,
            super.getHardSuccessors()
        );
    }

    @Override
    public Iterable<Node> getAllSuccessorsInReverseOrder() {
        return Iterables.concat(
            super.getAllSuccessorsInReverseOrder(),
            mustSuccessors.descendingSet(),
            getGroup().getSuccessorsInReverseOrder(),
            shouldSuccessors.descendingSet()
        );
    }

    @Override
    protected void visitAllDependents(Consumer<Node> visitor) {
        super.visitAllDependents(visitor);
        for (Node node : mustPredecessors) {
            visitor.accept(node);
        }
        for (Node node : finalizers) {
            node.getFinalizerGroup().visitAllMembers(visitor);
        }
    }

    @Override
    public boolean hasHardSuccessor(Node successor) {
        if (super.hasHardSuccessor(successor)) {
            return true;
        }
        if (!(successor instanceof TaskNode)) {
            return false;
        }
        return getMustSuccessors().contains(successor)
            || getFinalizingSuccessors().contains(successor);
    }

    public abstract TaskInternal getTask();

    protected void deprecateLifecycleHookReferencingNonLocalTask(String hookName, Node taskNode) {
        if (taskNode instanceof TaskInAnotherBuild) {
            DeprecationLogger.deprecateAction("Using " + hookName + " to reference tasks from another build")
                .willBecomeAnErrorInGradle8()
                .withUpgradeGuideSection(6, "referencing_tasks_from_included_builds")
                .nagUser();
        }
    }

    @Override
    public void maybeInheritGroupAsFinalizer(Node node) {
        NodeGroup fromGroup = node.getGroup();
        NodeGroup current = getGroup();
        if (current instanceof FinalizerGroup) {
            FinalizerGroup finalizerGroup = (FinalizerGroup) current;
            finalizerGroup.maybeInheritFrom(fromGroup);
        } else {
            setGroup(new FinalizerGroup(this, fromGroup));
        }
    }
}

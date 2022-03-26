/*
 * Copyright 2022 the original author or authors.
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

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * The set of nodes reachable from a particular finalizer node.
 */
class FinalizerGroup extends NodeGroup {
    @Nullable
    private OrdinalGroup ordinal;
    private final TaskNode node;
    private final Set<Node> members = new LinkedHashSet<>();

    public FinalizerGroup(TaskNode node, NodeGroup fromGroup) {
        this.ordinal = fromGroup.asOrdinal();
        this.node = node;
        this.members.add(node);
    }

    @Override
    public String toString() {
        return "finalizer in " + ordinal;
    }

    public Collection<Node> getFinalizedNodes() {
        return node.getFinalizingSuccessors();
    }

    @Nullable
    @Override
    public OrdinalGroup asOrdinal() {
        return ordinal;
    }

    @Nullable
    @Override
    public FinalizerGroup asFinalizer() {
        return this;
    }

    @Override
    public Collection<Node> getSuccessors() {
        return node.getFinalizingSuccessors();
    }

    @Override
    public Collection<Node> getSuccessorsInReverseOrder() {
        return node.getFinalizingSuccessorsInReverseOrder();
    }

    public void maybeInheritFrom(NodeGroup fromGroup) {
        OrdinalGroup ordinal = fromGroup.asOrdinal();
        if (ordinal != null && (this.ordinal == null || this.ordinal.getOrdinal() < ordinal.getOrdinal())) {
            this.ordinal = ordinal;
        }
    }

    @Override
    public void addMember(Node node) {
        members.add(node);
    }

    @Override
    public void removeMember(Node node) {
        members.remove(node);
    }

    public void visitAllMembers(Consumer<Node> visitor) {
        for (Node member : members) {
            visitor.accept(member);
        }
    }
}

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
import java.util.Collections;

/**
 * Represents the "entry point" that caused a node to be included in the graph.
 * This can affect how the node is scheduled relative to other nodes in the graph.
 */
abstract class NodeGroup {
    public static final NodeGroup DEFAULT_GROUP = new NodeGroup() {
        @Override
        public String toString() {
            return "default group";
        }

        @Nullable
        @Override
        public OrdinalGroup asOrdinal() {
            return null;
        }
    };

    public boolean isEntryPoint() {
        return false;
    }

    @Nullable
    public abstract OrdinalGroup asOrdinal();

    @Nullable
    public FinalizerGroup asFinalizer() {
        return null;
    }

    /**
     * Returns the set of nodes which must complete before any node in this group can start.
     */
    public Collection<Node> getSuccessors() {
        return Collections.emptyList();
    }

    public Collection<Node> getSuccessorsInReverseOrder() {
        return Collections.emptyList();
    }

    public void addMember(Node node) {
    }

    public void removeMember(Node node) {
    }
}

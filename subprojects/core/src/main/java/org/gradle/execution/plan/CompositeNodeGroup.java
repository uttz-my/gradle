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
import java.util.Set;

/**
 * Represents a group of nodes that are reachable from more than one root node.
 */
public class CompositeNodeGroup extends HasFinalizers {
    @Nullable
    private final OrdinalGroup ordinalGroup;
    private final Set<FinalizerGroup> finalizerGroups;

    public CompositeNodeGroup(@Nullable OrdinalGroup ordinalGroup, Set<FinalizerGroup> finalizerGroups) {
        this.ordinalGroup = ordinalGroup;
        this.finalizerGroups = finalizerGroups;
    }

    @Nullable
    @Override
    public OrdinalGroup asOrdinal() {
        return ordinalGroup;
    }

    @Override
    public Set<FinalizerGroup> getFinalizerGroups() {
        return finalizerGroups;
    }
}

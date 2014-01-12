/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.was.event;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class UserClosedCraftInHandUI implements Event {
    private EntityRef entity1;
    private EntityRef entity2;
    private EntityRef entity3;

    public UserClosedCraftInHandUI(EntityRef entity1, EntityRef entity2, EntityRef entity3) {
        this.entity1 = entity1;
        this.entity2 = entity2;
        this.entity3 = entity3;
    }

    public EntityRef getEntity1() {
        return entity1;
    }

    public EntityRef getEntity2() {
        return entity2;
    }

    public EntityRef getEntity3() {
        return entity3;
    }
}

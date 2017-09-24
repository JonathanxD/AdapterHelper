/*
 *      AdapterHelper - Adapter management helper. <https://github.com/JonathanxD/AdapterHelper>
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2017 TheRealBuggy/JonathanxD (https://github.com/JonathanxD/ & https://github.com/TheRealBuggy/) <jonathan.scripter@programmer.net>
 *      Copyright (c) contributors
 *
 *
 *      Permission is hereby granted, free of charge, to any person obtaining a copy
 *      of this software and associated documentation files (the "Software"), to deal
 *      in the Software without restriction, including without limitation the rights
 *      to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *      copies of the Software, and to permit persons to whom the Software is
 *      furnished to do so, subject to the following conditions:
 *
 *      The above copyright notice and this permission notice shall be included in
 *      all copies or substantial portions of the Software.
 *
 *      THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *      IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *      FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *      AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *      LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *      OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *      THE SOFTWARE.
 */
package com.github.jonathanxd.adapterhelper.test;

import com.github.jonathanxd.adapterhelper.Adapter;
import com.github.jonathanxd.adapterhelper.AdapterManager;
import com.github.jonathanxd.adapterhelper.AdapterSpecification;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AdapterCollectionFuncTest {

    private static final Map<Class<?>, Class<?>> classMapping = new HashMap<>();

    static {
        classMapping.put(Player1.class, Player2.class);
        classMapping.put(Player2.class, Player1.class);
    }

    @Test
    public void listFuncTest() {
        AdapterManager manager = new AdapterManager();

        manager.register(AdapterSpecification.createFromInterface(EntityAdapter.class, Entity1.class, Entity2.class));
        manager.register(AdapterSpecification.createFromInterface(DogAdapter.class, Dog1.class, Dog2.class));
        manager.register(AdapterSpecification.createFromInterface(PlayerAdapter.class, Player1.class, Player2.class));
        manager.register(AdapterSpecification.create((e, amanager) ->
                        new Player2.Impl(UUID.fromString(e.getId())),
                Player2.class, Player1.class));

        List<Entity2> entity2List = new ArrayList<>();

        entity2List.add(new Player2.Impl(UUID.randomUUID()));

        List<Entity1> entity1List = manager.createDynamicAdapterList(Entity2.class, entity2List, Entity1.class);

        Player1 player1 = (Player1) entity1List.get(0);

        System.out.println(player1.getId());

        entity1List.add(new Player1.Impl("fc7620e4-0ebe-4421-bbcb-e17594e923d5"));

        Player1 player1_ = (Player1) entity1List.get(1);

        System.out.println(player1_.getId());
    }

    @Test
    public void adapterMapFuncTest() {
        AdapterManager manager = new AdapterManager();

        manager.register(AdapterSpecification.createFromInterface(EntityAdapter.class, Entity1.class, Entity2.class));
        manager.register(AdapterSpecification.createFromInterface(DogAdapter.class, Dog1.class, Dog2.class));
        manager.register(AdapterSpecification.createFromInterface(PlayerAdapter.class, Player1.class, Player2.class));
        manager.register(AdapterSpecification.create((e, amanager) ->
                        new Player2.Impl(UUID.fromString(e.getId())),
                Player2.class, Player1.class));

        manager.register(AdapterSpecification.create((e, amanager) ->
                        new Id2.Impl(UUID.fromString(e.toString())),
                Id2.class, Id1.class));

        manager.register(AdapterSpecification.create((e, amanager) ->
                        new Id1.Impl(e.toString()),
                Id1.class, Id2.class));

        Map<Id2, Entity2> map = new HashMap<>();

        UUID uuid2 = UUID.fromString("9094bd95-b026-4cf6-8695-40df29a64bd2");

        map.put(new Id2.Impl(uuid2), new Player2.Impl(uuid2));

        Map<Id1, Entity1> adaptMap = manager.createDynamicAdapterMap(
                Id2.class, Entity2.class,
                map,
                Id1.class, Entity1.class);

        Player1 player1 = (Player1) adaptMap.get(new Id1.Impl("00000000-0000-0000-0000-000000000000"));

        Assert.assertEquals(null, player1);

        player1 = (Player1) adaptMap.get(new Id1.Impl(uuid2.toString()));

        Assert.assertNotEquals(null, player1);

        System.out.println(player1.getId());

        String id1 = "fc7620e4-0ebe-4421-bbcb-e17594e923d5";

        adaptMap.put(new Id1.Impl(id1), new Player1.Impl(id1));

        Player1 player1_ = (Player1) adaptMap.get(new Id1.Impl(id1));

        System.out.println(player1_.getId());
    }

    public interface Id1 {
        @Override
        String toString();

        class Impl implements Id1 {
            private final String idString;

            public Impl(String idString) {
                this.idString = idString;
            }

            @Override
            public String toString() {
                return this.idString.toString();
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Id1 && obj.toString().equals(this.toString());
            }

            @Override
            public int hashCode() {
                return this.idString.hashCode();
            }
        }
    }

    public interface Id2 {
        @Override
        String toString();

        class Impl implements Id2 {
            private final UUID uuid;

            public Impl(UUID uuid) {
                this.uuid = uuid;
            }

            @Override
            public String toString() {
                return this.uuid.toString();
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Id2 && obj.toString().equals(this.toString());
            }

            @Override
            public int hashCode() {
                return this.uuid.hashCode();
            }
        }

    }

    public interface EntityAdapter<T extends Entity2> extends Adapter<T>, Entity1 {
        @Override
        default String getId() {
            return this.getAdapteeInstance().getId().toString();
        }
    }

    public interface DogAdapter extends EntityAdapter<Dog2>, Dog1 {

    }

    public interface PlayerAdapter extends EntityAdapter<Player2>, Player1 {

    }

    public interface Entity1 {
        String getId();
    }

    public interface Dog1 extends Entity1 {
        class Impl implements Dog1 {
            private final String id;

            public Impl(String id) {
                this.id = id;
            }

            @Override
            public String getId() {
                return this.id;
            }
        }
    }

    public interface Human1 extends Entity1 {
    }

    public interface Player1 extends Human1 {
        class Impl implements Player1 {
            private final String id;

            Impl(String id) {
                this.id = id;
            }

            @Override
            public String getId() {
                return this.id;
            }
        }
    }

    public interface Entity2 {
        UUID getId();
    }

    public interface Human2 extends Entity2 {

    }

    public interface Player2 extends Human2 {

        class Impl implements Player2 {
            private final UUID id;

            Impl(UUID id) {
                this.id = id;
            }

            @Override
            public UUID getId() {
                return this.id;
            }
        }

    }

    public interface Dog2 extends Entity2 {

        class Impl implements Dog2 {
            private final UUID id;

            Impl(UUID id) {
                this.id = id;
            }

            @Override
            public UUID getId() {
                return this.id;
            }
        }
    }

}

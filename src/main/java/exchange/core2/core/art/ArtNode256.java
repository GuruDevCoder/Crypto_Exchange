/*
 * Copyright 2019 Maksim Zheravin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package exchange.core2.core.art;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static exchange.core2.core.art.ArtNode4.toNodeIndex;

/**
 * The largest node type is simply an array of 256
 * pointers and is used for storing between 49 and 256 entries.
 * With this representation, the next node can be found very
 * efficiently using a single lookup of the key byte in that array.
 * No additional indirection is necessary. If most entries are not
 * null, this representation is also very space efficient because
 * only pointers need to be stored.
 */
@Slf4j
public final class ArtNode256<V> implements IArtNode<V> {

    public static final int NODE48_SWITCH_THRESHOLD = 37;

    // direct addressing
    final Object[] nodes = new Object[256];

    short numChildren;

    public ArtNode256(ArtNode48<V> artNode48, short subKey, Object newElement) {
        final int sourceSize = 48;
        for (short i = 0; i < 256; i++) {
            final byte index = artNode48.indexes[i];
            if (index != -1) {
                this.nodes[i] = artNode48.nodes[index];
            }
        }
        this.nodes[subKey] = newElement;
        this.numChildren = sourceSize + 1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V getValue(final long key, final int level) {
        final short idx = toNodeIndex(key, level);
        final Object node = nodes[idx];
        if (node != null) {
            return level == 0
                    ? (V) node
                    : ((IArtNode<V>) node).getValue(key, level - 8);
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IArtNode<V> put(final long key, final int level, final V value) {
        final short idx = toNodeIndex(key, level);
        if (nodes[idx] == null) {
            // new object will be inserted
            numChildren++;
        }

        if (level == 0) {
            nodes[idx] = value;
        } else {
            IArtNode<V> node = (IArtNode<V>) nodes[idx];
            if (node != null) {
                final IArtNode<V> resizedNode = node.put(key, level - 8, value);
                if (resizedNode != null) {
                    // TODO put old into the pool
                    // update resized node if capacity has increased
                    nodes[idx] = resizedNode;
                }
            } else {
                nodes[idx] = new ArtNode4(key, level - 8, value);
            }
        }

        // never need to increase size
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IArtNode<V> remove(long key, int level) {
        final short idx = toNodeIndex(key, level);

        if (nodes[idx] == null) {
            return this;
        }

        if (level == 0) {
            nodes[idx] = null;
            numChildren--;
        } else {
            final IArtNode<V> node = (IArtNode<V>) nodes[idx];
            final IArtNode<V> resizedNode = node.remove(key, level - 8);
            if (resizedNode != node) {
                // TODO put old into the pool
                // update resized node if capacity has decreased
                nodes[idx] = resizedNode;
                if (resizedNode == null) {
                    numChildren--;
                }
            }
        }

        return (numChildren == NODE48_SWITCH_THRESHOLD) ? new ArtNode48(this) : this;
    }

    @Override
    public void validateInternalState() {
        int found = 0;
        for (int i = 0; i < 256; i++) {
            Object node = nodes[i];
            if (node != null) {
                if (node instanceof IArtNode) {
                    IArtNode artNode = (IArtNode) node;
                    artNode.validateInternalState();
                }
                found++;
            }
        }
        if (found != numChildren) {
            throw new IllegalStateException("wrong numChildren");
        }
        if (numChildren <= NODE48_SWITCH_THRESHOLD) throw new IllegalStateException("too small");
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map.Entry<Long, V>> entries(long keyPrefix, int level) {
        final long keyPrefixNext = keyPrefix << 8;
        final List<Map.Entry<Long, V>> list = new ArrayList<>();
        final short[] keys = createKeysArray();
        for (int i = 0; i < numChildren; i++) {
            if (level == 0) {
                list.add(new LongAdaptiveRadixTreeMap.Entry<>(keyPrefixNext + keys[i], (V) nodes[keys[i]]));
            } else {
                list.addAll(((IArtNode<V>) nodes[keys[i]]).entries(keyPrefixNext + keys[i], level - 8));
            }
        }
        return list;
    }

    @Override
    public String printDiagram(String prefix, int level) {
        final short[] keys = createKeysArray();
        return LongAdaptiveRadixTreeMap.printDiagram(prefix, level, numChildren, idx -> keys[idx], idx -> nodes[keys[idx]]);
    }

    private short[] createKeysArray() {
        short[] keys = new short[numChildren];
        int j = 0;
        for (short i = 0; i < 256; i++) {
            if (nodes[i] != null) {
                keys[j++] = i;
            }
        }
        return keys;
    }
}
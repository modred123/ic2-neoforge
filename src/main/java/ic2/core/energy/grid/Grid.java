/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.BlockGetter
 */
package ic2.core.energy.grid;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import ic2.core.IC2;
import ic2.core.energy.grid.EnergyNetGlobal;
import ic2.core.energy.grid.EnergyNetLocal;
import ic2.core.energy.grid.EnergyNetSettings;
import ic2.core.energy.grid.GridInfo;
import ic2.core.energy.grid.Node;
import ic2.core.energy.grid.NodeLink;
import ic2.core.energy.grid.NodeType;
import ic2.core.util.LogCategory;
import ic2.core.util.Util;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

public class Grid {
    private final int uid;
    private final EnergyNetLocal enet;
    private final Map<Integer, Node> nodes = new HashMap<Integer, Node>();
    private boolean dirty;
    private Object data;

    Grid(EnergyNetLocal energyNetLocal) {
        this.uid = energyNetLocal.allocateGridId();
        this.enet = energyNetLocal;
        energyNetLocal.addGrid(this);
    }

    public EnergyNetLocal getEnergyNet() {
        return this.enet;
    }

    public Node getNode(int n) {
        return this.nodes.get(n);
    }

    public Collection<Node> getNodes() {
        return this.nodes.values();
    }

    public boolean clearDirty() {
        if (!this.dirty) {
            return false;
        }
        this.dirty = false;
        return true;
    }

    public <T> T getData() {
        return (T)this.data;
    }

    public void setData(Object object) {
        this.data = object;
    }

    public String toString() {
        return "Grid " + this.uid;
    }

    void add(Node node, Collection<Node> collection) {
        if (EnergyNetSettings.logGridUpdatesVerbose) {
            IC2.log.debug(LogCategory.EnergyNet, "%d Add %s to %s neighbors: %s.", this.uid, node, this, collection);
        }
        this.invalidate();
        assert (!this.nodes.isEmpty() || collection.isEmpty());
        assert (this.nodes.isEmpty() || !collection.isEmpty() || node.isExtraNode());
        assert (node.links.isEmpty());
        this.add(node);
        for (Node node2 : collection) {
            assert (node2 != node);
            assert (this.nodes.containsKey(node2.uid));
            double d = (node.getInnerLoss() + node2.getInnerLoss()) / 2.0;
            NodeLink nodeLink = new NodeLink(node, node2, d);
            node.links.add(nodeLink);
            node2.links.add(nodeLink);
        }
    }

    void remove(Node node) {
        if (EnergyNetSettings.logGridUpdatesVerbose) {
            IC2.log.debug(LogCategory.EnergyNet, "%d Remove Node %s from %s with %d nodes.", this.uid, node, this, this.nodes.size());
        }
        this.invalidate();
        Iterator<NodeLink> iterator = node.links.iterator();
        while (iterator.hasNext()) {
            NodeLink nodeLink = iterator.next();
            Node neighbor = nodeLink.getNeighbor(node);
            int removed = 0;
            Iterator<NodeLink> linkIterator = neighbor.links.iterator();
            while (linkIterator.hasNext()) {
                if (linkIterator.next() != nodeLink) continue;
                linkIterator.remove();
                removed = 1;
                break;
            }
            assert (removed != 0);
            this.enet.addTileToNotify(neighbor.getTile().getMainTile());
            if (!neighbor.links.isEmpty() || !neighbor.tile.removeExtraNode(neighbor)) continue;
            if (EnergyNetSettings.logGridUpdatesVerbose) {
                IC2.log.debug(LogCategory.EnergyNet, "%d Removing isolated extra node %s.", this.uid, neighbor);
            }
            assert (neighbor.getType() != NodeType.Conductor);
            iterator.remove();
            this.nodes.remove(neighbor.uid);
            neighbor.clearGrid();
        }
        this.nodes.remove(node.uid);
        node.clearGrid();
        int n2 = node.links.size();
        if (n2 == 0) {
            assert (this.nodes.isEmpty());
            this.enet.removeGrid(this);
        } else if (n2 > 1 && node.nodeType == NodeType.Conductor) {
            Set<Node>[] setArray = new Set[n2];
            int[] groupIndex = new int[n2];
            int groupCount = 0;
            Queue<Node> queue = new ArrayDeque<Node>();
            block2: for (int n3 = 0; n3 < n2; ++n3) {
                Node neighbor = node.links.get(n3).getNeighbor(node);
                if (neighbor.getType() != NodeType.Conductor) {
                    if (neighbor.links.isEmpty()) {
                        setArray[n3] = Collections.singleton(neighbor);
                        ++groupCount;
                        continue;
                    }
                    groupIndex[n3] = -1;
                    continue;
                }
                for (int i = 0; i < n3; ++i) {
                    Set<Node> existingGroup = setArray[i];
                    if (existingGroup == null || !existingGroup.contains(neighbor)) continue;
                    groupIndex[n3] = i;
                    continue block2;
                }
                Set<Node> set = Collections.newSetFromMap(new IdentityHashMap());
                queue.add(neighbor);
                set.add(neighbor);
                Node current;
                while ((current = queue.poll()) != null) {
                    for (NodeLink nodeLink : current.links) {
                        Node node2 = nodeLink.getNeighbor(current);
                        if (!set.add(node2) || node2.getType() != NodeType.Conductor) continue;
                        queue.add(node2);
                    }
                }
                assert (!set.contains(node));
                setArray[n3] = set;
                ++groupCount;
            }
            assert (groupCount > 0);
            if (EnergyNetSettings.logGridUpdatesVerbose) {
                IC2.log.debug(LogCategory.EnergyNet, "%d Neighbor connectivity (%d links, %d new grids):", this.uid, n2, groupCount);
                for (int n3 = 0; n3 < n2; ++n3) {
                    Set<Node> group = setArray[n3];
                    if (group != null) {
                        IC2.log.debug(LogCategory.EnergyNet, "%d %d: %s: %s (%d).", this.uid, n3, node.links.get(n3).getNeighbor(node), group, group.size());
                        continue;
                    }
                    IC2.log.debug(LogCategory.EnergyNet, "%d %d: %s contained in %d.", this.uid, n3, node.links.get(n3).getNeighbor(node), groupIndex[n3]);
                }
            }
            if (groupCount <= 1) {
                return;
            }
            for (int n3 = 1; n3 < n2; ++n3) {
                Set<Node> group = setArray[n3];
                if (group == null) continue;
                Grid grid = new Grid(this.enet);
                if (EnergyNetSettings.logGridUpdatesVerbose) {
                    IC2.log.debug(LogCategory.EnergyNet, "%d Moving %d nodes from net %d to new grid %d.", this.uid, group.size(), n3, grid.uid);
                }
                Iterator<Node> nodeIterator = group.iterator();
                while (nodeIterator.hasNext()) {
                    Node node3 = nodeIterator.next();
                    boolean bl = false;
                    if (!node3.links.isEmpty() && node3.nodeType != NodeType.Conductor) {
                        for (int i = 0; i < n3; ++i) {
                            Set<Node> otherGroup = setArray[i];
                            if (otherGroup == null || !otherGroup.contains(node3)) continue;
                            bl = true;
                            break;
                        }
                    }
                    if (bl) {
                        Node node4 = new Node(this.enet.allocateNodeId(), node3.tile, node3.nodeType);
                        if (EnergyNetSettings.logGridUpdatesVerbose) {
                            IC2.log.debug(LogCategory.EnergyNet, "%s Create extra Node %d for %s in grid %d.", this.uid, node4.uid, node3, grid.uid);
                        }
                        node3.tile.addExtraNode(node4);
                        Iterator<NodeLink> linkIterator = node3.links.iterator();
                        while (linkIterator.hasNext()) {
                            NodeLink nodeLink = linkIterator.next();
                            Node node5 = nodeLink.getNeighbor(node3);
                            if (!group.contains(node5)) continue;
                            assert (node5.nodeType == NodeType.Conductor);
                            nodeLink.replaceNode(node3, node4);
                            node4.links.add(nodeLink);
                            linkIterator.remove();
                        }
                        assert (!node4.links.isEmpty());
                        grid.add(node4);
                        assert (node4.getGrid() != null);
                        continue;
                    }
                    if (EnergyNetSettings.logGridUpdatesVerbose) {
                        IC2.log.debug(LogCategory.EnergyNet, "%d Move Node %s to grid %d.", this.uid, node3, grid.uid);
                    }
                    assert (this.nodes.containsKey(node3.uid));
                    this.nodes.remove(node3.uid);
                    node3.clearGrid();
                    grid.add(node3);
                    assert (node3.getGrid() != null);
                }
            }
        }
    }

    void merge(Grid grid, Map<Node, Node> map) {
        if (EnergyNetSettings.logGridUpdatesVerbose) {
            IC2.log.debug(LogCategory.EnergyNet, "%d Merge %s -> %s.", this.uid, grid, this);
        }
        assert (this.enet.hasGrid(grid));
        this.invalidate();
        for (Node node : grid.nodes.values()) {
            boolean bl = false;
            if (node.nodeType != NodeType.Conductor) {
                for (Node node2 : node.tile.nodes) {
                    if (node2.nodeType != node.nodeType || node2.getGrid() != this) continue;
                    if (EnergyNetSettings.logGridUpdatesVerbose) {
                        IC2.log.debug(LogCategory.EnergyNet, "%d Merge Node %s -> %s.", this.uid, node, node2);
                    }
                    bl = true;
                    for (NodeLink nodeLink : node.links) {
                        nodeLink.replaceNode(node, node2);
                        node2.links.add(nodeLink);
                    }
                    node2.tile.removeExtraNode(node);
                    map.put(node, node2);
                    break;
                }
            }
            if (bl) continue;
            if (EnergyNetSettings.logGridUpdatesVerbose) {
                IC2.log.debug(LogCategory.EnergyNet, "%d Add Node %s.", this.uid, node);
            }
            node.clearGrid();
            this.add(node);
            assert (node.getGrid() != null);
        }
        if (EnergyNetSettings.logGridUpdatesVerbose) {
            IC2.log.debug(LogCategory.EnergyNet, "Remove %s.", grid);
        }
        this.enet.removeGrid(grid);
    }

    private void add(Node node) {
        node.setGrid(this);
        Node node2 = this.nodes.put(node.uid, node);
        if (node2 != null) {
            throw new IllegalStateException("duplicate node uid, new " + node + ", old " + node2);
        }
    }

    private void invalidate() {
        this.dirty = true;
    }

    GridInfo getInfo() {
        int n = 0;
        int n2 = Integer.MAX_VALUE;
        int n3 = Integer.MAX_VALUE;
        int n4 = Integer.MAX_VALUE;
        int n5 = Integer.MIN_VALUE;
        int n6 = Integer.MIN_VALUE;
        int n7 = Integer.MIN_VALUE;
        for (Node node : this.nodes.values()) {
            if (node.links.size() > 2) {
                ++n;
            }
            for (IEnergyTile iEnergyTile : node.tile.subTiles) {
                BlockPos blockPos = EnergyNet.instance.getPos(iEnergyTile);
                if (blockPos.getX() < n2) {
                    n2 = blockPos.getX();
                }
                if (blockPos.getY() < n3) {
                    n3 = blockPos.getY();
                }
                if (blockPos.getZ() < n4) {
                    n4 = blockPos.getZ();
                }
                if (blockPos.getX() > n5) {
                    n5 = blockPos.getX();
                }
                if (blockPos.getY() > n6) {
                    n6 = blockPos.getY();
                }
                if (blockPos.getZ() <= n7) continue;
                n7 = blockPos.getZ();
            }
        }
        return new GridInfo(this.uid, this.nodes.size(), n, n2, n3, n4, n5, n6, n7);
    }

    void dumpInfo(String string, PrintStream printStream, PrintStream printStream2) {
        printStream2.printf("%sGrid %d info:%n", string, this.uid);
        printStream2.printf("%s %d nodes%n", string, this.nodes.size());
    }

    void dumpNodeInfo(Node node, String string, PrintStream printStream, PrintStream printStream2) {
        IEnergyTile iEnergyTile = node.getTile().getMainTile();
        printStream2.printf("%sNode %s info:%n", string, node);
        printStream2.printf("%s pos: %s%n", string, Util.formatPosition((BlockGetter)EnergyNet.instance.getWorld(iEnergyTile), EnergyNet.instance.getPos(iEnergyTile)));
        printStream2.printf("%s type: %s%n", new Object[]{string, node.nodeType});
        switch (node.nodeType) {
            case Conductor: {
                break;
            }
            case Sink: {
                IEnergySink sink = (IEnergySink)iEnergyTile;
                printStream2.printf("%s demanded: %.2f%n", string, sink.getDemandedEnergy());
                printStream2.printf("%s tier: %d%n", string, sink.getSinkTier());
                break;
            }
            case Source: {
                IEnergySource source = (IEnergySource)iEnergyTile;
                printStream2.printf("%s offered: %.2f%n", string, source.getOfferedEnergy());
                printStream2.printf("%s tier: %d%n", string, source.getSourceTier());
                break;
            }
        }
        printStream2.printf("%s %d neighbor links:%n", string, node.links.size());
        for (NodeLink nodeLink : node.links) {
            printStream2.printf("%s  %s %.4f %s%n", string, nodeLink.getNeighbor(node), nodeLink.loss, nodeLink.skippedNodes);
        }
        EnergyNetGlobal.getCalculator().dumpNodeInfo(node, string + " ", printStream, printStream2);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void dumpGraph() {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter("graph_" + this.uid + "_raw.txt");
            fileWriter.write("graph nodes {\n  overlap=false;\n");
            Collection<Node> collection = this.nodes.values();
            HashSet<Node> hashSet = new HashSet<Node>();
            for (Node node : collection) {
                fileWriter.write("  \"" + node + "\";\n");
                for (NodeLink nodeLink : node.links) {
                    Node node2 = nodeLink.getNeighbor(node);
                    if (hashSet.contains(node2)) continue;
                    fileWriter.write("  \"" + node + "\" -- \"" + node2 + "\" [label=\"" + nodeLink.loss + "\"];\n");
                }
                hashSet.add(node);
            }
            fileWriter.write("}\n");
        }
        catch (IOException iOException) {
            IC2.log.debug(LogCategory.EnergyNet, iOException, "Graph saving failed.");
        }
        finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            }
            catch (IOException iOException) {}
        }
    }
}


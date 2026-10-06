/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 */
package ic2.core.energy.grid;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.IEnergyNetEventReceiver;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.energy.tile.IMetaDelegate;
import ic2.core.IC2;
import ic2.core.energy.grid.EnergyNetGlobal;
import ic2.core.energy.grid.EnergyNetLocal;
import ic2.core.energy.grid.EnergyNetSettings;
import ic2.core.energy.grid.Grid;
import ic2.core.energy.grid.GridChange;
import ic2.core.energy.grid.Node;
import ic2.core.energy.grid.NodeType;
import ic2.core.energy.grid.Tile;
import ic2.core.util.LogCategory;
import ic2.core.util.Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

class ChangeHandler {
    ChangeHandler() {
    }

    static boolean prepareSync(EnergyNetLocal energyNetLocal, GridChange gridChange) {
        Level level = energyNetLocal.getWorld();
        GridChange.Type type = gridChange.type;
        IEnergyTile iEnergyTile = gridChange.ioTile;
        BlockPos blockPos = gridChange.pos;
        if (EnergyNet.instance.getWorld(iEnergyTile) != level) {
            if (EnergyNetSettings.logGridUpdateIssues) {
                IC2.log.warn(LogCategory.EnergyNet, "Tile %s had the wrong world in grid update (%s)", new Object[]{Util.toString(iEnergyTile, (BlockGetter)energyNetLocal.getWorld(), blockPos), type});
            }
            return false;
        }
        if (type != GridChange.Type.REMOVAL && !EnergyNet.instance.getPos(iEnergyTile).equals((Object)blockPos)) {
            if (EnergyNetSettings.logGridUpdateIssues) {
                IC2.log.warn(LogCategory.EnergyNet, "Tile %s has the wrong position in grid update (%s)", new Object[]{Util.toString(iEnergyTile, (BlockGetter)energyNetLocal.getWorld(), blockPos), type});
            }
            return false;
        }
        if (type != GridChange.Type.REMOVAL && !level.isLoaded(blockPos)) {
            if (EnergyNetSettings.logGridUpdateIssues) {
                IC2.log.warn(LogCategory.EnergyNet, "Tile %s was unloaded in grid update (%s)", new Object[]{Util.toString(iEnergyTile, (BlockGetter)energyNetLocal.getWorld(), blockPos), type});
            }
            return false;
        }
        if (type != GridChange.Type.REMOVAL && iEnergyTile instanceof BlockEntity && ((BlockEntity)iEnergyTile).isRemoved()) {
            if (EnergyNetSettings.logGridUpdateIssues) {
                IC2.log.warn(LogCategory.EnergyNet, "Tile %s was invalidated in grid update (%s)", new Object[]{Util.toString(iEnergyTile, (BlockGetter)energyNetLocal.getWorld(), blockPos), type});
            }
            return false;
        }
        if (EnergyNetSettings.logGridUpdatesVerbose) {
            IC2.log.debug(LogCategory.EnergyNet, "Considering tile %s for grid update (%s)", new Object[]{Util.toString(iEnergyTile, (BlockGetter)energyNetLocal.getWorld(), blockPos), type});
        }
        if (type == GridChange.Type.ADDITION) {
            if (iEnergyTile instanceof IMetaDelegate) {
                gridChange.subTiles = new ArrayList<IEnergyTile>(((IMetaDelegate)iEnergyTile).getSubTiles());
                if (gridChange.subTiles.isEmpty()) {
                    throw new RuntimeException(String.format("Tile %s must return at least 1 sub tile for IMetaDelegate.getSubTiles().", Util.toString(iEnergyTile, (BlockGetter)energyNetLocal.getWorld(), blockPos)));
                }
            } else {
                gridChange.subTiles = Arrays.asList(iEnergyTile);
            }
        }
        return true;
    }

    static void applyAddition(EnergyNetLocal energyNetLocal, IEnergyTile iEnergyTile, BlockPos blockPos, List<IEnergyTile> list, Collection<GridChange> collection) {
        Tile tile;
        if (energyNetLocal.registeredIoTiles.containsKey(iEnergyTile)) {
            if (EnergyNetSettings.logGridUpdateIssues) {
                IC2.log.warn(LogCategory.EnergyNet, "Tile %s is already registered", Util.toString(iEnergyTile, (BlockGetter)energyNetLocal.getWorld(), blockPos));
            }
            return;
        }
        for (IEnergyTile object : list) {
            BlockPos blockPos2 = EnergyNet.instance.getPos(object);
            tile = energyNetLocal.registeredTiles.get(blockPos2);
            if (tile == null) continue;
            IEnergyTile iEnergyTile2 = tile.getMainTile();
            boolean bl = false;
            Iterator<GridChange> iterator = collection.iterator();
            while (iterator.hasNext()) {
                GridChange gridChange = iterator.next();
                if (gridChange.type == GridChange.Type.REMOVAL && gridChange.ioTile == iEnergyTile2) {
                    if (EnergyNetSettings.logGridUpdatesVerbose) {
                        IC2.log.debug(LogCategory.EnergyNet, "Expediting pending removal of %s due to addition conflict.", Util.toString(gridChange.ioTile, (BlockGetter)energyNetLocal.getWorld(), gridChange.pos));
                    }
                    bl = true;
                    iterator.remove();
                    ChangeHandler.applyRemoval(energyNetLocal, gridChange.ioTile, gridChange.pos);
                    assert (!energyNetLocal.registeredTiles.containsKey(blockPos2));
                    break;
                }
                if (gridChange.type != GridChange.Type.ADDITION || gridChange.ioTile != iEnergyTile2) continue;
                break;
            }
            if (bl) continue;
            if (EnergyNetSettings.logGridUpdateIssues) {
                IC2.log.warn(LogCategory.EnergyNet, "Tile %s, sub tile %s addition is conflicting with a previous registration at the same location: %s.", Util.toString(iEnergyTile, (BlockGetter)energyNetLocal.getWorld(), blockPos), Util.toString(object, (BlockGetter)energyNetLocal.getWorld(), blockPos2), iEnergyTile2);
            }
            return;
        }
        if (EnergyNetSettings.logGridUpdatesVerbose) {
            IC2.log.debug(LogCategory.EnergyNet, "Adding tile %s.", Util.toString(iEnergyTile, (BlockGetter)energyNetLocal.getWorld(), blockPos));
        }
        Tile tile2 = new Tile(energyNetLocal, iEnergyTile, list);
        energyNetLocal.registeredIoTiles.put(iEnergyTile, tile2);
        if (iEnergyTile instanceof IEnergySource) {
            energyNetLocal.sources.add(tile2);
        }
        for (IEnergyTile iEnergyTile3 : list) {
            BlockPos subTilePos = EnergyNet.instance.getPos(iEnergyTile3);
            energyNetLocal.registeredTiles.put(subTilePos, tile2);
        }
        ChangeHandler.addTileToGrids(energyNetLocal, tile2);
        for (IEnergyNetEventReceiver iEnergyNetEventReceiver : EnergyNetGlobal.getEventReceivers()) {
            iEnergyNetEventReceiver.onAdd(iEnergyTile);
        }
    }

    /*
     * WARNING - void declaration
     */
    private static void addTileToGrids(EnergyNetLocal energyNetLocal, Tile tile) {
        ArrayList<ic2.core.energy.grid.Node> extraNodes = new ArrayList<ic2.core.energy.grid.Node>();
        IEnergyTile iEnergyTile = tile.getMainTile();
        block4: for (ic2.core.energy.grid.Node node : tile.nodes) {
            if (EnergyNetSettings.logGridUpdatesVerbose) {
                IC2.log.debug(LogCategory.EnergyNet, "Adding node %s.", node);
            }
            ArrayList<ic2.core.energy.grid.Node> neighbors = new ArrayList<ic2.core.energy.grid.Node>();
            for (IEnergyTile subTile : tile.subTiles) {
                for (Direction direction : Util.ALL_DIRS) {
                    BlockPos neighborPos = EnergyNet.instance.getPos(subTile).relative(direction);
                    Tile neighborTile = energyNetLocal.registeredTiles.get(neighborPos);
                    if (neighborTile == null || neighborTile == node.tile) continue;
                    for (ic2.core.energy.grid.Node node2 : neighborTile.nodes) {
                        if (node2.isExtraNode()) continue;
                        IEnergyTile neighborMainTile = node2.tile.getMainTile();
                        boolean emits = false;
                        if ((node.nodeType == NodeType.Source || node.nodeType == NodeType.Conductor) && node2.nodeType != NodeType.Source) {
                            IEnergyEmitter iEnergyEmitter = (IEnergyEmitter)(subTile instanceof IEnergyEmitter ? subTile : iEnergyTile);
                            IEnergyTile acceptorSubTile = neighborTile.getSubTileAt(neighborPos);
                            IEnergyAcceptor acceptor = (IEnergyAcceptor)(acceptorSubTile instanceof IEnergyAcceptor ? acceptorSubTile : neighborMainTile);
                            emits = iEnergyEmitter.emitsEnergyTo((IEnergyAcceptor)neighborMainTile, direction) && acceptor.acceptsEnergyFrom((IEnergyEmitter)iEnergyTile, direction.getOpposite());
                        }
                        boolean accepts = false;
                        if (!(emits || node.nodeType != NodeType.Sink && node.nodeType != NodeType.Conductor || node2.nodeType == NodeType.Sink)) {
                            IEnergyAcceptor acceptor2 = (IEnergyAcceptor)(subTile instanceof IEnergyAcceptor ? subTile : iEnergyTile);
                            IEnergyTile emitterSubTile = neighborTile.getSubTileAt(neighborPos);
                            IEnergyEmitter emitter2 = (IEnergyEmitter)(emitterSubTile instanceof IEnergyEmitter ? emitterSubTile : neighborMainTile);
                            accepts = acceptor2.acceptsEnergyFrom((IEnergyEmitter)neighborMainTile, direction) && emitter2.emitsEnergyTo((IEnergyAcceptor)iEnergyTile, direction.getOpposite());
                        }
                        if (!emits && !accepts) continue;
                        neighbors.add(node2);
                    }
                }
            }
            if (neighbors.isEmpty()) {
                if (EnergyNetSettings.logGridUpdatesVerbose) {
                    IC2.log.debug(LogCategory.EnergyNet, "Creating new grid for %s.", node);
                }
                new Grid(energyNetLocal).add(node, neighbors);
                continue;
            }
            switch (node.nodeType) {
                case Conductor: {
                    Grid grid = null;
                    for (ic2.core.energy.grid.Node node3 : neighbors) {
                        if (node3.nodeType != NodeType.Conductor && !node3.links.isEmpty()) continue;
                        if (EnergyNetSettings.logGridUpdatesVerbose) {
                            IC2.log.debug(LogCategory.EnergyNet, "Using %s for %s with neighbors %s.", node3.getGrid(), node, neighbors);
                        }
                        grid = node3.getGrid();
                        break;
                    }
                    if (grid == null) {
                        if (EnergyNetSettings.logGridUpdatesVerbose) {
                            IC2.log.debug(LogCategory.EnergyNet, "Creating new grid for %s with neighbors %s.", node, neighbors);
                        }
                        grid = new Grid(energyNetLocal);
                    }
                    HashMap<ic2.core.energy.grid.Node, ic2.core.energy.grid.Node> nodeMap = new HashMap<ic2.core.energy.grid.Node, ic2.core.energy.grid.Node>();
                    ListIterator<ic2.core.energy.grid.Node> neighborIterator = neighbors.listIterator();
                    while (neighborIterator.hasNext()) {
                        ic2.core.energy.grid.Node node4 = neighborIterator.next();
                        if (node4.getGrid() == grid) continue;
                        if (node4.nodeType != NodeType.Conductor && !node4.links.isEmpty()) {
                            boolean replaced = false;
                            for (int i = 0; i < neighborIterator.previousIndex(); ++i) {
                                ic2.core.energy.grid.Node existing = neighbors.get(i);
                                if (existing.tile != node4.tile || existing.nodeType != node4.nodeType || existing.getGrid() != grid) continue;
                                if (EnergyNetSettings.logGridUpdatesVerbose) {
                                    IC2.log.debug(LogCategory.EnergyNet, "Using neighbor node %s instead of %s.", existing, neighbors);
                                }
                                replaced = true;
                                neighborIterator.set(existing);
                                break;
                            }
                            if (replaced) continue;
                            if (EnergyNetSettings.logGridUpdatesVerbose) {
                                IC2.log.debug(LogCategory.EnergyNet, "Creating new extra node for neighbor %s.", node4);
                            }
                            node4 = new Node(energyNetLocal.allocateNodeId(), node4.tile, node4.nodeType);
                            node4.tile.addExtraNode(node4);
                            grid.add(node4, Collections.emptyList());
                            neighborIterator.set(node4);
                            assert (node4.getGrid() != null);
                            continue;
                        }
                        grid.merge(node4.getGrid(), nodeMap);
                    }
                    ListIterator<ic2.core.energy.grid.Node> neighborIterator2 = neighbors.listIterator();
                    while (neighborIterator2.hasNext()) {
                        ic2.core.energy.grid.Node node5 = neighborIterator2.next();
                        ic2.core.energy.grid.Node node6 = nodeMap.get(node5);
                        if (node6 != null) {
                            node5 = node6;
                            neighborIterator2.set(node6);
                        }
                        assert (node5.getGrid() == grid);
                    }
                    grid.add(node, neighbors);
                    assert (node.getGrid() != null);
                    break;
                }
                case Sink:
                case Source: {
                    List<List<ic2.core.energy.grid.Node>> groups = new ArrayList<List<ic2.core.energy.grid.Node>>();
                    for (ic2.core.energy.grid.Node node7 : neighbors) {
                        boolean found = false;
                        if (node.nodeType == NodeType.Conductor) {
                            for (List<ic2.core.energy.grid.Node> group : groups) {
                                ic2.core.energy.grid.Node first = group.get(0);
                                if (first.nodeType != NodeType.Conductor || first.getGrid() != node7.getGrid()) continue;
                                group.add(node7);
                                found = true;
                                break;
                            }
                        }
                        if (found) continue;
                        List<ic2.core.energy.grid.Node> newGroup = new ArrayList<ic2.core.energy.grid.Node>();
                        newGroup.add(node7);
                        groups.add(newGroup);
                    }
                    if (EnergyNetSettings.logGridUpdatesVerbose) {
                        IC2.log.debug(LogCategory.EnergyNet, "Neighbor groups detected for %s: %s.", node, groups);
                    }
                    assert (!groups.isEmpty());
                    for (int i = 0; i < groups.size(); ++i) {
                        List<ic2.core.energy.grid.Node> group = groups.get(i);
                        ic2.core.energy.grid.Node node9 = group.get(0);
                        if (node9.nodeType != NodeType.Conductor && !node9.links.isEmpty()) {
                            assert (group.size() == 1);
                            if (EnergyNetSettings.logGridUpdatesVerbose) {
                                IC2.log.debug(LogCategory.EnergyNet, "Creating new extra node for neighbor %s.", node9);
                            }
                            node9 = new Node(energyNetLocal.allocateNodeId(), node9.tile, node9.nodeType);
                            node9.tile.addExtraNode(node9);
                            new Grid(energyNetLocal).add(node9, Collections.emptyList());
                            group.set(0, node9);
                            assert (node9.getGrid() != null);
                        }
                        ic2.core.energy.grid.Node node8;
                        if (i == 0) {
                            node8 = node;
                        } else {
                            if (EnergyNetSettings.logGridUpdatesVerbose) {
                                IC2.log.debug(LogCategory.EnergyNet, "Creating new extra node for %s.", node);
                            }
                            node8 = new Node(energyNetLocal.allocateNodeId(), tile, node.nodeType);
                            node8.setExtraNode(true);
                            extraNodes.add(node8);
                        }
                        node9.getGrid().add(node8, group);
                        assert (node8.getGrid() != null);
                    }
                    continue block4;
                }
            }
            energyNetLocal.addTileToNotify(iEnergyTile);
            for (ic2.core.energy.grid.Node node10 : neighbors) {
                energyNetLocal.addTileToNotify(node10.getTile().getMainTile());
            }
        }
        for (ic2.core.energy.grid.Node node : extraNodes) {
            tile.addExtraNode(node);
        }
    }

    static void applyRemoval(EnergyNetLocal energyNetLocal, IEnergyTile iEnergyTile, BlockPos blockPos) {
        Tile tile = energyNetLocal.registeredIoTiles.remove(iEnergyTile);
        if (tile == null) {
            if (EnergyNetSettings.logGridUpdateIssues) {
                IC2.log.warn(LogCategory.EnergyNet, "Tile %s removal without registration", Util.toString(iEnergyTile, (BlockGetter)energyNetLocal.getWorld(), blockPos));
            }
            return;
        }
        if (EnergyNetSettings.logGridUpdatesVerbose) {
            IC2.log.debug(LogCategory.EnergyNet, "Removing tile %s.", Util.toString(iEnergyTile, (BlockGetter)energyNetLocal.getWorld(), blockPos));
        }
        assert (tile.getMainTile() == iEnergyTile);
        if (iEnergyTile instanceof IEnergySource) {
            energyNetLocal.sources.remove(tile);
        }
        for (IEnergyTile object : tile.subTiles) {
            BlockPos blockPos2 = EnergyNet.instance.getPos(object);
            energyNetLocal.registeredTiles.remove(blockPos2);
        }
        ChangeHandler.removeTileFromGrids(tile);
        energyNetLocal.removeTileToNotify(iEnergyTile);
        for (IEnergyNetEventReceiver iEnergyNetEventReceiver : EnergyNetGlobal.getEventReceivers()) {
            iEnergyNetEventReceiver.onRemove(iEnergyTile);
        }
    }

    private static void removeTileFromGrids(Tile tile) {
        for (Node node : tile.nodes) {
            node.getGrid().remove(node);
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  org.apache.commons.lang3.mutable.MutableObject
 */
package ic2.core.util;

import ic2.core.util.Util;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableObject;

public class AabbUtil {
    public static Direction getIntersection(Vec3 origin, Vec3 direction, AABB bbox, MutableObject<Vec3> intersection) {
        Direction intersectingDirection;
        double length = Util.square(direction.x) + Util.square(direction.y) + Util.square(direction.z);
        if (Math.abs(length - 1.0) > 1.0E-5) {
            length = Math.sqrt(length);
            direction = new Vec3(direction.x / length, direction.y / length, direction.z / length);
        }
        if ((intersectingDirection = AabbUtil.intersects(origin, direction, bbox)) == null) {
            return null;
        }
        Vec3 planeOrigin = direction.x < 0.0 && direction.y < 0.0 && direction.z < 0.0 ? new Vec3(bbox.maxX, bbox.maxY, bbox.maxZ) : (direction.x < 0.0 && direction.y < 0.0 && direction.z >= 0.0 ? new Vec3(bbox.maxX, bbox.maxY, bbox.minZ) : (direction.x < 0.0 && direction.y >= 0.0 && direction.z < 0.0 ? new Vec3(bbox.maxX, bbox.minY, bbox.maxZ) : (direction.x < 0.0 && direction.y >= 0.0 && direction.z >= 0.0 ? new Vec3(bbox.maxX, bbox.minY, bbox.minZ) : (direction.x >= 0.0 && direction.y < 0.0 && direction.z < 0.0 ? new Vec3(bbox.minX, bbox.maxY, bbox.maxZ) : (direction.x >= 0.0 && direction.y < 0.0 && direction.z >= 0.0 ? new Vec3(bbox.minX, bbox.maxY, bbox.minZ) : (direction.x >= 0.0 && direction.y >= 0.0 && direction.z < 0.0 ? new Vec3(bbox.minX, bbox.minY, bbox.maxZ) : new Vec3(bbox.minX, bbox.minY, bbox.minZ)))))));
        Vec3 planeNormalVector = null;
        switch (intersectingDirection) {
            case WEST: 
            case EAST: {
                planeNormalVector = new Vec3(1.0, 0.0, 0.0);
                break;
            }
            case DOWN: 
            case UP: {
                planeNormalVector = new Vec3(0.0, 1.0, 0.0);
                break;
            }
            case NORTH: 
            case SOUTH: {
                planeNormalVector = new Vec3(0.0, 0.0, 1.0);
            }
        }
        if (intersection != null) {
            intersection.setValue(AabbUtil.getIntersectionWithPlane(origin, direction, planeOrigin, planeNormalVector));
        }
        return intersectingDirection;
    }

    public static Direction intersects(Vec3 origin, Vec3 direction, AABB bbox) {
        double[] ray = AabbUtil.getRay(origin, direction);
        if (direction.x < 0.0 && direction.y < 0.0 && direction.z < 0.0) {
            if (origin.x < bbox.minX) {
                return null;
            }
            if (origin.y < bbox.minY) {
                return null;
            }
            if (origin.z < bbox.minZ) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.EF, bbox)) > 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.EH, bbox)) < 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.DH, bbox)) > 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.DC, bbox)) < 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.BC, bbox)) > 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.BF, bbox)) < 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.HG, bbox)) > 0.0 && AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.FG, bbox)) < 0.0) {
                return Direction.SOUTH;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.CG, bbox)) < 0.0) {
                return Direction.UP;
            }
            return Direction.EAST;
        }
        if (direction.x < 0.0 && direction.y < 0.0 && direction.z >= 0.0) {
            if (origin.x < bbox.minX) {
                return null;
            }
            if (origin.y < bbox.minY) {
                return null;
            }
            if (origin.z > bbox.maxZ) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.HG, bbox)) > 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.DH, bbox)) > 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.AD, bbox)) > 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.AB, bbox)) < 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.BF, bbox)) < 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.FG, bbox)) < 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.DC, bbox)) > 0.0 && AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.CG, bbox)) > 0.0) {
                return Direction.EAST;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.BC, bbox)) < 0.0) {
                return Direction.UP;
            }
            return Direction.NORTH;
        }
        if (direction.x < 0.0 && direction.y >= 0.0 && direction.z < 0.0) {
            if (origin.x < bbox.minX) {
                return null;
            }
            if (origin.y > bbox.maxY) {
                return null;
            }
            if (origin.z < bbox.minZ) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.FG, bbox)) > 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.EF, bbox)) > 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.AE, bbox)) > 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.AD, bbox)) < 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.DC, bbox)) < 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.CG, bbox)) < 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.EH, bbox)) > 0.0 && AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.HG, bbox)) > 0.0) {
                return Direction.SOUTH;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.DH, bbox)) < 0.0) {
                return Direction.EAST;
            }
            return Direction.DOWN;
        }
        if (direction.x < 0.0 && direction.y >= 0.0 && direction.z >= 0.0) {
            if (origin.x < bbox.minX) {
                return null;
            }
            if (origin.y > bbox.maxY) {
                return null;
            }
            if (origin.z > bbox.maxZ) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.EH, bbox)) > 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.AE, bbox)) > 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.AB, bbox)) < 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.BC, bbox)) < 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.CG, bbox)) < 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.HG, bbox)) > 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.AD, bbox)) > 0.0 && AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.DH, bbox)) > 0.0) {
                return Direction.DOWN;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.DC, bbox)) < 0.0) {
                return Direction.NORTH;
            }
            return Direction.EAST;
        }
        if (direction.x >= 0.0 && direction.y < 0.0 && direction.z < 0.0) {
            if (origin.x > bbox.maxX) {
                return null;
            }
            if (origin.y < bbox.minY) {
                return null;
            }
            if (origin.z < bbox.minZ) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.AB, bbox)) > 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.AE, bbox)) < 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.EH, bbox)) < 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.HG, bbox)) < 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.CG, bbox)) > 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.BC, bbox)) > 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.EF, bbox)) > 0.0 && AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.BF, bbox)) < 0.0) {
                return Direction.WEST;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.FG, bbox)) < 0.0) {
                return Direction.SOUTH;
            }
            return Direction.UP;
        }
        if (direction.x >= 0.0 && direction.y < 0.0 && direction.z >= 0.0) {
            if (origin.x > bbox.maxX) {
                return null;
            }
            if (origin.y < bbox.minY) {
                return null;
            }
            if (origin.z > bbox.maxZ) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.DC, bbox)) > 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.AD, bbox)) > 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.AE, bbox)) < 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.EF, bbox)) < 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.FG, bbox)) < 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.CG, bbox)) > 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.AB, bbox)) > 0.0 && AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.BC, bbox)) > 0.0) {
                return Direction.NORTH;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.BF, bbox)) < 0.0) {
                return Direction.WEST;
            }
            return Direction.UP;
        }
        if (direction.x >= 0.0 && direction.y >= 0.0 && direction.z < 0.0) {
            if (origin.x > bbox.maxX) {
                return null;
            }
            if (origin.y > bbox.maxY) {
                return null;
            }
            if (origin.z < bbox.minZ) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.BF, bbox)) > 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.AB, bbox)) > 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.AD, bbox)) < 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.DH, bbox)) < 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.HG, bbox)) < 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.FG, bbox)) > 0.0) {
                return null;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.AE, bbox)) > 0.0 && AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.EF, bbox)) > 0.0) {
                return Direction.WEST;
            }
            if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.EH, bbox)) < 0.0) {
                return Direction.DOWN;
            }
            return Direction.SOUTH;
        }
        if (origin.x > bbox.maxX) {
            return null;
        }
        if (origin.y > bbox.maxY) {
            return null;
        }
        if (origin.z > bbox.maxZ) {
            return null;
        }
        if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.EF, bbox)) < 0.0) {
            return null;
        }
        if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.EH, bbox)) > 0.0) {
            return null;
        }
        if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.DH, bbox)) < 0.0) {
            return null;
        }
        if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.DC, bbox)) > 0.0) {
            return null;
        }
        if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.BC, bbox)) < 0.0) {
            return null;
        }
        if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.BF, bbox)) > 0.0) {
            return null;
        }
        if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.AB, bbox)) < 0.0 && AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.AE, bbox)) > 0.0) {
            return Direction.WEST;
        }
        if (AabbUtil.side(ray, AabbUtil.getEdgeRay(Edge.AD, bbox)) < 0.0) {
            return Direction.NORTH;
        }
        return Direction.DOWN;
    }

    private static double[] getRay(Vec3 origin, Vec3 direction) {
        double[] ret = new double[]{origin.x * direction.y - direction.x * origin.y, origin.x * direction.z - direction.x * origin.z, -direction.x, origin.y * direction.z - direction.y * origin.z, -direction.z, direction.y};
        return ret;
    }

    private static double[] getEdgeRay(Edge edge, AABB bbox) {
        switch (edge) {
            case AD: {
                return new double[]{-bbox.minY, -bbox.minZ, -1.0, 0.0, 0.0, 0.0};
            }
            case AB: {
                return new double[]{bbox.minX, 0.0, 0.0, -bbox.minZ, 0.0, 1.0};
            }
            case AE: {
                return new double[]{0.0, bbox.minX, 0.0, bbox.minY, -1.0, 0.0};
            }
            case DC: {
                return new double[]{bbox.maxX, 0.0, 0.0, -bbox.minZ, 0.0, 1.0};
            }
            case DH: {
                return new double[]{0.0, bbox.maxX, 0.0, bbox.minY, -1.0, 0.0};
            }
            case BC: {
                return new double[]{-bbox.maxY, -bbox.minZ, -1.0, 0.0, 0.0, 0.0};
            }
            case BF: {
                return new double[]{0.0, bbox.minX, 0.0, bbox.maxY, -1.0, 0.0};
            }
            case EH: {
                return new double[]{-bbox.minY, -bbox.maxZ, -1.0, 0.0, 0.0, 0.0};
            }
            case EF: {
                return new double[]{bbox.minX, 0.0, 0.0, -bbox.maxZ, 0.0, 1.0};
            }
            case CG: {
                return new double[]{0.0, bbox.maxX, 0.0, bbox.maxY, -1.0, 0.0};
            }
            case FG: {
                return new double[]{-bbox.maxY, -bbox.maxZ, -1.0, 0.0, 0.0, 0.0};
            }
            case HG: {
                return new double[]{bbox.maxX, 0.0, 0.0, -bbox.maxZ, 0.0, 1.0};
            }
        }
        return new double[0];
    }

    private static double side(double[] ray1, double[] ray2) {
        return ray1[2] * ray2[3] + ray1[5] * ray2[1] + ray1[4] * ray2[0] + ray1[1] * ray2[5] + ray1[0] * ray2[4] + ray1[3] * ray2[2];
    }

    private static Vec3 getIntersectionWithPlane(Vec3 origin, Vec3 direction, Vec3 planeOrigin, Vec3 planeNormalVector) {
        double distance = AabbUtil.getDistanceToPlane(origin, direction, planeOrigin, planeNormalVector);
        return new Vec3(origin.x + direction.x * distance, origin.y + direction.y * distance, origin.z + direction.z * distance);
    }

    private static double getDistanceToPlane(Vec3 origin, Vec3 direction, Vec3 planeOrigin, Vec3 planeNormalVector) {
        Vec3 base = new Vec3(planeOrigin.x - origin.x, planeOrigin.y - origin.y, planeOrigin.z - origin.z);
        return AabbUtil.dotProduct(base, planeNormalVector) / AabbUtil.dotProduct(direction, planeNormalVector);
    }

    private static double dotProduct(Vec3 a, Vec3 b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }

    static enum Edge {
        AD,
        AB,
        AE,
        DC,
        DH,
        BC,
        BF,
        EH,
        EF,
        CG,
        FG,
        HG;

    }
}


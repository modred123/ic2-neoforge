/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 */
package ic2.core.util;

import ic2.core.IC2;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class RotationUtil {
    public static Direction rotateByRay(net.minecraft.world.phys.HitResult ray) {
        assert (ray.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK);
        Vec3 hit = ray.getLocation();
        BlockPos pos = ((net.minecraft.world.phys.BlockHitResult)ray).getBlockPos();
        return RotationUtil.rotateByHit(((net.minecraft.world.phys.BlockHitResult)ray).getDirection(), (float)hit.x - (float)pos.getX(), (float)hit.y - (float)pos.getY(), (float)hit.z - (float)pos.getZ());
    }

    public static Direction rotateByHit(Direction facingHit, float hitX, float hitY, float hitZ) {
        switch (facingHit) {
            case DOWN: {
                if (hitX <= 0.25f) {
                    if (hitZ > 0.25f && hitZ < 0.75f) {
                        return Direction.WEST;
                    }
                    return Direction.UP;
                }
                if (hitX > 0.25f && hitX < 0.75f) {
                    if (hitZ <= 0.25f) {
                        return Direction.NORTH;
                    }
                    if (hitZ >= 0.75f) {
                        return Direction.SOUTH;
                    }
                    return Direction.DOWN;
                }
                if (!(hitX >= 0.75f)) break;
                if (hitZ > 0.25f && hitZ < 0.75f) {
                    return Direction.EAST;
                }
                return Direction.UP;
            }
            case UP: {
                if (hitX <= 0.25f) {
                    if (hitZ > 0.25f && hitZ < 0.75f) {
                        return Direction.WEST;
                    }
                    return Direction.DOWN;
                }
                if (hitX > 0.25f && hitX < 0.75f) {
                    if (hitZ <= 0.25f) {
                        return Direction.NORTH;
                    }
                    if (hitZ >= 0.75f) {
                        return Direction.SOUTH;
                    }
                    return Direction.UP;
                }
                if (!(hitX >= 0.75f)) break;
                if (hitZ > 0.25f && hitZ < 0.75f) {
                    return Direction.EAST;
                }
                return Direction.DOWN;
            }
            case NORTH: {
                if (hitX <= 0.25f) {
                    if (hitY > 0.25f && hitY < 0.75f) {
                        return Direction.WEST;
                    }
                    return Direction.SOUTH;
                }
                if (hitX > 0.25f && hitX < 0.75f) {
                    if (hitY <= 0.25f) {
                        return Direction.DOWN;
                    }
                    if (hitY >= 0.75f) {
                        return Direction.UP;
                    }
                    return Direction.NORTH;
                }
                if (!(hitX >= 0.75f)) break;
                if (hitY > 0.25f && hitY < 0.75f) {
                    return Direction.EAST;
                }
                return Direction.SOUTH;
            }
            case SOUTH: {
                if (hitX <= 0.25f) {
                    if (hitY > 0.25f && hitY < 0.75f) {
                        return Direction.WEST;
                    }
                    return Direction.NORTH;
                }
                if (hitX > 0.25f && hitX < 0.75f) {
                    if (hitY <= 0.25f) {
                        return Direction.DOWN;
                    }
                    if (hitY >= 0.75f) {
                        return Direction.UP;
                    }
                    return Direction.SOUTH;
                }
                if (!(hitX >= 0.75f)) break;
                if (hitY > 0.25f && hitY < 0.75f) {
                    return Direction.EAST;
                }
                return Direction.NORTH;
            }
            case WEST: {
                if (hitZ <= 0.25f) {
                    if (hitY > 0.25f && hitY < 0.75f) {
                        return Direction.NORTH;
                    }
                    return Direction.EAST;
                }
                if (hitZ > 0.25f && hitZ < 0.75f) {
                    if (hitY <= 0.25f) {
                        return Direction.DOWN;
                    }
                    if (hitY >= 0.75f) {
                        return Direction.UP;
                    }
                    return Direction.WEST;
                }
                if (!(hitZ >= 0.75f)) break;
                if (hitY > 0.25f && hitY < 0.75f) {
                    return Direction.SOUTH;
                }
                return Direction.EAST;
            }
            case EAST: {
                if (hitZ <= 0.25f) {
                    if (hitY > 0.25f && hitY < 0.75f) {
                        return Direction.NORTH;
                    }
                    return Direction.WEST;
                }
                if (hitZ > 0.25f && hitZ < 0.75f) {
                    if (hitY <= 0.25f) {
                        return Direction.DOWN;
                    }
                    if (hitY >= 0.75f) {
                        return Direction.UP;
                    }
                    return Direction.EAST;
                }
                if (!(hitZ >= 0.75f)) break;
                if (hitY > 0.25f && hitY < 0.75f) {
                    return Direction.SOUTH;
                }
                return Direction.WEST;
            }
        }
        return facingHit;
    }

    public static int[] shuffledFacings() {
        int[] ordinals = new int[]{0, 1, 2, 3, 4, 5};
        for (int i = ordinals.length - 1; i > 0; --i) {
            int index = IC2.random.nextInt(i + 1);
            if (index == i) continue;
            int n = index;
            ordinals[n] = ordinals[n] ^ ordinals[i];
            int n2 = i;
            ordinals[n2] = ordinals[n2] ^ ordinals[index];
            int n3 = index;
            ordinals[n3] = ordinals[n3] ^ ordinals[i];
        }
        return ordinals;
    }
}


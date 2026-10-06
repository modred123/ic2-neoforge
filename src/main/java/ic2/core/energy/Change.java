/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 */
package ic2.core.energy;

import ic2.core.energy.Node;
import net.minecraft.core.Direction;

class Change {
    Node node;
    final Direction dir;
    private double amount;
    private double voltage;

    Change(Node node, Direction direction, double d, double d2) {
        this.node = node;
        this.dir = direction;
        this.setAmount(d);
        this.setVoltage(d2);
    }

    public String toString() {
        return this.node + "@" + this.dir + " " + this.getAmount() + " EU / " + this.voltage + " V";
    }

    double getAmount() {
        // 1.21 迁移修复：CFR 反编译错误（原版为 `return this.amount;`，ic2_src 实证），
        // 与 Tile.getAmount 同款自递归雷。
        return this.amount;
    }

    void setAmount(double d) {
        double d2 = Math.rint(d);
        if (Math.abs(d - d2) < 0.001) {
            d = d2;
        }
        assert (!Double.isInfinite(d) && !Double.isNaN(d));
        this.amount = d;
    }

    double getVoltage() {
        return this.voltage;
    }

    private void setVoltage(double d) {
        double d2 = Math.rint(this.getAmount());
        if (Math.abs(d - d2) < 0.001) {
            d = d2;
        }
        assert (!Double.isInfinite(d) && !Double.isNaN(d));
        this.voltage = d;
    }
}


package ic2.integration.jeirei;

import ic2.core.gui.SlotGrid;

public class SlotPosition {
    private final int x;
    private final int y;
    private final SlotGrid.SlotStyle style;

    public SlotPosition(int x, int y) {
        this(x, y, SlotGrid.SlotStyle.Normal);
    }

    public SlotPosition(SlotPosition position, int xOffset, int yOffset) {
        this(position.x + xOffset, position.y + yOffset, position.style);
    }

    public SlotPosition(int x, int y, SlotGrid.SlotStyle style) {
        this.x = x;
        this.y = y;
        this.style = style;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public SlotGrid.SlotStyle getStyle() {
        return this.style;
    }
}

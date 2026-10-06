/*
 * Decompiled with CFR 0.152.
 */
package ic2.core.gui;

import ic2.core.Ic2Gui;
import ic2.core.gui.AbstractScrollingList;
import ic2.core.gui.MouseButton;
import java.util.ArrayList;
import java.util.List;

public class ScrollableList
extends AbstractScrollingList<ScrollableList, ScrollableList.IListItem> {
    public ScrollableList(Ic2Gui<?> ic2Gui, int n, int n2, int n3, int n4) {
        this(ic2Gui, n, n2, n3, n4, (List<ScrollableList.IListItem>)new ArrayList<ScrollableList.IListItem>());
    }

    public ScrollableList(Ic2Gui<?> ic2Gui, int n, int n2, int n3, int n4, List<ScrollableList.IListItem> list) {
        super(ic2Gui, n, n2, n3, n4, list);
    }

    @Override
    protected boolean onItemClick(ScrollableList.IListItem iListItem, MouseButton mouseButton, int n, int n2) {
        return iListItem.onClick(mouseButton, n, n2);
    }

    public static interface IListItem
    extends AbstractScrollingList.IListItem {
        public boolean onClick(MouseButton var1, int var2, int var3);
    }
}


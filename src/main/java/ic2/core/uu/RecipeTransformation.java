/*
 * Decompiled with CFR 0.152.
 */
package ic2.core.uu;

import ic2.core.uu.LeanItemStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class RecipeTransformation {
    public final double transformCost;
    public List<List<LeanItemStack>> inputs;
    public List<LeanItemStack> outputs;

    public RecipeTransformation(double d, List<List<LeanItemStack>> list, LeanItemStack ... leanItemStackArray) {
        this(d, list, Arrays.asList(leanItemStackArray));
    }

    public RecipeTransformation(double d, List<List<LeanItemStack>> list, List<LeanItemStack> list2) {
        this.transformCost = d;
        this.inputs = list;
        this.outputs = list2;
    }

    protected void merge() {
        ArrayList<List<LeanItemStack>> arrayList = new ArrayList<List<LeanItemStack>>();
        for (List<LeanItemStack> object2 : this.inputs) {
            boolean leanItemStack = false;
            ListIterator<List<LeanItemStack>> listIterator = arrayList.listIterator();
            while (listIterator.hasNext()) {
                List<LeanItemStack> merged = this.mergeEqualLists(object2, listIterator.next());
                if (merged == null) continue;
                leanItemStack = true;
                listIterator.set(merged);
                break;
            }
            if (leanItemStack) continue;
            arrayList.add(object2);
        }
        for (List<LeanItemStack> list : this.inputs) {
            block3: for (List<LeanItemStack> list2 : arrayList) {
                LinkedList<LeanItemStack> working = new LinkedList<LeanItemStack>(list);
                boolean bl = false;
                for (LeanItemStack leanItemStack : list2) {
                    bl = false;
                    Iterator<LeanItemStack> iterator = working.iterator();
                    while (iterator.hasNext()) {
                        LeanItemStack leanItemStack2 = iterator.next();
                        if (!leanItemStack.hasSameItem(leanItemStack2)) continue;
                        bl = true;
                        iterator.remove();
                        break;
                    }
                    if (bl) continue;
                    continue block3;
                }
            }
        }
        this.inputs = arrayList;
        ArrayList<LeanItemStack> arrayList2 = new ArrayList<LeanItemStack>();
        for (LeanItemStack leanItemStack : this.outputs) {
            boolean bl = false;
            ListIterator<LeanItemStack> outIterator = arrayList2.listIterator();
            while (outIterator.hasNext()) {
                LeanItemStack leanItemStack3 = outIterator.next();
                if (!leanItemStack.hasSameItem(leanItemStack3)) continue;
                bl = true;
                outIterator.set(leanItemStack3.copyWithSize(leanItemStack3.getSize() + leanItemStack.getSize()));
                break;
            }
            if (bl) continue;
            arrayList2.add(leanItemStack);
        }
        this.outputs = arrayList2;
    }

    public String toString() {
        return "{ " + this.transformCost + " + " + this.inputs + " -> " + this.outputs + " }";
    }

    private List<LeanItemStack> mergeEqualLists(List<LeanItemStack> list, List<LeanItemStack> list2) {
        if (list.size() != list2.size()) {
            return null;
        }
        ArrayList<LeanItemStack> arrayList = new ArrayList<LeanItemStack>(list.size());
        LinkedList<LeanItemStack> linkedList = new LinkedList<LeanItemStack>(list2);
        for (LeanItemStack leanItemStack : list) {
            boolean bl = false;
            Iterator iterator = linkedList.iterator();
            while (iterator.hasNext()) {
                LeanItemStack leanItemStack2 = (LeanItemStack)iterator.next();
                if (!leanItemStack.hasSameItem(leanItemStack2)) continue;
                bl = true;
                arrayList.add(leanItemStack.copyWithSize(leanItemStack.getSize() + leanItemStack2.getSize()));
                iterator.remove();
                break;
            }
            if (bl) continue;
            return null;
        }
        return arrayList;
    }
}


package com.jaskarth.reflow.optgraph.opto;

import com.jaskarth.reflow.optgraph.CompileUnit;
import com.jaskarth.reflow.optgraph.block.Block;
import com.jaskarth.reflow.optgraph.hash.NodeCache;
import com.jaskarth.reflow.optgraph.nodes.Node;
import com.jaskarth.reflow.optgraph.nodes.vars.SpillLoadNode;
import com.jaskarth.reflow.optgraph.nodes.vars.SpillStoreNode;
import com.jaskarth.reflow.optgraph.nodes.ctrl.CtrlNode;
import com.jaskarth.reflow.optgraph.nodes.ctrl.PhiNode;
import com.jaskarth.reflow.util.ValidationHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Regalloc {
    // This runs on the pre-schedule ideal graph. All nodes with two or more outputs will be disconnected and a local var
    // load/store will be placed in their place.
    public static void spillEarly(CompileUnit unit) {
        NodeCache cache = unit.cache();
        ValidationHelper.assertTrue(!cache.isHashing(), "can't hash at this point");

        for (Node node : new ArrayList<>(cache.live())) {
            if (node instanceof CtrlNode) {
                continue;
            }

            if (node.outs().size() > 1) {
                // rematerialize constants and simple loads
                if (node.remat()) {
//                    System.out.println("REMAT " + imm);

                    List<Node> outs = new ArrayList<>(node.outs());
                    for (int i = 0; i < outs.size(); i++) {
                        // keep first out
                        if (i > 0) {
                            Node out = outs.get(i);
                            List<Node> nodes = new ArrayList<>(out.ins());

                            for (int j = 0; j < nodes.size(); j++) {
                                Node in = nodes.get(j);
                                if (in == node) {
                                    out.replaceInput(j, cache.get(node.copy()));
                                }
                            }
                        }
                    }
                } else {
//                    System.out.println("SPILL " + node);

                    Node store = cache.get(new SpillStoreNode(node));

                    for (Node out : new ArrayList<>(node.outs())) {
                        if (out == store) {
                            continue;
                        }

                        List<Node> nodes = new ArrayList<>(out.ins());

                        for (int i = 0; i < nodes.size(); i++) {
                            Node in = nodes.get(i);
                            if (in == node) {
                                out.replaceInput(i, cache.get(new SpillLoadNode(store)));
                            }
                        }
                    }
                }
            }
        }
    }

    public static void spillMiddle(CompileUnit unit) {
        // Try to find nodes depending on nodes from other blocks, and spill
        for (Node node : new ArrayList<>(unit.cache().live())) {
            if (outOfBlockInsAllowed(node)) {
                // no need to schedule these
                continue;
            }

            for (int i = 1; i < node.ins().size(); i++) {
                if (node.in(0) != node.in(i).in(0)) {
//                    System.out.println("Needs to spill!! " + node + " " + node.in(i) + " " + node.in(0) + " " + node.in(i).in(0) + " ");

                    ValidationHelper.assertTrue(node.in(i).outs().size() == 1, "only 1 out at this point");

                    Node store = unit.cache().get(new SpillStoreNode(node.in(i)));
                    store.addIn(0, node.in(i).in(0));

                    Node load = unit.cache().get(new SpillLoadNode(store));
                    load.addIn(0, node.in(0));

                    node.replaceInput(i, load);
                }
            }
        }
    }

    public static void spillLate(CompileUnit unit) {
        NodeCache cache = unit.cache();
        ValidationHelper.assertTrue(!cache.isHashing(), "can't hash at this point");

//        System.out.println("--------------------");

        // TODO: does visitation order matter?
        while (true) {
            boolean spilled = false;
            for (Block block : unit.blocks().blocks()) {
                if (splitInBlock(unit, block)) {
                    spilled = true;
                    break;
                }
            }

            if (!spilled) {
                break;
            }
        }
    }

    // TODO: guard all these debug messages

    private static boolean splitInBlock(CompileUnit unit, Block block) {
        int idx = block.code().size() - 1;
        Cursor cursor = new Cursor();
        cursor.i = idx;

        HashSet<Node> seen = new HashSet<>();
        while (cursor.i > 0) {
            SpillLocation res = tryFindMisplaced(block, block.code().get(cursor.i), cursor, seen);
            if (res != null) {
                // spill it now
                int i = block.code().indexOf(res.def);
                ValidationHelper.assertTrue(i < res.loc, "def after use?");
                ValidationHelper.assertTrue(i >= 1, "def must be in this block!");

//                System.out.println("Before:");
//                dumpCode(block);
//                System.out.println("-------");

                Node store = unit.cache().get(new SpillStoreNode(res.def));
                store.addIn(0, block.head());

                Node load = unit.cache().get(new SpillLoadNode(store));
                load.addIn(0, block.head());

                block.code().add(res.loc + 1, load);
                res.use.replaceInput(res.useIdx, load);

                // add this one later to avoid messing up the idx we got from the spilllocation
                // the idx from indexOf should be good because it must be before the loc
                block.code().add(i + 1, store);

//                System.out.println("After:");
//                dumpCode(block);
//                System.out.println("-------");

//                System.out.println("---- LATE SPILLED " + res.def + " for node " + res.use);

                return true;
            } else {
//                System.out.println("FINISHED EXECUTION: ");
                HashSet<Node> nodes = new HashSet<>(block.code());
                nodes.removeAll(seen);
//                System.out.println(nodes);
//                System.out.println("CURSOR " + cursor.i);

                cursor.i--;

//                System.out.println("RESTARTING AT " + cursor.i + " @ " + block.code().get(cursor.i).toString());
            }
        }

        return false;
    }

    private static void dumpCode(Block block) {
        for (int i = 0; i < block.code().size(); i++) {
            System.out.println(i + ": " + block.code().get(i).toString());
        }
    }

    public static class Cursor {
        private int i;
    }

    record SpillLocation(int loc, int useIdx, Node use, Node def) {}

    // only nodes that can sink values are the last node in the block, and SpillStore
    // This can likely be an iterative algorithm, but I was way too lazy to do it that way

    private static SpillLocation tryFindMisplaced(Block block, Node node, Cursor cursor, Set<Node> seen) {
        if (outOfBlockInsAllowed(node)) {
            // no need to schedule inputs for these
            seen.add(node);
            return null;
        }

        // Don't bother traversing the ctrl inputs
        for (int i = node.ins().size() - 1; i >= 1; i--) {
            cursor.i--;
            ValidationHelper.assertTrue(cursor.i >= 0, "must not underflow");
            Node nd = block.code().get(cursor.i);
            Node def = node.ins().get(i);
//            System.out.println("loc " + cursor.i + " idx " + i + " for " + node + " wants " + def + " and found " + nd);
            if (nd == def) {
                // node in correct position, recurse
                SpillLocation res = tryFindMisplaced(block, nd, cursor, seen);
                if (res != null) {
                    return res;
                }
            } else {
                // store? recurse on it, the spill creates another subtree
                if (nd instanceof SpillStoreNode) {
                    SpillLocation res = tryFindMisplaced(block, nd, cursor, seen);
                    if (res != null) {
                        return res;
                    }

                    // bump the pointer, because we still need to search for the def!
                    i++;
                } else {
                    // wrong node completely? this is our selection for the spill
//                    System.out.println("NEEDS TO SPILL!!");
                    return new SpillLocation(cursor.i, i, node, def);
                }
            }
        }

        seen.add(node);
        return null;
    }

    private static boolean outOfBlockInsAllowed(Node node) {
        return node instanceof PhiNode || node instanceof SpillLoadNode || (node instanceof CtrlNode ctrl && ctrl.startsBlock());
    }
}

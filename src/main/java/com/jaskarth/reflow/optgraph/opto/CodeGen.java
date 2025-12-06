package com.jaskarth.reflow.optgraph.opto;

import com.jaskarth.reflow.compile.ClassBuilder;
import com.jaskarth.reflow.compile.MethodBuilder;
import com.jaskarth.reflow.optgraph.CompileUnit;
import com.jaskarth.reflow.optgraph.block.Block;
import com.jaskarth.reflow.optgraph.nodes.Node;
import com.jaskarth.reflow.optgraph.nodes.ctrl.CtrlNode;
import com.jaskarth.reflow.optgraph.nodes.ctrl.IfProjNode;
import com.jaskarth.reflow.util.ValidationHelper;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.*;

public class CodeGen {
    public static void generate(CompileUnit unit, MethodBuilder method) {
        // Linearize blocks
        List<Block> blocks = new ArrayList<>();

        scheduleBlock(unit.blocks().start(), blocks);

//        Set<Block> scheduled = new HashSet<>();
//        Deque<Block> q = new ArrayDeque<>();
//        Block start = unit.blocks().start();
//        q.add(start);
//        while (!q.isEmpty()) {
//            for (Iterator<Block> iterator = q.iterator(); iterator.hasNext(); ) {
//                Block block = iterator.next();
//                if (scheduled.contains(block)) {
//                    iterator.remove();
//                    continue;
//                }
//
//                boolean allScheduled = true;
//                for (Block in : block.ins()) {
//                    if (!scheduled.contains(in)) {
//                        allScheduled = false;
//                        break;
//                    }
//                }
//
//                if (allScheduled) {
//                    blocks.add(block);
//                    scheduled.add(block);
//
//                    List<Block> outs = block.outs();
//                    q.addAll(outs);
//                    iterator.remove();
//                }
//            }
//        }

        // Setup block labels
        for (Block block : blocks) {
            block.head().setupCodegen(new Label());
//            System.out.println(block.head().toString());
        }

        // Now, actually generate the code
        method.start();

        for (Block block : blocks) {
            for (Node node : block.code()) {
                node.generate(method);
            }

            // link ifs to successor, now that all their code is generated
            if (block.head() instanceof IfProjNode ifProj) {
                CtrlNode region = null;
                for (Node out : block.head().outs()) {
                    if (out instanceof CtrlNode ctrl && ctrl.startsBlock()) {
                        ValidationHelper.assertTrue(region == null, "only one region out");
                        region = ctrl;
                    }
                }
                if (region != null) {
                    ValidationHelper.assertTrue(region.label != null, "label must not be null");
                    method.insn(new JumpInsnNode(Opcodes.GOTO, region.label));
                }
            }
        }

        method.end();
    }

    // Linear code generate - works because we don't have loops
    private static void scheduleBlock(Block block, List<Block> blocks) {
        blocks.add(block);

        List<Block> outs = block.outs();

        // Generated true of if - don't schedule region yet, let the false do it
        if (block.head() instanceof IfProjNode iproj && iproj.isTrue()) {
            return;
        }

        if (outs.size() == 1) {
            scheduleBlock(outs.getFirst(), blocks);
        } else if (outs.size() == 2) {
            Block b1 = outs.get(0);
            Block b2 = outs.get(1);

            if (b1.head() instanceof IfProjNode iproj && iproj.isTrue()) {
                scheduleBlock(b1, blocks);
                scheduleBlock(b2, blocks);
            } else {
                // Inverted
                ValidationHelper.assertTrue(b2.head() instanceof IfProjNode iproj && iproj.isTrue(), "");
                scheduleBlock(b2, blocks);
                scheduleBlock(b1, blocks);
            }
        }
    }
}

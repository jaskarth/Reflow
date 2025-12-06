package com.jaskarth.reflow.compile;

import com.jaskarth.reflow.optgraph.CompileUnit;
import com.jaskarth.reflow.optgraph.block.Block;
import com.jaskarth.reflow.optgraph.nodes.Node;
import com.jaskarth.reflow.optgraph.nodes.ctrl.PhiNode;
import com.jaskarth.reflow.optgraph.nodes.ctrl.RegionNode;
import com.jaskarth.reflow.optgraph.block.Pack;
import com.jaskarth.reflow.optgraph.nodes.vars.SpillStoreNode;
import com.jaskarth.reflow.util.ValidationHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class DotExporter {
    public static final boolean EXPORT = false;
    private static final Map<String, Integer> NAMES = new HashMap<>();

    private static String toDotLegacyAvoidUsingThis(Node node) {
        Set<Node> seen = new HashSet<>();
        Deque<Node> q = new ArrayDeque<>();
        Map<Node, Integer> ids = new HashMap<>();
        int i = 0;

        q.add(node);

        while (!q.isEmpty()) {
            Node n = q.poll();

            if (seen.add(n)) {
                ids.put(n, i++);

                q.addAll(n.ins());
            }
        }

        StringBuilder sb = new StringBuilder();

        sb.append("digraph G {\n");

        for (Node n : seen) {
            sb.append("  ").append(ids.get(n)).append(" [label=\"").append(n.describe()).append("\"];\n");
            for (Node in : n.ins()) {
                sb.append("  ").append(ids.get(n)).append(" -> ").append(ids.get(in)).append(";\n");
            }
        }

        sb.append("}");

        return sb.toString();
    }

    private static String toDot(CompileUnit unit) {
        Set<Node> live = unit.cache().live();
        Map<Node, Integer> ids = new HashMap<>();
        Set<Node> bad = new HashSet<>();
//        findDead(live, bad);

        int i = 0;
        for (Node node : live) {
            ids.put(node, i++);
        }

        StringBuilder sb = new StringBuilder();

        sb.append("digraph G {\n");

        sb.append("  edge [fontname=Helvetica,fontsize=10];\n");
        sb.append("  rankdir=BT;\n");

        for (Node n : live) {
            sb.append("  ").append(ids.get(n)).append(" [weight=0.2,label=\"").append(n.describe()).append("\"");
            if (n.kind() == Node.Kind.CTRL) {
                sb.append(",shape=box");
            }
            // Node cache debug
//            if (!unit.cache().has(n)) {
//                sb.append(",fillcolor=\"red\",style=filled");
//            }
            if (bad.contains(n)) {
                sb.append(",fillcolor=\"red\",style=filled");
            }
            sb.append("];\n");
            List<Node> inputs = n.ins();
            for (int j = 0; j < inputs.size(); j++) {
                Node in = inputs.get(j);
                if (in == null) {
                    continue;
                }

                String color = "";
                String weight = "0.5";
                if (j == 0 && n instanceof PhiNode) {
                    color = "blue";
                } else if (j == 0 || n instanceof RegionNode) {
                    color = "red";
                    if (n.kind() != Node.Kind.DATA) {
                        weight = "1.5";
                    } else {
                        weight = "1.0";
                    }
                }
                sb.append("  ").append(ids.get(n)).append(" -> ").append(ids.get(in)).append(" [taillabel=\"").append(j).append("\"");
                if (!color.isEmpty()) {
                    sb.append(",color=").append(color);
                }
                if (in instanceof SpillStoreNode) {
                    sb.append(",style=\"dashed\"");
                }
//                sb.append(",weight=").append(weight);
                sb.append("];\n");
            }

            // debug outs
//            for (Node out : n.outs()) {
//                Integer id = ids.get(out);
//                sb.append("  ").append(id).append(" -> ").append(ids.get(n)).append(" [color=green];\n");
//            }
        }

        sb.append("}");

        return sb.toString();
    }

    private static void findDead(Set<Node> live, Set<Node> bad) {
        while (true) {
            boolean changed = false;
            for (Node node : new ArrayList<>(live)) {
                for (Node in : node.ins()) {
                    if (in != null && !live.contains(in)) {
                        live.add(in);
                        bad.add(in);
                        changed = true;
                    }
                }
                for (Node out : node.outs()) {
                    ValidationHelper.assertTrue(out != null, "");
                    if (!live.contains(out)) {
                        live.add(out);
                        bad.add(out);
                        changed = true;
                    }
                }
            }
            if (!changed) {
                break;
            }
        }
    }

    private static String toDot(Pack pack) {
        StringBuilder sb = new StringBuilder();

        sb.append("digraph G {\n");
        sb.append("  rankdir=LR;\n");
        sb.append("  node [shape=record];\n");

        sb.append("  struct [label=\"");
        int i = 0;
        Map<Node, Integer> ids = new HashMap<>();
        for (Node node : pack.get()) {
            if (i > 0) {
                sb.append("|");
            }

            sb.append("<").append(i).append(">");
            ids.put(node, i);

            sb.append(node.describe().replaceAll(" ", "\\\\ "));

            i++;
        }

        sb.append("\"];\n");

        for (Node node : pack.get()) {
            for (Node input : node.ins()) {
                if (input == null) {
                    continue;
                }
                sb.append("  struct:").append(ids.get(node)).append(" -> ").append("struct:").append(ids.get(input)).append(";\n");
            }
        }

        sb.append("}");

        return sb.toString();
    }

    private static String toDot(Block block) {
        Set<Block> seen = new HashSet<>();
        Deque<Block> q = new ArrayDeque<>();
        Map<Block, Integer> ids = new HashMap<>();
        int i = 0;

        q.add(block);

        while (!q.isEmpty()) {
            Block n = q.poll();

            if (seen.add(n)) {
                ids.put(n, i++);

                q.addAll(n.outs());
            }
        }

        StringBuilder sb = new StringBuilder();

        sb.append("digraph G {\n");
        sb.append("  rankdir=LR;\n");
        sb.append("  node [shape=record];\n");

        for (Block b : seen) {
            int id = ids.get(b);

            sb.append("  struct");
            sb.append(id);
            sb.append(" [label=\"");

            int j = 0;
            for (Node node : b.code()) {
                if (j > 0) {
                    sb.append("|");
                }

                sb.append("<").append(j).append(">");

                sb.append(node.describe().replaceAll(" ", "\\\\ "));

                j++;
            }

            sb.append("\"];\n");

            for (Block out : b.outs()) {
                sb.append("  struct");
                sb.append(id);
                sb.append(" -> struct");
                sb.append(ids.get(out));
                sb.append(";\n");
            }
        }

        sb.append("}");

        return sb.toString();
    }

    public static void export(String name, Node node) {
        try {
            if (!Files.exists(Paths.get("dots"))) {
                Files.createDirectory(Paths.get("dots"));
            }

            name += "_" + NAMES.compute(name, (k, v) -> v == null ? 0 : ++v) + ".dot";

            Files.writeString(Paths.get("dots", name), toDotLegacyAvoidUsingThis(node));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void export(String name, CompileUnit unit) {
        if (!DotExporter.EXPORT) {
            return;
        }
        try {
            if (!Files.exists(Paths.get("dots"))) {
                Files.createDirectory(Paths.get("dots"));
            }

            name += "_" + NAMES.compute(name, (k, v) -> v == null ? 0 : ++v) + ".dot";

            Files.writeString(Paths.get("dots", name), toDot(unit));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void export(String name, Pack pack) {
        try {
            if (!Files.exists(Paths.get("dots"))) {
                Files.createDirectory(Paths.get("dots"));
            }

            name += "_" + NAMES.compute(name, (k, v) -> v == null ? 0 : ++v) + ".dot";

            Files.writeString(Paths.get("dots", name), toDot(pack));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void export(String name, Block block) {
        if (!DotExporter.EXPORT) {
            return;
        }
        try {
            if (!Files.exists(Paths.get("dots"))) {
                Files.createDirectory(Paths.get("dots"));
            }

            name += "_" + NAMES.compute(name, (k, v) -> v == null ? 0 : ++v) + ".dot";

            Files.writeString(Paths.get("dots", name), toDot(block));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

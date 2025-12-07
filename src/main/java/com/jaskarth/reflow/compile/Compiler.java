package com.jaskarth.reflow.compile;

import com.jaskarth.reflow.optgraph.CompileUnit;
import com.jaskarth.reflow.optgraph.block.Blocks;
import com.jaskarth.reflow.optgraph.opto.*;
import com.jaskarth.reflow.runtime.ReflowFunc;
import com.jaskarth.reflow.runtime.resource.Resource;
import com.jaskarth.reflow.runtime.resource.ResourceHolder;
import com.jaskarth.reflow.runtime.resource.ResourceLease;
import com.jaskarth.reflow.util.ValidationHelper;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Compiler {
    private static final Map<String, Integer> NAMES = new HashMap<>();

    public static ReflowFunc compile(String name, SharedCompileData shared, DensityFunction func) {
        name = "ReflowCompiled_" + name + "_" + NAMES.compute(name, (k, v) -> v == null ? 0 : ++v);

        StaticCompileData.currentlyCompiling = name;

        CompileUnit unit = Parser.parseFully(shared, func);
        ValidationHelper.invariants(unit);
        DotExporter.export(name + "_Parse", unit);

        Idealize.idealize(unit);
        ValidationHelper.invariants(unit);
        DotExporter.export(name + "_Idealize", unit);

        // No more ideals after this point
        unit.cache().stopHashing();

        Regalloc.spillEarly(unit);
        DotExporter.export(name + "_SplitEarly", unit);

        unit.setDoms(DomGraph.create(unit));
        GlobalCodeMotion.scheduleEarly(unit);
        DotExporter.export(name + "_GCMEarly", unit);
        GlobalCodeMotion.scheduleLate(unit);
        DotExporter.export(name + "_GCMLate", unit);

        Regalloc.spillMiddle(unit);
        DotExporter.export(name + "_SplitMiddle", unit);

        Blocks blocks = LocalCodeMotion.schedule(unit);
        unit.setBlocks(blocks);
        DotExporter.export(name + "_LCM", unit);
        DotExporter.export(name + "_InitialSchedule", blocks.start());
        Regalloc.spillLate(unit);
        DotExporter.export(name + "_LateSchedule", blocks.start());
        VarsAndResources.apply(unit);
        DotExporter.export(name + "_FinalSchedule", blocks.start());
        DotExporter.export(name + "_Final", unit);
        ClassBuilder clazz = new ClassBuilder(name);
        CodeGen.generate(unit, clazz.createMainBuilder());

        return finalizeAndCreate(unit, clazz);
    }

    private static ReflowFunc finalizeAndCreate(CompileUnit unit, ClassBuilder clazz) {
        List<Resource> resources = new ArrayList<>();

        MethodNode init = clazz.createInit();

        int i = 0;
        for (ResourceLease lease : unit.resources().leases()) {
            String desc = lease.type().ref().asDescriptor();
            clazz.clazz().fields.add(new FieldNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, lease.getName(), desc, null, null));
            resources.add(lease.type().creator().get());
            init.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            init.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
            init.instructions.add(new LdcInsnNode(i++));
            init.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
                    "com/jaskarth/reflow/runtime/resource/ResourceHolder", "get", "(I)Lcom/jaskarth/reflow/runtime/resource/Resource;"));

            init.instructions.add(new TypeInsnNode(Opcodes.CHECKCAST, lease.type().ref().get()));

            init.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, clazz.name(), lease.getName(), desc));
        }
        clazz.clazz().fields.add(new FieldNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "holder", "Lcom/jaskarth/reflow/runtime/resource/ResourceHolder;", null, null));
        init.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        init.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        init.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, clazz.name(), "holder", "Lcom/jaskarth/reflow/runtime/resource/ResourceHolder;"));
        init.instructions.add(new InsnNode(Opcodes.RETURN));

        clazz.createRemake();
        clazz.createResources();

        clazz.clazz().accept(new CheckClassAdapter(null));

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        clazz.clazz().accept(writer);

        byte[] bytes = writer.toByteArray();
        try {
            if (DotExporter.EXPORT) {
                if (!Files.exists(Paths.get("compiled"))) {
                    Files.createDirectory(Paths.get("compiled"));
                }

                Files.write(Paths.get("compiled", clazz.name() + ".class"), bytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Class<?> funcClazz = defineClass(clazz.name(), bytes);
            Constructor<?> cons = funcClazz.getConstructor(ResourceHolder.class);
            return (ReflowFunc) cons.newInstance(new ResourceHolder(resources));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Class<?> defineClass(String className, byte[] bytes) throws ClassNotFoundException {
        ClassLoader classLoader = new ClassLoader(Compiler.class.getClassLoader()) {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                if (name.equals(className)) {
                    return super.defineClass(name, bytes, 0, bytes.length);
                }

                return super.loadClass(name);
            }
        };

        return classLoader.loadClass(className);
    }
}

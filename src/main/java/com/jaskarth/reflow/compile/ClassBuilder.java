package com.jaskarth.reflow.compile;

import com.jaskarth.reflow.runtime.ref.Refs;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class ClassBuilder {
    private final String name;
    private final ClassNode clazz;

    public ClassBuilder(String name) {
        this.name = name;
        this.clazz = new ClassNode(Opcodes.ASM9);
        clazz.name = this.name;
        clazz.access = Opcodes.ACC_PUBLIC;
        clazz.interfaces.add("com/jaskarth/reflow/runtime/ReflowFunc");
        clazz.superName = "java/lang/Object";
        clazz.version = Opcodes.V21;
    }

    public MethodBuilder createMainBuilder() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "compute",
                Refs.desc(Refs.D, Refs.FUNCTION_CTX), null, null);
        clazz.methods.add(method);

        return new MethodBuilder(this.name, method);
    }

    public MethodNode createInit() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "(Lcom/jaskarth/reflow/runtime/resource/ResourceHolder;)V", null, null);
        clazz.methods.add(method);
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V"));
        return method;
    }

    public void createRemake() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "create", "(Lcom/jaskarth/reflow/runtime/resource/ResourceHolder;)Lcom/jaskarth/reflow/runtime/ReflowFunc;", null, null);
        clazz.methods.add(method);
        method.instructions.add(new TypeInsnNode(Opcodes.NEW, name));
        method.instructions.add(new InsnNode(Opcodes.DUP));
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, name, "<init>", "(Lcom/jaskarth/reflow/runtime/resource/ResourceHolder;)V"));
        method.instructions.add(new InsnNode(Opcodes.ARETURN));
    }

    public void createResources() {
        MethodNode method = new MethodNode(Opcodes.ACC_PUBLIC, "resources", "()Lcom/jaskarth/reflow/runtime/resource/ResourceHolder;", null, null);
        clazz.methods.add(method);
        method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        method.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, name, "holder", "Lcom/jaskarth/reflow/runtime/resource/ResourceHolder;"));
        method.instructions.add(new InsnNode(Opcodes.ARETURN));
    }

    public String name() {
        return this.name;
    }

    public ClassNode clazz() {
        return clazz;
    }
}

package net.neganote.monilabs.mixin.asm;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public final class ClassNodes {

    private ClassNodes() {}

    public static boolean hasMethod(ClassNode cn, String name, String desc) {
        return findMethod(cn, name, desc) != null;
    }

    public static MethodNode findMethod(ClassNode cn, String name, String desc) {
        for (MethodNode m : cn.methods) {
            if (name.equals(m.name) && desc.equals(m.desc)) return m;
        }
        return null;
    }

    public static boolean hasField(ClassNode cn, String name, String desc) {
        for (FieldNode f : cn.fields) {
            if (name.equals(f.name) && desc.equals(f.desc)) return true;
        }
        return false;
    }
}

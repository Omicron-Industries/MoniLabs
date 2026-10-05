package net.neganote.monilabs.mixin.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class EncodedPatternItemStackComponentPatch implements AsmPatch {

    public static final String TARGET = "appeng/crafting/pattern/EncodedPatternItem";

    private static final String WITH_BOOL = "(Lappeng/api/stacks/GenericStack;Z)Lnet/minecraft/network/chat/Component;";
    private static final String NO_BOOL = "(Lappeng/api/stacks/GenericStack;)Lnet/minecraft/network/chat/Component;";

    @Override
    public String targetInternalName() {
        return TARGET;
    }

    @Override
    public String description() {
        return "EncodedPatternItem.getStackComponent(no bool)";
    }

    @Override
    public boolean isPresent(ClassNode target) {
        return ClassNodes.hasMethod(target, "getStackComponent", NO_BOOL);
    }

    @Override
    public void apply(ClassNode cn) {
        if (isPresent(cn)) return;
        if (!ClassNodes.hasMethod(cn, "getStackComponent", WITH_BOOL)) return;

        MethodNode m = new MethodNode(
                Opcodes.ACC_PROTECTED | Opcodes.ACC_STATIC,
                "getStackComponent",
                NO_BOOL,
                null,
                null);

        InsnList insn = m.instructions;
        insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insn.add(new InsnNode(Opcodes.ICONST_0));
        insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, TARGET, "getStackComponent", WITH_BOOL, false));
        insn.add(new InsnNode(Opcodes.ARETURN));

        m.maxStack = 2;
        m.maxLocals = 1;
        cn.methods.add(m);
    }
}

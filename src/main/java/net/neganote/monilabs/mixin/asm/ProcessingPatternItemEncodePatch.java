package net.neganote.monilabs.mixin.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class ProcessingPatternItemEncodePatch implements AsmPatch {

    public static final String TARGET = "appeng/crafting/pattern/ProcessingPatternItem";

    private static final String NO_AUTHOR = "([Lappeng/api/stacks/GenericStack;[Lappeng/api/stacks/GenericStack;)" +
            "Lnet/minecraft/world/item/ItemStack;";
    private static final String WITH_AUTHOR = "([Lappeng/api/stacks/GenericStack;" +
            "[Lappeng/api/stacks/GenericStack;Ljava/lang/String;)" +
            "Lnet/minecraft/world/item/ItemStack;";

    @Override
    public String targetInternalName() {
        return TARGET;
    }

    @Override
    public String description() {
        return "ProcessingPatternItem.encode(no author)";
    }

    @Override
    public boolean isPresent(ClassNode target) {
        return ClassNodes.hasMethod(target, "encode", NO_AUTHOR);
    }

    @Override
    public void apply(ClassNode cn) {
        if (isPresent(cn)) return;
        if (!ClassNodes.hasMethod(cn, "encode", WITH_AUTHOR)) return;

        MethodNode m = new MethodNode(Opcodes.ACC_PUBLIC, "encode", NO_AUTHOR, null, null);
        InsnList insn = m.instructions;

        insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insn.add(new VarInsnNode(Opcodes.ALOAD, 1));
        insn.add(new VarInsnNode(Opcodes.ALOAD, 2));
        insn.add(new LdcInsnNode(""));

        insn.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, TARGET, "encode", WITH_AUTHOR, false));
        insn.add(new InsnNode(Opcodes.ARETURN));

        m.maxStack = 4;
        m.maxLocals = 3;
        cn.methods.add(m);
    }
}

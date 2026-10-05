package net.neganote.monilabs.mixin.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class CraftingJobStatusPacketCtorPatch implements AsmPatch {

    public static final String TARGET = "appeng/core/sync/packets/CraftingJobStatusPacket";

    private static final String CTOR_AE2 = "(Ljava/util/UUID;Lappeng/api/stacks/AEKey;JJ" +
            "Lappeng/core/sync/packets/CraftingJobStatusPacket$Status;)V";
    private static final String CTOR_AE2CL_6 = "(Ljava/util/UUID;Lappeng/api/stacks/AEKey;JJJ" +
            "Lappeng/core/sync/packets/CraftingJobStatusPacket$Status;)V";
    private static final String CTOR_AE2CL_7 = "(Ljava/util/UUID;Lappeng/api/stacks/AEKey;JJJZ" +
            "Lappeng/core/sync/packets/CraftingJobStatusPacket$Status;)V";

    @Override
    public String targetInternalName() {
        return TARGET;
    }

    @Override
    public String description() {
        return "CraftingJobStatusPacket legacy ctor";
    }

    @Override
    public boolean isPresent(ClassNode target) {
        return ClassNodes.hasMethod(target, "<init>", CTOR_AE2);
    }

    @Override
    public void apply(ClassNode cn) {
        if (isPresent(cn)) return;

        boolean has6 = ClassNodes.hasMethod(cn, "<init>", CTOR_AE2CL_6);
        boolean has7 = ClassNodes.hasMethod(cn, "<init>", CTOR_AE2CL_7);
        if (!has6 && !has7) return;

        MethodNode m = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", CTOR_AE2, null, null);
        InsnList insn = m.instructions;

        insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insn.add(new VarInsnNode(Opcodes.ALOAD, 1));
        insn.add(new VarInsnNode(Opcodes.ALOAD, 2));
        insn.add(new VarInsnNode(Opcodes.LLOAD, 3));
        insn.add(new VarInsnNode(Opcodes.LLOAD, 5));
        insn.add(new InsnNode(Opcodes.LCONST_0));

        if (has6) {
            insn.add(new VarInsnNode(Opcodes.ALOAD, 7));
            insn.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, TARGET, "<init>", CTOR_AE2CL_6, false));
            insn.add(new InsnNode(Opcodes.RETURN));
            m.maxStack = 10;
            m.maxLocals = 8;
        } else {
            insn.add(new InsnNode(Opcodes.ICONST_0));
            insn.add(new VarInsnNode(Opcodes.ALOAD, 7));
            insn.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, TARGET, "<init>", CTOR_AE2CL_7, false));
            insn.add(new InsnNode(Opcodes.RETURN));
            m.maxStack = 11;
            m.maxLocals = 8;
        }

        cn.methods.add(m);
    }
}

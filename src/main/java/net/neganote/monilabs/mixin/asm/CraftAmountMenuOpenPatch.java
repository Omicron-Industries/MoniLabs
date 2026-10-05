package net.neganote.monilabs.mixin.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class CraftAmountMenuOpenPatch implements AsmPatch {

    public static final String TARGET = "appeng/menu/me/crafting/CraftAmountMenu";

    @Override
    public String targetInternalName() {
        return TARGET;
    }

    @Override
    public String description() {
        return "CraftAmountMenu.open(..., int) -> open(..., long) bridge";
    }

    @Override
    public boolean isPresent(ClassNode target) {
        return findOpen(target, Type.INT) != null;
    }

    @Override
    public void apply(ClassNode cn) {
        if (isPresent(cn)) return;

        MethodNode longOpen = findOpen(cn, Type.LONG);
        if (longOpen == null) return;

        Type[] bridgeArgs = Type.getArgumentTypes(longOpen.desc);
        bridgeArgs[3] = Type.INT_TYPE;

        MethodNode bridge = new MethodNode(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "open",
                Type.getMethodDescriptor(Type.VOID_TYPE, bridgeArgs),
                null,
                null);

        InsnList insn = bridge.instructions;
        insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insn.add(new VarInsnNode(Opcodes.ALOAD, 1));
        insn.add(new VarInsnNode(Opcodes.ALOAD, 2));
        insn.add(new VarInsnNode(Opcodes.ILOAD, 3));
        insn.add(new InsnNode(Opcodes.I2L));
        insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, TARGET, "open", longOpen.desc, false));
        insn.add(new InsnNode(Opcodes.RETURN));

        bridge.maxStack = 5;
        bridge.maxLocals = 4;
        cn.methods.add(bridge);
    }

    private static MethodNode findOpen(ClassNode cn, int amountSort) {
        for (MethodNode m : cn.methods) {
            if (!"open".equals(m.name) || (m.access & Opcodes.ACC_STATIC) == 0) continue;
            if (Type.getReturnType(m.desc).getSort() != Type.VOID) continue;

            Type[] args = Type.getArgumentTypes(m.desc);
            if (args.length != 4) continue;

            if (!"Lnet/minecraft/server/level/ServerPlayer;".equals(args[0].getDescriptor())) continue;
            if (args[1].getSort() != Type.OBJECT ||
                    !args[1].getInternalName().startsWith("appeng/menu/locator/"))
                continue;
            if (!"Lappeng/api/stacks/AEKey;".equals(args[2].getDescriptor())) continue;
            if (args[3].getSort() != amountSort) continue;

            return m;
        }
        return null;
    }
}

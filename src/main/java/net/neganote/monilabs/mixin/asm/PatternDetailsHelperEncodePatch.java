package net.neganote.monilabs.mixin.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class PatternDetailsHelperEncodePatch implements AsmPatch {

    public static final String TARGET = "appeng/api/crafting/PatternDetailsHelper";

    private static final String NO_AUTHOR = "(Lnet/minecraft/world/item/crafting/CraftingRecipe;" +
            "[Lnet/minecraft/world/item/ItemStack;" +
            "Lnet/minecraft/world/item/ItemStack;ZZ)" +
            "Lnet/minecraft/world/item/ItemStack;";
    private static final String WITH_AUTHOR = "(Lnet/minecraft/world/item/crafting/CraftingRecipe;" +
            "[Lnet/minecraft/world/item/ItemStack;" +
            "Lnet/minecraft/world/item/ItemStack;ZZLjava/lang/String;)" +
            "Lnet/minecraft/world/item/ItemStack;";

    @Override
    public String targetInternalName() {
        return TARGET;
    }

    @Override
    public String description() {
        return "PatternDetailsHelper.encodeCraftingPattern(no author)";
    }

    @Override
    public boolean isPresent(ClassNode target) {
        return ClassNodes.hasMethod(target, "encodeCraftingPattern", NO_AUTHOR);
    }

    @Override
    public void apply(ClassNode cn) {
        if (isPresent(cn)) return;
        if (!ClassNodes.hasMethod(cn, "encodeCraftingPattern", WITH_AUTHOR)) return;

        MethodNode m = new MethodNode(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "encodeCraftingPattern",
                NO_AUTHOR,
                null,
                null);

        InsnList insn = m.instructions;
        insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insn.add(new VarInsnNode(Opcodes.ALOAD, 1));
        insn.add(new VarInsnNode(Opcodes.ALOAD, 2));
        insn.add(new VarInsnNode(Opcodes.ILOAD, 3));
        insn.add(new VarInsnNode(Opcodes.ILOAD, 4));
        insn.add(new LdcInsnNode(""));

        insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, TARGET, "encodeCraftingPattern", WITH_AUTHOR, false));
        insn.add(new InsnNode(Opcodes.ARETURN));

        m.maxStack = 6;
        m.maxLocals = 5;
        cn.methods.add(m);
    }
}

package net.neganote.monilabs.mixin.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class GuiTextWithConstantPatch implements AsmPatch {

    public static final String TARGET = "appeng/core/localization/GuiText";

    private static final String ENUM_DESC = "L" + TARGET + ";";
    private static final String VALUES_ARRAY_DESC = "[L" + TARGET + ";";
    private static final String CTOR_3 = "(Ljava/lang/String;ILjava/lang/String;)V";
    private static final String CTOR_4 = "(Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V";

    @Override
    public String targetInternalName() {
        return TARGET;
    }

    @Override
    public String description() {
        return "GuiText.With enum constant";
    }

    @Override
    public boolean isPresent(ClassNode target) {
        return ClassNodes.hasField(target, "With", ENUM_DESC);
    }

    @Override
    public void apply(ClassNode cn) {
        if (isPresent(cn)) return;

        String valuesField = findValuesArrayFieldName(cn);
        if (valuesField == null) return;

        String ctorDesc;
        if (ClassNodes.hasMethod(cn, "<init>", CTOR_3)) ctorDesc = CTOR_3;
        else if (ClassNodes.hasMethod(cn, "<init>", CTOR_4)) ctorDesc = CTOR_4;
        else return;

        cn.fields.add(new FieldNode(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_ENUM,
                "With",
                ENUM_DESC,
                null,
                null));

        MethodNode clinit = null;
        for (MethodNode m : cn.methods) {
            if ("<clinit>".equals(m.name)) {
                clinit = m;
                break;
            }
        }
        if (clinit == null) return;

        AbstractInsnNode ret = null;
        for (AbstractInsnNode insn = clinit.instructions.getLast(); insn != null; insn = insn.getPrevious()) {
            if (insn.getOpcode() == Opcodes.RETURN) {
                ret = insn;
                break;
            }
        }
        if (ret == null) return;

        int ordLocal = clinit.maxLocals;
        int oldArrLocal = ordLocal + 1;
        int newArrLocal = ordLocal + 2;
        clinit.maxLocals += 3;

        InsnList patch = new InsnList();

        patch.add(new FieldInsnNode(Opcodes.GETSTATIC, TARGET, valuesField, VALUES_ARRAY_DESC));
        patch.add(new VarInsnNode(Opcodes.ASTORE, oldArrLocal));

        patch.add(new VarInsnNode(Opcodes.ALOAD, oldArrLocal));
        patch.add(new InsnNode(Opcodes.ARRAYLENGTH));
        patch.add(new VarInsnNode(Opcodes.ISTORE, ordLocal));

        patch.add(new TypeInsnNode(Opcodes.NEW, TARGET));
        patch.add(new InsnNode(Opcodes.DUP));
        patch.add(new LdcInsnNode("With"));
        patch.add(new VarInsnNode(Opcodes.ILOAD, ordLocal));
        patch.add(new LdcInsnNode("with"));
        if (CTOR_4.equals(ctorDesc)) {
            patch.add(new LdcInsnNode("gui.ae2"));
        }
        patch.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, TARGET, "<init>", ctorDesc, false));
        patch.add(new FieldInsnNode(Opcodes.PUTSTATIC, TARGET, "With", ENUM_DESC));

        patch.add(new VarInsnNode(Opcodes.ILOAD, ordLocal));
        patch.add(new InsnNode(Opcodes.ICONST_1));
        patch.add(new InsnNode(Opcodes.IADD));
        patch.add(new TypeInsnNode(Opcodes.ANEWARRAY, TARGET));
        patch.add(new VarInsnNode(Opcodes.ASTORE, newArrLocal));

        patch.add(new VarInsnNode(Opcodes.ALOAD, oldArrLocal));
        patch.add(new InsnNode(Opcodes.ICONST_0));
        patch.add(new VarInsnNode(Opcodes.ALOAD, newArrLocal));
        patch.add(new InsnNode(Opcodes.ICONST_0));
        patch.add(new VarInsnNode(Opcodes.ILOAD, ordLocal));
        patch.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "java/lang/System",
                "arraycopy",
                "(Ljava/lang/Object;ILjava/lang/Object;II)V",
                false));

        patch.add(new VarInsnNode(Opcodes.ALOAD, newArrLocal));
        patch.add(new VarInsnNode(Opcodes.ILOAD, ordLocal));
        patch.add(new FieldInsnNode(Opcodes.GETSTATIC, TARGET, "With", ENUM_DESC));
        patch.add(new InsnNode(Opcodes.AASTORE));

        patch.add(new VarInsnNode(Opcodes.ALOAD, newArrLocal));
        patch.add(new FieldInsnNode(Opcodes.PUTSTATIC, TARGET, valuesField, VALUES_ARRAY_DESC));

        clinit.maxStack = Math.max(clinit.maxStack, 8);
        clinit.instructions.insertBefore(ret, patch);
    }

    private static String findValuesArrayFieldName(ClassNode cn) {
        for (FieldNode f : cn.fields) {
            if (VALUES_ARRAY_DESC.equals(f.desc) && (f.access & Opcodes.ACC_STATIC) != 0) {
                if ((f.access & Opcodes.ACC_SYNTHETIC) != 0) return f.name;
            }
        }
        for (FieldNode f : cn.fields) {
            if (VALUES_ARRAY_DESC.equals(f.desc) && (f.access & Opcodes.ACC_STATIC) != 0) {
                return f.name;
            }
        }
        return null;
    }
}

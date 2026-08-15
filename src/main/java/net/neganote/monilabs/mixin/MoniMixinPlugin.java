package net.neganote.monilabs.mixin;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class MoniMixinPlugin implements IMixinConfigPlugin {

    private static final Logger LOGGER = LogManager.getLogger("MoniLabs-Mixin");
    private static final boolean VERBOSE = Boolean.parseBoolean(System.getProperty("monilabs.mixin.verbose", "false"));

    private static final Set<String> AAE_MIXINS = Set.of(
            "net.neganote.monilabs.mixin.aae.MixinCraftingJobStatusPacketCtorShim",
            "net.neganote.monilabs.mixin.aae.MixinCraftingServiceAAE",
            "net.neganote.monilabs.mixin.aae.MixinEncodedPatternItemShim",
            "net.neganote.monilabs.mixin.aae.MixinGuiTextShim",
            "net.neganote.monilabs.mixin.aae.MixinPatternDetailsHelperShim",
            "net.neganote.monilabs.mixin.aae.MixinProcessingPatternItemShim");

    private static final Set<String> AE2WTLIB_MIXINS = Set.of(
            "net.neganote.monilabs.mixin.ae2wtlib.MixinCraftAmountMenuShim");

    // --- Targets (internal names)
    private static final String PACKET_INTERNAL = "appeng/core/sync/packets/CraftingJobStatusPacket";
    private static final String PDH_INTERNAL = "appeng/api/crafting/PatternDetailsHelper";
    private static final String PPI_INTERNAL = "appeng/crafting/pattern/ProcessingPatternItem";
    private static final String EPI_INTERNAL = "appeng/crafting/pattern/EncodedPatternItem";
    private static final String GUITEXT_INTERNAL = "appeng/core/localization/GuiText";
    private static final String CRAFT_AMOUNT_MENU_INTERNAL = "appeng/menu/me/crafting/CraftAmountMenu";

    // --- Packet ctors
    private static final String CTOR_AE2 = "(Ljava/util/UUID;Lappeng/api/stacks/AEKey;JJLappeng/core/sync/packets/CraftingJobStatusPacket$Status;)V";
    private static final String CTOR_AE2CL_6 = "(Ljava/util/UUID;Lappeng/api/stacks/AEKey;JJJLappeng/core/sync/packets/CraftingJobStatusPacket$Status;)V";
    private static final String CTOR_AE2CL_7 = "(Ljava/util/UUID;Lappeng/api/stacks/AEKey;JJJZLappeng/core/sync/packets/CraftingJobStatusPacket$Status;)V";

    // --- PatternDetailsHelper.encodeCraftingPattern overloads
    private static final String PDH_NO_AUTHOR = "(Lnet/minecraft/world/item/crafting/CraftingRecipe;" +
            "[Lnet/minecraft/world/item/ItemStack;" +
            "Lnet/minecraft/world/item/ItemStack;ZZ)" +
            "Lnet/minecraft/world/item/ItemStack;";
    private static final String PDH_WITH_AUTHOR = "(Lnet/minecraft/world/item/crafting/CraftingRecipe;" +
            "[Lnet/minecraft/world/item/ItemStack;" +
            "Lnet/minecraft/world/item/ItemStack;ZZLjava/lang/String;)" +
            "Lnet/minecraft/world/item/ItemStack;";

    // --- ProcessingPatternItem.encode overloads
    private static final String PPI_ENCODE_NO_AUTHOR = "([Lappeng/api/stacks/GenericStack;[Lappeng/api/stacks/GenericStack;)" +
            "Lnet/minecraft/world/item/ItemStack;";
    private static final String PPI_ENCODE_WITH_AUTHOR = "([Lappeng/api/stacks/GenericStack;[Lappeng/api/stacks/GenericStack;Ljava/lang/String;)" +
            "Lnet/minecraft/world/item/ItemStack;";

    // --- EncodedPatternItem.getStackComponent overloads
    private static final String EPI_GET_STACK_COMPONENT_WITH_BOOL = "(Lappeng/api/stacks/GenericStack;Z)Lnet/minecraft/network/chat/Component;";
    private static final String EPI_GET_STACK_COMPONENT_NO_BOOL = "(Lappeng/api/stacks/GenericStack;)Lnet/minecraft/network/chat/Component;";

    // --- GuiText enum
    private static final String GUITEXT_ENUM_DESC = "L" + GUITEXT_INTERNAL + ";";
    private static final String GUITEXT_VALUES_ARRAY_DESC = "[L" + GUITEXT_INTERNAL + ";";
    private static final String GUITEXT_CTOR_3 = "(Ljava/lang/String;ILjava/lang/String;)V";
    private static final String GUITEXT_CTOR_4 = "(Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)V";

    private static boolean isModLoaded(String modId) {
        try {
            if (ModList.get() == null) {
                return LoadingModList.get().getMods().stream()
                        .map(ModInfo::getModId)
                        .anyMatch(modId::equals);
            } else {
                return ModList.get().isLoaded(modId);
            }
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public void onLoad(String mixinPackage) {
        if (VERBOSE) {
            LOGGER.info("[MoniMixinPlugin] Plugin onLoad: {}", mixinPackage);
        }
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        boolean apply = true;
        if (AAE_MIXINS.contains(mixinClassName)) {
            apply = isModLoaded("advanced_ae");
        }
        if (AE2WTLIB_MIXINS.contains(mixinClassName)) {
            apply = isModLoaded("ae2wtlib");
        }
        boolean isDatagen = System.getProperty("sun.java.command").contains("dataRun");
        if (mixinClassName.toLowerCase().contains("render") && isDatagen) {
            apply = false;
        }
        if (targetClassName.contains("jellysquid.mods.sodium")) {
            apply = isModLoaded("embeddium");
        }
        if (VERBOSE) {
            LOGGER.info("[MoniMixinPlugin] {} {} -> {}", apply ? "APPLY" : "SKIP", mixinClassName, targetClassName);
        }
        return apply;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        final String targetInternal = targetClassName.replace('.', '/');

        try {
            switch (targetInternal) {
                case PACKET_INTERNAL -> {
                    ensurePacketAe2CtorExists(targetClass);
                    logPatch(targetClassName, "CraftingJobStatusPacket legacy ctor",
                            hasMethod(targetClass, "<init>", CTOR_AE2));
                }
                case PDH_INTERNAL -> {
                    ensurePdhNoAuthorOverloadExists(targetClass);
                    logPatch(targetClassName, "PatternDetailsHelper.encodeCraftingPattern(no author)",
                            hasMethod(targetClass, "encodeCraftingPattern", PDH_NO_AUTHOR));
                }
                case PPI_INTERNAL -> {
                    ensurePpiNoAuthorOverloadExists(targetClass);
                    logPatch(targetClassName, "ProcessingPatternItem.encode(no author)",
                            hasMethod(targetClass, "encode", PPI_ENCODE_NO_AUTHOR));
                }
                case EPI_INTERNAL -> {
                    ensureEpiNoBoolOverloadExists(targetClass);
                    logPatch(targetClassName, "EncodedPatternItem.getStackComponent(no bool)",
                            hasMethod(targetClass, "getStackComponent", EPI_GET_STACK_COMPONENT_NO_BOOL));
                }
                case GUITEXT_INTERNAL -> {
                    ensureGuiTextWithEnumConstantExists(targetClass);
                    logPatch(targetClassName, "GuiText.With enum constant",
                            hasField(targetClass, "With", GUITEXT_ENUM_DESC));
                }
                case CRAFT_AMOUNT_MENU_INTERNAL -> {
                    ensureCraftAmountMenuLegacyIntOpenExists(targetClass);
                    logPatch(targetClassName, "CraftAmountMenu.open(..., int) -> open(..., long) bridge",
                            findCraftAmountOpen(targetClass, Type.INT) != null);
                }
                default -> {}
            }
        } catch (Throwable t) {
            LOGGER.error("[MoniMixinPlugin] postApply patch failed for target {}", targetClassName, t);
        }
    }

    // =========================
    // PATCH: CraftingJobStatusPacket ctor
    // =========================
    private static void ensurePacketAe2CtorExists(ClassNode cn) {
        if (hasMethod(cn, "<init>", CTOR_AE2)) return;

        boolean has6 = hasMethod(cn, "<init>", CTOR_AE2CL_6);
        boolean has7 = hasMethod(cn, "<init>", CTOR_AE2CL_7);
        if (!has6 && !has7) return;

        MethodNode m = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", CTOR_AE2, null, null);
        InsnList insn = m.instructions;

        // locals: 0=this, 1=UUID, 2=AEKey, 3=req(long), 5=rem(long), 7=Status
        insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insn.add(new VarInsnNode(Opcodes.ALOAD, 1));
        insn.add(new VarInsnNode(Opcodes.ALOAD, 2));
        insn.add(new VarInsnNode(Opcodes.LLOAD, 3));
        insn.add(new VarInsnNode(Opcodes.LLOAD, 5));
        insn.add(new InsnNode(Opcodes.LCONST_0)); // elapsedTime=0

        if (has6) {
            insn.add(new VarInsnNode(Opcodes.ALOAD, 7));
            insn.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, PACKET_INTERNAL, "<init>", CTOR_AE2CL_6, false));
            insn.add(new InsnNode(Opcodes.RETURN));
            m.maxStack = 10;
            m.maxLocals = 8;
        } else {
            insn.add(new InsnNode(Opcodes.ICONST_0)); // isFollowing=false
            insn.add(new VarInsnNode(Opcodes.ALOAD, 7));
            insn.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, PACKET_INTERNAL, "<init>", CTOR_AE2CL_7, false));
            insn.add(new InsnNode(Opcodes.RETURN));
            m.maxStack = 11;
            m.maxLocals = 8;
        }

        cn.methods.add(m);
    }

    // =========================
    // PATCH: PatternDetailsHelper overload (no author)
    // =========================
    private static void ensurePdhNoAuthorOverloadExists(ClassNode cn) {
        if (hasMethod(cn, "encodeCraftingPattern", PDH_NO_AUTHOR)) return;
        if (!hasMethod(cn, "encodeCraftingPattern", PDH_WITH_AUTHOR)) return;

        MethodNode m = new MethodNode(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "encodeCraftingPattern",
                PDH_NO_AUTHOR,
                null,
                null);

        InsnList insn = m.instructions;
        insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insn.add(new VarInsnNode(Opcodes.ALOAD, 1));
        insn.add(new VarInsnNode(Opcodes.ALOAD, 2));
        insn.add(new VarInsnNode(Opcodes.ILOAD, 3));
        insn.add(new VarInsnNode(Opcodes.ILOAD, 4));
        insn.add(new LdcInsnNode(""));

        insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, PDH_INTERNAL, "encodeCraftingPattern", PDH_WITH_AUTHOR,
                false));
        insn.add(new InsnNode(Opcodes.ARETURN));

        m.maxStack = 6;
        m.maxLocals = 5;
        cn.methods.add(m);
    }

    // =========================
    // PATCH: ProcessingPatternItem.encode overload (no author)
    // =========================
    private static void ensurePpiNoAuthorOverloadExists(ClassNode cn) {
        if (hasMethod(cn, "encode", PPI_ENCODE_NO_AUTHOR)) return;
        if (!hasMethod(cn, "encode", PPI_ENCODE_WITH_AUTHOR)) return;

        MethodNode m = new MethodNode(Opcodes.ACC_PUBLIC, "encode", PPI_ENCODE_NO_AUTHOR, null, null);
        InsnList insn = m.instructions;

        insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insn.add(new VarInsnNode(Opcodes.ALOAD, 1));
        insn.add(new VarInsnNode(Opcodes.ALOAD, 2));
        insn.add(new LdcInsnNode(""));

        insn.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, PPI_INTERNAL, "encode", PPI_ENCODE_WITH_AUTHOR, false));
        insn.add(new InsnNode(Opcodes.ARETURN));

        m.maxStack = 4;
        m.maxLocals = 3;
        cn.methods.add(m);
    }

    // =========================
    // PATCH: EncodedPatternItem.getStackComponent overload (no bool)
    // =========================
    private static void ensureEpiNoBoolOverloadExists(ClassNode cn) {
        if (hasMethod(cn, "getStackComponent", EPI_GET_STACK_COMPONENT_NO_BOOL)) return;
        if (!hasMethod(cn, "getStackComponent", EPI_GET_STACK_COMPONENT_WITH_BOOL)) return;

        MethodNode m = new MethodNode(
                Opcodes.ACC_PROTECTED | Opcodes.ACC_STATIC,
                "getStackComponent",
                EPI_GET_STACK_COMPONENT_NO_BOOL,
                null,
                null);

        InsnList insn = m.instructions;
        insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insn.add(new InsnNode(Opcodes.ICONST_0));
        insn.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC, EPI_INTERNAL, "getStackComponent", EPI_GET_STACK_COMPONENT_WITH_BOOL, false));
        insn.add(new InsnNode(Opcodes.ARETURN));

        m.maxStack = 2;
        m.maxLocals = 1;
        cn.methods.add(m);
    }

    // =========================
    // PATCH: CraftAmountMenu.open(..., int) compatibility bridge for AE2WTLib
    // =========================
    private static void ensureCraftAmountMenuLegacyIntOpenExists(ClassNode cn) {
        if (findCraftAmountOpen(cn, Type.INT) != null) return;

        MethodNode longOpen = findCraftAmountOpen(cn, Type.LONG);
        if (longOpen == null) return;

        Type[] longArgs = Type.getArgumentTypes(longOpen.desc);
        Type[] bridgeArgs = longArgs.clone();
        bridgeArgs[3] = Type.INT_TYPE;

        String bridgeDesc = Type.getMethodDescriptor(Type.VOID_TYPE, bridgeArgs);

        MethodNode bridge = new MethodNode(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "open",
                bridgeDesc,
                null,
                null);

        InsnList insn = bridge.instructions;
        insn.add(new VarInsnNode(Opcodes.ALOAD, 0)); // ServerPlayer
        insn.add(new VarInsnNode(Opcodes.ALOAD, 1)); // MenuLocator / MenuHostLocator
        insn.add(new VarInsnNode(Opcodes.ALOAD, 2)); // AEKey
        insn.add(new VarInsnNode(Opcodes.ILOAD, 3)); // int initialAmount
        insn.add(new InsnNode(Opcodes.I2L));
        insn.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                CRAFT_AMOUNT_MENU_INTERNAL,
                "open",
                longOpen.desc,
                false));
        insn.add(new InsnNode(Opcodes.RETURN));

        bridge.maxStack = 5;
        bridge.maxLocals = 4;
        cn.methods.add(bridge);
    }

    private static MethodNode findCraftAmountOpen(ClassNode cn, int amountSort) {
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

    // =========================
    // PATCH: GuiText.With enum constant
    // =========================
    private static void ensureGuiTextWithEnumConstantExists(ClassNode cn) {
        if (hasField(cn, "With", GUITEXT_ENUM_DESC)) return;

        String valuesField = findEnumValuesArrayFieldName(cn);
        if (valuesField == null) return;

        String ctorDesc = null;
        if (hasMethod(cn, "<init>", GUITEXT_CTOR_3)) ctorDesc = GUITEXT_CTOR_3;
        else if (hasMethod(cn, "<init>", GUITEXT_CTOR_4)) ctorDesc = GUITEXT_CTOR_4;
        else return;

        cn.fields.add(new FieldNode(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_ENUM,
                "With",
                GUITEXT_ENUM_DESC,
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

        patch.add(new FieldInsnNode(Opcodes.GETSTATIC, GUITEXT_INTERNAL, valuesField, GUITEXT_VALUES_ARRAY_DESC));
        patch.add(new VarInsnNode(Opcodes.ASTORE, oldArrLocal));

        patch.add(new VarInsnNode(Opcodes.ALOAD, oldArrLocal));
        patch.add(new InsnNode(Opcodes.ARRAYLENGTH));
        patch.add(new VarInsnNode(Opcodes.ISTORE, ordLocal));

        patch.add(new TypeInsnNode(Opcodes.NEW, GUITEXT_INTERNAL));
        patch.add(new InsnNode(Opcodes.DUP));
        patch.add(new LdcInsnNode("With"));
        patch.add(new VarInsnNode(Opcodes.ILOAD, ordLocal));
        patch.add(new LdcInsnNode("with"));
        if (GUITEXT_CTOR_4.equals(ctorDesc)) {
            patch.add(new LdcInsnNode("gui.ae2"));
        }
        patch.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, GUITEXT_INTERNAL, "<init>", ctorDesc, false));
        patch.add(new FieldInsnNode(Opcodes.PUTSTATIC, GUITEXT_INTERNAL, "With", GUITEXT_ENUM_DESC));

        patch.add(new VarInsnNode(Opcodes.ILOAD, ordLocal));
        patch.add(new InsnNode(Opcodes.ICONST_1));
        patch.add(new InsnNode(Opcodes.IADD));
        patch.add(new TypeInsnNode(Opcodes.ANEWARRAY, GUITEXT_INTERNAL));
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
        patch.add(new FieldInsnNode(Opcodes.GETSTATIC, GUITEXT_INTERNAL, "With", GUITEXT_ENUM_DESC));
        patch.add(new InsnNode(Opcodes.AASTORE));

        patch.add(new VarInsnNode(Opcodes.ALOAD, newArrLocal));
        patch.add(new FieldInsnNode(Opcodes.PUTSTATIC, GUITEXT_INTERNAL, valuesField, GUITEXT_VALUES_ARRAY_DESC));

        clinit.maxStack = Math.max(clinit.maxStack, 8);
        clinit.instructions.insertBefore(ret, patch);
    }

    private static String findEnumValuesArrayFieldName(ClassNode cn) {
        for (FieldNode f : cn.fields) {
            if (GUITEXT_VALUES_ARRAY_DESC.equals(f.desc) && (f.access & Opcodes.ACC_STATIC) != 0) {
                if ((f.access & Opcodes.ACC_SYNTHETIC) != 0) return f.name;
            }
        }
        for (FieldNode f : cn.fields) {
            if (GUITEXT_VALUES_ARRAY_DESC.equals(f.desc) && (f.access & Opcodes.ACC_STATIC) != 0) {
                return f.name;
            }
        }
        return null;
    }

    // =========================
    // Helpers
    // =========================
    private static boolean hasMethod(ClassNode cn, String name, String desc) {
        return findMethod(cn, name, desc) != null;
    }

    private static MethodNode findMethod(ClassNode cn, String name, String desc) {
        for (MethodNode m : cn.methods) {
            if (name.equals(m.name) && desc.equals(m.desc)) return m;
        }
        return null;
    }

    private static boolean hasField(ClassNode cn, String name, String desc) {
        for (FieldNode f : cn.fields) {
            if (name.equals(f.name) && desc.equals(f.desc)) return true;
        }
        return false;
    }

    private static void logPatch(String target, String what, boolean ok) {
        if (VERBOSE) {
            LOGGER.info("[MoniMixinPlugin] PATCH {} | {} => {}", target, what, ok ? "OK" : "FAILED");
        }
    }
}

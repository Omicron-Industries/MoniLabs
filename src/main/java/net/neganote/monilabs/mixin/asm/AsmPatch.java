package net.neganote.monilabs.mixin.asm;

import org.objectweb.asm.tree.ClassNode;

public interface AsmPatch {

    String targetInternalName();

    String description();

    void apply(ClassNode target);

    boolean isPresent(ClassNode target);
}

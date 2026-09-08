package net.neganote.monilabs.common.data.materials;

import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.common.data.GTElements;

public class MoniElements {

    public static final Element CrystalMatrix = GTElements.createAndRegister(6, 6, -1, null, "Crystal Matrix", "C☆",
            false);
    public static final Element SculkBioalloy = GTElements.createAndRegister(-1, 481, -1, null, "Sculk Bioalloy", "ᛋ**",
            false);
    public static final Element Eltz = GTElements.createAndRegister(15, 15, -1, null, "Eltz", "Ez", false);
    public static final Element TranscendentalMatrix = GTElements.createAndRegister(6, 32, -1, null,
            "Transcendental Matrix", "ᛝ",
            false);
    public static final Element Mana = GTElements.createAndRegister(-1, 1, -1, null, "Mana", "ᛗ", false);

    public static void init() {}
}

package net.neganote.monilabs.common.data.materials;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.FluidBuilder;
import com.gregtechceu.gtceu.api.fluids.FluidState;

import net.neganote.monilabs.MoniLabs;

public class MoniMaterials {

    public static Material CrystalMatrix;
    public static Material SculkBioalloy;
    public static Material Eltz;
    public static Material TranscendentalMatrix;
    public static Material Mana;

    public static void register() {
        CrystalMatrix = new Material.Builder(MoniLabs.id("crystal_matrix"))
                .ingot()
                .fluid()
                .element(MoniElements.CrystalMatrix)
                .color(0x66ffff)
                .secondaryColor(0x004590)
                .blastTemp(3823, BlastProperty.GasTier.HIGHEST, GTValues.VA[GTValues.ZPM])
                .fluidPipeProperties(4773, 1200, true, false, true, false)
                .flags(MaterialFlags.NO_WORKING, MaterialFlags.EXCLUDE_BLOCK_CRAFTING_RECIPES,
                        MaterialFlags.GENERATE_FOIL, MaterialFlags.GENERATE_FRAME, MaterialFlags.GENERATE_ROTOR)
                .iconSet(MoniMaterialIconSets.Crystal)
                .register();

        SculkBioalloy = new Material.Builder(MoniLabs.id("sculk_bioalloy"))
                .ingot()
                .liquid(new FluidBuilder().state(FluidState.LIQUID).customStill())
                .element(MoniElements.SculkBioalloy)
                .color(0xffffff)
                .iconSet(MoniMaterialIconSets.SculkAlloy)
                .flags(MaterialFlags.EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES,
                        MaterialFlags.EXCLUDE_BLOCK_CRAFTING_RECIPES, MaterialFlags.GENERATE_PLATE,
                        MaterialFlags.GENERATE_ROD, MaterialFlags.GENERATE_RING, MaterialFlags.GENERATE_ROUND,
                        MaterialFlags.GENERATE_GEAR, MaterialFlags.GENERATE_SMALL_GEAR, MaterialFlags.GENERATE_SPRING,
                        MaterialFlags.GENERATE_BOLT_SCREW, MaterialFlags.GENERATE_FRAME, MaterialFlags.NO_SMELTING,
                        MaterialFlags.NO_WORKING)
                .ignoredTagPrefixes(TagPrefix.dustTiny, TagPrefix.dustSmall, TagPrefix.dust)
                .register();

        Eltz = new Material.Builder(MoniLabs.id("eltz"))
                .ingot()
                .liquid(new FluidBuilder().state(FluidState.LIQUID).customStill())
                .element(MoniElements.Eltz)
                .color(0xffffff)
                .iconSet(MoniMaterialIconSets.Eltz)
                .flags(MaterialFlags.GENERATE_PLATE, MaterialFlags.GENERATE_FRAME, MaterialFlags.PHOSPHORESCENT)
                .cableProperties(GTValues.V[GTValues.UEV], 12, 12)
                .register();

        TranscendentalMatrix = new Material.Builder(MoniLabs.id("transcendental_matrix"))
                .ingot()
                .liquid(new FluidBuilder().state(FluidState.LIQUID).customStill())
                .element(MoniElements.TranscendentalMatrix)
                .color(0xffffff).secondaryColor(0x000000)
                .iconSet(MoniMaterialIconSets.TranscendentalMatrix)
                .flags(MaterialFlags.GENERATE_FOIL, MaterialFlags.GENERATE_FRAME, MaterialFlags.GENERATE_GEAR)
                .register();

        Mana = new Material.Builder(MoniLabs.id("mana"))
                .liquid(new FluidBuilder().state(FluidState.LIQUID).customStill())
                .element(MoniElements.Mana)
                .iconSet(MoniMaterialIconSets.Mana)
                .flags(MaterialFlags.DISABLE_DECOMPOSITION)
                .langValue("Primal Mana")
                .register();
    }
}

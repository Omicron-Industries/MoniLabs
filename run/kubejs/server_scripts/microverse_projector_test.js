ServerEvents.recipes(event => {
    event.recipes.gtceu.microverse("normal_microverse_projection")
        .itemInputs("minecraft:iron_ingot")
        .updateMicroverse(1) // Normal
        .addData("projector_tier", 1)
        .EUt(GTValues.VHA[GTValues.IV])
        .duration(20)

    event.recipes.gtceu.microverse("using_normal_microverse")
        .itemInputs("minecraft:gold_ingot")
        .itemOutputs("minecraft:gold_ingot", "minecraft:diamond")
        .requiredMicroverse(1)
        .addData("projector_tier", 1)
        .EUt(GTValues.VHA[GTValues.IV])
        .duration(50) // 100s, like a T1 mission

    event.recipes.gtceu.microverse("normal_to_hostile_microverse")
        .itemInputs("minecraft:rotten_flesh")
        .requiredMicroverse(1) // Normal
        .updateMicroverse(2, true) // Hostile
        .addData("projector_tier", 1)
        .EUt(GTValues.VHA[GTValues.IV])
        .duration(20)

    event.recipes.gtceu.microverse("normal_to_shattered_microverse")
        .itemInputs("minecraft:glass")
        .requiredMicroverse(1) // Normal
        .updateMicroverse(3, true) // Shattered
        .addData("projector_tier", 1)
        .EUt(GTValues.VHA[GTValues.IV])
        .duration(20)

    // One shard drains a full shattered microverse to 0 in ~25s, which turns it corrupted at 50%
    event.recipes.gtceu.microverse("draining_shattered_microverse")
        .itemInputs("minecraft:amethyst_shard")
        .requiredMicroverse(3) // Shattered
        .damageRate(200) // 200/tick of 100k max integrity
        .addData("duration", 600) // read by the projector to scale damage; 0 if missing
        .addData("projector_tier", 1)
        .EUt(GTValues.VHA[GTValues.IV])
        .duration(600)

    // One shard drains corrupted from 50% to 0 in ~12s (plus its passive 10/tick)
    event.recipes.gtceu.microverse("draining_corrupted_microverse")
        .itemInputs("minecraft:amethyst_shard")
        .requiredMicroverse(4) // Corrupted
        .damageRate(200)
        .addData("duration", 600)
        .addData("projector_tier", 1)
        .EUt(GTValues.VHA[GTValues.IV])
        .duration(600)

    // One glowstone repairs corrupted from 50% to 100% in ~13s, glitch gets worse as it heals
    event.recipes.gtceu.microverse("repairing_corrupted_microverse")
        .itemInputs("minecraft:glowstone_dust")
        .requiredMicroverse(4) // Corrupted
        .damageRate(-200) // negative damage heals, clamped at max integrity
        .addData("duration", 600)
        .addData("projector_tier", 1)
        .EUt(GTValues.VHA[GTValues.IV])
        .duration(600)

    event.recipes.gtceu.microverse("shattered_to_corrupted_microverse")
        .itemInputs("minecraft:chorus_fruit")
        .requiredMicroverse(3) // Shattered
        .updateMicroverse(4, true) // Corrupted
        .addData("projector_tier", 1)
        .EUt(GTValues.VHA[GTValues.IV])
        .duration(20)
})
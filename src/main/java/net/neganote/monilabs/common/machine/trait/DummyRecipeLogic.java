package net.neganote.monilabs.common.machine.trait;

import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DummyRecipeLogic extends RecipeLogic {

    @Override
    public void serverTick() {
        // Do nothing! This is so we can register recipes normally but actually do the
        // handling of them in a generic tick subscription in the machine itself.
    }
}

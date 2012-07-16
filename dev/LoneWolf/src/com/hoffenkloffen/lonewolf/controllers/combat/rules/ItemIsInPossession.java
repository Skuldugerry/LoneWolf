package com.hoffenkloffen.lonewolf.controllers.combat.rules;

import com.hoffenkloffen.lonewolf.controllers.combat.CombatState;
import com.hoffenkloffen.lonewolf.models.LoneWolf;

import java.util.Collection;

public class ItemIsInPossession extends BaseRule {
    private String item;

    public ItemIsInPossession(String item) {
        this.item = item;
    }

    @Override
    public boolean match(Collection<CombatState> states) {
        LoneWolf character = getLoneWolf(states);

        if(character == null) return false;

        return character.possess(item);
    }

    @Override
    public String toString() {
        return super.toString() + ": " + item;
    }
}

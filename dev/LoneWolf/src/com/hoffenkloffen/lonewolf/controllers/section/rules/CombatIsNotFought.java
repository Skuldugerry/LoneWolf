package com.hoffenkloffen.lonewolf.controllers.section.rules;

import com.hoffenkloffen.lonewolf.controllers.section.SectionState;

import java.util.Collection;

public class CombatIsNotFought extends CombatIsFought {

    public CombatIsNotFought() {
    }

    public CombatIsNotFought(int index) {
        super(index);
    }

    @Override
    public boolean match(Collection<SectionState> states) {
        return !super.match(states);
    }
}

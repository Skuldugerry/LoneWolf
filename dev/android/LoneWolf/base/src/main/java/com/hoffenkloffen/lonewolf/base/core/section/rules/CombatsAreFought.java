package com.hoffenkloffen.lonewolf.base.core.section.rules;

import com.hoffenkloffen.lonewolf.base.core.abstractions.SectionState;
import com.hoffenkloffen.lonewolf.base.core.combat.CombatResultList;
import com.hoffenkloffen.lonewolf.base.core.combat.Outcome;

import java.util.Collection;

public class CombatsAreFought extends BaseRule {

    @Override
    public boolean match(Collection<SectionState> states) {
        CombatResultList list = getCombatResultList(states);

        return list != null && list.getOutcome() == Outcome.Win;

    }
}

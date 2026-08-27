package org.smartregister.rule;

import android.content.Context;

import com.vijay.jsonwizard.rules.RulesEngineFactory;

import org.jeasy.rules.api.Facts;

import java.util.Map;

public class RevealRuleEngineFactory extends RulesEngineFactory {

    public RevealRuleEngineFactory(Context context, Map<String, String> globalValues){
        super(context, globalValues);
    }
    @Override
    protected Facts initializeFacts(Facts facts) {
        super.initializeFacts(facts);

        facts.put("revealhelper", new RevealRuleEngineHelper());
        return facts;
    }
}

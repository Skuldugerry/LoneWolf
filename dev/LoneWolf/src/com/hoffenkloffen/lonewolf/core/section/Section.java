package com.hoffenkloffen.lonewolf.core.section;

import com.hoffenkloffen.lonewolf.abstractions.Logger;
import com.hoffenkloffen.lonewolf.abstractions.SectionResourceHandler;
import com.hoffenkloffen.lonewolf.core.abstractions.JavascriptInjection;
import com.hoffenkloffen.lonewolf.core.abstractions.SectionRule;
import com.hoffenkloffen.lonewolf.core.abstractions.SectionState;
import com.hoffenkloffen.lonewolf.core.combat.Combat;
import com.hoffenkloffen.lonewolf.core.common.Preferences;
import com.hoffenkloffen.lonewolf.core.items.Item;
import com.hoffenkloffen.lonewolf.util.StringUtil;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Section {

    private SectionResourceHandler resourceHandler;
    private Preferences preferences;
    private Logger logger;

    private String number;
    private boolean omitDefaultRules = false;

    private Map<String, SectionState> states = new Hashtable<String, SectionState>();
    private List<SectionRule> rules = new ArrayList<SectionRule>();
    private Combat combat;
    private List<Item> items = new ArrayList<Item>();

    public Section(String number) {
        setNumber(number);
    }

    public Section(String number, boolean omitDefaultRules) {
        this(number);
        this.omitDefaultRules = omitDefaultRules;
    }

    public String getNumber() {
        return number;
    }

    private void setNumber(String number) {
        this.number = number;
    }

    public boolean omitDefaultRules() {
        return omitDefaultRules;
    }

    public Section set(SectionResourceHandler resourceHandler) {
        this.resourceHandler = resourceHandler;

        return this;
    }

    public Section set(Preferences preferences) {
        this.preferences = preferences;

        return this;
    }

    public Section set(Logger logger) {
        this.logger = logger;

        return this;
    }

    public Section when(SectionRule rule) {
        rules.add(rule);

        return this;
    }

    public Collection<SectionRule> getRules() {
        return rules;
    }

    public Section add(SectionState state) {
        states.put(getSectionStateKey(state), state);

        return this;
    }

    public Collection<SectionState> getStates() {
        return states.values();
    }

    public <T extends SectionState> T getState(Class<T> type) {
        return (T) states.get(getSectionStateKey(type));
    }

    private String getSectionStateKey(SectionState state) {
        return getSectionStateKey(state.getClass());
    }

    private String getSectionStateKey(Class<? extends SectionState> type) {
        return type.getSimpleName();
    }

    public Section set(Combat combat) {
        this.combat = combat;

        return this;
    }

    public Combat getCombat() {
        return combat;
    }

    public Section add(Item item) {
        items.add(item);

        return this;
    }

    public Section add(Item item, int quantity) {
        for (int i = 0; i < quantity; i++) {
            items.add((Item) item.clone());
        }

        return this;
    }

    public void remove(Item item) {
        items.remove(item);
    }

    public Collection<Item> getItems() {
        return items;
    }

    public Item getItem(String item) {
        for (Item i : items) {
            if(i.getName().equals(item)) return i;
        }

        return null;
    }

    public List<String> getChoices() {
        List<String> result = new ArrayList<String>();

        String content = resourceHandler.getHtmlContent(number);

        Pattern p = Pattern.compile("javascript:Choice.turnTo\\((.+)\\);", Pattern.MULTILINE);
        Matcher m = p.matcher(content);

        while (m.find()) {
            result.add(m.group(1));
        }

        return result;
    }

    public List<Illustration> getIllustrations() {
        List<Illustration> result = new ArrayList<Illustration>();

        String content = resourceHandler.getHtmlContent(number);

        if (content == null) return result;

        Pattern p = Pattern.compile("<img alt=\"\" src=\"(.+)\" />", Pattern.MULTILINE);
        Matcher m = p.matcher(content);

        while (m.find()) {
            result.add(new Illustration(m.group(1)));
        }

        return result;
    }

    public String getContent() {
        String template = resourceHandler.getHtmlTemplate();
        String title = resourceHandler.getHtmlTitle(number);
        String revised = Long.toString(System.currentTimeMillis());
        String style = resourceHandler.getHtmlStyle();
        String script = resourceHandler.getHtmlScript();
        String content = resourceHandler.getHtmlContent(number);

        if (template == null) return null;

        // Base64 images
        if (preferences.getIllustrations()) {
            for (Illustration illustration : getIllustrations()) {
                String data = resourceHandler.getBase64Image(illustration);
                content = content.replace(illustration.getFilename(), "data:image/png;base64," + data);
            }
        }

        // Javascript injections
        StringBuilder injections = new StringBuilder();

        for (JavascriptInjection javascriptInjection : getJavascriptInjections()) {
            injections.append(javascriptInjection.getScript(getStates()));
        }

        // NOTE: %1=title, %2=revised, %3=style, %4=script, %5=content, %6=injections
        return String.format(template, title, revised, style, script, content, injections);
    }

    public String getMimeType() {
        return "text/html; charset=UTF-8";
    }

    public String getEncoding() {
        return "UTF-8";
    }

    public void enter() {
        logger.debug(toString());
    }

    public void exit() {
    }

    private Iterable<SectionRule> getJavascriptInjectionRules() {
        List<SectionRule> result = new ArrayList<SectionRule>();

        for (SectionRule rule : getRules()) {
            if (rule.getJavascriptInjection() == null) continue;

            result.add(rule);
        }

        return result;
    }

    private Iterable<JavascriptInjection> getJavascriptInjections() {
        List<JavascriptInjection> result = new ArrayList<JavascriptInjection>();

        for (SectionRule rule : getJavascriptInjectionRules()) {
            if (!rule.match(getStates())) continue;

            result.add(rule.getJavascriptInjection());
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append(getNumber());
        result.append(":\n");

        if (!getStates().isEmpty()) {
            result.append("\tStates; ");
            result.append(StringUtil.toString(getStates()));
            result.append("\n");
        }

        if (!getRules().isEmpty()) {
            result.append("\tRules; ");
            result.append(StringUtil.toString(getRules()));
            result.append("\n");
        }

        if (combat != null) {
            result.append("\tCombat; ");
            result.append(combat.toString());
            result.append("\n");
        }

        if (!getItems().isEmpty()) {
            result.append("\tItems; ");
            result.append(StringUtil.toString(getItems()));
            result.append("\n");
        }

        if (!getIllustrations().isEmpty()) {
            result.append("\tIllustrations; ");
            result.append(StringUtil.toString(getIllustrations()));
            result.append("\n");
        }

        if (getJavascriptInjections().iterator().hasNext()) {
            result.append("\tInjections; ");
            result.append(StringUtil.toString(getJavascriptInjections()));
            result.append("\n");
        }

        return result.toString();
    }
}

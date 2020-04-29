package com.elletrudgett.cards.cah;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class ScrollPanel extends HorizontalLayout {
    private HorizontalLayout content;

    public ScrollPanel(){
        super();
        preparePanel();
    }

    public ScrollPanel(Component... children){
        super();
        preparePanel();
        this.add(children);
    }

    private void preparePanel() {
        getStyle().set("overflow", "auto");
        content = new HorizontalLayout();
        content.setWidth(null);
        content.setHeight("100%");
        super.add(content);
        setWidth("100%");
    }

    public HorizontalLayout getContent(){
        return content;
    }

    @Override
    public void add(Component... components){
        content.add(components);
    }

    @Override
    public void remove(Component... components){
        content.remove(components);
    }

    @Override
    public void removeAll(){
        content.removeAll();
    }

    @Override
    public void addComponentAsFirst(Component component) {
        content.addComponentAtIndex(0, component);
    }
}

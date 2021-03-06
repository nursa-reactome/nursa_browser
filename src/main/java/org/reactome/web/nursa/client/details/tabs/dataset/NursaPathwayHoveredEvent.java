package org.reactome.web.nursa.client.details.tabs.dataset;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Fred Loney <loneyf@ohsu.edu>
 */
public abstract class NursaPathwayHoveredEvent<K> extends GwtEvent<PathwayLoader> {
    private final K key;

    public NursaPathwayHoveredEvent(K key) {
        this.key = key;
    }

    public K getKey() {
        return key;
    }
}

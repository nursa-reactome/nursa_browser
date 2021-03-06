package org.reactome.web.nursa.client.details.tabs.dataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.reactome.web.nursa.analysis.client.model.ComparisonExpressionSummary;
import org.reactome.web.nursa.analysis.client.model.PseudoPathwaySummary;

public abstract class ComparisonPartition<R> {

    private Map<String, List<R>> shared;
    
    private List<List<R>> unshared;

    private List<R> result1;

    private List<R> result2;

    protected ComparisonPartition(List<R> result1, List<R> result2, double filter) {
        this.result1 = result1;
        this.result2 = result2;
        partition(filter);
    }
    
    public void repartition(double filter) {
        partition(filter);
    }

    private void partition(double filter) {
        // Map pathways for the first result.
        Map<String, R> first = new HashMap<String, R>();
        shared = new HashMap<String, List<R>>();
        for (R r1: result1) {
            if (getFdr(r1) < filter) {
                first.put(getKey(r1), r1);
            }
        }
        List<R> second = new ArrayList<R>();
        shared = new HashMap<String, List<R>>();
        // Build the partition.
        for (R r2: result2) {
            String k = getKey(r2);
            if (getFdr(r2) < filter) {
                R r1 = first.get(k);
                if (r1 == null) {
                    second.add(r2);
                } else {
                    first.remove(k);
                    List<R> both = Arrays.asList(r1, r2);
                    shared.put(k, both);
                }
            }
        }
        unshared = Arrays.asList(new ArrayList<R>(first.values()), second);
    }

    abstract protected String getKey(R result);

    abstract protected double getPvalue(R result);

    abstract protected double getFdr(R result);

    /**
     * Returns the results for each cancer type with a disjoint pathways.
     * 
     * @return the {pathway -> [results]} map
     */
    public Map<String, List<R>> getShared() {
        return shared;
    }

    /**
     * Returns the results for each cancer type for the common pathways.
     * @return the unshared results lists
     */
    public List<List<R>> getUnshared() {
        return unshared;
    }

    public PseudoPathwaySummary[] createPseudoPathwaySummary() {
        // Make the unshared pathway summaries.
        List<PseudoPathwaySummary> summaries = new ArrayList<PseudoPathwaySummary>();
        for (int i=0; i < 2; i++) {
            double unsharedPvalue = ComparisonExpressionSummary.UNSHARED_PSEUDO_VALUES[i];
            for (R result : getUnshared().get(i)) {
                PseudoPathwaySummary summary =
                        createPathwaySummary(result, unsharedPvalue);
                summaries.add(summary);
            }
        }
        
        // Make the shared pathway summaries. Since the summary
        // only uses pathway name and id fields, we can create
        // the summary from the first result.
        double sharedPvalue = ComparisonExpressionSummary.SHARED_PSEUDO_VALUE;
        for (List<R> results : getShared().values()) {
            PseudoPathwaySummary summary =
                    createPathwaySummary(results.get(0), sharedPvalue);
            summaries.add(summary);
        }
        
        return summaries.toArray(new PseudoPathwaySummary[summaries.size()]);
    }

    /**
     * Makes the pathway summary with the name, id fields and
     * the given p-value.
     * 
     * @param result the analysis result
     * @param pValue the partition p-value
     * @return the pathway summary
     */
    abstract protected PseudoPathwaySummary createPathwaySummary(R result, double pValue);

}

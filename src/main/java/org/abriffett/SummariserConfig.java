package org.abriffett;

import java.util.List;

/**
 * Config for the summariser, including fields to produce and where to find them.
 */
public class SummariserConfig {

    public SummariserConfig(List<String> metaFields, List<String> metaAttributes) {
        this.metaFields = metaFields;
        this.metaAttributes = metaAttributes;
    }

    /**
     * The fields to include in summarisation
     */
    private List<String> metaFields;

    /** The attributes to use to find these fields in meta tags
     *
     */
    private List<String> metaAttributes;

    public List<String> getMetaFields() {
        return metaFields;
    }

    public List<String> getMetaAttributes() {
        return metaAttributes;
    }
}

package eu.dnetlib.iis.wf.collapsers.basic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import eu.dnetlib.iis.common.citations.schemas.Citation;
import eu.dnetlib.iis.common.citations.schemas.CitationEntry;

/**
 * Collapses {@link Citation} records by position field by merging all fields 
 * taking confidenceLevel into account when handling destinationDocumentId.
 * 
 * It is expected sourceDocumentId is the same for all {@link Citation} objects.
 * 
 * @author mhorst
 */
public class GenericCitationCollapser extends AbstractSimpleCollapser<Citation> {

    /**
     * Total extracted citation texts counter. 
     */
    private Counter totalTextCounter;
    
    /**
     * Documents with at least one extracted citation text counter.
     */
    private Counter docsWithAtLeastOneCitationTextCounter;
    
    /**
     * Total extracted citations counter. 
     */
    private Counter totalMatchedCounter;
    
    /**
     * Documents with at least one extracted citation counter.
     */
    private Counter docsWithAtLeastOneMatchedCitationCounter;
    
    /**
     * Hadoop counters enum of citation records. 
     */
    public static enum CitationTextCounters {
        TOTAL,
        DOCS_WITH_AT_LEAST_ONE_CITATION
    }
    
    /**
     * Hadoop counters enum of matched citation records. 
     */
    public static enum MatchedCitationCounters {
        TOTAL,
        DOCS_WITH_AT_LEAST_ONE_CITATION
    }
    
    // --------------------- LOGIC -------------------------------

    @Override
    public void setup(TaskAttemptContext context) {
        totalTextCounter = context.getCounter(CitationTextCounters.TOTAL);
        totalTextCounter.setValue(0);
        docsWithAtLeastOneCitationTextCounter = context.getCounter(CitationTextCounters.DOCS_WITH_AT_LEAST_ONE_CITATION);
        docsWithAtLeastOneCitationTextCounter.setValue(0);
        totalMatchedCounter = context.getCounter(MatchedCitationCounters.TOTAL);
        totalMatchedCounter.setValue(0);
        docsWithAtLeastOneMatchedCitationCounter = context.getCounter(MatchedCitationCounters.DOCS_WITH_AT_LEAST_ONE_CITATION);
        docsWithAtLeastOneMatchedCitationCounter.setValue(0);
    }
    
    @Override
    protected List<Citation> collapseNonEmpty(List<Citation> objects) {
		Map<Integer, List<Citation>> citationsByPositionMap = new HashMap<Integer, List<Citation>>();
		for (Citation citation : objects) {
			List<Citation> list = citationsByPositionMap.get(citation.getEntry().getPosition());
			if (list==null) {
			    list = new ArrayList<Citation>();
				citationsByPositionMap.put(citation.getEntry().getPosition(), list);
			}
			list.add(citation);
		}
		List<Citation> results = new ArrayList<Citation>(citationsByPositionMap.size());
		int citationsWithTextCount = 0;
		int matchedCitationsCount = 0;
		for (List<Citation> citationsByPosition : citationsByPositionMap.values()) {
		    Citation collapsedCitation = collapseForPosition(citationsByPosition);
		    if (hasTextDefined(collapsedCitation)) {
		        citationsWithTextCount ++;
		    }
		    if (isMatched(collapsedCitation)) {
		        matchedCitationsCount ++;
		    }
			results.add(collapsedCitation);
		}
		if (citationsWithTextCount > 0) {
		    totalTextCounter.increment(citationsWithTextCount);
		    docsWithAtLeastOneCitationTextCounter.increment(1);
		}
		if (matchedCitationsCount > 0) {
		    totalMatchedCounter.increment(matchedCitationsCount);
		    docsWithAtLeastOneMatchedCitationCounter.increment(1);
        }
		return results;
    }

    // --------------------- PRIVATE -------------------------------
    
    /**
     * Checks whether text was defined for given citation.
     */
    private boolean hasTextDefined(Citation citation) {
        return StringUtils.isNotBlank(citation.getEntry().getRawText());
    }

    /**
     * Checks whether text was defined for given citation.
     */
    private boolean isMatched(Citation citation) {
        return StringUtils.isNotBlank(citation.getEntry().getDestinationDocumentId());
    }
    
    /**
     * Collapses citations for the same sourceDocumentId and position.
     * @param objects
     * @return collapsed citation or null when nothing to collapse
     */
    private Citation collapseForPosition(List<Citation> objects) {
    	if (objects.size()==1) {
    		return objects.get(0);
    	} else {
    		Citation resultCandidate = objects.get(0);
    		for (int i=1; i<objects.size(); i++) {
    			resultCandidate = merge(resultCandidate, objects.get(i).getEntry());
    		}
    		return resultCandidate;
    	}
    }
    
    /**
     * Merges existing citation with new citation entry details.
     * @param existingCitation
     * @param newCitationEntry
     * @return existing citation supplemented with new citation entry details
     */
    private Citation merge(Citation existingCitation, CitationEntry newCitationEntry) {
    	if (newCitationEntry != null) {
    		if (newCitationEntry.getDestinationDocumentId()!=null && newCitationEntry.getConfidenceLevel()!=null) {
//    			setting only when not set or when confidence level higher than already stored
//    			important assumption is based on schema assurance: confidenceLevel is always set when destinationDocumentId was set
    			if (existingCitation.getEntry().getDestinationDocumentId()==null ||
    					existingCitation.getEntry().getConfidenceLevel() == null ||
    					existingCitation.getEntry().getConfidenceLevel() < newCitationEntry.getConfidenceLevel()) {
    				existingCitation.getEntry().setDestinationDocumentId(newCitationEntry.getDestinationDocumentId());
    				existingCitation.getEntry().setConfidenceLevel(newCitationEntry.getConfidenceLevel());
    			}
    		}
//    		according to schema externalDestinationDocumentIds cannot be null
    		if (!newCitationEntry.getExternalDestinationDocumentIds().isEmpty()) {
				existingCitation.getEntry().getExternalDestinationDocumentIds().putAll(
						newCitationEntry.getExternalDestinationDocumentIds());
    		}
    		if (newCitationEntry.getRawText()!=null && newCitationEntry.getRawText().length()>0) {
    			existingCitation.getEntry().setRawText(newCitationEntry.getRawText());
    		}
    	}		
    	return existingCitation;
    }
}
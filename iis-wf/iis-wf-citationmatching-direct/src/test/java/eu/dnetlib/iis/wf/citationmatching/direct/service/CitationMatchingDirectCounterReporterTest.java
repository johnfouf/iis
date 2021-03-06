package eu.dnetlib.iis.wf.citationmatching.direct.service;

import com.google.common.collect.Maps;
import eu.dnetlib.iis.common.citations.schemas.Citation;
import eu.dnetlib.iis.common.citations.schemas.CitationEntry;
import eu.dnetlib.iis.common.schemas.ReportEntry;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.sparkutils.avro.SparkAvroSaver;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author madryk
 */
@ExtendWith(MockitoExtension.class)
public class CitationMatchingDirectCounterReporterTest {

    @InjectMocks
    private CitationMatchingDirectCounterReporter counterReporter = new CitationMatchingDirectCounterReporter();
    
    @Mock
    private SparkAvroSaver avroSaver;
    
    
    
    @Mock
    private JavaRDD<Citation> matchedCitations;
    
    @Mock
    private JavaRDD<String> matchedCitationsDocumentIds;
    
    @Mock
    private JavaRDD<String> matchedCitationsDistinctDocumentIds;
    
    @Mock
    private JavaRDD<ReportEntry> reportCounters;
    
    
    @Captor
    private ArgumentCaptor<Function<Citation,String>> extractDocIdFunction;
    
    @Captor
    private ArgumentCaptor<List<ReportEntry>> reportEntriesCaptor;
    
    
    //------------------------ TESTS --------------------------
    
    @Test
    public void report() throws Exception {
        
        // given
        
        JavaSparkContext sparkContext = mock(JavaSparkContext.class);
        String reportPath = "/report/path";
        
        when(matchedCitations.count()).thenReturn(14L);
        
        doReturn(matchedCitationsDocumentIds).when(matchedCitations).map(any());
        when(matchedCitationsDocumentIds.distinct()).thenReturn(matchedCitationsDistinctDocumentIds);
        when(matchedCitationsDistinctDocumentIds.count()).thenReturn(3L);
        
        doReturn(reportCounters).when(sparkContext).parallelize(any(), eq(1));
        
        
        // execute
        
        counterReporter.report(sparkContext, matchedCitations, reportPath);
        
        
        // assert
        
        verify(matchedCitations).map(extractDocIdFunction.capture());
        assertExtractDocIdFunction(extractDocIdFunction.getValue());
        
        verify(sparkContext).parallelize(reportEntriesCaptor.capture(), eq(1));
        assertReportEntries(reportEntriesCaptor.getValue());
        
        verify(avroSaver).saveJavaRDD(reportCounters, ReportEntry.SCHEMA$, reportPath);
        
    }
    
    
    //------------------------ PRIVATE --------------------------
    
    private void assertExtractDocIdFunction(Function<Citation,String> function) throws Exception {
        
        CitationEntry citationEntry = CitationEntry.newBuilder()
                .setDestinationDocumentId("DEST_ID")
                .setPosition(2)
                .setExternalDestinationDocumentIds(Maps.newHashMap())
                .build();
        Citation citation = Citation.newBuilder()
                .setSourceDocumentId("SOURCE_ID")
                .setEntry(citationEntry)
                .build();
        
        
        String docId = function.call(citation);
        
        
        assertEquals("SOURCE_ID", docId);
    }
    
    private void assertReportEntries(List<ReportEntry> reportEntries) {
        
        assertEquals(2, reportEntries.size());
        
        assertEquals("processing.citationMatching.citDocReferences.direct", reportEntries.get(0).getKey());
        assertEquals("14", reportEntries.get(0).getValue());
        
        assertEquals("processing.citationMatching.docs.direct", reportEntries.get(1).getKey());
        assertEquals("3", reportEntries.get(1).getValue());
    }
}

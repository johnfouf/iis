package eu.dnetlib.iis.wf.citationmatching.direct.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Maps;

import eu.dnetlib.iis.common.citations.schemas.Citation;
import eu.dnetlib.iis.common.citations.schemas.CitationEntry;
import eu.dnetlib.iis.common.schemas.ReportParam;
import pl.edu.icm.sparkutils.avro.SparkAvroSaver;

/**
 * @author madryk
 */
@RunWith(MockitoJUnitRunner.class)
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
    private JavaRDD<ReportParam> reportCounters;
    
    
    @Captor
    private ArgumentCaptor<Function<Citation,String>> extractDocIdFunction;
    
    @Captor
    private ArgumentCaptor<List<ReportParam>> reportParamsCaptor;
    
    
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
        
        doReturn(reportCounters).when(sparkContext).parallelize(any());
        
        
        // execute
        
        counterReporter.report(sparkContext, matchedCitations, reportPath);
        
        
        // assert
        
        verify(matchedCitations).map(extractDocIdFunction.capture());
        assertExtractDocIdFunction(extractDocIdFunction.getValue());
        
        verify(sparkContext).parallelize(reportParamsCaptor.capture());
        assertReportParams(reportParamsCaptor.getValue());
        
        verify(avroSaver).saveJavaRDD(reportCounters, ReportParam.SCHEMA$, reportPath);
        
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
    
    private void assertReportParams(List<ReportParam> reportParams) {
        
        assertEquals(2, reportParams.size());
        
        assertEquals("export.matchedCitations.direct.total", reportParams.get(0).getKey());
        assertEquals("14", reportParams.get(0).getValue());
        
        assertEquals("export.matchedCitations.direct.docsWithAtLeastOneMatch", reportParams.get(1).getKey());
        assertEquals("3", reportParams.get(1).getValue());
    }
}
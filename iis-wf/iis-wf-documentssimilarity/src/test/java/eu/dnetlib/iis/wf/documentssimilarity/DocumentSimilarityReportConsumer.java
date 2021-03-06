package eu.dnetlib.iis.wf.documentssimilarity;

import eu.dnetlib.iis.common.java.PortBindings;
import eu.dnetlib.iis.common.java.Process;
import eu.dnetlib.iis.common.java.io.DataStore;
import eu.dnetlib.iis.common.java.io.FileSystemPath;
import eu.dnetlib.iis.common.java.porttype.AvroPortType;
import eu.dnetlib.iis.common.java.porttype.PortType;
import eu.dnetlib.iis.common.schemas.ReportEntry;
import eu.dnetlib.iis.common.schemas.ReportEntryType;
import eu.dnetlib.iis.documentssimilarity.schemas.DocumentSimilarity;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Workflow java node that checks if report generated by document similarity
 * is correct.
 * 
 * @author madryk
 */
public class DocumentSimilarityReportConsumer implements Process {

    private final static String REPORT_PORT_INPUT_NAME = "report";
    
    private final static String DOCUMENT_SIMILARITY_PATH_PROPERTY = "docsim";
    
    private final static String REPORT_DOCUMENT_SIMILARITY_COUNT_KEY = "processing.documentSimilarity.docDocReference";
    
    
    //------------------------ LOGIC --------------------------
    
    public Map<String, PortType> getInputPorts() {
        HashMap<String, PortType> ports = new HashMap<String, PortType>();
        ports.put(REPORT_PORT_INPUT_NAME, new AvroPortType(ReportEntry.SCHEMA$));
        return ports;
    }
    
    @Override
    public Map<String, PortType> getOutputPorts() {
        return new HashMap<String, PortType>();
    }

    @Override
    public void run(PortBindings portBindings, Configuration conf, Map<String, String> parameters) throws Exception {
        
        FileSystem fs = FileSystem.get(conf);
        
        Path docSimilarityPath = new Path(parameters.get(DOCUMENT_SIMILARITY_PATH_PROPERTY));
        
        List<DocumentSimilarity> docSimilarityRecords = DataStore.read(new FileSystemPath(fs, docSimilarityPath), DocumentSimilarity.SCHEMA$);
        int docSimilarityCount = docSimilarityRecords.size();
        
        
        Path reportPath = portBindings.getInput().get(REPORT_PORT_INPUT_NAME);
        
        List<ReportEntry> report = DataStore.read(new FileSystemPath(fs, reportPath), ReportEntry.SCHEMA$);
        
        assertEquals(1, report.size());
        assertEquals(REPORT_DOCUMENT_SIMILARITY_COUNT_KEY, report.get(0).getKey().toString());
        assertEquals(ReportEntryType.COUNTER, report.get(0).getType());
        assertEquals(String.valueOf(docSimilarityCount), report.get(0).getValue().toString());
        
    }
}

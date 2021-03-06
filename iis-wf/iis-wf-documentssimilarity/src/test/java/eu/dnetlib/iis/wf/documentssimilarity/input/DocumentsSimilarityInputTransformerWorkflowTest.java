package eu.dnetlib.iis.wf.documentssimilarity.input;

import eu.dnetlib.iis.common.AbstractOozieWorkflowTestCase;
import eu.dnetlib.iis.common.OozieWorkflowTestConfiguration;
import org.junit.jupiter.api.Test;

/**
 * 
 * @author Michal Oniszczuk (m.oniszczuk@icm.edu.pl)
 *
 */
public class DocumentsSimilarityInputTransformerWorkflowTest extends AbstractOozieWorkflowTestCase {

    @Test
	public void testWorkflow() throws Exception {
    	OozieWorkflowTestConfiguration wf = new OozieWorkflowTestConfiguration();
        wf.setTimeoutInSeconds(720);
        testWorkflow("eu/dnetlib/iis/wf/documentssimilarity/input_transformer/test", wf);
    }

}

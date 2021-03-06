package eu.dnetlib.iis.common.importer;

import org.jdom.Element;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
* @author Łukasz Dumiszewski
*/

public class CermineAffiliationBuilderTest {

    private CermineAffiliationBuilder builder = new CermineAffiliationBuilder();
    
    
    
    //------------------------ TESTS --------------------------
    
    
    @Test
    public void build_NULL() {
        
        // execute
        assertThrows(NullPointerException.class, () -> builder.build(null));
    }
    
    
    @Test
    public void build() {
        
        // given
        
        Element affNode = createAffNode();
        
        
        // execute
        
        CermineAffiliation cAff = builder.build(affNode);
        
        
        // assert
        
        assertEquals("PolandInterdyscyplinarne Centrum ModelowaniaUniwersytet Warszawski Prosta 69, Warszawa", cAff.getRawText());
        assertEquals("Poland", cAff.getCountryName());
        assertEquals("PL", cAff.getCountryCode());
        assertEquals("Prosta 69, Warszawa", cAff.getAddress());
        assertEquals("Interdyscyplinarne Centrum Modelowania, Uniwersytet Warszawski", cAff.getInstitution());
        
    
    }

    
    @Test
    public void build_Country_NULL() {
        
        // given
        
        Element affNode = createAffNode();
        affNode.removeChild("country");
        
        // execute
        
        CermineAffiliation cAff = builder.build(affNode);
        
        
        // assert
        
        assertEquals("Interdyscyplinarne Centrum ModelowaniaUniwersytet Warszawski Prosta 69, Warszawa", cAff.getRawText());
        assertNull(cAff.getCountryName());
        assertNull(cAff.getCountryCode());
        assertEquals("Prosta 69, Warszawa", cAff.getAddress());
        assertEquals("Interdyscyplinarne Centrum Modelowania, Uniwersytet Warszawski", cAff.getInstitution());
        
    
    }
    
    @Test
    public void build_with_label() {
        // given
        
        Element affNode = createAffNode("123");
        
        // execute
        
        CermineAffiliation cAff = builder.build(affNode);
        
        // assert
        assertEquals("PolandInterdyscyplinarne Centrum ModelowaniaUniwersytet Warszawski Prosta 69, Warszawa", cAff.getRawText());
    }
    
    //------------------------ PRIVATE --------------------------

    
    private Element createAffNode() {
        return createAffNode(null);
    }

    private Element createAffNode(String labelText) {
        Element affNode = new Element("aff");
        affNode.setAttribute("id", "someId");

        if (labelText != null) {
            Element label = new Element("label");
            label.setText(labelText);
            affNode.addContent(label);
        }

        Element country = new Element("country");
        country.setText("Poland");
        country.setAttribute("country", "PL");
        affNode.addContent(country);
        
        Element institution1 = new Element("institution");
        institution1.setText("Interdyscyplinarne Centrum Modelowania");
        Element institution2 = new Element("institution");
        institution2.setText("Uniwersytet Warszawski");
        affNode.addContent(institution1);
        affNode.addContent(institution2);
        
        Element address = new Element("addr-line");
        address.setText(" Prosta 69, Warszawa");
        affNode.addContent(address);
        return affNode;
    }
    
}

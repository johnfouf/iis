package eu.dnetlib.iis.wf.citationmatching;

import eu.dnetlib.iis.citationmatching.schemas.Citation;
import eu.dnetlib.iis.wf.citationmatching.converter.MatchedCitationToCitationConverter;
import org.apache.hadoop.io.NullWritable;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.PairFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.coansys.citations.data.IdWithSimilarity;
import pl.edu.icm.coansys.citations.data.MatchableEntity;
import scala.Tuple2;

import static org.junit.jupiter.api.Assertions.assertSame;

import static org.mockito.Mockito.*;

/**
 * @author madryk
 */
@ExtendWith(MockitoExtension.class)
public class CitationOutputConverterTest {

    private CitationOutputConverter citationOutputConverter = new CitationOutputConverter();

    @Mock
    private MatchedCitationToCitationConverter converter;


    @Mock
    private JavaPairRDD<MatchableEntity, IdWithSimilarity> matchedCitations;

    @Mock
    private JavaPairRDD<Citation, NullWritable> convertedMatchedCitations;

    @Captor
    private ArgumentCaptor<PairFunction<Tuple2<MatchableEntity, IdWithSimilarity>, Citation, NullWritable>> convertMatchedCitationFunction;


    @BeforeEach
    public void setup() {
        citationOutputConverter.setConverter(converter);
    }


    //------------------------ TESTS --------------------------

    @Test
    public void convertMatchedCitations() throws Exception {

        // given

        doReturn(convertedMatchedCitations).when(matchedCitations).mapToPair(any());


        // execute

        JavaPairRDD<Citation, NullWritable> retConvertedMatchedCitations =
                citationOutputConverter.convertMatchedCitations(matchedCitations);


        // assert

        assertSame(retConvertedMatchedCitations, convertedMatchedCitations);

        verify(matchedCitations).mapToPair(convertMatchedCitationFunction.capture());
        assertConvertMatchedCitationFunction(convertMatchedCitationFunction.getValue());
    }


    //------------------------ PRIVATE --------------------------

    private void assertConvertMatchedCitationFunction(PairFunction<Tuple2<MatchableEntity, IdWithSimilarity>, Citation, NullWritable> function) throws Exception {

        MatchableEntity entity = mock(MatchableEntity.class);
        IdWithSimilarity idWithSimilarity = mock(IdWithSimilarity.class);
        Citation citation = mock(Citation.class);

        when(converter.convertToCitation(entity, idWithSimilarity)).thenReturn(citation);


        Tuple2<Citation, NullWritable> retConverted = function.call(new Tuple2<>(entity, idWithSimilarity));


        assertSame(retConverted._1, citation);
        verify(converter).convertToCitation(entity, idWithSimilarity);
    }
}

package eu.dnetlib.iis.workflows.citationmatching;

import java.io.Serializable;

import org.apache.hadoop.io.NullWritable;
import org.apache.spark.api.java.JavaPairRDD;

import eu.dnetlib.iis.citationmatching.schemas.Citation;
import pl.edu.icm.coansys.citations.OutputWriter;
import pl.edu.icm.sparkutils.avro.SparkAvroSaver;

/**
 * Writer of output {@link Citation}s
 * 
 * @author madryk
 *
 */
public class CitationOutputWriter implements OutputWriter<Citation, NullWritable>, Serializable {

    private static final long serialVersionUID = 1L;


    //------------------------ LOGIC --------------------------

    /**
     * Writes rdd with {@link Citation}s to path specified as argument
     */
    @Override
    public void writeMatchedCitations(JavaPairRDD<Citation, NullWritable> matchedCitations, String path) {

        SparkAvroSaver.saveJavaRDD(matchedCitations.keys(), Citation.SCHEMA$, path);

    }

}
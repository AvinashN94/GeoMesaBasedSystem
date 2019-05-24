package com.geomesa.home;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geotools.data.Query;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.locationtech.geomesa.utils.geotools.SimpleFeatureTypes;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geotools.factory.Hints;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class RailRoadBridgeData implements InputData{
	
	private static final Logger logger = LoggerFactory.getLogger(RailRoadBridgeData.class);

    private SimpleFeatureType sft = null;
    private List<SimpleFeature> features = null;
    private List<Query> queries = null;
    //private Filter subsetFilter = null;

    public String getTypeName() {
        return "railroadbridgedata";
    }
	
	@Override
    public SimpleFeatureType getSimpleFeatureType() {
        if (sft == null) {
            // list the attributes that constitute the feature type
            // this is a reduced set of the attributes from GDELT 2.0
            StringBuilder attributes = new StringBuilder();
            attributes.append("NAICSdescr:String,");
            attributes.append("Name:String,");
            attributes.append("State:String,");
            attributes.append("County:String,");
            attributes.append("Bid:String,");
            //attributes.append("EventCode:String:index=true,"); // marks this attribute for indexing
            attributes.append("Fid:String:index=true,");
            attributes.append("City:String,");
            attributes.append("Fips:Integer,");
            attributes.append("Zip:Integer,");
            attributes.append("*geom:Point:srid=4326"); // the "*" denotes the default geometry (used for indexing)

            // create the simple-feature type - use the GeoMesa 'SimpleFeatureTypes' class for best compatibility
            // may also use geotools DataUtilities or SimpleFeatureTypeBuilder, but some features may not work
            sft = SimpleFeatureTypes.createType(getTypeName(), attributes.toString());

            // use the user-data (hints) to specify which date field to use for primary indexing
            // if not specified, the first date attribute (if any) will be used
            // could also use ':default=true' in the attribute specification string
            //sft.getUserData().put("test", "dtg");
        }
        return sft;
    }
	
	@Override
    public List<Query> getTestQueries() {
        if (queries == null) {
            try {
                List<Query> queries = new ArrayList<>();

                // most of the data is from 2018-01-01
                // note: DURING is endpoint exclusive
                String during = "dtg DURING 2017-12-31T00:00:00.000Z/2018-01-02T00:00:00.000Z";
                // bounding box over most of the united states
                String bbox = "bbox(geom,-120,30,-75,55)";

                // basic spatio-temporal query
                queries.add(new Query(getTypeName(), ECQL.toFilter(bbox + " AND " + during)));
                // basic spatio-temporal query with projection down to a few attributes
                queries.add(new Query(getTypeName(), ECQL.toFilter(bbox + " AND " + during),
                                      new String[]{ "GLOBALEVENTID", "dtg", "geom" }));
                // attribute query on a secondary index - note we specified index=true for EventCode
                queries.add(new Query(getTypeName(), ECQL.toFilter("EventCode = '051'")));
                // attribute query on a secondary index with a projection
                queries.add(new Query(getTypeName(), ECQL.toFilter("EventCode = '051' AND " + during),
                                      new String[]{ "GLOBALEVENTID", "dtg", "geom" }));

                this.queries = Collections.unmodifiableList(queries);
            } catch (CQLException e) {
                throw new RuntimeException("Error creating filter:", e);
            }
        }
        return queries;
    }

	@Override
	public List<SimpleFeature> getTestData() {
		// TODO Auto-generated method stub
		if (features == null) {
            List<SimpleFeature> features = new ArrayList<>();

            // read the bundled GDELT 2.0 TSV
            URL input = getClass().getClassLoader().getResource("20180101000000.export.CSV");
            if (input == null) {
                throw new RuntimeException("Couldn't load resource 20180101000000.export.CSV");
            }

            // date parser corresponding to the CSV format

            // use a geotools SimpleFeatureBuilder to create our features
            SimpleFeatureBuilder builder = new SimpleFeatureBuilder(getSimpleFeatureType());

            // use apache commons-csv to parse the GDELT file
            try (CSVParser parser = CSVParser.parse(input, StandardCharsets.UTF_8, CSVFormat.TDF)) {
                for (CSVRecord record : parser) {
                    try {
                        // pull out the fields corresponding to our simple feature attributes
                        builder.set("FID", record.get(0));
                        builder.set("ID", record.get(1));

                        builder.set("Name", record.get(2));
                        builder.set("City", record.get(3));
                        builder.set("State", record.get(4));
                        builder.set("Zip", record.get(17));
                        builder.set("County", record.get(26));

                        // we can also explicitly convert to the appropriate type
                        builder.set("FIPS", Integer.valueOf(record.get(31)));
                        builder.set("NAICSDESCR", Integer.valueOf(record.get(32)));
                                               
                        double latitude = Double.parseDouble(record.get(56));
                        double longitude = Double.parseDouble(record.get(57));
                        builder.set("geom", "POINT (" + longitude + " " + latitude + ")");

                        // be sure to tell GeoTools explicitly that we want to use the ID we provided
                        builder.featureUserData(Hints.USE_PROVIDED_FID, java.lang.Boolean.TRUE);

                        // build the feature - this also resets the feature builder for the next entry
                        // use the GLOBALEVENTID as the feature ID
                        SimpleFeature feature = builder.buildFeature(record.get(0));

                        features.add(feature);
                    } catch (Exception e) {
                        logger.debug("Invalid GDELT record: " + e.toString() + " " + record.toString());
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Error reading GDELT data:", e);
            }
            this.features = Collections.unmodifiableList(features);
        }
        return features;
		
		
		//return null;
	}
	
	public List<SimpleFeature> getSingleTestData(){
		if(features == null) {
			List<SimpleFeature> features = new ArrayList<>();
			
			
			return features;
		}
		return features;
	}
	
}

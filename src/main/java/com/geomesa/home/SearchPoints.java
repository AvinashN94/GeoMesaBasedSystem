package com.geomesa.home;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.ecql.ECQL;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Component
public class SearchPoints {
	private List<Query> queries = null;
	
	public String getTypeName() {
        return "railroadbridgedata";
    }
	
	public void searchBBox(Map<String, Double> searchData, DataStore datastore) {
		try {
			List<Query> queries = new ArrayList<>();
			double minX = searchData.get("Minx");
			double minY = searchData.get("Miny");
			double maxX = searchData.get("Maxx");
			double maxY = searchData.get("Maxy");
			String bbox = "bbox(geom,"+minX+","+minY+","+maxX+","+maxY+")";
			queries.add(new Query(getTypeName(), ECQL.toFilter(bbox)));
			this.queries = Collections.unmodifiableList(queries);
			StopWatch sw = new StopWatch();
			sw.start();
			queryFeatures(datastore, queries);
			sw.stop();
			System.out.println("Total time taken to search (ms) :"+(sw.getTotalTimeMillis()));
			System.out.println("Total time taken to search (seconds) :"+(sw.getTotalTimeSeconds()));
		}
		catch (CQLException e) {
            throw new RuntimeException("Error creating filter:", e);
        }
		catch (Exception e) {
            throw new RuntimeException("Error running quickstart:", e);
        }
	}
	
	public void ensureSchema(DataStore datastore, RailRoadBridgeData data) throws IOException {
        SimpleFeatureType sft = datastore.getSchema(data.getTypeName());
        if (sft == null) {
            throw new IllegalStateException("Schema '" + data.getTypeName() + "' does not exist. " +
                                            "Please run the associated QuickStart to generate the test data.");
        }
    }
	
	public void queryFeatures(DataStore datastore, List<Query> queries) throws IOException {
		for(Query query : queries) {
			System.out.println("Running query " + ECQL.toCQL(query.getFilter()));
			RailRoadBridgeData data = new RailRoadBridgeData();
			ensureSchema(datastore, data);
			// submit the query, and get back an iterator over matching features
	        // use try-with-resources to ensure the reader is closed
	        try (FeatureReader<SimpleFeatureType, SimpleFeature> reader =
	                 datastore.getFeatureReader(query, Transaction.AUTO_COMMIT)) {
	            // loop through all results, only print out the first 10
	            while (reader.hasNext()) {
	                SimpleFeature feature = reader.next();
	                // use geotools data utilities to get a printable string
	                System.out.println(String.format("%02d") + " " + DataUtilities.encodeFeature(feature));
	            }
        }
		}
		
	}

}

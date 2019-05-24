package com.geomesa.home;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.factory.Hints;
//import org.locationtech.geomesa.cassandra.data.CassandraDataStoreFactory;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.identity.FeatureIdImpl;
import org.geotools.filter.text.ecql.ECQL;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.sort.SortBy;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Component
public class SingleDataGeoMesa {
	private List<SimpleFeature> features = null;
	
	public DataStore createDataStore(Map<String, Serializable> params) throws IOException {
		DataStore datastore = DataStoreFinder.getDataStore(params);
		if (datastore == null) {
            throw new RuntimeException("Could not create data store with provided parameters");
        }
        return datastore;
	}
	
	public SimpleFeatureType getSimpleFeatureType() {
		InputData data = new RailRoadBridgeData();
        return data.getSimpleFeatureType();
    }
	
	public void createSchema(DataStore datastore, SimpleFeatureType sft) throws IOException {
        System.out.println("Creating schema: ");
        // we only need to do the once - however, calling it repeatedly is a no-op
        datastore.createSchema(sft);
    }
	
	public List<SimpleFeature> getFeatures(Map<String, String> singleData) {
        System.out.println("Converting the single data into SimpleFeature object");
        if(features == null) {
        	List<SimpleFeature> features = new ArrayList<>();
	        SimpleFeatureBuilder builder = new SimpleFeatureBuilder(getSimpleFeatureType());
	        builder.set("Name", singleData.get("Name"));
	        builder.set("City", singleData.get("City"));
	        builder.set("State", singleData.get("State"));
	        builder.set("County", singleData.get("County"));
	        builder.set("NAICSdescr", singleData.get("NAICSdescr"));
	        builder.set("Bid", singleData.get("id"));
	        builder.set("Fid", singleData.get("Fid"));
	        builder.set("Fips", singleData.get("Fips"));
	        builder.set("Zip", singleData.get("Zip"));
	        double latitude = Double.parseDouble(singleData.get("YCoor"));
            double longitude = Double.parseDouble(singleData.get("XCoor"));
            builder.set("geom", "POINT (" + longitude + " " + latitude + ")");
            builder.featureUserData(Hints.USE_PROVIDED_FID, java.lang.Boolean.TRUE);
            SimpleFeature feature = builder.buildFeature(singleData.get("FID"));
            features.add(feature);
            this.features = Collections.unmodifiableList(features);
        }
        return features;
    }
	
	public List<Query> getTestQueries(InputData data) {
        return data.getTestQueries();
    }
	
	public void writeFeatures(DataStore datastore, SimpleFeatureType sft, List<SimpleFeature> features) throws IOException {
        if (features.size() > 0) {
            System.out.println("Writing test data");
            // use try-with-resources to ensure the writer is closed
            try (FeatureWriter<SimpleFeatureType, SimpleFeature> writer =
                     datastore.getFeatureWriterAppend(sft.getTypeName(), Transaction.AUTO_COMMIT)) {
                for (SimpleFeature feature : features) {
                    // using a geotools writer, you have to get a feature, modify it, then commit it
                    // appending writers will always return 'false' for haveNext, so we don't need to bother checking
                    SimpleFeature toWrite = writer.next();

                    // copy attributes
                    toWrite.setAttributes(feature.getAttributes());

                    // if you want to set the feature ID, you have to cast to an implementation class
                    // and add the USE_PROVIDED_FID hint to the user data
                     ((FeatureIdImpl) toWrite.getIdentifier()).setID(feature.getID());
                     toWrite.getUserData().put(Hints.USE_PROVIDED_FID, Boolean.TRUE);

                    // alternatively, you can use the PROVIDED_FID hint directly
                    // toWrite.getUserData().put(Hints.PROVIDED_FID, feature.getID());

                    // if no feature ID is set, a UUID will be generated for you

                    // make sure to copy the user data, if there is any
                    toWrite.getUserData().putAll(feature.getUserData());

                    // write the feature
                    writer.write();
                }
            }
            System.out.println("Wrote " + features.size() + " features");
            System.out.println();
        }
    }
	
	
	public void queryFeatures(DataStore datastore, List<Query> queries) throws IOException {
        for (Query query : queries) {
            System.out.println("Running query " + ECQL.toCQL(query.getFilter()));
            if (query.getPropertyNames() != null) {
                System.out.println("Returning attributes " + Arrays.asList(query.getPropertyNames()));
            }
            if (query.getSortBy() != null) {
                SortBy sort = query.getSortBy()[0];
                System.out.println("Sorting by " + sort.getPropertyName() + " " + sort.getSortOrder());
            }
            // submit the query, and get back an iterator over matching features
            // use try-with-resources to ensure the reader is closed
            try (FeatureReader<SimpleFeatureType, SimpleFeature> reader =
                     datastore.getFeatureReader(query, Transaction.AUTO_COMMIT)) {
                // loop through all results, only print out the first 10
                int n = 0;
                while (reader.hasNext()) {
                    SimpleFeature feature = reader.next();
                    if (n++ < 10) {
                        // use geotools data utilities to get a printable string
                        System.out.println(String.format("%02d", n) + " " + DataUtilities.encodeFeature(feature));
                    } else if (n == 10) {
                        System.out.println("...");
                    }
                }
                System.out.println();
                System.out.println("Returned " + n + " total features");
                System.out.println();
            }
        }
    }
	
	public void insertSingleData(Map<String, String> singleData, DataStore datastore) throws IOException {
		try {
			StopWatch sw = new StopWatch();
			SimpleFeatureType sft = getSimpleFeatureType();
			createSchema(datastore, sft);
			List<SimpleFeature> features = getFeatures(singleData);
			sw.start();
			writeFeatures(datastore, sft, features);
			sw.stop();
			System.out.println("Data insertion complete");
			System.out.println("Time taken for single data insertion (ms):" +(sw.getTotalTimeMillis()));
			System.out.println("Time taken for single data insertion (seconds):" +(sw.getTotalTimeSeconds()));
		}
		catch (Exception e) {
            throw new RuntimeException("Error running quickstart:", e);
        }
	}

}

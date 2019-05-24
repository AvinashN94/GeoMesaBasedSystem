package com.geomesa.home;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.geotools.data.DataStore;
import org.geotools.factory.Hints;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import com.geomesa.model.FileUpload;

@Component
public class BulkDataGeomesa {
	@Autowired
	SingleDataGeoMesa singleDataGeoMesa;
	
	private List<SimpleFeature> features = null;
	
	public SimpleFeatureType getSimpleFeatureType() {
		InputData data = new RailRoadBridgeData();
        return data.getSimpleFeatureType();
    }
	
	public void createSchema(DataStore datastore, SimpleFeatureType sft) throws IOException {
        System.out.println("Creating schema: ");
        // we only need to do the once - however, calling it repeatedly is a no-op
        datastore.createSchema(sft);
    }
	
	public List<SimpleFeature> getFeatures(FileUpload fileUpload) throws IllegalStateException, IOException {
		List<SimpleFeature> features = new ArrayList<>();
		if (fileUpload == null)
			throw new RuntimeException("Couldn't load the CSV File");
		SimpleFeatureBuilder builder = new SimpleFeatureBuilder(getSimpleFeatureType());
		File convFile = null;
		if (fileUpload.getFile() != null) {
			convFile = new File(fileUpload.getFile().getOriginalFilename());
			fileUpload.getFile().transferTo(convFile);
		}
		try (CSVParser parser = CSVParser.parse(convFile, StandardCharsets.UTF_8, CSVFormat.TDF)) {
			for (CSVRecord record : parser) {
				try {
					String[] result = record.get(0).split(",");
					builder.set("Fid", result[0]);
					builder.set("Bid", result[1]);
			        builder.set("Name", result[2]);
			        builder.set("City", result[3]);
			        builder.set("State", result[4]);
			        builder.set("Zip", result[5]);
			        builder.set("County", result[6]);
			        builder.set("Fips", result[7]);
			        String naic = result[8].toString();
			        naic = naic.replaceAll("\"", "");
			        builder.set("NAICSdescr", naic);
			        double longitude = Double.parseDouble(result[11]);
		            double latitude = Double.parseDouble(result[12]);
		            builder.set("geom", "POINT (" + longitude + " " + latitude + ")");
		            builder.featureUserData(Hints.USE_PROVIDED_FID, java.lang.Boolean.TRUE);
		            SimpleFeature feature = builder.buildFeature(record.get(0));
		            features.add(feature);
				}catch (Exception e) {
                    System.out.println("Invalid CSV Data record: " + e.toString() + " " + record.toString());
                }
			}
		}catch (IOException e) {
            throw new RuntimeException("Error reading GDELT data:", e);
		}
		this.features = Collections.unmodifiableList(features);
		return features;
	}
	
	
	public void insertBulkData(FileUpload fileUpload, DataStore datastore) throws IOException {
		try {
			StopWatch sw = new StopWatch();
			SimpleFeatureType sft = getSimpleFeatureType();
			createSchema(datastore, sft);
			List<SimpleFeature> features = getFeatures(fileUpload);
			sw.start();
			singleDataGeoMesa.writeFeatures(datastore, sft, features);
			sw.stop();
			System.out.println("Data insertion complete");
			System.out.println("Time taken for bulk data insertion (Seconds):"+ sw.getTotalTimeSeconds());
			System.out.printf(" Time taken for bulk data insertion (ms) :"+sw.getTotalTimeMillis());
		}
		catch (Exception e) {
            throw new RuntimeException("Error running quickstart:", e);
        }
	}

}

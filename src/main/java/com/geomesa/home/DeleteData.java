package com.geomesa.home;

import org.geotools.data.DataStore;
import org.geotools.data.simple.SimpleFeatureStore;
import org.locationtech.geomesa.index.geotools.GeoMesaDataStore;
import org.opengis.filter.Filter;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Component
public class DeleteData {

	public String getTypeName() {
        return "railroadbridgedata";
    }
	
	public void clearAllData(DataStore datastore) {
		if (datastore != null) {
			try {
				StopWatch sw = new StopWatch();
				sw.start();
				if (datastore instanceof GeoMesaDataStore) {
                    ((GeoMesaDataStore) datastore).delete();
                } else {
                    ((SimpleFeatureStore) datastore.getFeatureSource(getTypeName())).removeFeatures(Filter.INCLUDE);
                    datastore.removeSchema(getTypeName());
                }
				sw.stop();
				System.out.println("Total time taken to delete all (ms):"+(sw.getTotalTimeMillis()));
				System.out.println("Total time taken to delete all (seconds):"+(sw.getTotalTimeSeconds()));

			} catch (Exception e) {
				System.err.println("Exception cleaning up test data: " + e.toString());
			} finally {
				// make sure that we dispose of the datastore when we're done with it
				datastore.dispose();
			}
		}
	}
	
}

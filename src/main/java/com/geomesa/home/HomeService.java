package com.geomesa.home;

import java.io.IOException;
import java.util.Map;

import org.geotools.data.DataStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.geomesa.model.FileUpload;

@Service
public class HomeService {
	@Autowired
	SingleDataGeoMesa singleDataGeoMesa;
	@Autowired
	BulkDataGeomesa bulkDataGeomesa;
	@Autowired
	SearchPoints searchPoints;
	@Autowired
	DeleteData deleteData;
	
	private static Logger log = LoggerFactory.getLogger(HomeService.class);
	
	public void insertSingleData(Map<String, String> singleData, DataStore datastore) {
		log.info("Inside the HomeService class executing single point data insertion");
		try {
			singleDataGeoMesa.insertSingleData(singleData, datastore);
		}
		catch (Exception e) {
			// TODO: handle exception
			System.out.println("Handles all exception");
		}
		
	}
	
	public void insertBulkData(DataStore datastore, FileUpload fileUpload) {
		try {
			bulkDataGeomesa.insertBulkData(fileUpload, datastore);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void searchData(Map<String, Double> searchData, DataStore datastore) {
		try {
			searchPoints.searchBBox(searchData, datastore);
		}
		catch (Exception e) {
			System.out.println("Handles all exception");
		}
	}
	
	public void deleteAll(DataStore datastore) {
		try {
			deleteData.clearAllData(datastore);
		} catch (Exception e) {
			System.out.println("Handles all exception");
		}
	}
}

package com.geomesa.home;


import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.geomesa.model.FileUpload;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;


@Controller
public class HomeController {
	String message = "Welcome to Spring MVC!";
	
	@Autowired
	public HomeService homeService;
	private Logger log = LoggerFactory.getLogger(HomeController.class);
	private DataStore datastore = null;
	public HomeController() throws IOException {
		super();
		Map<String, Serializable> parameters = new HashMap<>();
		parameters.put("cassandra.contact.point", "localhost:9042");
		parameters.put("cassandra.keyspace", "geomesa");
		parameters.put("cassandra.catalog", "mycatalog");
		datastore = DataStoreFinder.getDataStore(parameters);
	}

	
	
	@RequestMapping("/")
	@ResponseBody
	public ModelAndView showMessage() {
		log.info("In Home page");
		ModelAndView mv = new ModelAndView("index");
		FileUpload formUpload = new FileUpload();
		mv.addObject("formUpload", formUpload);
		return mv;
	}
	@RequestMapping(value = "/pointInsert", method = RequestMethod.POST)
	@ResponseBody
	public ModelAndView insertPoint(@ModelAttribute("singlePointData")SinglePointData singlePointData) {
		log.info("Inserting Single data");
		ModelAndView mv = null;
		Map<String, String> singleData = new HashMap<>();
		singleData.put("Fid", singlePointData.getFid());
		singleData.put("id", singlePointData.getId());
		singleData.put("Name", singlePointData.getName());
		singleData.put("City", singlePointData.getCity());
		singleData.put("State", singlePointData.getState());
		singleData.put("County", singlePointData.getCounty());
		singleData.put("Fips", singlePointData.getFips());
		singleData.put("NAICSdescr", singlePointData.getNaicsdescr());
		singleData.put("XCoor", singlePointData.getX());
		singleData.put("YCoor", singlePointData.getY());
		singleData.put("Zip", singlePointData.getZip());
		if(datastore != null) {
			homeService.insertSingleData(singleData, datastore);
			mv = new ModelAndView("helloworld");
		}
		return mv;
		
	} 
	
	@RequestMapping(value = "/search", method = RequestMethod.POST)
	@ResponseBody
	public ModelAndView searchData(@ModelAttribute("searchData")SearchData searchData) {
		log.info("Searching data");
		ModelAndView mv = null;
		Map<String, Double> singleSearchData = new HashMap<>();
		singleSearchData.put("Minx", Double.parseDouble(searchData.getMinx()));
		singleSearchData.put("Miny", Double.parseDouble(searchData.getMiny()));
		singleSearchData.put("Maxx", Double.parseDouble(searchData.getMaxx()));
		singleSearchData.put("Maxy", Double.parseDouble(searchData.getMaxy()));
		if(datastore != null) {
			homeService.searchData(singleSearchData, datastore);
			mv = new ModelAndView("helloworld");
		}
		return mv;
	}
	
	
	@RequestMapping(value = "/bulk", method = RequestMethod.POST)
	@ResponseBody
	public ModelAndView bulkUpload(@ModelAttribute("formUpload") FileUpload fileupload) {
		log.info("Insering data from CSV File. Bulk upload");
		ModelAndView mv = null;
		if(datastore != null) {
			homeService.insertBulkData(datastore, fileupload);
			mv = new ModelAndView("index");
		}
		return mv;
	}
	
	@RequestMapping(value = "/deleteOne", method = RequestMethod.POST)
	@ResponseBody
	public ModelAndView deleteSinglePoint() {
		log.info("Deleting single data");
		ModelAndView mv = null;
		return mv;
	}
	
	@RequestMapping(value = "/deleteAll", method = RequestMethod.POST)
	@ResponseBody
	public ModelAndView deleteAllData() {
		log.info("Deleting all data");
		System.out.println("Cleaning up test data");
		homeService.deleteAll(datastore);
		ModelAndView mv = null;
		return mv;
	}
	

}

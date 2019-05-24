package com.geomesa.home;

import java.util.List;

import org.geotools.data.Query;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.geotools.filter.text.ecql.ECQL;
import org.geotools.filter.text.cql2.CQLException;

public interface InputData {
	
	String getTypeName();
    SimpleFeatureType getSimpleFeatureType();
    List<SimpleFeature> getTestData();
    List<Query> getTestQueries();
    //Filter getSubsetFilter();
    
	static Filter createFilter(String geomField, double x0, double y0, double x1, double y1, String dateField,
			String t0, String t1, String attributesQuery) throws CQLException {

		// there are many different geometric predicates that might be used;
		// here, we just use a bounding-box (BBOX) predicate as an example.
		// this is useful for a rectangular query area
		String cqlGeometry = "BBOX(" + geomField + ", " + x0 + ", " + y0 + ", " + x1 + ", " + y1 + ")";

		// there are also quite a few temporal predicates; here, we use a
		// "DURING" predicate, because we have a fixed range of times that
		// we want to query
		String cqlDates = "(" + dateField + " DURING " + t0 + "/" + t1 + ")";

		// there are quite a few predicates that can operate on other attribute
		// types; the GeoTools Filter constant "INCLUDE" is a default that means
		// to accept everything
		String cqlAttributes = attributesQuery == null ? "INCLUDE" : attributesQuery;

		String cql = cqlGeometry + " AND " + cqlDates + " AND " + cqlAttributes;

		// we use geotools ECQL class to parse a CQL string into a Filter object
		return ECQL.toFilter(cql);
	}

}

<%@ page language="java" contentType="text/html; charset=ISO-8859-1" 
pageEncoding="ISO-8859-1"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
</head>
<body>
 
	<center>
		<div class="jumbotron" align="center">
    <div class="container">
        <h3>GeoMesa Cassandra Datastore</h3>
        <hr/>
        <div class="form-group">
            <h4>Insertion</h4><hr/>
            <form:form method="post" action = "/pointInsert"  modelAttribute="singlePointData">
	            <div class="form-group">
	                <b>Single Data Insertion</b><br/><br/>
	                <label for="fId">F-ID</label>
	                <input type="text" class="form-control-dark" name="fid">
	                <label for="id">ID</label>
	                <input type="text" class="form-control-dark" name="id">
	                <label for="name">Name</label>
	                <input type="text" class="form-control-dark" name="name">
	                <label for="city">City</label>
	                <input type="text" class="form-control-dark" name="city">
	                <label for="state">State</label>
	                <input type="text" class="form-control-dark" name="state"><br/>
	                <label for="zip">Zip</label>
	                <input type="text" class="form-control-dark" name="zip">
	                <label for="county">County</label>
	                <input type="text" class="form-control-dark" name="county">
	                <label for="fips">FIPS</label>
	                <input type="text" class="form-control-dark" name="fips">
	                <label for="naicsdescr">NAICSDESCR</label>
	                <input type="text" class="form-control-dark" name="naicsdescr"><br/>
	                <label for="x">Latitude</label>
	                <input type="text" class="form-control-dark" name="x">
	                <label for="y">Longitude</label>
	                <input type="text" class="form-control-dark" name="y">
	                <br/><br/>
	                <input type="submit" class="btn btn-primary" value="Insert"><br/>
	            </div>
            </form:form>
            <form:form action = "/bulk" enctype="multipart/form-data" method="post" modelAttribute="formUpload">
	            <div class="form-group">
	                <br/>
	                <b>Bulk data load </b><br/><br/>
	                <!-- <input name="csvfile" type="file"/>  -->
	                <form:input path="file" type="file"/>
	                <input type="submit" class="btn btn-primary" value="Bulk load"><br/>
	            </div>
            </form:form>
        </div>
        <hr/>
        <div class="form-group">
            <h4>Deletion</h4><hr/>
            <form:form action="/deleteSingle" method="post">
	            <div class="form-group">
	                <b>Single Point Data Deletion</b>
	                <input type="text" class="form-control-dark" name="pointDelete">
	                <input type="submit" class="btn btn-primary" value="Delete">
	            </div>
            </form:form>
            <form:form action="/deleteAll" method="post">
	            <div class="form-group">
	                <b>Delete All Data</b>
	                <input type="submit" class="btn btn-primary" value="Delete All">
	            </div>
            </form:form>
        </div>
        <hr/>
        <div class="form-group">
            <h4>Query Data</h4><hr/>
            <form:form action="/search" method="post" modelAttribute="searchData">
                <div class="form-group">
                    <b>Overlaps</b>
                    <label for="minX">Min-X</label>
	                <input type="text" class="form-control-dark" name="minx">
	                <label for="minY">Min-Y</label>
	                <input type="text" class="form-control-dark" name="miny">
	                <label for="maxX">Max-X</label>
	                <input type="text" class="form-control-dark" name="maxx">
	                <label for="maxY">Max-Y</label>
	                <input type="text" class="form-control-dark" name="maxy">
	                <br/><br/>
	                <input type="submit" class="btn btn-primary" value="Search"><br/>
                </div>
            </form:form>
            <br/>
            <form:form action="/" method="post">
                <div class="form-group">
                    <b>K-Nearest Neighbors</b>
                </div>
            </form:form>
        </div>
    </div>
</div>
	</center>
</body>
</html>
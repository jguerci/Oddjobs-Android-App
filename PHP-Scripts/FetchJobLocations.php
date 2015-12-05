<?php
	require 'config.php';
	
	$mysqli = @new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);
	
	if ($mysqli->connect_error) {
		die('Connect Error: ' . $mysqli->connect_error);
	}
	
	$myArray = array();
	if ($result = $mysqli->query("SELECT * FROM OpenJobs")) {
		while($row = $result->fetch_array(MYSQL_ASSOC)) {
			$myArray[] = $row;
		}
		echo json_encode($myArray);
	}
	$result->close();

	$mysqli->close();
	exit();
?>
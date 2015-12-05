<?php
	require 'config.php';
	
	$mysqli = @new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);
	
	if ($mysqli->connect_error) {
		die('Connect Error: ' . $mysqli->connect_error);
	}
	
	if (isset($_POST['employee'])) {
		$employee = $_POST['employee'];
		
		$myArray = array();
		if ($result = $mysqli->query("SELECT * FROM ClosedJobs where employee='$employee' ORDER BY date ASC")) {
			while($row = $result->fetch_array(MYSQL_ASSOC)) {
				$myArray[] = $row;
			}
			echo json_encode($myArray);
		}

		$result->close();
	}

	$mysqli->close();
	exit();
?>
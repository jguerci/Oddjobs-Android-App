<?php
	require 'config.php';
	
	$mysqli = @new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);
	
	if ($mysqli->connect_error) {
		die('Connect Error: ' . $mysqli->connect_error);
	}
	
	if (isset($_POST['openjob_id'], $_POST['employee'])) {
		$openjob_id = $_POST['openjob_id'];
		$employee = $_POST['employee'];
		
		$query = "INSERT INTO ClosedJobs (employer, title, address, city, state, zip, date, time, duration, payment, description, tag) 
				  SELECT employer, title, address, city, state, zip, date, time, duration, payment, description, tag
				  FROM OpenJobs WHERE openjob_id='$openjob_id'";
				  
		if ($mysqli->query($query)) {
			
			$query2 = "UPDATE ClosedJobs 
					   SET employee='$employee'
					   ORDER BY `closedjob_id` DESC LIMIT 1";
					   
			$mysqli->query($query2);
		}
		
		$query3 = "DELETE FROM OpenJobs
				   WHERE openjob_id='$openjob_id'";
				   
		if ($mysqli->query($query3)) {
			$json['output'] = 'success';
			echo json_encode($json);
		}
	}

	$mysqli->close();
	exit();
?>
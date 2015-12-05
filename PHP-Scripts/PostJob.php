<?php
	require 'config.php';
	
	$con = mysqli_connect(DB_HOST, DB_USER, DB_PASS, DB_NAME) or die("Could not connect to db");
	
	if(isset($_POST['employer'],$_POST['title'], $_POST['address'], $_POST['city'],$_POST['state'],$_POST['zip'],$_POST['date'],$_POST['time'],$_POST['duration'],$_POST['payment'],$_POST['description'],$_POST['tag'])) {
		$employer    = $_POST['employer'];
		$title       = $_POST['title'];
		$address     = $_POST['address'];
		$city        = $_POST['city'];
		$state       = $_POST['state'];
		$zip         = $_POST['zip'];
		$date        = $_POST['date'];
		$time        = $_POST['time'];
		$duration    = $_POST['duration'];
		$payment     = $_POST['payment'];
		$description = $_POST['description'];
		$tag         = $_POST['tag'];
		
		$query = "INSERT INTO OpenJobs (employer, title, address, city, state, zip, date, time, duration, payment, description, tag)
					VALUES ('$employer', '$title', '$address', '$city', '$state', '$zip', '$date', '$time', '$duration', '$payment', '$description', '$tag')";
		$inserted = mysqli_query($con, $query);
		if($inserted == 1){
			$json['output'] = 'success';
			echo json_encode($json);
		}
		else {
			$json['output'] = 'error';
			echo json_encode($json);
		}
	}

	mysqli_close($con);
	exit();
?>
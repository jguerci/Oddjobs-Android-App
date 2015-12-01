<?php
	require 'config.php';
	
	$con = mysqli_connect(DB_HOST, DB_USER, DB_PASS, DB_NAME) or die("Could not connect to db");
	
	if(isset($_POST['lastname'],$_POST['firstname'], $_POST["username"], $_POST['password'],$_POST['address'],$_POST['city'],$_POST['state'],$_POST['zip'],$_POST["image"])) {
		$lastname  = $_POST['lastname'];
		$firstname = $_POST['firstname'];
		$username  = $_POST['username'];
		$password  = $_POST['password'];
		$address   = $_POST['address'];
		$city      = $_POST["city"];
		$state     = $_POST['state'];
		$zip       = $_POST['zip'];
		$image     = $_POST["image"];
		
		$query = "Select * from Users where username='$username'";
		$result = mysqli_query($con, $query);
		if(mysqli_num_rows($result) > 0) {
			$json['output'] = 'User already exists';
			echo json_encode($json);
		}
		else if (empty($lastname) || empty($firstname) || empty($username) || empty($password) || empty($address) || empty($city) || empty($state) || empty($zip)) {
			$json['output'] = 'Must complete form';
			echo json_encode($json);
		}
		else {
			if (empty($image)) {
				$query = "insert into Users (lastname, firstname, username, password, address, city, state, zip, image) 
						  values ('$lastname', '$firstname', '$username', '$password', '$address', '$city', '$state', '$zip', NULL)";
			}
			else {
				$query = "insert into Users (lastname, firstname, username, password, address, city, state, zip, image) 
						  values ('$lastname', '$firstname', '$username', '$password', '$address', '$city', '$state', '$zip', '$image')";
			}
			$inserted = mysqli_query($con, $query);
			if($inserted == 1){
				$json['output'] = 'success';
			}
			else {
				$json['output'] = 'Failed to create';
			}
			echo json_encode($json);
		}
	}

	mysqli_close($con);
	exit();
?>
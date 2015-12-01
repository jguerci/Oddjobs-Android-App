<?php
	require 'config.php';
	
	$con = mysqli_connect(DB_HOST, DB_USER, DB_PASS, DB_NAME) or die("Could not connect to db");
	
	if(isset($_POST['username'],$_POST['password'])) {
		$username = $_POST['username'];
		$password = $_POST['password'];
		
		$query = "Select * from Users where username = '$username' and password = '$password' ";
		$result = mysqli_query($con, $query);
		if(mysqli_num_rows($result) > 0) {
			$json['output'] = 'success';
			echo json_encode($json);
		}
		else{
			$json['output'] = 'Wrong username or password';
			echo json_encode($json);
		}
	}

	mysqli_close($con);
	exit();
?>
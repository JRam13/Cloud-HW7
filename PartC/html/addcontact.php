<?php

require_once 'awssdk/sdk.class.php';
 
// Set timezone to your local timezone (if it is not set in your php.ini)
date_default_timezone_set('America/Oregon');

// Instantiate
$sdb = new AmazonSDB();
$response = $sdb->select("select * from `MyAddressBook` ");

?>

<html>
<body>
	<h1>Add Contact</h1>

	<ul>
		<li style="display: inline"><a href="index.php">Contacts</a></li>
		<li style="display: inline"> | </li>
		<li style="display: inline"><a href="subscribe.php">Subscribe</a></li>
		<li style="display: inline"> | </li>
		<li style="display: inline"><a href="addcontact.php">Add Contact</a></li>
	</ul>

	<form action="contactform.php" method="POST">
		First Name: <input type="text" name="fname"><br />
		Last Name: <input type="text" name="lname"><br />
		<input type="submit" value="Submit">
	</form>

</body>
</html>

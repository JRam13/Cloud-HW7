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
	<h1>My Contact List</h1>

	<ul>
		<li style="display: inline"><a href="index.php">Contacts</a></li>
		<li style="display: inline"> | </li>
		<li style="display: inline"><a href="subscribe.php">Subscribe</a></li>
		<li style="display: inline"> | </li>
		<li style="display: inline"><a href="addcontact.php">Add Contact</a></li>
	</ul>

	<p><strong>LISTING CONTACTS:</strong></p>
<?php
 
// Success?
echo "<ul>";
foreach ($response as $key) {
	foreach ($key as $test => $value) {
		//echo $test."<br />";
		foreach ($value as $val2 => $val3) {
			foreach ($val3 as $val4 => $val5) {
				if($val5 != ''){
					$str = str_replace(" ", "_", $val5);
					echo "<li><a href='https://s3-us-west-2.amazonaws.com/contactsbucketsimpledb/
					mycontacts_".$str.".html'>".$val5."</li><br />";
				}
			}

		}
	}
}
echo "</ul>";

?>


</body>
</html>

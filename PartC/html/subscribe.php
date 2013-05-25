<?php

require_once 'awssdk/sdk.class.php';
 
// Set timezone to your local timezone (if it is not set in your php.ini)
date_default_timezone_set('America/Oregon');

// Instantiate
$sns = new AmazonSNS();
 
// List all topics
$response = $sns->get_topic_list();
 

?>

<html>
<body>
	<h1>Subscribe To SNS</h1>

	<ul>
		<li style="display: inline"><a href="index.php">Contacts</a></li>
		<li style="display: inline"> | </li>
		<li style="display: inline"><a href="subscribe.php">Subscribe</a></li>
		<li style="display: inline"> | </li>
		<li style="display: inline"><a href="addcontact.php">Add Contact</a></li>
	</ul>

	<?php 
		$action=$_REQUEST['action']; 
		if ($action=="")    /* display the contact form */ 
    	{ 
    ?> 
    
    <form  action="" method="POST" enctype="multipart/form-data">
    <input type="hidden" name="action" value="submit">


    <label>Topic:</label>
		<select name="topics">
			<?php 

			foreach ($response as $key => $value) {
				$str = substr($value, 35);
				echo "<option value='" . $value . "'>" . $str . "</option>\n";
			}

			?>
		</select>
		<br />
		Subscribe (enter email): <input type="text" name="email">
		<br />


    <input type="submit" value="Submit"/>
    </form>

    <?php 

    }  
	else                /* send the submitted data */ 
    { 
    	$email = $_REQUEST['email'];
    	// echo 'this ran';
	    $topics=$_REQUEST['topics']; 
	    // $email=$_REQUEST['email']; 
	    // $message=$_REQUEST['message']; 
	    if (($email=="")) 
	        { 
	        	echo "Email can't be blank. Try again."; 
	        } 
	    else{         
		        $subs = $sns->subscribe(
    			$topics,
    			'email',
    			$email
    			);

    			// Success?
				if($subs->isOK()){
					echo "Subscribed!"; 
				}
				else{
					echo 'Try Again.';
				}

				

		        
	        } 
    }   
?> 


</body>
</html>


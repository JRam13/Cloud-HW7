package contacts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
 
public class MyContacts {
 
    private static BufferedWriter br;
    private static int option;
    private static int optionB;
    private static AmazonS3 s3;
    private static AmazonSimpleDB sdb;
    private static AmazonSNS sns;
    private static String fName;
    private static String lName;
    private static String pNumber;
    private static String pNumber2;
    private static String email;
    private static String bday;
    private static String address;
    private static String city;
    private static String state;
    private static String zip;
    private static boolean abFound = false;
    private static String myDomain = "MyAddressBook";

    private static String currentContact;
	private static String currentBucket;

	public static void main(String[] args) throws Exception {
    	
    	s3 = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider());
		Region usEastOne = Region.getRegion(Regions.US_EAST_1);
		s3.setRegion(usEastOne);
		sns = new AmazonSNSClient(new ClasspathPropertiesFileCredentialsProvider());
		
        sdb = new AmazonSimpleDBClient(new ClasspathPropertiesFileCredentialsProvider());
		sdb.setRegion(usEastOne);
		
		createAB();
		
        
		System.out.println("===========================================");
		System.out.println("My Adress Book");
	    System.out.println("===========================================\n");
	     
		while (option != 12) {
       
	        System.out.println("AWS S3: ");
	        System.out.println("--------------------------");
	        
	        System.out.println("1) Setup/Change Access Keys (Using IAM Credentials)");
	        if(hasKeys()){
		        System.out.println("2) Create a Bucket");
		        System.out.println("3) List Buckets");
		        System.out.println("4) Delete Bucket \n");
		        System.out.println("SimpleDB: (AddressBook automatically created)");
		        System.out.println("--------------------------");
		        System.out.println("5) Create Contact");
		        if(hasContacts()){
			        System.out.println("6) List Contacts");
			        System.out.println("7) Add Tags To Contact");
			        System.out.println("8) Edit Contact");
			        System.out.println("9) View Contact");
			        System.out.println("10) Search Contacts");
			        System.out.println("11) Delete Contact \n");
		        }
	        }
	        System.out.println("12) Exit \n");
	        
	        Scanner scn = new Scanner(System.in); 
			System.out.println("Choose a number: ");
			try {
				option = scn.nextInt();
			} catch (Exception e) {
				// TODO Auto-generated catch block
			}
		
			if(option == 1){
				System.out.println();
				setKeys();
			}
			if(hasKeys()){
				if(option == 2){
					System.out.println();
					createBucket();
				}
				else if(option == 3){
					System.out.println();
					listBuckets();
				}
				else if(option == 4){
					System.out.println();
					deleteBucket();
				}
				else if(option == 5){
					System.out.println();
					createContact();
				}
				if(hasContacts()){
					if(option == 6){
						System.out.println();
						listContacts();
					}
					else if(option == 7){
						System.out.println();
						addTags();
					}
					else if(option == 8){
						System.out.println();
						editContact();
					}
					else if(option == 9){
						System.out.println();
						viewContact();
					}
					else if(option == 10){
						System.out.println();
						searchContact();
					}
					else if(option == 11){
						System.out.println();
						deleteContact();
					}
				}
			}
			else if(option == 12){
				System.out.println("Goodbye.");
				System.exit(1);
			}
			else{
				System.err.println("Please select a valid option");
			}
		}
    	

    }

	private static void createAB() {
		if(hasKeys()){
			//create address book if it doesn't exists
	        for (String domainName : sdb.listDomains().getDomainNames()) {
	            if(domainName.equals("MyAddressBook")){
	            	abFound = true;
	            }
	        }
			
	        if(!abFound){
	            System.out.println("Creating domain called " + myDomain + ".\n");
	            sdb.createDomain(new CreateDomainRequest(myDomain));
	        }
		}
	}
	

	private static void setKeys() throws Exception{
    	
    	//Ask users for AWS keys
    	
    	System.out.println("Create AWS Keys");
        System.out.println("--------------------------\n");
    	
    	
    	// Location of file to read
        File file = new File("bin/AwsCredentials.properties");
        file.delete();
        Thread.sleep(4000);
        file = new File("bin/AwsCredentials.properties");
        String secretK = "";
        String accessK = "";
        	
        System.out.println("AWS Access Keys One-Time Setup");
    	
    	Scanner scn = new Scanner(System.in); 
		System.out.println("Insert Secret Key: ");
		secretK = scn.nextLine();
		System.out.println("Insert Access Key: ");
		accessK = scn.nextLine();
		System.out.println("AWS Keys Vertified");
		
		FileWriter fr = new FileWriter(file);
		br = new BufferedWriter(fr);
		
		java.util.Date date= new java.util.Date();
		 
		 
		br.write("#Insert your AWS Credentials from http://aws.amazon.com/security-credentials");
		br.newLine();
		br.write("#" + new Timestamp(date.getTime()));
		br.newLine();
		br.write("secretKey=" + secretK);
		br.newLine();
		br.write("accessKey=" + accessK);
		br.close();
		System.out.println();
		
		Thread.sleep(1000);
		createAB();
 
    }
    
    private static void createBucket(){
    	
    	//Creates bucket. If not unique- will ask to provide a unique bucket name
    	
    	String bucketName;
    	
    	System.out.println("Create Bucket: ");
        System.out.println("--------------------------\n");
		
		Scanner scn = new Scanner(System.in); 
		System.out.println("Choose a Bucket Name: ");
		bucketName = scn.nextLine();
		String bucketNameOriginal = bucketName;
		
        System.out.println("Creating bucket ..." + bucketNameOriginal + "\n");
        try {
			s3.createBucket(bucketNameOriginal);
			System.out.println("Success!");
		} catch (AmazonServiceException e) {
			// TODO Auto-generated catch block
			System.err.println("Sorry, bucket name must be globally unique. Try again.");
			createBucket();
		}
        
    }
    
    private static void listBuckets(){
    	
    	//Lists all buckets on the server
    	
    	System.out.println("List Buckets: ");
        System.out.println("--------------------------");
        
    	int count = 1;
    	for (Bucket bucket : s3.listBuckets()) {
            System.out.println(count + ") - " + bucket.getName());
            count++;
        }
        System.out.println();
    }
    
    private static String chooseABucket(){
		
    	 System.out.println("Please choose a bucket: ");
         
         //lists buckets
     	int count = 1;
     	for (Bucket bucket : s3.listBuckets()) {
             System.out.println(count + ") - " + bucket.getName());
             count++;
         }
     	
     	Scanner scn = new Scanner(System.in); 
     	try {
 			optionB = scn.nextInt();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 		}
 		
 		if(optionB > count - 1 || optionB < 1){
 			System.out.println("Please select a proper bucket");
 			chooseABucket();
 		}
 		//gets the buckets name
 		else{
 	    	count = 1;
 	    	currentBucket = null;
 	    	for (Bucket bucket : s3.listBuckets()) {
 	            if(count == optionB){
 	            	currentBucket = bucket.getName();
 	            }
 	            count++;
 	        }
 		}
    	return currentBucket;
    	
    }
    
    private static void createContact() throws Exception{
    	
    	//First it lists all the buckets
    	//Then it asks for credentials
    	
    	System.out.println("Create Contact: ");
        System.out.println("--------------------------");
        
        chooseABucket();
       
    	//asks for contact info
    	System.out.println("Enter first name: ");
    	Scanner scn = new Scanner(System.in); 
    	fName = scn.nextLine();
    	System.out.println("Last Name: ");
    	lName = scn.nextLine();
    	System.out.println("Phone Number (Home): xxx-xxx-xxxx");
    	pNumber = scn.nextLine();
    	System.out.println("Phone Number (Cell): xxx-xxx-xxxx");
    	pNumber2 = scn.nextLine();
    	System.out.println("Street Address");
    	address = scn.nextLine();
    	System.out.println("City");
    	city = scn.nextLine();
    	System.out.println("State: Fl");
    	state = scn.nextLine();
    	System.out.println("Zip");
    	zip = scn.nextLine();
    	System.out.println("E-Mail");
    	email = scn.nextLine();
    	System.out.println("Birthday: 11/13/1985");
    	bday = scn.nextLine();
		System.out.println("Uploading a new contact to S3 from a file\n");
		
		createSimpleDB();
        listContacts();
        publishNotification("c");
        System.out.println();
    }
    
    private static void publishNotification(String string) {
    	if(string.equals("c")){
	    	PublishRequest request = new PublishRequest("arn:aws:sns:us-east-1:283530117241:51083-updated", fName + " has been added to Contacts" +
	    			" <https://s3-us-west-2.amazonaws.com/contactsbucketsimpledb/" + getKey() + ">").withSubject("Contact has been added");
	    	
	    	sns.publish(request);
    	}
    	else if(string.equals("m")){
    		PublishRequest request = new PublishRequest("arn:aws:sns:us-east-1:283530117241:51083-updated", fName + " has been modified to Contacts" +
	    			" <https://s3-us-west-2.amazonaws.com/contactsbucketsimpledb/" + getKey() + ">").withSubject("Contact has been modified");
	    	
	    	sns.publish(request);
    	}
    	else if(string.equals("d")){
    		PublishRequest request = new PublishRequest("arn:aws:sns:us-east-1:283530117241:51083-updated", fName + " has been deleted from Contacts" +
	    			" <https://s3-us-west-2.amazonaws.com/contactsbucketsimpledb/" + getKey() + ">").withSubject("Contact has been deleted");
	    	
	    	sns.publish(request);
    	}
		
	}

	private static File createContactHTML() throws Exception{
        
    	Thread.sleep(2000);
    	
    	//this method created an html file with the contacts
    	File file = new File("mycontacts_" + fName +"_" + lName + ".html");
    	FileWriter fr = new FileWriter(file);
    	br = new BufferedWriter(fr);
    	
    	br.write("<h1>My Address Book</h1>");
    	br.newLine();
    	
      String selectExpression = "select * from `" + myDomain + "` where FirstName = '" + fName + "'";
      
      //System.out.println("Selecting: " + selectExpression + "\n");
      SelectRequest selectRequest = new SelectRequest(selectExpression);
      for (Item item : sdb.select(selectRequest).getItems()) {
          //System.out.println("  Item");
    	  br.write("<h2>" + item.getName() + "</h2>");
      	  br.newLine();
          br.write("<img src='http://d3r6vrqzwazs3l.cloudfront.net/hlaurie.jpg' alt='logo' height='120' width='120'>");
          br.newLine();
          br.write("<table border='1'>");
          br.newLine();
          //System.out.println("    H2 Attribute: " + item.getName());
          br.write("<tr>");
          br.newLine();
          for (Attribute attribute : item.getAttributes()) {
              //System.out.println("      Attribute");
        	  if(!attribute.getValue().isEmpty()){
        		  br.write("<td><strong>" + attribute.getName() + "</strong></td>");
        		  br.newLine();
        	  }
              //System.out.println("        Column 1:  " + attribute.getName());
              //System.out.println("        Value: " + attribute.getValue());
          }
          br.write("</tr>");
          br.newLine();
          br.write("<tr>");
          br.newLine();
          for (Attribute attribute : item.getAttributes()) {
              //System.out.println("      Attribute");
        	  if(!attribute.getValue().isEmpty()){
        		  br.write("<td>" + attribute.getValue() + "</td>");
        		  br.newLine();
        	  }
              //System.out.println("        Column 1:  " + attribute.getName());
             // System.out.println("        Value: " + attribute.getValue());
          }
          br.write("</tr>");
          br.newLine();
          br.write("</table>");
          br.newLine();
          br.newLine();
          br.write("<hr />");
          br.newLine();
      }
      System.out.println();
      
      br.close();
	
    	
      return file;
    }
    
    private static void editContact() throws Exception{
    	
        System.out.println("Edit Contacts");
        System.out.println("--------------------------");
        
        chooseABucket();
    	System.out.println("Choose a contact to edit: ");
    	
    	String selectExpression = "select * from `" + myDomain + "`";
        
        SelectRequest selectRequest = new SelectRequest(selectExpression);
        int count = 1;
        System.out.println("Listing Contacts ");
        System.out.println("--------------------------");
        for (Item item : sdb.select(selectRequest).getItems()) {
            System.out.println(count + ") " + item.getName());
            count++;
        }
        System.out.println();
    	
    	Scanner scn = new Scanner(System.in); 
    	try {
			optionB = scn.nextInt();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println("Please select a proper option");
			editContact();
		}
    	
    	count = 1;
    	currentContact = null;
    	for (Item item : sdb.select(selectRequest).getItems()) {
            if(count == optionB){
            	currentContact = item.getName();
            	System.out.println(currentContact);
            	
            	for (Attribute attribute : item.getAttributes()) {
            		if(attribute.getName().equals("FirstName")){
            			fName = attribute.getValue();
            		}
            		if(attribute.getName().equals("LastName")){
            			lName = attribute.getValue();
            		}
            	}
            }
            count++;
        }
    	
    	s3.deleteObject(currentBucket, getKey());
    	
    	//asks for contact info
    	System.out.println("Enter first name: ");
    	scn = new Scanner(System.in); 
    	fName = scn.nextLine();
    	System.out.println("Last Name: ");
    	lName = scn.nextLine();
    	System.out.println("Phone Number (Home): xxx-xxx-xxxx");
    	pNumber = scn.nextLine();
    	System.out.println("Phone Number (Cell): xxx-xxx-xxxx");
    	pNumber2 = scn.nextLine();
    	System.out.println("Street Address");
    	address = scn.nextLine();
    	System.out.println("City");
    	city = scn.nextLine();
    	System.out.println("State: Fl");
    	state = scn.nextLine();
    	System.out.println("Zip");
    	zip = scn.nextLine();
    	System.out.println("E-Mail");
    	email = scn.nextLine();
    	System.out.println("Birthday: 11/13/1985");
    	bday = scn.nextLine();
		System.out.println("Uploading a new contact to S3 from a file\n");
		sdb.deleteAttributes(new DeleteAttributesRequest(myDomain, currentContact));
		createSimpleDB();
		s3.putObject(new PutObjectRequest(currentBucket, getKey(), createContactHTML()).withAccessControlList(getAcl()));
        listContacts();
        publishNotification("m");
        
	}

	private static void deleteContact() throws Exception{
    	
        System.out.println("Edit Contacts");
        System.out.println("--------------------------");
        
        chooseABucket();
    	System.out.println("Choose a contact to Delete: ");
    	
    	String selectExpression = "select * from `" + myDomain + "`";
        
        SelectRequest selectRequest = new SelectRequest(selectExpression);
        int count = 1;
        System.out.println("Listing Contacts ");
        System.out.println("--------------------------");
        for (Item item : sdb.select(selectRequest).getItems()) {
            System.out.println(count + ") " + item.getName());
            count++;
        }
        System.out.println();
    	
    	Scanner scn = new Scanner(System.in); 
    	try {
			optionB = scn.nextInt();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println("Please select a proper option");
			deleteContact();
		}
    	
    	count = 1;
    	currentContact = null;
    	for (Item item : sdb.select(selectRequest).getItems()) {
            if(count == optionB){
            	currentContact = item.getName();
            	System.out.println(currentContact);
            	
            	for (Attribute attribute : item.getAttributes()) {
            		if(attribute.getName().equals("FirstName")){
            			fName = attribute.getValue();
            		}
            		if(attribute.getName().equals("LastName")){
            			lName = attribute.getValue();
            		}
            	}
            }
            count++;
        }
    	
    	s3.deleteObject(currentBucket, getKey());
		
		sdb.deleteAttributes(new DeleteAttributesRequest(myDomain, currentContact));
		publishNotification("d");
		System.err.println("Contact Deleted");
    	
    }
    
    private static void deleteBucket(){
    	
    	System.out.println("Delete Buckets: ");
        System.out.println("--------------------------");
        
        System.out.println("Please choose a bucket to delete (must be empty): ");
        System.err.println("BEWARE: This will erase the bucket");
        
        chooseABucket();
    
    	System.out.println("Deleting a bucket\n");
        try {
			s3.deleteBucket(currentBucket);
		} catch (AmazonServiceException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.err.println("Cannot Delete. Contents must be empty");
		} catch (AmazonClientException e) {
			// TODO Auto-generated catch block
		}
        System.out.println();
    }
    
    private static void createSimpleDB() throws Exception{
    	
    	sdb.batchPutAttributes(new BatchPutAttributesRequest(myDomain, createDBcontact()));
    	s3.putObject(new PutObjectRequest(currentBucket, getKey(), createContactHTML()).withAccessControlList(getAcl()));
    }
    
    private static List<ReplaceableItem> createDBcontact() {
        List<ReplaceableItem> sampleData = new ArrayList<ReplaceableItem>();

        sampleData.add(new ReplaceableItem(fName + " " + lName).withAttributes(
                new ReplaceableAttribute("FirstName", fName, true),
                new ReplaceableAttribute("LastName", lName, true),
                new ReplaceableAttribute("Phone_Home", pNumber, true),
                new ReplaceableAttribute("Phone_Cell", pNumber2, true),
                new ReplaceableAttribute("Email", email, true),
                new ReplaceableAttribute("BirthDay", bday, true),
                new ReplaceableAttribute("StreetAddress", address, true),
                new ReplaceableAttribute("City", city, true),
        		new ReplaceableAttribute("State", state, true),
        		new ReplaceableAttribute("Zip", zip, true)
                
        		));
		
        return sampleData;
        
    }

	private static void listContacts() {
		String selectExpression = "select * from `" + myDomain + "`";
        
        SelectRequest selectRequest = new SelectRequest(selectExpression);
        int count = 1;
        System.out.println("Listing Contacts ");
        System.out.println("--------------------------");
        for (Item item : sdb.select(selectRequest).getItems()) {
            System.out.println(count + ") " + item.getName());
            count++;
        }
        System.out.println();
	}
    
    private static void addTags() throws Exception {
        System.out.println("Add Tags");
        System.out.println("--------------------------");
        
        chooseABucket();
        
        
    	System.out.println("Choose a contact to add tags to: ");
    	
    	String selectExpression = "select * from `" + myDomain + "`";
        
        SelectRequest selectRequest = new SelectRequest(selectExpression);
        int count = 1;
        System.out.println("Listing Contacts ");
        System.out.println("--------------------------");
        for (Item item : sdb.select(selectRequest).getItems()) {
            System.out.println(count + ") " + item.getName());
            count++;
        }
        System.out.println();
    	
    	Scanner scn = new Scanner(System.in); 
    	try {
			optionB = scn.nextInt();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println("Please select a proper option");
			addTags();
		}
    	
    	count = 1;
    	currentContact = null;
    	for (Item item : sdb.select(selectRequest).getItems()) {
            if(count == optionB){
            	currentContact = item.getName();
            	System.out.println(currentContact);
            	for (Attribute attribute : item.getAttributes()) {
            		if(attribute.getName().equals("FirstName")){
            			fName = attribute.getValue();
            		}
            		if(attribute.getName().equals("LastName")){
            			lName = attribute.getValue();
            		}
            	}
            }
            count++;
        }
    	System.out.println("Tag name : e.g., Phone: Work, Relationship, etc");
    	scn = new Scanner(System.in); 
    	String tagName = scn.nextLine();
    	System.out.println(tagName + " : ");
    	String value = scn.nextLine();
    	List<ReplaceableAttribute> replaceableAttributes = new ArrayList<ReplaceableAttribute>();
        replaceableAttributes.add(new ReplaceableAttribute(tagName, value, true));
    	sdb.putAttributes(new PutAttributesRequest(myDomain, currentContact, replaceableAttributes));
    	
    	s3.putObject(new PutObjectRequest(currentBucket, getKey(), createContactHTML()).withAccessControlList(getAcl()));
    	
    	
//        selectExpression = "select * from `" + myDomain + "`";
//        System.out.println("Selecting: " + selectExpression + "\n");
//        selectRequest = new SelectRequest(selectExpression);
//        for (Item item : sdb.select(selectRequest).getItems()) {
//            System.out.println("  Item");
//            System.out.println("    Name: " + item.getName());
//            for (Attribute attribute : item.getAttributes()) {
//                System.out.println("      Attribute");
//                System.out.println("        Name:  " + attribute.getName());
//                System.out.println("        Value: " + attribute.getValue());
//            }
//        }
//        System.out.println();
        
    	
    	
	}
    
	private static void viewContact() {
		
		System.out.println("View Contact");
        System.out.println("--------------------------");
    	System.out.println("Choose a contact to view its details: ");
    	
    	String selectExpression = "select * from `" + myDomain + "`";
        
        SelectRequest selectRequest = new SelectRequest(selectExpression);
        int count = 1;
        System.out.println("Listing Contacts ");
        System.out.println("--------------------------");
        for (Item item : sdb.select(selectRequest).getItems()) {
            System.out.println(count + ") " + item.getName());
            count++;
        }
        System.out.println();
    	
    	Scanner scn = new Scanner(System.in); 
    	try {
			optionB = scn.nextInt();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println("Please select a proper option");
			viewContact();
		}
    	
    	count = 1;
    	currentContact = null;
    	for (Item item : sdb.select(selectRequest).getItems()) {
            if(count == optionB){
            	currentContact = item.getName();
            	System.out.println(currentContact);
            }
            count++;
        }
    	
      selectRequest = new SelectRequest(selectExpression);
      for (Item item : sdb.select(selectRequest).getItems()) {
    	  if( item.getName().equals(currentContact)){
    		  for (Attribute attribute : item.getAttributes()) {
	              System.out.println(attribute.getName() + ": " + attribute.getValue());
	          }
          }
      }
      System.out.println();
    	
	}
	
	private static void searchContact() {
		
		System.out.println("Search Contact");
        System.out.println("--------------------------");
    	System.out.println("Search By: ");
    	
    	System.out.println("1) First Name");
    	System.out.println("2) Last Name");
    	System.out.println("3) City");
    	System.out.println("4) Zip");
    	System.out.println("5) Tag");
    	System.out.println("6) Both First Name & City");
    	
    	Scanner scn = new Scanner(System.in); 
    	try {
			optionB = scn.nextInt();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println("Please select a proper option");
			searchContact();
		}
    	String search = null;
    	String search2 = null;
    	if(optionB != 6){
    		scn = new Scanner(System.in); 
    		System.out.println("Enter search value: ");
    		search = scn.nextLine();
    	}
    	else{
    		scn = new Scanner(System.in); 
        	System.out.println("Enter First Name: ");
        	search = scn.nextLine();
        	System.out.println("Enter City: ");
        	search2 = scn.nextLine();
    	}
    	
    	String selectExpression = null;
    	if(optionB == 1){
    		selectExpression = "select * from `" + myDomain + "` where FirstName like '" + search + "%'";
    	}
    	if(optionB == 2){
    		selectExpression = "select * from `" + myDomain + "` where LastName like '" + search + "%'";
    	}
    	if(optionB == 3){
    		selectExpression = "select * from `" + myDomain + "` where City like '" + search + "%'";
    	}
    	if(optionB == 4){
    		selectExpression = "select * from `" + myDomain + "` where Zip = '" + search + "'";
    	}
    	if(optionB == 5){
    		selectExpression = "select * from `" + myDomain + "` where Tag like '" + search + "%'";
    	}
    	if(optionB == 6){
    		selectExpression = "select * from `" + myDomain + "` where FirstName like '" + search + "%' AND City like '" + search2 + "%'";
    	}
        SelectRequest selectRequest = new SelectRequest(selectExpression);
        int count = 1;
        System.out.println("Listing Contacts ");
        System.out.println("--------------------------");
        for (Item item : sdb.select(selectRequest).getItems()) {
            System.out.println(count + ") " + item.getName());
            count++;
        }
        System.out.println();
        
	}
	
	private static boolean hasContacts() {
		try {
			String selectExpression = "select * from `" + myDomain + "`";
			
			SelectRequest selectRequest = new SelectRequest(selectExpression);
			for (@SuppressWarnings("unused") Item item : sdb.select(selectRequest).getItems()) {
			    return true;
			}
			
		} catch (AmazonServiceException e) {
			// TODO Auto-generated catch block
			return false;
		} catch (AmazonClientException e) {
			// TODO Auto-generated catch block
			return false;
		}
		return false;
	}
	
    private static boolean hasKeys(){
        
    	try {
			for (@SuppressWarnings("unused") Bucket bucket : s3.listBuckets()) {
				return true;
			}
		} catch (AmazonServiceException e) {
			// TODO Auto-generated catch block
			return false;
		} catch (AmazonClientException e) {
			// TODO Auto-generated catch block
			return false;
		}
    	return true;
    }
    
	public static String getKey() {
		return "mycontacts_" + fName +"_" + lName + ".html";
	}
	
	private static AccessControlList getAcl() {
		AccessControlList acl = new AccessControlList();
		acl.grantPermission(GroupGrantee.AllUsers, Permission.Read);
		return acl;
	}
    
    
}

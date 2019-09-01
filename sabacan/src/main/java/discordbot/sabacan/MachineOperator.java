package discordbot.sabacan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Expected;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.CancelSpotInstanceRequestsRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeSpotInstanceRequestsRequest;
import com.amazonaws.services.ec2.model.DescribeSpotInstanceRequestsResult;
import com.amazonaws.services.ec2.model.IamInstanceProfileSpecification;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.LaunchSpecification;
import com.amazonaws.services.ec2.model.RequestSpotInstancesRequest;
import com.amazonaws.services.ec2.model.RequestSpotInstancesResult;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.SpotInstanceRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

import discordbot.sabacan.exceptions.DatabaseException;
import discordbot.sabacan.exceptions.GameServerException;
import discordbot.sabacan.exceptions.IPNotFoundException;

public class MachineOperator {
	private static AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.standard().build();
	private static DynamoDB dynamoDB = new DynamoDB(dynamoDBClient);
	private static Table gscTable = dynamoDB.getTable("GCS");

	private static AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

	public static void startGameServer(String gamename) throws DatabaseException{

        RequestSpotInstancesRequest requestRequest = new RequestSpotInstancesRequest();
        LaunchSpecification launchSpecification = new LaunchSpecification();
        IamInstanceProfileSpecification iam = new IamInstanceProfileSpecification();
        ArrayList<String> securityGroups = new ArrayList<String>();

        // startするゲームサーバの状態をDBに登録.既に登録してあった場合、エラーを返す。
    	try{
    	Item item = new Item()
    		.withPrimaryKey("gamename", gamename)
    		.withString("status", "startup");
    	gscTable.putItem(item, new Expected("gamename").notExist());
    	} catch (ConditionalCheckFailedException e){
    		throw new DatabaseException("既に存在するデータです");
    	}

    	if(gamename == "minecraft forge12.2") {
        	requestRequest.setSpotPrice("0.0272");
            requestRequest.setInstanceCount(Integer.valueOf(1));

            launchSpecification.setImageId("ami-0fc2c98c56501e2b9");
            launchSpecification.setInstanceType("t3.small");
            launchSpecification.setIamInstanceProfile(iam.withArn("arn:aws:iam::267450259852:instance-profile/GameServer"));
            securityGroups.add("Minecraft Server");

    	}
    	else if(gamename =="7days to die"){
            requestRequest.setSpotPrice("0.0544");
            requestRequest.setInstanceCount(Integer.valueOf(1));
            launchSpecification.setImageId("ami-00245311ff45c1f98");
            launchSpecification.setInstanceType("t3.medium");
            launchSpecification.setIamInstanceProfile(iam.withArn("arn:aws:iam::267450259852:instance-profile/GameServer"));
            securityGroups.add("7d2d Server");
    	}

    	launchSpecification.setSecurityGroups(securityGroups);
        requestRequest.setLaunchSpecification(launchSpecification);

        // Call the RequestSpotInstance API.
        RequestSpotInstancesResult requestResult = new RequestSpotInstancesResult();
        requestResult = ec2.requestSpotInstances(requestRequest);

        List<SpotInstanceRequest> requestResponses = requestResult.getSpotInstanceRequests();
        List<String> spotInstanceRequestIds = new ArrayList<String>();
        for (SpotInstanceRequest requestResponse : requestResponses) {
        	    spotInstanceRequestIds.add(requestResponse.getSpotInstanceRequestId());
        }

        Map<String,String> updateAttributeNames = new HashMap<String,String>();
        updateAttributeNames.put("#SID", "SID");

        Map<String, Object> updateAttributeValues = new HashMap<String, Object>();
        updateAttributeValues.put(":val1", spotInstanceRequestIds);
        gscTable.updateItem(
        		new PrimaryKey("gamename", gamename),
        		"set #SID = :val1",
        		updateAttributeNames,
        		updateAttributeValues);
    }

	public static void monitoringGameServer(String gamename){
    	List<String> instanceIds = new ArrayList<String>();
    	List<String> spotInstanceRequestIds = gscTable.getItem(new PrimaryKey("gamename", gamename)).getList("SID");

		boolean anyOpen;
		do {
			DescribeSpotInstanceRequestsRequest describeRequest = new DescribeSpotInstanceRequestsRequest();
			describeRequest.setSpotInstanceRequestIds(spotInstanceRequestIds);

			anyOpen=false;

			try {
				DescribeSpotInstanceRequestsResult describeResult = ec2.describeSpotInstanceRequests(describeRequest);
				List<SpotInstanceRequest> describeResponses = describeResult.getSpotInstanceRequests();

				for (SpotInstanceRequest describeResponse : describeResponses) {
					if (describeResponse.getState().equals("open")) {
						anyOpen = true;
						break;
					}
					instanceIds.add(describeResponse.getInstanceId());
				}
			} catch (AmazonServiceException e) {
				anyOpen = true;
			} try {
				// Sleep for 60 seconds.
				Thread.sleep(30*1000);
				} catch (Exception e) {
					// Do nothing because it woke up early.
					}
		} while (anyOpen);

		Map<String,String> updateAttributeNames = new HashMap<String,String>();
        updateAttributeNames.put("#status", "status");
        updateAttributeNames.put("#IID", "IID");

        Map<String, Object> updateAttributeValues = new HashMap<String, Object>();
        updateAttributeValues.put(":val1", "running");
        updateAttributeValues.put(":val2", instanceIds);
        gscTable.updateItem(
    		new PrimaryKey("gamename", gamename),
    		"SET #status = :val1, #IID = :val2",
    		updateAttributeNames,
    		updateAttributeValues);
	}

    public static String getGameServerInfo(String gamename) throws IPNotFoundException{
    	String result = "";

    	HashMap<String, AttributeValue> key_to_get = new HashMap<String,AttributeValue>();
    	key_to_get.put("gamename", new AttributeValue(gamename));

    	Item gameItem = gscTable.getItem(new PrimaryKey("gamename", gamename));
    	if(gameItem != null){
    		Set<String> instanceIds = gameItem.getStringSet("IID");
    		DescribeInstancesRequest request = new DescribeInstancesRequest().withInstanceIds(instanceIds);

    		DescribeInstancesResult response = ec2.describeInstances(request);
    		for(Reservation reservation : response.getReservations()) {
    			for(Instance instance : reservation.getInstances()) {

    				String pIP = instance.getPublicIpAddress();

    				Map<String,String> updateAttributeNames = new HashMap<String,String>();
    		        updateAttributeNames.put("#PublicIP", "PublicIP");
    		        Map<String, Object> updateAttributeValues = new HashMap<String, Object>();
    		        updateAttributeValues.put(":val1", pIP);
    		        gscTable.updateItem(
    		    		new PrimaryKey("gamename", gamename),
    		    		"SET #PublicIP = :val1",
    		    		updateAttributeNames,
    		    		updateAttributeValues);

    				result = pIP;
    			}
    		}
    	}

    	if(result == "")throw new IPNotFoundException("IPが見つかりません");

    	return result;
    }

    public static void stopGameService(String gamename) throws GameServerException{

    	String serviceName = "";
    	if(gamename == "minecraft forge12.2") serviceName = "minecraft";
    	else serviceName = "sdtd";

    	Item gameItem = gscTable.getItem(new PrimaryKey("gamename", gamename));

    	if(gameItem != null){
			String ip = gameItem.getString("PublicIP");

			try (RemoteShellExecutor executor = new RemoteShellExecutor(ip, "ec2-user", 22)) {
				executor.execute("sudo systemctl stop " + serviceName);
		  	} catch (Exception e) {
		  		throw new GameServerException("GameServerとのリモートシェル実行でエラーが発生しました");
		  	}
    	}
    }

    public static void stopGameServer(String gamename) throws DatabaseException{

    	Item gameItem = gscTable.getItem(new PrimaryKey("gamename", gamename));
    	if(gameItem != null){
    		List<String> spotInstanceRequestIds = gameItem.getList("SID");
    		List<String> instanceIds = gameItem.getList("IID");

    		// stopするゲームサーバの状態を削除.存在しないかrunning状態でない場合、エラーを返す。
    		try{
        		gscTable.deleteItem(new PrimaryKey("gamename", gamename), new Expected("gamename").exists(), new Expected("status").eq("running"));
    		} catch (ConditionalCheckFailedException e){
    			throw new DatabaseException("データがか削除できる状態ではありません");
        	}

    		CancelSpotInstanceRequestsRequest cancelRequest = new CancelSpotInstanceRequestsRequest(spotInstanceRequestIds);
        	ec2.cancelSpotInstanceRequests(cancelRequest);

        	TerminateInstancesRequest terminateRequest = new TerminateInstancesRequest(instanceIds);
            ec2.terminateInstances(terminateRequest);
    	}
    	else throw new DatabaseException("データが存在しません");
    }
}

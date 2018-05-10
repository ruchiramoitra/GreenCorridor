<?php
    $con = mysqli_connect("localhost", "id2263671_sherlock", "sherlock", "id2263671_sherlock");
  
  
    $traffic_id = $_POST["traffic_id"];
  
  
    
    $statement = mysqli_prepare($con, "SELECT * FROM ambulance WHERE traffic_id = ?");
   
 mysqli_stmt_bind_param($statement, "s", $traffic_id);
  
  mysqli_stmt_execute($statement);
    

    mysqli_stmt_store_result($statement);
 
   mysqli_stmt_bind_result($statement, $traffic_id,$source,$destination,$current);
   
 
    $response = array();
 
   $response["success"] = false;  
  
  
    while(mysqli_stmt_fetch($statement)){
    
    $response["success"] = true;
       
 $response["traffic_id"] = $traffic_id;
   
     $response["source"]=$source;

$response["destination"]=$destination ;

$response["current"]=$current ;

         

        
    }
    
  
  echo json_encode($response);

echo json_encode(array("result"=>$response));
?>
## This is a fork

This repository is forked from [Eclipse Dataspace Connector](https://github.com/eclipse-dataspaceconnector/DataSpaceConnector)


## Building and Running

* Generate empty keystore (_dataspaceconnector-keystore.jks_) and put it into the root folder, the content does not matter.


    `keytool -keystore clientkeystore -genkey`

* Create dataspaceconnector-vault.properties with AWS credentials for each artifact:

  
    `part-00000-beb25069-8ffc-496d-be96-bcda0ed55edd-c000.csv={ "edctype" \: "dataspaceconnector\:secrettoken"  , "sessionToken" \: "<...>", "accessKeyId" \: "<...>", "secretAccessKey" \: "<...>" }`

* Build and run as described in _README.md_
* Run as follows:

    `java -jar -DdestinationBucket=<BUCKET_NAME> -DdestinationRegion=<REGION> launchers/basic/build/libs/dataspaceconnector-basic.jar`

* Use postman collection in _/postman_ folder

**NOTE:** Filename does not change during transfer.
## This is a fork

This repository is forked from [Eclipse Dataspace Connector](https://github.com/eclipse-dataspaceconnector/DataSpaceConnector)

## Building and Running

* Build as described in _README.md_
* Run as follows:

  `java -jar -Dcreds.part-00000-beb25069-8ffc-496d-be96-bcda0ed55edd-c000.csv="<SERIALIZED_JSON_TOKEN>" -DdestinationBucket=041167962674-eu-central-1-gaia-x-data -DdestinationRegion=eu-central-1 launchers/basic/build/libs/dataspaceconnector-basic.jar`

* Use postman collection in _/postman_ folder


`JSON_TOKEN` format (without escape '\' for quotes):

    `{
      "edctype":"dataspaceconnector:secrettoken",
      "sessionToken":"<TOKEN>",
      "accessKeyId":"<ACCESS_KEY>",
      "secretAccessKey":"<SECRET_KEY>"
    }`


**NOTE:** Filename does not change during transfer.

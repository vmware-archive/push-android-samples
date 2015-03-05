#!/bin/bash
APPUUID=3f19f4a4-67b4-45a9-aa19-e73b9fc8bc68
APPKEY=92d293de-ebf7-4426-8546-b98c8ebb4333
#RESPONSE_DATA_JSON=response_data_delete_all.json
RESPONSE_DATA_JSON=response_data_complex.json
#RESPONSE_DATA_JSON=response_data_one_item.json

# Read the registered device ID from the currently attached Android device
adb pull /mnt/sdcard/Android/data/io.pivotal.android.push.sample/files/pushlib/device_uuid.txt device_uuid.txt
DEVICEID=`cat device_uuid.txt`

UPDATE_JSON=`jq '.|tostring' < ${RESPONSE_DATA_JSON}`

http -a ${APPUUID}:${APPKEY} http://transit-push.cfapps.io/v1/push <<HTTPBODY
  {"message":
    {"body":"hello rob!",
      "custom":
        {"android":
          {"message":"SNEH",
           "pivotal.push.geofence_update_available":true,
           "pivotal.push.geofence_update_json":${UPDATE_JSON}
          }
        }
    },
    "target":{"devices":["${DEVICEID}"]}
  }
HTTPBODY

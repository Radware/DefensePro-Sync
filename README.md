# DefensePro-Sync

Sync DefensePro Configuration & Baselines.

## Installation

Download the release version of this project and upload to vDirect.

# Important Notes
Sync may take couple of min, during that time DP may terminate the connection, it is good practice to set the below 
values (value may change upon the amount of policies needed to be sync):
    
    - manage telnet session-timeout set 35
    - manage ssh session-timeout set 35

In the event of large number of policies, you may need to set to true the "Update Policies for each import operation" boolean.

## Supported Features

	- Network Policy with all its dependencies.
	- Server Policy with all its dependencies.
	- HTTPs Flood Protection
	- SSL Protected Objects
	- SSL Certificates\Keys
	- Network classes
	- Black \ White Lists
	- Packet anamoly settings

## Prerequisite & Limitations

1. Radware recommends using SNMPv3 once registering the devices in Vision.
2. If the max number of policies (Tunning values) is different between the devices the script will halt (versions 6 & 7).
3. All devices version must match.
4. Current number of Network \ Server policies should not reach the max values (1 less than the max number in tunning values).
5. Script should be run once \ twice a day (baselines are not updated that frequently).
6. Minimum of 30 min should be left between each run(regardless which device).

## General Information

1. the script takes as input minimum of two devices, one source device (Master) and array of destination devices.
2. During synchronization NO configuration changes are allowed.
3. The script goes ahead and apply all configuration changes.
4. No rollback option is available, set device backup schedule task on Vision server.
5. Max duration for the Script should take not more than 10 min.

## Supported Versions
[Supported Versions](https://support.radware.com/app/answers/answer_view/a_id/1036305/loc/en_US)

## Radware KB
[Radware KB](https://support.radware.com/app/answers/answer_view/a_id/1033631/loc/en_US)

## How to install

1. Upload the zip file as is to vDirect workflow template (Inventory->workflow template).
2. create a workflow instance from the workflow template.
3. From this point forward you can re-use the workflow instance for each sync operation.
4. Under the workflow instance (operations->workflows) you should have two options; 'Sync Action', 'Test Email'.
5. For scheduled job (operations-> scheduled job)

## How to Use
1. 'Test Email' - Since this script can be executed from vDirect only, you can recieve the result output as an email, use this function
	to test SMTP is working as exepected.
2. 'Sync Action' - 
	* Source DP - setting the device to be imported from.
	* Dest DP - setting the destination DP list.
    * Select src device Dynamic (false) - overriding the source & dest device and choosing the source device as the one who has the current higher bw.
	* Delete Passive Device spare Policies (false) - Any policy that exists on dest device but not on source will be deleted.
	* Sync SSL Certificates (false) - Syncs Certs+Key, Requires HTTPs credentials input (Credentials must be the same for all devices)
	* Sync Configuration (true) - Sync device config, mainly network policies and their dependencies (as mentioned in 'General Information').
	* Sync Black\White Lists (false) - This function will sync all network classes and black\white lists.
	* Policy Import Interval (in seconds) - Import Interval between each import policy.
 	* Update Policies for each import operation (false) - In case of import policy failures, enable this checkbox.
	* DNS Baselines (true) - Sync network policy DNS baseline, if any.
	* BDoS Baselines (true) - Sync network policy BDoS baseline, if any.
	* Network Protection Baseline Exception List - In case of Active-Active you can select which policy baseline to to sync.
	* HTTP Baselines (false) - Relevant for DP versions 6 & 7 only, syncing HTTP baselines.
	* Mail Notification (false)- Send Mail report on each exectuion, including errors if any.

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

## CopyRights
Copyright 2020 Radware – All Rights Reserved

## License
[Apache2.0](https://choosealicense.com/licenses/apache-2.0/)

## Mail Snapshot
![](https://i.imgur.com/gCW3gzx.png)

## Disclaimer
There is no warranty, expressed or implied, associated with this product.
Use at your own risk.

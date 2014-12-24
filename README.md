# translation

Example code for the DigiKoppeling V3.0 Translatiedienst. This code performs the conversation mapping of message between ebMS and WS-RM. The specifications of the DigiKoppeling V3.0 Translatiedienst can be found under:
	https://www.logius.nl/fileadmin/logius/product/digikoppeling/Digikoppeling_3_0_TranslatieSpecificatie_v1_0_.pdf

## Building
### How to build translation from source
Requires ant or maven 3.0+  and Java 7+
----	 
	 git clone git@github.com:rinis-dev/translation.git
	 cd translation
	 mvn clean install
----	 
	 
This will create translation-1.0-SNAPSHOT.jar in directory 'target', place this jar in any directory you want.

## Install and configure database
This code provides support for PostgreSQL only, other database can be added.

### PostgreSQL

1. install postgresql 8.3+ - During the install, for the default user 'postgres', make the password 'postgres' (ignore quotes). For Mac OSX you should read the link - [memory configuration info for OSX](http://support.bitrock.com/article/postgresql-cannot-allocate-memory-on-mac-os-x)

2. copy the database creation scripts to your PostgreSQL install

   	copy translation/src/main/sql/translation.sql to the PostgreSQL bin directory

3. log in as the PostgreSQL user 'postgres' created during the install

4. set the password for the default user 'postgres' as a temporary environment variable

----	 
       export PGPASSWORD="postgres"
----	 

5. cd to the PostgreSQL bin directory

6. create a postgres user translation with default password translation

----	 
   	  ./createuser -s -d -P translation
----	 
	  	       
7. create the translation database

----	 
   	  ./createdb -O corvus translation
----	 
		     		
8. Run the db create tables scripts

----	 
       ./psql -f translation.sql translation
----	 

9. Logout as user postgres

# Running translation
## The java translation

----	 
[shell]$ java -classpath <jar location>/translation-1.0-SNAPSHOT.jar translation
No arguments found:

translation [MODES:-eu|-wu|-er|-wr|-i|-h][-m <host> -p <port> -d <database> -u <user> -pw <passwd>]
[-t <timestamp> -me <eb:MessageId> -re <eb:RefToMessageId> -ce <eb:ConversationId>
-mw <wsa:MessageId> -rw <wsa:RelatesTo>]

Translation modes:
-eu  update WS-RM attributes for message from ebMS
-wu  update ebMS attributes for message from WS-RM
-er  retrieve the ebMS conversationId based of WS-RM RelatesTo
-wr  retrieve the WS-RM RelatesTo based of ebMS RefToMessageId or ConversationId
-i   insert WS-RM or ebMS data

Translation attributes:
-m   database host
-p   database port
-d   database name
-u   database user
-pw  database user password
-t   timestamp
-me  ebMS MessageId
-re  ebMS RefToMessageId
-ce  ebMS ConversationId
-mw  WS-RM MessageId
-rw  WS-RM RelatesTo
-h   this help page
[shell]$
----	 

## bash translation

Under translation/src/main/bash a set of bash scripts can be found. Those scripts perform the same actions as the java code. Also these scripts only work with a PostgreSQL database. All PostgreSQL specific scripts are under the postgresql directory.

Within the postgresl the file common_psql contains the PostgreSQL setting:

----	 
#!/bin/bash

HOST=localhost
PORT=5432
USER=translation
DBASE=translation

PSQL="/usr/bin/psql -h ${HOST} -p ${PORT} -U ${USER} -d ${DBASE}"
----	 

You can change these to your own settings.

----	 
The scripts themselves have the following commandline options:
./insert_translation [-t <timestamp>][-m <ebms_message_id> -r <ebms_ref_to_message_id> -c <ebms_conversation_id>][-i <ws_message_id> -w <ws_relates_to>]
./update_translation [-e][-m <ebms_message_id> -r <ebms_ref_to_message_id> -c <ebms_conversation_id>][-i <ws_message_id> -w <ws_relates_to>]
./ebms_wsrm_translation -r <ebms_ref_to_message_id> -c <ebms_conversation_id>
./wsrm_ebms_translation -r <ws_relates_to> -c <conversation_id>
----	 

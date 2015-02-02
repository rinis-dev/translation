/*
 * Translation - Example code for the DigiKoppeling V3.0 Translation Service
 *
 * Copyright (C) 2014 Stichting RINIS
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 *
 * Stichting RINIS
 * Amstelveen
 * the Netherlands
 * info@rinis.nl
 */
import translation.pgsql.connect;
import translation.pgsql.db_translation;

import java.io.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.sql.Connection;

/***
 * This the main class of translation
 *
 * @author Hans Sinnige
 */
public class translation {
    public static void main(String[] argv) {
	// For simplicity all parameters are passed via commandline
	// arguments. First declarations of all arguments that can
	// be set.
	String dbase = "translation";
	String port = "5432";
	String host = "localhost";
	String user = "translation";
	String pw = "";
	String TimeStamp = "";
	String EbmsMessageId = "";
	String EbmsRefToMessageId = "";
	String EbmsConversationId = "";
	String WsMessageId = "";
	String WsRelatesTo = "";
	String FileName = "";
	String FileDirectory = "";
	boolean ebmsUpdate = false;
	boolean wsUpdate = false;
	boolean ebmsRead = false;
	boolean wsRead = false;
	boolean ebmsReadFile = false;
	boolean wsReadFile = false;
	boolean insert = false;

	// Parse all arguments and assign them to their variable
        if (argv.length > 0) {
            int index = 0;
            while(index < argv.length) {
                if (argv[index].equals("-d")) {
                    dbase = argv[++index];
                } else if (argv[index].equals("-m")) {
                    host = argv[++index];
                } else if (argv[index].equals("-p")) {
                    port = argv[++index];
                } else if (argv[index].equals("-u")) {
                    user = argv[++index];
                } else if (argv[index].equals("-pw")) {
                    pw = argv[++index];
                } else if (argv[index].equals("-t")) {
                    TimeStamp = argv[++index];
                } else if (argv[index].equals("-eu")) {
                    ebmsUpdate = true;
                } else if (argv[index].equals("-wu")) {
                    wsUpdate = true;
                } else if (argv[index].equals("-er")) {
                    ebmsRead = true;
                } else if (argv[index].equals("-wr")) {
                    wsRead = true;
                } else if (argv[index].equals("-ef")) {
                    ebmsReadFile = true;
                } else if (argv[index].equals("-wf")) {
                    wsReadFile = true;
                } else if (argv[index].equals("-i")) {
                    insert = true;
                } else if (argv[index].equals("-me")) {
                    EbmsMessageId = argv[++index];
                } else if (argv[index].equals("-re")) {
                    EbmsRefToMessageId = argv[++index];
                } else if (argv[index].equals("-ce")) {
                    EbmsConversationId = argv[++index];
                } else if (argv[index].equals("-mw")) {
                    WsMessageId = argv[++index];
                } else if (argv[index].equals("-rw")) {
                    WsRelatesTo = argv[++index];
                } else if (argv[index].equals("-fn")) {
                    FileName = argv[++index];
                } else if (argv[index].equals("-fd")) {
                    FileDirectory = argv[++index];
		} else if (argv[index].equals("-h")) {
		    ShowHelp();
		    System.exit(0);
		}
		index++;
	    }
	} else {
	    System.out.println("No arguments found:\n");
	    ShowHelp();
	    System.exit(1);
	}

	// Check if any of the modes is selected
	if (!ebmsUpdate && !wsUpdate && !ebmsRead && !wsRead && !insert && !ebmsReadFile && !wsReadFile) {
	    System.out.println("No mode selected:\n");
	    ShowHelp();
	    System.exit(1);
	}

	// Connect to the database and create a db_translation instance
	Connection con = connect.GetConnection( host, port, dbase, user, pw );
	db_translation dbtrans = new db_translation();
	int ret_val = -1;

	// Adding the WS-RM attribute(s) to the data of a message received via ebMS
	if (ebmsUpdate) {
	    ret_val = dbtrans.UpdateEbmsTranslation( con, TimeStamp, EbmsMessageId, WsMessageId, WsRelatesTo );
	    if (ret_val != 0) {
		System.out.println("Update entry failed.");
	    }
	}

	// Adding the ebMS attribute(s) to the data of a message received via WS-RM
	else if (wsUpdate) {
	    ret_val = dbtrans.UpdateWsTranslation( con, TimeStamp, EbmsMessageId, EbmsRefToMessageId, EbmsConversationId, WsMessageId );
	    if (ret_val != 0) {
		System.out.println("Update entry failed.");
	    }
	}

	// Insert ebMS or WS-RM data to the database
	else if (insert) {
	    String timeStamp = GetCheckTimeStamp( TimeStamp );
	    // Find out if this is data received via WS-RM
	    if (WsMessageId.length() != 0) {
		ret_val = dbtrans.InsertWsTranslation( con, timeStamp, WsMessageId, WsRelatesTo );
		if (ret_val == 0) {
		    if (FileName.length() != 0) {
			ret_val = dbtrans.AddWsFile( con, WsMessageId, FileName );
		    }
		}
	    // Or via ebMS
	    } else if (EbmsMessageId.length() != 0) {
		ret_val = dbtrans.InsertEbmsTranslation( con, timeStamp, EbmsMessageId, EbmsRefToMessageId, EbmsConversationId );
		if (ret_val == 0) {
		    if (FileName.length() != 0) {
			ret_val = dbtrans.AddEbmsFile( con, EbmsMessageId, FileName );
		    }
		}
	    }

	    if (ret_val != 0) {
		System.out.println("Insert entry failed.");
	    }
	}

	// Retrieve the conversation ID
	else if (ebmsRead) {
	    String conversationId = ReadTranslationEbmsConvId( con, WsMessageId, WsRelatesTo, EbmsConversationId );
	    System.out.println( "ebms_conversation_id=" + conversationId + ";");
	}

	// Retrieve the RelatesTo
	else if (wsRead) {
	    String wsRelatesTo = "";
	    // First check if RefToMessageId is given
	    if (EbmsRefToMessageId.length() != 0) {
		ret_val = dbtrans.ReadTranslationWsOnEbmsMsgId( con, EbmsRefToMessageId );
		if (ret_val == 0) {
		    wsRelatesTo = dbtrans.ws_message_id;
		}
	    // Else use the ConversationId
	    } else if (EbmsConversationId.length() != 0) {
		ret_val = dbtrans.ReadTranslationWsOnEbmsConvId( con, EbmsConversationId );
		if (ret_val == 0) {
		    wsRelatesTo = dbtrans.ws_message_id;
		}
	    }
	    System.out.println( "ws_relates_to=" + wsRelatesTo + ";");
	}

	// Read the file related to given ebMS MessageId
	else if (ebmsReadFile) {
	    if (EbmsMessageId.length() != 0 && FileDirectory.length() != 0) {
		ret_val = dbtrans.RetrieveEbmsFile( con, EbmsMessageId, FileDirectory );
	    } else {
		ShowHelp();
	    }

	    if (ret_val == 0) {
		System.out.println( "file=" + FileDirectory + "/" + dbtrans.filename + ";");
	    } else {
		System.out.println( "Failed to retrieve file from database.");		
	    }
	}

	// Read the file related to given WS MessageId
	else if (wsReadFile) {
	    if (WsMessageId.length() != 0 && FileDirectory.length() != 0) {
		ret_val = dbtrans.RetrieveWsFile( con, WsMessageId, FileDirectory );
	    } else {
		ShowHelp();
	    }

	    if (ret_val == 0) {
		System.out.println( "file=" + FileDirectory + "/" + dbtrans.filename + ";");
	    } else {
		System.out.println( "Failed to retrieve file from database.");		
	    }
	}
	connect.CloseConnection( con );
    }

    /**
     * ReadTranslationEbmsConvId() 
     * Retrieve the ConversationId from the database based upon the value of WsRelatesTo
     * by quering the translation table via ReadRranslationEbmsConvId().
     * If no conversationId is found the passed ConversationId is returned.
     * If no ConversationId is passed a conversationID is generated by GenerateConversationId()
     *
     * @param con - Connection to the database
     * @param WsMessageId - WsMessageID used in case no ConversationID was found and need to be added to the database
     * @param WsRelatesTo - WsRelatesTo on which the conversationID is queried
     * @param ConversationId - conversationId provided
     *
     * @return The conversationId found or used
     */
    public static String ReadTranslationEbmsConvId( Connection con, String WsMessageId, String WsRelatesTo, String ConversationId )
    {
	db_translation dbtrans = new db_translation();
	String conversationId = "";
	int ret_val = -1;
	boolean update = false; 

	// Retrieve the conversation ID from the database via the relatesto attribute
	ret_val = dbtrans.ReadTranslationEbmsConvId( con, WsRelatesTo );
	if (ret_val == 0) {
	    if (dbtrans.ebms_conversation_id.length() != 0) {
		conversationId = dbtrans.ebms_conversation_id;
	    } else {
		update = true;
	    }
	} else {
	    update = true;
	}

	// If no converstaion ID was found, use the provided one or generate one.
	// In all cases update current database entry and return the conversation ID
	if (update) {
	    if (ConversationId.length() != 0) {
		conversationId = ConversationId;
	    } else {
		conversationId = GenerateConversationId();
	    }
	    // Try to update the database, if fails we still return the conversation ID
	    ret_val = dbtrans.UpdateWsTranslation( con, "", "", "", conversationId, WsMessageId );
	}
	return conversationId;
    }

    /**
     * GenerateConversationId()
     * Generate a Conversation ID based of the currenttime adding TRANS_ in front to add
     * an indication that is was generated by the translation service
     *
     * @return The conversationId generated
     */
    public static String GenerateConversationId() {
	long time;
	String conversationId;
	Date timeDate = new Date();
	time = timeDate.getTime();
	conversationId = "TRANS_" + Long.toString(time);
	return conversationId;
    }

    /**
     * GetCheckTimeStamp()
     * Checks if the given timestamp contains any data, if not a timestamp is
     * generated and returned
     *
     * @param TimeStamp - TimeStamp provided
     *
     * @return The timestamp
     */
    public static String GetCheckTimeStamp( String TimeStamp ) {
	String timeStamp = TimeStamp;
	if (timeStamp.length() == 0) {
	    Date timeDate = new Date();
	    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    timeStamp = format.format(timeDate);
	}
	return timeStamp;
    }

    /**
     * ShowHelp()
     * Shows the help page.
     */
    public static void ShowHelp() {
	System.out.println("translation [MODES:-eu|-wu|-er|-wr|-ef|-wf|-i|-h][-m <host> -p <port> -d <database> -u <user> -pw <passwd>]");
	System.out.println("[-t <timestamp> -me <eb:MessageId> -re <eb:RefToMessageId> -ce <eb:ConversationId>");
	System.out.println("-mw <wsa:MessageId> -rw <wsa:RelatesTo> -fn <Filename> -fd <Directory>]\n");
	System.out.println("Translation modes:");
	System.out.println("-eu  update WS-RM attributes for message from ebMS");
	System.out.println("-wu  update ebMS attributes for message from WS-RM");
	System.out.println("-er  retrieve the ebMS conversationId based of WS-RM RelatesTo");
	System.out.println("-wr  retrieve the WS-RM RelatesTo based of ebMS RefToMessageId or ConversationId");
	System.out.println("-ef  retrieve the file based on the ebMS MessageId");
	System.out.println("-wf  retrieve the file based on the WS-RM MessageId");
	System.out.println("-i   insert WS-RM or ebMS data\n");
	System.out.println("Translation attributes:");
	System.out.println("-m   database host");
	System.out.println("-p   database port");
	System.out.println("-d   database name");
	System.out.println("-u   database user");
	System.out.println("-pw  database user password");
	System.out.println("-t   timestamp");
	System.out.println("-me  ebMS MessageId");
	System.out.println("-re  ebMS RefToMessageId");
	System.out.println("-ce  ebMS ConversationId");
	System.out.println("-mw  WS-RM MessageId");
	System.out.println("-rw  WS-RM RelatesTo");
	System.out.println("-fn  Filename");
	System.out.println("-fd  File directory");
	System.out.println("-h   this help page");
    }
}


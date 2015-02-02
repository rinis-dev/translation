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
package translation.pgsql;

import java.io.*;
import java.io.File;
import java.io.InputStream;

import java.sql.*;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This class provides the Postgresql interface to the translation table
 *
 * @author Hans Sinnige
 */
public class db_translation {
    public long   id;
    public String timestamp;
    public String ebms_conversation_id;
    public String ebms_message_id;
    public String ebms_ref_to_message_id;
    public String ws_message_id;
    public String ws_relates_to;
    public String filename;

    /***
     * ReadTranslationEbmsConvId()
     * Finds the conversationId that is related to the given WsRelatesTo. The
     * found conversationId is stored inside the class parameters
     *
     * @param con - connection to the Postgresql database
     * @param WsRelatesTo - WsRelatesTo
     *
     * @return 0 if successfully found
     */
    public int ReadTranslationEbmsConvId( Connection con, String WsRelatesTo )
    {
	int retval = -1;
	Statement statement = null;

	try {
	    statement = con.createStatement();
	    ResultSet resset = null;
	    resset = statement.executeQuery("SELECT ebms_conversation_id,ebms_message_id FROM translation WHERE ws_message_id = '" + WsRelatesTo + "'");

	    if (resset.next()) {
		this.ebms_conversation_id = resset.getString("ebms_conversation_id");
		this.ebms_message_id = resset.getString("ebms_message_id");
		retval = 0;
	    }
	    else {
		this.ebms_conversation_id = "";
		this.ebms_message_id = "";
		retval = -1;
	    }

	    statement.close();

	} catch (SQLException se) {
	    se.printStackTrace();
	    return -1;
	}
	return(retval);
    }

    /***
     * ReadTranslationWsOnEbmsMsgId()
     * Searches for the WsMessageId connected to the given EbmsMessageId, the 
     * found WsMessageId is stored inside the class parameters
     *
     * @param con - connection the the Postgresql database
     * @param EbmsMessageId - EbmsMessageId
     *
     * @return 0 if successfull 
     */
    public int ReadTranslationWsOnEbmsMsgId( Connection con, String EbmsMessageId )
    {
	int retval = -1;
	Statement statement = null;

	try {
	    statement = con.createStatement();
	    ResultSet resset = null;
	    resset = statement.executeQuery("SELECT ws_message_id FROM translation WHERE id = (SELECT MAX(id) FROM translation WHERE ebms_message_id = '" + EbmsMessageId + "' AND ws_message_id IS NOT NULL)");

	    if (resset.next()) {
		this.ws_message_id = resset.getString("ws_message_id");
		retval = 0;
	    }
	    else {
		this.ws_message_id = "";
		retval = -1;
	    }

	    statement.close();

	} catch (SQLException se) {
	    se.printStackTrace();
	    return -1;
	}
	return(retval);
    }

    /***
     * ReadTranslationWsOnEbmsConvId()
     * Searches for the WsMessageId connected to the given ConversationId, the 
     * found WsMessageId is stored inside the class parameters
     *
     * @param con - connection the the Postgresql database
     * @param EbmsConversationId - EbmsConversationId
     *
     * @return 0 if successfull 
     */
    public int ReadTranslationWsOnEbmsConvId( Connection con, String EbmsConversationId )
    {
	int retval = -1;
	Statement statement = null;

	try {
	    statement = con.createStatement();
	    ResultSet resset = null;

	    resset = statement.executeQuery("SELECT ws_message_id FROM translation WHERE id = (SELECT MAX(id) FROM translation WHERE ebms_conversation_id = '" + EbmsConversationId + "' AND ws_message_id IS NOT NULL)");

	    if (resset.next()) {
		this.ws_message_id = resset.getString("ws_message_id");
		retval = 0;
	    }
	    else {
		this.ws_message_id = "";
		retval = -1;
	    }

	    statement.close();

	} catch (SQLException se) {
	    se.printStackTrace();
	    return -1;
	}
	return(retval);
    }

    /***
     * InsertTranslation()
     * Main method for adding a new entry into the translation table
     *
     * @param con - connection to the Postgresql database
     * @param TimeStamp - TimeStamp
     * @param EbmsMessageId - EbmsMessageId
     * @param EbmsRefToMessageId - EbmsRefToMessageId
     * @param EbmsConversationId - EbmsConversationId
     * @param WsMessageId - WsMessageId
     * @param WsRelatesTo - WsRelatesTo
     *
     * @return 0 if successfull 
     */
    public int InsertTranslation( Connection con, String TimeStamp, String EbmsMessageId, String EbmsRefToMessageId,
				  String EbmsConversationId, String WsMessageId, String WsRelatesTo )
    {
	int retval = -1;
	Statement statement = null;

	try {
	    statement = con.createStatement();
	    retval = statement.executeUpdate("INSERT INTO translation ( timestamp, ebms_message_id, ebms_ref_to_message_id, ebms_conversation_id, ws_message_id, ws_relates_to )" +
					     " VALUES ( '" + TimeStamp + "', '" + EbmsMessageId  + "', '" + EbmsRefToMessageId  +
					     "', '" + EbmsConversationId + "', '" + WsMessageId  + "', '" + WsRelatesTo + "') ");
	    statement.close();
	} catch (SQLException se) {
	    se.printStackTrace();
	    return -1;
	}

	if (retval != 1)
	    return -1;
	else
	    return 0;
    }

    /***
     * InsertEbmsTranslation()
     * Adding a new ebMS entry into the translation table
     *
     * @param con - connection to the Postgresql database
     * @param TimeStamp - TimeStamp
     * @param EbmsMessageId - EbmsMessageId
     * @param EbmsRefToMessageId - EbmsRefToMessageId
     * @param EbmsConversationId - EbmsConversationId
     *
     * @return 0 if successfull 
     */
    public int InsertEbmsTranslation( Connection con, String TimeStamp, String EbmsMessageId, String EbmsRefToMessageId,
				  String EbmsConversationId )
    {
	return InsertTranslation( con, TimeStamp, EbmsMessageId, EbmsRefToMessageId,
				  EbmsConversationId, "", "" );
    }

    /***
     * InsertWsTranslation()
     * Adding a new WS-RM entry into the translation table
     *
     * @param con - connection to the Postgresql database
     * @param TimeStamp - TimeStamp
     * @param WsMessageId - WsMessageId
     * @param WsRelatesTo - WsRelatesTo
     *
     * @return 0 if successfull 
     */
    public int InsertWsTranslation( Connection con, String TimeStamp, String WsMessageId, String WsRelatesTo )
    {
	return InsertTranslation( con, TimeStamp, "", "",
				  "", WsMessageId, WsRelatesTo );
    }

    /***
     * UpdateTranslation()
     * The main method for updating the translation table
     *
     * @param con - connection to the Postgresql database
     * @param SqlSet - the set instruction for the update
     * @param SqlWhere - the where instruction for the update
     *
     * @return 0 if successfull 
     */
    public int UpdateTranslation( Connection con, String SqlSet, String SqlWhere )
    {
	int retval = -1;
	Statement statement = null;

	try {
	    statement = con.createStatement();
	    retval = statement.executeUpdate("UPDATE translation SET " + SqlSet + 
					     " WHERE id=(SELECT MAX(id) FROM translation WHERE " + SqlWhere + ")");
	    statement.close();
	} catch (SQLException se) {
	    se.printStackTrace();
	    return -1;
	}

	if (retval != 1)
	    return -1;
	else
	    return 0;
    }

    /***
     * UpdateEbmsTranslation()
     * method for updating an ebMS entry in the translation table
     *
     * @param con - connection to the Postgresql database
     * @param TimeStamp - TimeStamp
     * @param EbmsMessageId - EbmsMessageId
     * @param WsMessageId - WsMessageId
     * @param WsRelatesTo - WsRelatesTo
     *
     * @return 0 if successfull 
     */
    public int UpdateEbmsTranslation( Connection con, String TimeStamp, String EbmsMessageId, String WsMessageId,
				      String WsRelatesTo )
    {
	int ret_val = -1;
	Statement statement = null;
	String sql_set = "";
	String sql_where = "";
	boolean first_set = true;

	if (EbmsMessageId.length() != 0) {
	    sql_where = "ebms_message_id = '" + EbmsMessageId + "'";

	    if (WsMessageId.length() != 0) {
		sql_set = "ws_message_id = '" + WsMessageId + "'";
		first_set = false;
	    }
	    if (WsRelatesTo.length() != 0) {
		if (first_set) {
		    first_set = false;
		} else {
		    sql_set += ", ";
		}
		sql_set += "ws_relates_to = '" + WsRelatesTo + "'";
	    }
	    if (TimeStamp.length() != 0) {
		if (first_set) {
		    first_set = false;
		} else {
		    sql_set += ", ";
		}
		sql_set += "timestamp = '" + TimeStamp + "'";
	    }

	    if (sql_set.length() != 0) {
		ret_val = UpdateTranslation( con, sql_set, sql_where );
	    }
	}

	return ret_val;
    }

    /***
     * UpdateWsTranslation()
     * method for updating a WS-RM entry in the translation table
     *
     * @param con - connection to the Postgresql database
     * @param TimeStamp - TimeStamp
     * @param EbmsMessageId - EbmsMessageId
     * @param EbmsRefToMessageId - EbmsRefToMessageId
     * @param EbmsConversationId - EbmsConversationId
     * @param WsMessageId - WsMessageId
     *
     * @return 0 if successfull 
     */
    public int UpdateWsTranslation( Connection con, String TimeStamp, String EbmsMessageId, String EbmsRefToMessageId,
				  String EbmsConversationId, String WsMessageId )
    {
	int ret_val = -1;
	Statement statement = null;
	String sql_set = "";
	String sql_where = "";
	boolean first_set = true;

	if (WsMessageId.length() !=0) {
	    sql_where = "ws_message_id = '" + WsMessageId + "'";
	    if (EbmsMessageId.length() != 0) {
		sql_set = "ebms_message_id = '" + EbmsMessageId + "'";
		first_set = false;
	    }

	    if (EbmsRefToMessageId.length() != 0) {
		if (first_set) {
		    first_set = false;
		} else {
		    sql_set += ", ";
		}
		sql_set += "ebms_ref_to_message_id = '" + EbmsRefToMessageId + "'";
	    }
	    if (EbmsConversationId.length() != 0) {
		if (first_set) {
		    first_set = false;
		} else {
		    sql_set += ", ";
		}
		sql_set += "ebms_conversation_id = '" + EbmsConversationId + "'";
	    }
	    if (TimeStamp.length() != 0) {
		if (first_set) {
		    first_set = false;
		} else {
		    sql_set += ", ";
		}
		sql_set += "timestamp = '" + TimeStamp + "'";
	    }

	    if (sql_set.length() != 0) {
		ret_val = UpdateTranslation( con, sql_set, sql_where );
	    }
	}
	return ret_val;
    }

    /***
     * AddWsFile()
     * method for adding a file to a WS-RM entry in the translation table
     *
     * @param con - connection to the Postgresql database
     * @param WsMessageId - WsMessageId
     * @param FileName - FileName 
     *
     * @return 0 if successfull 
     */
    public int AddWsFile( Connection con, String WsMessageId, String FileName )
    {
	int ret_val = 0;
        try {
	    File file = new File( FileName );
	    FileInputStream fis = new FileInputStream(file);
	    PreparedStatement ps = con.prepareStatement("UPDATE translation SET filename = ?, data = ? WHERE ws_message_id = ?;");
	    ps.setString(1, file.getName());
	    ps.setBinaryStream(2, fis, (int)file.length());
	    ps.setString(3, WsMessageId); 
	    ps.executeUpdate();
	    ps.close();
	    fis.close();
	} catch (Exception se) {
		se.printStackTrace();
		ret_val=-1;
	}
	return ret_val;
    }

    /***
     * AddEbmsFile()
     * method for adding a file to an ebMS entry in the translation table
     *
     * @param con - connection to the Postgresql database
     * @param EbmsMessageId - EbmsMessageId
     * @param FileName - FileName 
     *
     * @return 0 if successfull 
     */
    public int AddEbmsFile( Connection con, String EbmsMessageId, String FileName )
    {
	int ret_val = 0;
        try {
	    File file = new File( FileName );
	    FileInputStream fis = new FileInputStream(file);
	    PreparedStatement ps = con.prepareStatement("UPDATE translation SET filename = ?, data = ? WHERE ebms_message_id = ?;");
	    ps.setString(1, file.getName());
	    ps.setBinaryStream(2, fis, (int)file.length());
	    ps.setString(3, EbmsMessageId); 
	    ps.executeUpdate();
	    ps.close();
	    fis.close();
	} catch (Exception se) {
		se.printStackTrace();
		ret_val=-1;
	}
	return ret_val;
    }

    /***
     * RetrieveWsFile()
     * method for retrieving a file from a WS-RM entry in the translation table
     *
     * @param con - connection to the Postgresql database
     * @param WsMessageId - WsMessageId
     * @param FileDirectory - FileDirectory
     *
     * @return 0 if successfull 
     */
    public int RetrieveWsFile( Connection con, String WsMessageId, String FileDirectory )
    {
	int ret_val = 0;
        try {
	    PreparedStatement ps = con.prepareStatement("SELECT filename,data from translation WHERE ws_message_id = ?;");
	    ps.setString(1, WsMessageId); 
	    ResultSet rs = ps.executeQuery();
	    if (rs != null) {
		while (rs.next()) {                                            
		    this.filename = rs.getString(1);
		    InputStream data = rs.getBinaryStream(2);
		    String fileOut = FileDirectory + "/" + this.filename;
		    SaveOutputStream(fileOut,data);
		    data.close();
		}
	    }
	    rs.close();
	    ps.close();
	} catch (Exception se) {
		se.printStackTrace();
		ret_val=-1;
	}
	return ret_val;
    }

    /***
     * RetrieveEbmsFile()
     * method for retrieving a file from an ebMS entry in the translation table
     *
     * @param con - connection to the Postgresql database
     * @param EbmsMessageId - EbmsMessageId
     * @param FileDirectory - FileDirectory
     *
     * @return 0 if successfull 
     */
    public int RetrieveEbmsFile( Connection con, String EbmsMessageId, String FileDirectory )
    {
	int ret_val = 0;
        try {
	    PreparedStatement ps = con.prepareStatement("SELECT filename,data from translation WHERE ebms_message_id = ?;");
	    ps.setString(1, EbmsMessageId); 
	    ResultSet rs = ps.executeQuery();
	    if (rs != null) {
		while (rs.next()) {                                            
		    this.filename = rs.getString(1);
		    InputStream data = rs.getBinaryStream(2);
		    String fileOut = FileDirectory + "/" + this.filename;
		    SaveOutputStream(fileOut,data);
		    data.close();
		}
	    }
	    rs.close();
	    ps.close();
	} catch (Exception se) {
		se.printStackTrace();
		ret_val=-1;
	}
	return ret_val;
    }

    /***
     * SaveOutputStream()
     * method for saving an outputstream
     *
     * @param name - filename
     * @param body - data to be stored
     *
     * @return 0 if successful
     */
    public static void SaveOutputStream(String name, InputStream body) {
	int c;
	try {
	    OutputStream f = new FileOutputStream(name);
	    while ((c=body.read())>-1) {
		f.write(c);
	    }
	    f.close();
	} catch (Exception e) {
	    System.err.println("Exception: "+e.getMessage());
	    e.printStackTrace();
	}
    }
}

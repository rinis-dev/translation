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

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;

/***
 * This class provides the connection to the Postgresql database
 *
 * @author Hans Sinnige
 */
public class connect {

    /**
     * GetConnection()
     * Creates and returns a connection to the Postgresql database
     *
     * @param Host - host address on which the Postgresql database runs
     * @param Port - port via which the Postgresql database can be accessed
     * @param Database - name of database that is used for translation
     * @param User - user under who the database is accessible
     * @param Password - password of user to access database 
     *
     * @return The connection
     */
    public static Connection  GetConnection( String Host, String Port, String Database, String User, String Password )
    {
	Connection con = null;
	try {
	    Class.forName("org.postgresql.Driver");
	} catch (ClassNotFoundException cnfe) {
	    System.out.println( "Couldn't find the Postgresql Driver" );
	    cnfe.printStackTrace();
	    System.exit(1);
	}

	try {
	     con = DriverManager.getConnection("jdbc:postgresql://" +
							 Host + ":" + Port + "/" + Database,
							 User, Password );
	} catch (SQLException se) {
	    System.out.println("Couldn't connect to Postgresql Server: " + Host + ":" + Port );
	    se.printStackTrace();
	    System.exit(1);
	}
	return con;
    }

    /***
     * CloseConnection()
     * Closes the provided connection
     *
     * @param con - connection to be closed
     */
    public static void CloseConnection( Connection con )
    {
	if (con != null) {
	    try {
		con.close();
	    } catch (SQLException se) {
		System.out.println("Couldn't close connection to Postgresql Server:");
		se.printStackTrace();
	    }
	}
    }
}

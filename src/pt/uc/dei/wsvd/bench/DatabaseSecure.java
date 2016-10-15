/** ****************************************************************************
 *
 *
 * Pedro Henrique Grandin
 *
 *
 *******************************************************************************
 * Last changed on : $Date: 2016-10-15 $
 * Last changed by : $Author: pedro_grandin $
 ***************************************************************************** */
package pt.uc.dei.wsvd.bench;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements a simple
 *
 * @since r334
 * @version $Revision: 1436 $
 *
 * @author $Author: nmsa $
 */
public class DatabaseSecure {
	
	
    private static final Logger logger = Logger.getLogger(Database.class.getName());
    public static final int CIVS_DATABASE_MAX_ROWS = 1000;
    private static final int DATABASE_CONNECTION_POOL_SIZE = 10;
    private static final int DATABASE_CONNECTION_MAX_USAGE = 100;

    private final static BlockingQueue<Connection> pooll;
    private final static ConcurrentHashMap<Connection, AtomicInteger> usage;

    
    static {
        logger.info("Setting UP Database ");
        pooll = new ArrayBlockingQueue<Connection>(DATABASE_CONNECTION_POOL_SIZE + 10);
        usage = new ConcurrentHashMap<Connection, AtomicInteger>(DATABASE_CONNECTION_POOL_SIZE + 10);
        boolean succes = false;
        try {
        	FileInputStream in = new FileInputStream(System.getProperty("dbConnection.properties"));
        	Properties prop = new Properties();
        	prop.load(in);
        	in.close();
        	
        	String driverName = prop.getProperty("driverName");
        	String url = prop.getProperty("url");
        	String userName = prop.getProperty("userName");
        	String passwd = prop.getProperty("passwd");
        	
            Class.forName(driverName);
            for (; pooll.size() < DATABASE_CONNECTION_POOL_SIZE;) {
                Connection c = null;
                try {
                    c = DriverManager.getConnection(url, userName, passwd);
                    succes = true;
                } catch (SQLException e) {
                    logger.log(Level.SEVERE,"Nao consegue criar conn:  {} ", e);
                }
                if (c != null) {
                    try {
                        usage.put(c, new AtomicInteger(0));
                        pooll.put(c);
                    } catch (InterruptedException ie) {
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE,"Cannot init Database:  {} ", e);
        }
        if (succes) {
            logger.info("Database UP!");
        } else {
            logger.info("Cannot set database UP!");
        }
    }

    public static Connection pickConnection() throws IOException {
        while (true) {
            try {
            	FileInputStream in = new FileInputStream(System.getProperty("dbConnection.properties"));
            	Properties prop = new Properties();
            	prop.load(in);
            	in.close();
            	
            	String url = prop.getProperty("url");
            	String userName = prop.getProperty("userName");
            	String passwd = prop.getProperty("passwd");
            	
                Connection con = pooll.take();
                if (con != null) {
                    if (!con.isClosed()) {
                        return con;
                    } else {
                        usage.remove(con);
                        Connection c = DriverManager.getConnection(url, userName, passwd);
                        usage.put(c, new AtomicInteger(0));
                        return c;
                    }
                }
            } catch (InterruptedException e) {
            } catch (SQLException se) {
            }
        }
    }

    public static void relaseConnection(Connection con) throws IOException {
        if (con != null) {
            try {
            	FileInputStream in = new FileInputStream(System.getProperty("dbConnection.properties"));
            	Properties prop = new Properties();
            	prop.load(in);
            	in.close();
            	
            	String url = prop.getProperty("url");
            	String userName = prop.getProperty("userName");
            	String passwd = prop.getProperty("passwd");
            	
                AtomicInteger get = usage.get(con);
                if (get == null) {
                    get = new AtomicInteger(0);
                }
                if (get.getAndIncrement() < DATABASE_CONNECTION_MAX_USAGE) {
                    try {
                        con.rollback();
                    } catch (SQLException e) {
                        logger.info("con.rollback()...." + e);
                    }
                    pooll.put(con);
                } else {
                    try {
                        con.close();
                    } catch (SQLException ex) {
                        logger.log(Level.SEVERE,"ex = {}", ex);
                    }
                    usage.remove(con);
                    Connection c = null;
                    try {
                        c = DriverManager.getConnection(url, userName, passwd);
                    } catch (SQLException e) {
                        logger.info("Nao consegue criar conn....");
                        logger.log(Level.SEVERE,"e = {}", e);
                    }
                    pooll.put(c);
                    usage.put(c, new AtomicInteger(0));
                }
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE,"e = {}", e);
            }
        }
    }

    public static Statement createStatement(final Connection con) throws SQLException {
        Statement st = con.createStatement();
        st.setMaxRows(CIVS_DATABASE_MAX_ROWS);
        return st;
    }
}


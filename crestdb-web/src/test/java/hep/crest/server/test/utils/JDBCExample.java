package hep.crest.server.test.utils;


import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

import hep.crest.data.repositories.externals.SqlRequests;

public class JDBCExample {

    public static void main(String[] args) {

        // https://docs.oracle.com/javase/8/docs/api/java/sql/package-summary.html#package.description
        // auto java.sql.Driver discovery -- no longer need to load a java.sql.Driver class via Class.forName

        // register JDBC driver, optional, since java 1.6
        /*try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }*/
        
        // auto close connection
        try (Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://127.0.0.1:5432/crestdb", "formica", "gelsomino")) {

            if (conn != null) {
                System.out.println("Connected to the database!");
                
             // All LargeObject API calls must be within a transaction block
                conn.setAutoCommit(false);

                // Get the Large Object Manager to perform operations with
                final LargeObjectManager lobj = conn.unwrap(org.postgresql.PGConnection.class).getLargeObjectAPI();

                // Create a new large object
                final long oid = lobj.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);
                final long soid = lobj.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE);

                // Open the large object for writing
                final LargeObject obj = lobj.open(oid, LargeObjectManager.WRITE);

                // Now open the file
                File file = new File("/tmp/t.inp");
                final FileInputStream fis = new FileInputStream(file);

                // Copy the data from the file to the large object
                final byte buf[] = new byte[2048];
                int s, tl = 0;
                while ((s = fis.read(buf, 0, 2048)) > 0)
                {
                    obj.write(buf, 0, s);
                    tl += s;
                }

                // Close the large object
                obj.close();
                fis.close();
                // Open the large object for writing
                final LargeObject sobj = lobj.open(soid, LargeObjectManager.WRITE);

                // Now open the file
                file = new File("/tmp/sinfo.inp");
                final FileInputStream sfis = new FileInputStream(file);

                // Copy the data from the file to the large object
                final byte sbuf[] = new byte[2048];
                int ss, stl = 0;
                while ((ss = sfis.read(buf, 0, 2048)) > 0)
                {
                    sobj.write(sbuf, 0, ss);
                    stl += ss;
                }

                // Close the large object
                sobj.close();
                sfis.close();
                System.out.println("Start insertion");
                // Now insert the row into imageslo
                final String sql = SqlRequests.getInsertAllQuery("PAYLOAD");
                final PreparedStatement ps = conn.prepareStatement(sql);
                
                
                ps.setString(1, "2anotherhash");
                ps.setString(2, "atype");
                ps.setString(3, "v1");
                ps.setLong(4, oid);
                ps.setLong(5, soid);
                ps.setDate(6, new java.sql.Date(10000000));
                ps.setInt(7, tl);

                ps.executeUpdate();
                ps.close();
                fis.close();

                // Finally, commit the transaction.
                conn.commit();                
                System.out.println("Start reading");

             // All LargeObject API calls must be within a transaction block
                conn.setAutoCommit(false);

                // Get the Large Object Manager to perform operations with
                final LargeObjectManager rlobj = conn.unwrap(org.postgresql.PGConnection.class).getLargeObjectAPI();
                final String rsql = SqlRequests.getFindDataQuery("PAYLOAD");
                final PreparedStatement rps = conn.prepareStatement(rsql);
                rps.setString(1, "ea0fcacc5e798719f4b84f06053ab377cb2ad9504a874d07aca509308bcdd057");
                final ResultSet rs = rps.executeQuery();
                while (rs.next())
                {
                    // Open the large object for reading
                    final long roid = rs.getLong(1);
                    final LargeObject robj = rlobj.open(roid, LargeObjectManager.READ);

                    // Read the data
                    final byte rbuf[] = new byte[robj.size()];
                    robj.read(rbuf, 0, robj.size());
                    // Do something with the data read here

                    // Close the object
                    robj.close();
                }
                rs.close();
                rps.close();

                // Finally, commit the transaction.
                conn.commit();
                
                System.out.println("Retrieved buffer of "+buf.length+" size");
                final String out = new String(buf);
                System.out.println("Content is "+out);

            } else {
                System.out.println("Failed to make connection!");
            }

        } catch (final SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        } catch (final Exception e) {
            e.printStackTrace();
        }

    }
}

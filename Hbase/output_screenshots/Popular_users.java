import java.util.ArrayList;
import java.util.List;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

public class Popular_users {
    public static String Table_Name = "Covid_tweets";

    public static void main(String[] args) throws Exception {
        Configuration conf = HBaseConfiguration.create();
        Connection connection = ConnectionFactory.createConnection(conf);
        @SuppressWarnings("deprecation")
		HTable table = new HTable(conf, Table_Name);
        //Table table = connection.getTable(TABLE_NAME);

        // Scan to retrieve rows from the table.
        Scan scan = new Scan();
        
        // Filter for users with more than 6 digits in "User Followers" count.
        SingleColumnValueFilter followersFilter = new SingleColumnValueFilter(
        		Bytes.toBytes("Users"), Bytes.toBytes("Followers"), CompareOp.GREATER_OR_EQUAL,
                Bytes.toBytes(1000000)); 
        System.out.println(followersFilter);        // Create a filter list with the followers filter.
        FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL);
        filterList.addFilter(followersFilter);
        
        // Apply the filter to the scan.
        scan.setFilter(filterList);

        // Get the result scanner for the scan.
        ResultScanner scanner = table.getScanner(scan);
        // Lists to store names of popular users.
        List<String> popularVerifiedUsers = new ArrayList<>();
        List<String> popularUnverifiedUsers = new ArrayList<>();
           // Iterate through the results and process them.
        for (Result result : scanner) {
            byte[] nameBytes = result.getValue(Bytes.toBytes("Users"), Bytes.toBytes("Name"));
            byte[] verifiedBytes = result.getValue(Bytes.toBytes("Users"), Bytes.toBytes("Verified"));

            String name = Bytes.toString(nameBytes);
            String verified = Bytes.toString(verifiedBytes);

            // Store names of popular verified users.
            if ("True".equals(verified)) {
                popularVerifiedUsers.add(name);
            } else {
                // Store names of popular unverified users.
                popularUnverifiedUsers.add(name);
            }
        }

        // Print the names of popular verified users.
        System.out.println("Popular Verified Users:");
        for (String name : popularVerifiedUsers) {
            System.out.println(name);
        }

        // Print the names of popular unverified users.
        System.out.println("Popular Unverified Users:");
        for (String name : popularUnverifiedUsers) {
            System.out.println(name);
        } 

        // Print the count of popular verified and unverified users.
        System.out.println("Number of Popular Verified Users: " + popularVerifiedUsers.size());
        System.out.println("Number of Popular Unverified Users: " + popularUnverifiedUsers.size());

        // Close the HBase connection and table.
        table.close();
        connection.close();
    }
}

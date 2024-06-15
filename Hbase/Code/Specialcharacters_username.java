import java.util.HashMap;
import java.util.Map;
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
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

public class Specialcharacters_username {
    public static String Table_Name = "Covid_tweets";

    public static void main(String[] args) throws Exception {
        Configuration conf = HBaseConfiguration.create();
        Connection connection = ConnectionFactory.createConnection(conf);
        HTable table = new HTable(conf, Table_Name);
        //Table table = connection.getTable(TABLE_NAME);

        // Scan to retrieve rows from the table.
        Scan scan = new Scan();

        // Filter for users with special characters in "Screen Name".
        SingleColumnValueFilter screenNameFilter = new SingleColumnValueFilter(
                Bytes.toBytes("Users"), Bytes.toBytes("Name"), CompareOp.NOT_EQUAL,
                new RegexStringComparator("^[a-zA-Z0-9]*$"));
        
        // Filter for verified users.
        SingleColumnValueFilter verifiedFilter = new SingleColumnValueFilter(
                Bytes.toBytes("Users"), Bytes.toBytes("Verified"), CompareOp.EQUAL,
                Bytes.toBytes("True"));

        // Create a filter list with an OR condition to combine the filters.
        FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ONE);
        filterList.addFilter(screenNameFilter);
        filterList.addFilter(verifiedFilter);
        
        // Apply the combined filter to the scan.
        scan.setFilter(filterList);

        List<String> usersWithSpecialChars = new ArrayList<>();
        List<String> verifiedUsers = new ArrayList<>();
        // Get the result scanner for the scan.
        ResultScanner scanner = table.getScanner(scan);
        //Result[] results = table.getScanner(scan).next(); // Limiting to 100 results for example.
        int count = 0;
        int verified_count = 0 ;
        // Iterate through the results and process them.
        for (Result result : scanner) {
            byte[] screenNameBytes = result.getValue(Bytes.toBytes("Users"), Bytes.toBytes("Name"));
            byte[] verifiedBytes = result.getValue(Bytes.toBytes("Users"), Bytes.toBytes("Verified"));

            String screenName = Bytes.toString(screenNameBytes);
            String verified = Bytes.toString(verifiedBytes);

            // Print or process the users with special characters in "Screen Name".
            if (!screenName.matches("^[a-zA-Z0-9 ]*$")) {
                usersWithSpecialChars.add(screenName);
                count++;
            }

            // Store verified users.
            if ("True".equals(verified)) {
                verifiedUsers.add(screenName);
                verified_count++;
            }

            // Print or process verified users.
            System.out.println("Users with Special Characters in Screen Name:");
            for (String user : usersWithSpecialChars) {
                System.out.println(user);
            }

            // Print verified users.
            System.out.println("Verified Users with Special Characters in Screen Name:");
            for (String user : verifiedUsers) {
                System.out.println(user);
            }
            
           } 
        System.out.println(count);
        System.out.println(verified_count);

        // Close the HBase connection and table.
        table.close();
        connection.close();
    }

    
}

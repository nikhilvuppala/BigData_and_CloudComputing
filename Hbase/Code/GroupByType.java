import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

public class GroupByType {
	public static String Table_Name = "Covid_tweets";

	public static void main(String[] args) throws Throwable {
		Configuration conf = HBaseConfiguration.create();
		@SuppressWarnings({ "deprecation", "resource" })
		HTable hTable = new HTable(conf, Table_Name);

		// define the filter
		SingleColumnValueFilter filter1 = new SingleColumnValueFilter(Bytes.toBytes("Users"), Bytes.toBytes("Location"),
				CompareOp.NOT_EQUAL, new BinaryComparator(Bytes.toBytes("")));

		Scan scan1 = new Scan();
		scan1.setFilter(filter1);

		// now we extract the result
		ResultScanner scanner1 = hTable.getScanner(scan1);

		// Create a map to count tweets for each location.
		Map<String, Integer> locationCountMap = new HashMap<>();
		int totalCount = 0;

		for (Result result : scanner1) {
			byte[] locationBytes = result.getValue(Bytes.toBytes("Users"), Bytes.toBytes("Location"));
			//System.out.println("--------------------------------");			

			//System.out.println(locationBytes);			
			String location = Bytes.toString(locationBytes);
			//System.out.println(location);
			// Increment the count for the location in the map.
			locationCountMap.put(location, locationCountMap.getOrDefault(location, 0) + 1);
			totalCount++;
		}

		// Print the count of tweets for each location.
		for (Map.Entry<String, Integer> entry : locationCountMap.entrySet()) {
			System.out.println("Location: " + entry.getKey() + " - Tweet Count: " + entry.getValue());
		}

		System.out.println(locationCountMap.size() + " - " + totalCount);

	}

}

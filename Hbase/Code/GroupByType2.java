
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.NullComparator;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

public class GroupByType2 {
	public static String Table_Name = "Covid_tweets";

	public static void main(String[] args) throws Throwable {
		Configuration conf = HBaseConfiguration.create();
		@SuppressWarnings({ "deprecation", "resource" })
		HTable hTable = new HTable(conf, Table_Name);

		Scan scan = new Scan();
		SingleColumnValueFilter filter = new SingleColumnValueFilter(Bytes.toBytes("Users"), Bytes.toBytes("Created"),
				CompareOp.NOT_EQUAL, new NullComparator());
		// Filter for user_verified equal to "true."
		SingleColumnValueFilter userVerifiedFilter = new SingleColumnValueFilter(Bytes.toBytes("Users"),
				Bytes.toBytes("Verified"), CompareOp.EQUAL, Bytes.toBytes("True"));

		FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ONE);

		filterList.addFilter(filter);
		filterList.addFilter(userVerifiedFilter);
		scan.setFilter(filterList);

		// Get the result scanner for users.
		ResultScanner scanner = hTable.getScanner(scan);

		// Create maps to count users by year and month.
		Map<String, Integer> userCountByYear = new HashMap<>();
		Map<String, Integer> userCountByMonth = new HashMap<>();
		Map<String, Integer> verifiedUserCountByYear = new HashMap<>();
		Map<String, Integer> verifiedUserCountByMonth = new HashMap<>();

		for (Result result : scanner) {
			byte[] userCreatedBytes = result.getValue(Bytes.toBytes("Users"), Bytes.toBytes("Created"));
			byte[] userVerifiedBytes = result.getValue(Bytes.toBytes("Users"), Bytes.toBytes("Verified"));

			String userCreated = Bytes.toString(userCreatedBytes);
			String userVerified = Bytes.toString(userVerifiedBytes);

			// Increment user count by year and month.
			String year = userCreated.substring(0, 4);
			String month = userCreated.substring(5, 7);

			userCountByYear.put(year, userCountByYear.getOrDefault(year, 0) + 1);
			userCountByMonth.put(month, userCountByMonth.getOrDefault(month, 0) + 1);

			if ("True".equals(userVerified)) {
				verifiedUserCountByYear.put(year, verifiedUserCountByYear.getOrDefault(year, 0) + 1);
				verifiedUserCountByMonth.put(month, verifiedUserCountByMonth.getOrDefault(month, 0) + 1);
			}
		}

		// Print the counts for users by year and month.
		System.out.println("User Counts by Year:");
		for (Map.Entry<String, Integer> entry : userCountByYear.entrySet()) {
			System.out.println("Year: " + entry.getKey() + " - User Count: " + entry.getValue());
		}

		System.out.println("User Counts by Month in 2020:");
		for (Map.Entry<String, Integer> entry : userCountByMonth.entrySet()) {
			System.out.println("Month: " + entry.getKey() + " - User Count: " + entry.getValue());
		}

		// Print the counts for verified users by year and month.
		System.out.println("Verified User Counts by Year:");
		for (Map.Entry<String, Integer> entry : verifiedUserCountByYear.entrySet()) {
			System.out.println("Year: " + entry.getKey() + " - Verified User Count: " + entry.getValue());
		}

		System.out.println("Verified User Counts by Month in 2020:");
		for (Map.Entry<String, Integer> entry : verifiedUserCountByMonth.entrySet()) {
			System.out.println("Month: " + entry.getKey() + " - Verified User Count: " + entry.getValue());
		}

	}
}

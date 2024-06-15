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



public class covid19_hashtag {
    public static String Table_Name = "Covid_tweets";

    public static void main(String[] args) throws Exception {
        Configuration conf = HBaseConfiguration.create();
        Connection connection = ConnectionFactory.createConnection(conf);
        //Table table = connection.getTable(TABLE_NAME);
        HTable table = new HTable(conf, Table_Name);

        // Scan to retrieve rows from the table.
        Scan scan = new Scan();

        // Filter for tweets that start with #covid19 (case-insensitive).
//        SingleColumnValueFilter hashtagFilter = new SingleColumnValueFilter(
//                Bytes.toBytes("Extras"), Bytes.toBytes("hashtags"), CompareOp.EQUAL,
//                new org.apache.hadoop.hbase.filter.SubstringComparator("Covid19"));
        
        
        // Filter for verified and popular users.
//        SingleColumnValueFilter verifiedFilter = new SingleColumnValueFilter(
//                Bytes.toBytes("Users"), Bytes.toBytes("Verified"), CompareOp.EQUAL,
//                Bytes.toBytes("True"));
       
        // Combine filters with an AND condition.
//        FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL);
//        filterList.addFilter(hashtagFilter);
//        filterList.addFilter(verifiedFilter);
        
        // Apply the combined filter to the scan.
//        scan.setFilter(filterList);
        int tweet_count = 0;

        // Get the result scanner for the scan.
        //Result[] results = table.getScanner(scan).next(1000); // Limiting to 1000 results
        ResultScanner scanner = table.getScanner(scan);
        // Lists to store tweet content for matching tweets.
        List<String> hashtagTweets = new ArrayList<>();
        List<String> verifiedPopularhashtagTweets = new ArrayList<>();

        // Iterate through the results and process them.
        for (Result result : scanner) {
            byte[] tweetBytes = result.getValue(Bytes.toBytes("Extra"), Bytes.toBytes("hashtags"));
            byte[] verifiedBytes = result.getValue(Bytes.toBytes("Users"), Bytes.toBytes("Verified"));
            byte[] followersBytes = result.getValue(Bytes.toBytes("Users"), Bytes.toBytes("Followers"));
            String tweetContent = Bytes.toString(tweetBytes);
            String verified = Bytes.toString(verifiedBytes);
            
            if (followersBytes != null && followersBytes.length == 8 && 
                    tweetBytes != null && verifiedBytes != null) {
            	long followersCount = Bytes.toLong(followersBytes);
            	if(tweetContent.toLowerCase().startsWith("['covid19")) {
            		hashtagTweets.add(tweetContent);
            		if("True".equals(verified) && followersCount > 999999) {
            			verifiedPopularhashtagTweets.add(tweetContent);
            		}
            	}
            //((Object) tweetContent).getClass().getName();
            // Store tweet content that matches the criteria.
            	hashtagTweets.add(tweetContent);
            	tweet_count ++;
            }
            
        }

        // Print tweet content starting with #covid19 for verified and popular users
      for (String tweet : verifiedPopularhashtagTweets) {
          System.out.println(tweet);
      }
      System.out.print("Tweets Starting with #covid19 :" + hashtagTweets.size());
      System.out.println("\n");
      System.out.println("Tweets Starting with #covid19 from Verified and Popular Users:" + verifiedPopularhashtagTweets.size());
      
        // Close the HBase connection and table.
        table.close();
        connection.close();
    }
}

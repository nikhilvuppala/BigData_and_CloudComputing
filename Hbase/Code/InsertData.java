import java.io.FileReader;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class InsertData extends Configured implements Tool {
	public String Table_Name = "Covid_tweets";
	private static final String csv_name = "covid19_tweets.csv";

	@SuppressWarnings("deprecation")
	@Override
	public int run(String[] argv) throws IOException {
		Configuration conf = HBaseConfiguration.create();
		HBaseAdmin admin = new HBaseAdmin(conf);

		boolean isExists = admin.tableExists(Table_Name);

		if (!isExists) {
			// Create the table with column families
			HTableDescriptor htb = new HTableDescriptor(Table_Name);
			HColumnDescriptor UsersFamily = new HColumnDescriptor("Users");
			HColumnDescriptor TweetsFamily = new HColumnDescriptor("Tweets");
			HColumnDescriptor ExtraFamily = new HColumnDescriptor("Extra");

			htb.addFamily(UsersFamily);
			htb.addFamily(TweetsFamily);
			htb.addFamily(ExtraFamily);
			admin.createTable(htb);
		}

		try (FileReader fileReader = new FileReader(csv_name);
				CSVParser csvParser = new CSVParser(fileReader,
						CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim())) {

			int row_count = 0;

			HTable hTable = new HTable(conf, Table_Name);

			for (CSVRecord csvRecord : csvParser) {
				row_count++;
				// Access individual fields using csvRecord.get(index)
				String user_name = csvRecord.get("user_name");
				String user_location = csvRecord.get("user_location");
				String user_description = csvRecord.get("user_description");
				String user_created = csvRecord.get("user_created");
				String user_followers = csvRecord.get("user_followers");
				String user_friends = csvRecord.get("user_friends");
				String user_favourites = csvRecord.get("user_favourites");
				String user_verified = csvRecord.get("user_verified");
				String extraDate = csvRecord.get("date");
				String tweetText = csvRecord.get("text");
				String hashtags = csvRecord.get("hashtags");
				String source = csvRecord.get("source");
				String is_retweet = csvRecord.get("is_retweet");

				String rowKey = UUID.randomUUID().toString();

				// Initialize a Put with the generated row key
				Put put = new Put(rowKey.getBytes());

				// Add column data to the respective column families
				put.add(Bytes.toBytes("Users"), Bytes.toBytes("Name"), Bytes.toBytes(user_name));
				put.add(Bytes.toBytes("Users"), Bytes.toBytes("Location"), Bytes.toBytes(user_location));
				put.add(Bytes.toBytes("Users"), Bytes.toBytes("Description"), Bytes.toBytes(user_description));
				put.add(Bytes.toBytes("Users"), Bytes.toBytes("Created"), Bytes.toBytes(user_created));
				put.add(Bytes.toBytes("Users"), Bytes.toBytes("Followers"), Bytes.toBytes(user_followers));
				put.add(Bytes.toBytes("Users"), Bytes.toBytes("Friends"), Bytes.toBytes(user_friends));
				put.add(Bytes.toBytes("Users"), Bytes.toBytes("Favourites"), Bytes.toBytes(user_favourites));
				put.add(Bytes.toBytes("Users"), Bytes.toBytes("Verified"), Bytes.toBytes(user_verified));
				put.add(Bytes.toBytes("Tweets"), Bytes.toBytes("TweetText"), Bytes.toBytes(tweetText));
				put.add(Bytes.toBytes("Extra"), Bytes.toBytes("ExtraDate"), Bytes.toBytes(extraDate));
				put.add(Bytes.toBytes("Extra"), Bytes.toBytes("hashtags"), Bytes.toBytes(hashtags));
				put.add(Bytes.toBytes("Extra"), Bytes.toBytes("source"), Bytes.toBytes(source));
				put.add(Bytes.toBytes("Extra"), Bytes.toBytes("is_retweet"), Bytes.toBytes(is_retweet));
				// Add the Put to the HBase table
				hTable.put(put);
			}
			hTable.close();
			System.out.println("Inserted " + row_count + " rows.");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return 0;
	}

	public static void main(String[] argv) throws Exception {
		int ret = ToolRunner.run(new InsertData(), argv);
		System.exit(ret);
	}
}

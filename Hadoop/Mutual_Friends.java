import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Mutual_Friends {

	public static class MyMapper extends Mapper<Object, Text, Text, Text> {

		private Map<String, String> keyToValueMap = new HashMap<>();

		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			String personWithFriends = value.toString();
			String[] parts = personWithFriends.toString().split("\t");
			String person = parts[0];
			String[] friends = parts.length > 1 ? parts[1].toString().split(",") : new String[0];

			for (String friend : friends) {
				String[] sortedPair = { person, friend };
				Arrays.sort(sortedPair);
				String friendPair = sortedPair[0] + " " + sortedPair[1];	
				// Group by key in the mapper
				if (keyToValueMap.containsKey(friendPair)) {
					// If the key is already in the map, append the value
					String existingValue = keyToValueMap.get(friendPair) + " " + parts[1];
					context.write(new Text(friendPair), new Text(existingValue));
				} else {
					// If the key is not in the map, add it with the value
					keyToValueMap.put(friendPair, parts[1]);
				}
			}
		}
	}

	public static class MyReducer extends Reducer<Text, Text, Text, Text> {
		private int Sum_of_MutualFriends=0,Count_MutualFriends=0,max_MutualFriends=-999;
		private Map<String, Set<String>> keyToMutual = new HashMap<>();
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			Set<String> commonFriends = new HashSet<>();
			Iterator<Text> iterator = values.iterator();
			// Initialize commonFriends with the first friend list
			String allFriendList = iterator.next().toString();
			String[] friendList = allFriendList.split(" ");
			String[] firstFriendList = friendList[0].toString().split(",");
			String[] secondFriendList = friendList.length > 1 ? friendList[1].toString().split(",") : new String[0];
			commonFriends.addAll(Arrays.asList(firstFriendList));
			commonFriends.retainAll(Arrays.asList(secondFriendList));
			Sum_of_MutualFriends+=commonFriends.size();
			keyToMutual.put(key.toString(), commonFriends);
			if(max_MutualFriends<commonFriends.size()) {
				max_MutualFriends=commonFriends.size();
			}
			if(commonFriends.size()>=1) {
				Count_MutualFriends+=1;
				context.write(key, new Text(" " + commonFriends));
			}
			
		}
		public void cleanup( Context context) throws IOException, InterruptedException {
			double AverageOfTotalMutualFriends=0;
			AverageOfTotalMutualFriends=(double)Sum_of_MutualFriends/Count_MutualFriends;
			context.write(new Text("Task2"), null);
			context.write(new Text("The Highest Number of Mutual Friends is "), new Text(String.valueOf(max_MutualFriends)));
			context.write(new Text("The list of Mutual friends with the maximum number"), null);
			//Iterating the haspmap to get the list of mutual friends whose size is equal to maximum number
			for (Map.Entry<String, Set<String>> entry : keyToMutual.entrySet()) {
				if(entry.getValue().size()==max_MutualFriends) {
					context.write(new Text(entry.getKey()), new Text(" " + entry.getValue()));
				}
			}
			//Iterating the hashmap to show the mutual friends between two peoples who's mutual friends list contains 
			//either "1" or "5"
			context.write(new Text("The list of mutual friends which contain either '1' or '5'"), null);
			for (Map.Entry<String, Set<String>> entry : keyToMutual.entrySet()) {
				Set<String> Temp = entry.getValue();
				Set<String> Tempwith1_5 = new HashSet<>();
				for ( String TempId : Temp) {
					if(TempId.startsWith("1") || TempId.startsWith("5")) {
						Tempwith1_5.add(TempId);
					}
				}
				if(Tempwith1_5.size()>0) {
					context.write(new Text(entry.getKey()), new Text(" " + Tempwith1_5));
				}
			}
			context.write(new Text("Task3"), null);
			context.write(new Text("Overall Average of the mutual friends in the data set is "), new Text(String.valueOf(AverageOfTotalMutualFriends)));
			// Iterating the hashmap for the list of mutual friends whose size is more then the total average
			for (Map.Entry<String, Set<String>> entry : keyToMutual.entrySet()) {
				if(entry.getValue().size()>AverageOfTotalMutualFriends) {
					context.write(new Text(entry.getKey()), new Text(" " + entry.getValue()));
				}
			}
			context.write(new Text("Thank You"), new Text("By : Nikhil,Sahithi,Deepika"));
				
		}
		
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		Job job = new Job(conf, "Find mutual friends");
		job.setJarByClass(Mutual_Friends.class);
		job.setMapperClass(MyMapper.class);
		job.setReducerClass(MyReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}

}

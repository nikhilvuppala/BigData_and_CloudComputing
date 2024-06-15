val startTime = System.currentTimeMillis()
print("Enter the number of lines: ")
val number = scala.io.StdIn.readInt()
val loadfile = sc.textFile("hdfs://localhost:9000/user/st8vf/InputFolder/soc-LiveJournal1Adj.txt")
// Take the first n lines from the input file
val specificLines = loadfile.take(number)
// Process the specific lines
val groupedFriends = sc.parallelize(specificLines).flatMap { line =>
  val parts = line.split("\t")
  val person = parts(0)
  val friends = if (parts.length > 1) parts(1).split(",") else Array.empty[String]
  friends.map { friend =>
    val sortedPair = Array(person, friend).sorted
    (sortedPair.mkString(" "), friends.toList)
  }
}

val reducedFriends = groupedFriends.groupBy(_._1)
val mutualFriends = reducedFriends.flatMap {
  case (_, values) if values.size==2 =>
  	val commonFriends = values.map(_._2).reduceOption(_.intersect(_))
  	commonFriends.map(intersectedFriends => (values.head._1, intersectedFriends.toList))
  case (key, _) => 
  	Some((key, List.empty[String]))    
}

val formattedResult = sc.parallelize(mutualFriends.collect()).map { case (key, friends) =>
  s"$key ${friends.mkString(", ")}"
}
formattedResult.saveAsTextFile("hdfs://localhost:9000/user/st8vf/OutputFolder15/Task1")
val task1EndTime = System.currentTimeMillis()-startTime


val maxMutualFriendsCount = mutualFriends.map { case (_, friends) => friends.size }.max()
val keysAndValuesWithMaxMutualFriends = mutualFriends.filter { case (_, friends) => friends.size == maxMutualFriendsCount }

println(s"The maximum count of mutual friends ($maxMutualFriendsCount):")


val filteredMutualFriends = mutualFriends.flatMap {
  case (key, friends) =>
    val filteredFriends = friends.filter(friend => friend.startsWith("1") || friend.startsWith("5"))
    if (filteredFriends.nonEmpty) Some(key -> filteredFriends) else None
}
filteredMutualFriends.saveAsTextFile("hdfs://localhost:9000/user/st8vf/OutputFolder15/Task2")
val task2EndTime = System.currentTimeMillis()-startTime

val max_count = sc.parallelize(mutualFriends.collect()).map { case (key, friends) =>
  s"$key ${friends.mkString(", ")}"
}

val (totalMutualFriends, totalKeys) = mutualFriends.aggregate((0, 0))(
  (acc, entry) => (acc._1 + entry._2.size, acc._2 + 1),
  (acc1, acc2) => (acc1._1 + acc2._1, acc1._2 + acc2._2)
)

val averageMutualFriends = if (totalKeys > 0) totalMutualFriends.toDouble / totalKeys else 0.0

println(s"Average number of mutual friends: $averageMutualFriends")

// Find friends who have mutual friends above the average
val friendsAboveAverage = mutualFriends.filter { case (_, friends) =>
  friends.size > averageMutualFriends
}
friendsAboveAverage.saveAsTextFile("hdfs://localhost:9000/user/st8vf/OutputFolder15/Task3")
val task3EndTime = System.currentTimeMillis()-startTime
println(s"Execution time of Task1 in milliseconds: $task1EndTime")
println(s"Execution time of Task2 in milliseconds: $task2EndTime")
println(s"Execution time of Task3 in milliseconds: $task3EndTime")



    
    

mlstorm
=======

Machine Learning in storm

Experimenting with parallel streaming PCA and Ensemble methods for collaborative learning.

1. The PCA and Clustering algorithm implementations are window based.
2. All the learning algorithms are implemented in Trident and use external EJML and Weka libraries.
3. The Kmeans clustering implementation allows querying different partitions. The result of such a query is a partitionId and the query result.
   Using the partion id returned and the usefulness of the results a human/machine can update the parameters of the model on the fly. The following is an example.
  
       a. The topology is run as usual.
         storm jar /damsl/software/storm/code/mlstorm/target/mlstorm-00.01-jar-with-dependencies.jar topology.weka.KmeansClusteringTopology /damsl/projects/bpti_db/features<directory containing feature vectors> 4<no of workers> 10000 10<k> 10<parallelism>

       b. A distributed query (querying for parameters/model statistics) on the model can be executed like the following.
         java -cp .: (output of "storm classpath" command) : $REPO/mlstorm/target/mlstorm-00.01-jar-with-dependencies.jar drpc.DrpcQueryRunner qp-hd3 kmeans "no args" *-- This returns the centroids of all the clusters for a given K --*

       c. An update could be made like the following.
	 java -cp .:/opt/storm-0.8.2/storm-0.8.2/storm-0.8.2.jar:/opt/storm-0.8.2/storm-0.8.2/lib/hiccup-0.3.6.jar:/opt/storm-0.8.2/storm-0.8.2/lib/ring-servlet-0.3.11.jar:/opt/storm-0.8.2/storm-0.8.2/lib/servlet-api-2.5.jar:/opt/storm-0.8.2/storm-0.8.2/lib/minlog-1.2.jar:/opt/storm-0.8.2/storm-0.8.2/lib/math.numeric-tower-0.0.1.jar:/opt/storm-0.8.2/storm-0.8.2/lib/httpcore-4.1.jar:/opt/storm-0.8.2/storm-0.8.2/lib/snakeyaml-1.9.jar:/opt/storm-0.8.2/storm-0.8.2/lib/commons-codec-1.4.jar:/opt/storm-0.8.2/storm-0.8.2/lib/kryo-2.17.jar:/opt/storm-0.8.2/storm-0.8.2/lib/slf4j-api-1.5.8.jar:/opt/storm-0.8.2/storm-0.8.2/lib/slf4j-log4j12-1.5.8.jar:/opt/storm-0.8.2/storm-0.8.2/lib/clojure-1.4.0.jar:/opt/storm-0.8.2/storm-0.8.2/lib/ring-core-1.1.5.jar:/opt/storm-0.8.2/storm-0.8.2/lib/core.incubator-0.1.0.jar:/opt/storm-0.8.2/storm-0.8.2/lib/junit-3.8.1.jar:/opt/storm-0.8.2/storm-0.8.2/lib/zookeeper-3.3.3.jar:/opt/storm-0.8.2/storm-0.8.2/lib/carbonite-1.5.0.jar:/opt/storm-0.8.2/storm-0.8.2/lib/asm-4.0.jar:/opt/storm-0.8.2/storm-0.8.2/lib/objenesis-1.2.jar:/opt/storm-0.8.2/storm-0.8.2/lib/curator-client-1.0.1.jar:/opt/storm-0.8.2/storm-0.8.2/lib/disruptor-2.10.1.jar:/opt/storm-0.8.2/storm-0.8.2/lib/commons-logging-1.1.1.jar:/opt/storm-0.8.2/storm-0.8.2/lib/tools.logging-0.2.3.jar:/opt/storm-0.8.2/storm-0.8.2/lib/jetty-6.1.26.jar:/opt/storm-0.8.2/storm-0.8.2/lib/clout-1.0.1.jar:/opt/storm-0.8.2/storm-0.8.2/lib/jetty-util-6.1.26.jar:/opt/storm-0.8.2/storm-0.8.2/lib/jzmq-2.1.0.jar:/opt/storm-0.8.2/storm-0.8.2/lib/commons-io-1.4.jar:/opt/storm-0.8.2/storm-0.8.2/lib/jgrapht-0.8.3.jar:/opt/storm-0.8.2/storm-0.8.2/lib/tools.macro-0.1.0.jar:/opt/storm-0.8.2/storm-0.8.2/lib/ring-jetty-adapter-0.3.11.jar:/opt/storm-0.8.2/storm-0.8.2/lib/reflectasm-1.07-shaded.jar:/opt/storm-0.8.2/storm-0.8.2/lib/curator-framework-1.0.1.jar:/opt/storm-0.8.2/storm-0.8.2/lib/commons-lang-2.5.jar:/opt/storm-0.8.2/storm-0.8.2/lib/json-simple-1.1.jar:/opt/storm-0.8.2/storm-0.8.2/lib/guava-13.0.jar:/opt/storm-0.8.2/storm-0.8.2/lib/servlet-api-2.5-20081211.jar:/opt/storm-0.8.2/storm-0.8.2/lib/clj-time-0.4.1.jar:/opt/storm-0.8.2/storm-0.8.2/lib/libthrift7-0.7.0.jar:/opt/storm-0.8.2/storm-0.8.2/lib/tools.cli-0.2.2.jar:/opt/storm-0.8.2/storm-0.8.2/lib/httpclient-4.1.1.jar:/opt/storm-0.8.2/storm-0.8.2/lib/joda-time-2.0.jar:/opt/storm-0.8.2/storm-0.8.2/lib/log4j-1.2.16.jar:/opt/storm-0.8.2/storm-0.8.2/lib/commons-fileupload-1.2.1.jar:/opt/storm-0.8.2/storm-0.8.2/lib/commons-exec-1.1.jar:/opt/storm-0.8.2/storm-0.8.2/lib/compojure-1.1.3.jar:/opt/storm-0.8.2/storm-0.8.2/lib/jline-0.9.94.jar:$REPO/mlstorm/target/mlstorm-00.01-jar-with-dependencies.jar drpc.DrpcQueryRunner qp-hd3 kUpdate 0,45
       Result : <[["0,35","k update request (30->35) received at [0]; average trainingtime for k = [30] = [334,809]ms"]]>

       The classpath above is returned by the "storm classpath" command.
       
       

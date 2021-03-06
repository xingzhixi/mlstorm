package topology.weka;

/**
 * Created by lbhat@DaMSl on 12/22/13.
 * <p/>
 * Copyright {2013} {Lakshmisha Bhat <laksh85@gmail.com>}
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.IRichSpout;
import bolt.ml.state.weka.cluster.KmeansClustererState;
import bolt.ml.state.weka.cluster.create.MlStormClustererFactory;
import bolt.ml.state.weka.cluster.query.MlStormClustererQuery;
import bolt.ml.state.weka.cluster.update.KmeansClusterUpdater;
import com.google.common.collect.Lists;
import spout.mddb.MddbFeatureExtractorSpout;
import storm.trident.state.QueryFunction;
import storm.trident.state.StateFactory;
import storm.trident.state.StateUpdater;


public class KmeansClusteringTopology extends WekaBaseLearningTopology {
    public static void main(String[] args) throws AlreadyAliveException, InvalidTopologyException {
        if (args.length < 5) {
            System.err.println(" Where are all the arguments? -- use args -- folder numWorkers windowSize k parallelism");
            return;
        }

        /* The fields our spout is going to emit. These field names are used by the State updaters, so edit with caution */
        String[] fields = {"keyField", "featureVectorField"};
        int numWorkers = Integer.valueOf(args[1]);
        int windowSize = Integer.valueOf(args[2]);
        int k = Integer.valueOf(args[3]);
        int parallelism = Integer.valueOf(args[4]);
        StateUpdater stateUpdater = new KmeansClusterUpdater();
        StateFactory stateFactory = new MlStormClustererFactory.KmeansClustererFactory(k, windowSize);
        QueryFunction<KmeansClustererState, String> queryFunction = new MlStormClustererQuery.KmeansClustererQuery();
        QueryFunction<KmeansClustererState, String> parameterUpdateFunction = new MlStormClustererQuery.KmeansNumClustersUpdateQuery();
        IRichSpout features = new MddbFeatureExtractorSpout(args[0], fields);
        StormTopology stormTopology = buildTopology(features, parallelism, stateUpdater, stateFactory, queryFunction, parameterUpdateFunction, "kmeans", "kUpdate");

        if (numWorkers == 1) {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("kmeans", getStormConfig(numWorkers), stormTopology);
        } else StormSubmitter.submitTopology("kmeans", getStormConfig(numWorkers), stormTopology);
    }

    public static Config getStormConfig(int numWorkers) {
        Config conf = new Config();
        conf.setNumAckers(numWorkers);
        conf.setNumWorkers(numWorkers);
        conf.setMaxSpoutPending(8); // This is critical; if you don't set this, it's likely that you'll run out of memory and storm will throw wierd errors
        conf.put("topology.spout.max.batch.size", 1 /* x1000 i.e. every tuple has 1000 feature vectors*/);
        conf.put("topology.trident.batch.emit.interval.millis", 50);
        conf.put(Config.DRPC_SERVERS, Lists.newArrayList("qp-hd3", "qp-hd4", "qp-hd5", "qp-hd6"));
        conf.put(Config.STORM_CLUSTER_MODE, "distributed");
        conf.put(Config.NIMBUS_TASK_TIMEOUT_SECS, 30);
        return conf;
    }
}


package topology.weka;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.generated.StormTopology;
import backtype.storm.topology.IRichSpout;
import bolt.ml.state.weka.cluster.CobwebClustererState;
import bolt.ml.state.weka.cluster.create.ClustererFactory;
import bolt.ml.state.weka.cluster.query.ClustererQuery;
import bolt.ml.state.weka.cluster.update.CobwebClusterUpdater;
import com.google.common.collect.Lists;
import spout.mddb.MddbFeatureExtractorSpout;
import storm.trident.state.QueryFunction;
import storm.trident.state.StateFactory;
import storm.trident.state.StateUpdater;

import java.util.logging.Level;
import java.util.logging.Logger;


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

public class CobwebClusteringTopology extends WekaBaseLearningTopology {
    public static void main(String[] args) throws AlreadyAliveException, InvalidTopologyException {
        final Logger logger = Logger.getLogger("topology.weka.CobwebClusteringTopology", null);
        if (args.length < 3) {
            logger.log(Level.ALL, "-- use args -- folder numWorkers windowSize");
            return;
        }

        String[] fields = {"key", "featureVector"};
        int numWorkers = Integer.valueOf(args[1]);
        int windowSize = Integer.valueOf(args[2]);
        StateUpdater stateUpdater = new CobwebClusterUpdater();
        StateFactory stateFactory = new ClustererFactory.CobwebClustererFactory(numWorkers, windowSize);
        QueryFunction<CobwebClustererState, String> queryFunction = new ClustererQuery.CobwebClustererQuery();
        IRichSpout features = new MddbFeatureExtractorSpout(args[0], fields);
        StormTopology stormTopology = buildTopology(features, numWorkers, stateUpdater, stateFactory, queryFunction, null, "cobweb", null);

        if (numWorkers == 1) {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("learner", getStormConfig(numWorkers), stormTopology);
        } else StormSubmitter.submitTopology("learner", getStormConfig(numWorkers), stormTopology);
    }

    public static Config getStormConfig(int numWorkers) {
        Config conf = new Config();
        conf.setNumAckers(numWorkers);
        conf.setNumWorkers(numWorkers);
        conf.setMaxSpoutPending(10);
        conf.put("topology.spout.max.batch.size", 1 /* x1000 i.e. every tuple has 1000 feature vectors*/);
        conf.put("topology.trident.batch.emit.interval.millis", 50);
        conf.put(Config.DRPC_SERVERS, Lists.newArrayList("qp-hd3", "qp-hd4", "qp-hd5", "qp-hd6", "qp-hd7", "qp-hd8", "qp-hd9"));
        conf.put(Config.STORM_CLUSTER_MODE, "distributed");
        conf.put(Config.NIMBUS_TASK_TIMEOUT_SECS, 30);
        return conf;
    }
}
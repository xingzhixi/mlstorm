package bolt.ml.state.weka;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by lbhat@DaMSl on 4/10/14.
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

public abstract class BaseOnlineWekaState implements MlStormWekaState {

    /**
     * Construct the State representation for any weka based online learning algorithm
     *
     * @param windowSize the size of the sliding window (cache size)
     */
    public BaseOnlineWekaState(final int windowSize) {
        this.windowSize = windowSize;
        featureVectorsInWindow = new LinkedHashMap<Integer, double[]>(windowSize, 0.75f /*load factor*/, false) {
            public boolean removeEldestEntry(Map.Entry<Integer, double[]> eldest) {
                return size() > windowSize;
            }
        };
    }

    @Override
    public Instance makeWekaInstance(double[] featureVector){
        if (wekaAttributes == null) loadWekaAttributes(featureVector);

        Instance instance = new DenseInstance(wekaAttributes.size());
        for (int i = 0; i < featureVector.length && i < wekaAttributes.size(); i++)
            instance.setValue(i , featureVector[i]);
        return instance;
    }

    /**
     * Do any DB setup etc work here before you commit
     *
     * @param txId
     */
    @Override
    public void beginCommit(final Long txId) {
    }

    /**
     * This is where you do online state commit
     * In our case we train the examples and update the model to incorporate the latest batch
     *
     * @param txId
     */
    @Override
    public synchronized void commit(final Long txId) {
        // Although this looks like a windowed learning, it isn't. This is online learning
        Collection<double[]> groundValues = getFeatureVectorsInWindow().values();
        try {
            preUpdate();
            for (double[] features : groundValues) {
                Instance trainingInstance = new DenseInstance(wekaAttributes.size());
                for (int i = 0; i < features.length && i < wekaAttributes.size(); i++)
                    trainingInstance.setValue(i /*(Attribute) wekaAttributes.elementAt(i)*/, features[i]);
                train(trainingInstance);
            }
            postUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Since we are doing online learning, we don't want to keep trained samples in memory
            // However, if you were doing windowed learning, then leave this alone i.e. don't clear the groundValues.
            //TODO persist model in database with txId as keyField
            groundValues.clear();
        }
    }

    /**
     * do anything you want after updating the classifier
     */
    protected abstract void postUpdate();

    /**
     * do anything you want after updating the classifier
     *
     * @throws Exception
     */
    protected abstract void preUpdate() throws Exception;

    /**
     * return the feature collection of the most recent window
     */

    @Override
    public Map<Integer, double[]> getFeatureVectorsInWindow() {
        return featureVectorsInWindow;
    }

    /**
     * @param features
     */
    protected abstract void loadWekaAttributes(final double[] features);

    /**
     * @param trainingInstance
     * @throws Exception
     */
    protected abstract void train(final Instance trainingInstance) throws Exception;

    protected abstract void train(Instances trainingInstances) throws Exception;

    protected Map<Integer, double[]> featureVectorsInWindow;
    protected ArrayList<Attribute> wekaAttributes = null;
    protected long windowSize;
    protected Instances dataset;
}

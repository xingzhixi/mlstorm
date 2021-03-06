package bolt.ml.state.ipca;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by lbhat@DaMSl on 1/9/14.
 * <p/>
 * Copyright {2013} {Lakshmisha Bhat}
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

public class WindowedStormPca extends PrincipalComponentsBase {
    public WindowedStormPca(int elementsInSample, int numPrincipalComponents, int localPartition, int numPartitions) throws SQLException {
        super(elementsInSample, numPrincipalComponents, localPartition, numPartitions);
    }

    /**
     * Before we start a batch, storm tells us which "storm transaction" we are going to commit
     *
     * @param txid
     */
    @Override
    public void beginCommit(final Long txid) {
    }

    /**
     * Nothing fancy. We push this sensor reading to the time-series window
     *
     * @param txId
     */
    @Override
    public synchronized void commit(final Long txId) {
        System.err.println("DEBUG: Commit called for transaction " + txId);

        final Set<String> sensorNames = this.sensorDictionary.keySet();
        final int numRows = this.sensorDictionary.size();
        final int numColumns = this.windowSize;

        System.err.println(MessageFormat.format("DEBUG: matrix has {0} rows and {1} columns", numRows, numColumns));

        if (currentSensors.size() > numRows / 2 + 10 /*we expect 50% + 10 success rate*/)
            windowTimesteps.put(txId, getCurrentSensorsAndReset(true));
        if (windowTimesteps.size() < windowSize) return;

        constructDataMatrixForPca(numRows, numColumns);
        addSamplesInWindowToMatrix(sensorNames, numColumns);
        if (numRows > 0 && numColumns > 0) computePrincipalComponentsAndResetDataMatrix();
    }

    /**
     * Compute PCA and set all elements of data matrix to 0
     */
    private void computePrincipalComponentsAndResetDataMatrix() {
        computeBasis(numExpectedComponents);
        {   // Reset the data matrix and its index
            A.zero();
            sampleIndex = 0;
        }
    }


    /**
     * Simply adds samples in the window to the data matrix by making sure 0's are added when a particular sensor isn't seen
     *
     * @param sensorNames
     * @param numColumns
     */
    private void addSamplesInWindowToMatrix(final Set<String> sensorNames, final int numColumns) {
        for (String sensorName : sensorNames) {
            int columnIndex = 0;
            double[] row = new double[numColumns];
            Iterator<Map<String, Double>> valuesIterator = windowTimesteps.values().iterator();
            while (valuesIterator.hasNext() && columnIndex < numColumns) {
                final Map<String, Double> timeStep = valuesIterator.next();
                row[columnIndex++] = timeStep.containsKey(sensorName) ? timeStep.get(sensorName) : 0.0;
            }
            addSample(row);
        }
    }
}

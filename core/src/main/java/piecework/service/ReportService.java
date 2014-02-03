/*
 * Copyright 2013 University of Washington
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package piecework.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapreduce.MapReduceOptions;
import org.springframework.data.mongodb.core.mapreduce.MapReduceResults;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import piecework.exception.PieceworkException;
import piecework.model.ChartData;
import piecework.model.ChartDataset;
import piecework.model.Process;
import piecework.model.Report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * @author James Renfro
 */
@Service
public class ReportService {

    @Autowired(required = false)
    @Qualifier(value="mongoTemplate")
    MongoOperations operations;

    public Report getReport(Process process, String reportName) throws PieceworkException {
        ChartDataset dataset1 = new ChartDataset();
        dataset1.setLabel("Jan 2013");
        dataset1.setData(Arrays.asList(65, 59, 90, 81, 56));

        ChartDataset dataset2 = new ChartDataset();
        dataset2.setLabel("Feb 2013");
        dataset2.setData(Arrays.asList(28, 48, 40, 19, 96));

        ChartData data = new ChartData();
        data.setLabels(Arrays.asList("Active", "Completed", "Cancelled", "Suspended"));
        data.setDatasets(Arrays.asList(dataset1, dataset2));

        return new Report(reportName, data);
    }

    private Report getMonthlyProcessStatusReport(String processDefinitionKey) {
        String mapFunction = "function() {         " +
            "   var key = this.processStatus;             " +
            "   var value = {                      " +
            "            label: this.processStatus,      " +
            "            month: this.startTime.getMonth() " +
            "            count: 1                 " +
            "    };                                " +
            "                                      " +
            "    emit( key, value );               " +
            "}";

        String reduceFunction = "function(key, values) {" +
                "var reducedObject = {" +
                "    month: key," +
                "    data: []" +
                "};" +
                "values.forEach( " +
                "    function(value) {" +
                "       reducedObject.total_time += value.total_time;" +
                "       reducedObject.data.push(value)" +
                "    }" +
                ");" +
                "return reducedObject;" +
            "};";

        Query query = new Query(where("processDefinitionKey").is(processDefinitionKey));
        MapReduceResults<ChartDataset> results = operations.mapReduce(query, "instance", mapFunction, reduceFunction, MapReduceOptions.options(), ChartDataset.class);
        Iterator<ChartDataset> iterator = results.iterator();

        List<ChartDataset> datasets = new ArrayList<ChartDataset>();
        while (iterator.hasNext()) {
            datasets.add(iterator.next());
        }

        ChartData data = new ChartData();
        data.setLabels(Arrays.asList("Active", "Completed", "Cancelled", "Suspended"));
        data.setDatasets(datasets);

        return new Report("Monthly Process Status Report", data);
    }

}

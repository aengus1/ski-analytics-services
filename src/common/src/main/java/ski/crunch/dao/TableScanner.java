/**
 * Copyright 2010-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * This file is licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License. A copy of
 * the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ski.crunch.dao;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ski.crunch.aws.DynamoFacade;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TableScanner {


    private static final Logger logger = LoggerFactory.getLogger(TableScanner.class);




    public static List<Item> parallelScan(DynamoFacade dynamoFacade, String tableName, int itemLimit, int numberOfThreads) {
       logger.info(
                "Scanning " + tableName + " using " + numberOfThreads + " threads " + itemLimit + " items at a time");
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // Divide DynamoDB table into logical segments
        // Create one task for scanning each segment
        // Each thread will be scanning one segment
        int totalSegments = numberOfThreads;
        List<ScanSegmentTask> tasks = new ArrayList<>();
        for (int segment = 0; segment < totalSegments; segment++) {
            // Runnable task that will only scan one segment
            ScanSegmentTask task = new ScanSegmentTask(dynamoFacade, tableName, itemLimit, totalSegments, segment);

            // Execute the task
            executor.execute(task);
            tasks.add(task);
        }

        shutDownExecutorService(executor);
        List<Item> items = new ArrayList<>();
        for (ScanSegmentTask result : tasks) {
            Iterator<Item> itemIterator = result.getResult().iterator();
            while(itemIterator.hasNext()){
                items.add(itemIterator.next());
            }
        }
        return items;
    }


    // Runnable task for scanning a single segment of a DynamoDB table
    private static class ScanSegmentTask implements Runnable {

        // DynamoDB table to scan
        private String tableName;

        // number of items each scan request should return
        private int itemLimit;

        // Total number of segments
        // Equals to total number of threads scanning the table in parallel
        private int totalSegments;

        // Segment that will be scanned with by this task
        private int segment;

        private DynamoFacade dynamoFacade;

        private ItemCollection<ScanOutcome> result;

        public ScanSegmentTask(DynamoFacade dynamoFacade, String tableName, int itemLimit, int totalSegments, int segment) {
            this.tableName = tableName;
            this.itemLimit = itemLimit;
            this.totalSegments = totalSegments;
            this.segment = segment;
            this.dynamoFacade = dynamoFacade;
        }

        @Override
        public void run() {
            logger.info("Scanning " + tableName + " segment " + segment + " out of " + totalSegments
                    + " segments " + itemLimit + " items at a time...");
            int totalScannedItemCount = 0;

            Table table = dynamoFacade.getTable(tableName);

            try {
                ScanSpec spec = new ScanSpec().withMaxResultSize(itemLimit).withTotalSegments(totalSegments)
                        .withSegment(segment);

                result = table.scan(spec);

                Iterator<Item> iterator = result.iterator();

                Item currentItem = null;
                while (iterator.hasNext()) {
                    totalScannedItemCount++;
                    currentItem = iterator.next();
                }

            }
            catch (Exception e) {
                e.printStackTrace();
               logger.error(e.getMessage());
            }
            finally {
                logger.info("Scanned " + totalScannedItemCount + " items from segment " + segment + " out of "
                        + totalSegments + " of " + tableName);
            }
        }

        public ItemCollection<ScanOutcome> getResult() {
            return result;
        }
    }

    private static void shutDownExecutorService(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }
        catch (InterruptedException e) {
            executor.shutdownNow();

            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}

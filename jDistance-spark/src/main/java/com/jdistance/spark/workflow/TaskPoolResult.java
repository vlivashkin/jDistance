package com.jdistance.spark.workflow;

import com.jdistance.workflow.AbstractTaskPoolResult;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;

public class TaskPoolResult extends AbstractTaskPoolResult {
    public TaskPoolResult(String name, Map<String, Map<Double, Pair<Double, Double>>> data) {
        super(name, data);
    }

    public TaskPoolResult writeData(String filePath) {
        StringWriter stringWriter = new StringWriter();
        try {
            super.writeData(stringWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String data = stringWriter.toString();
        Context.getInstance().getSparkContext()
                .parallelize(Collections.singletonList(data))
                .saveAsTextFile(filePath);
        return this;
    }
}
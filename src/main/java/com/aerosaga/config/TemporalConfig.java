package com.aerosaga.config;

import com.aerosaga.activities.DroneMissionActivitiesImpl;
import com.aerosaga.workflow.DroneMissionWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemporalConfig {


public static final String TASK_QUEUE = "DRONE_MISSION_TASK_QUEUE";

@Bean
public WorkflowServiceStubs workflowServiceStubs() {
    return WorkflowServiceStubs.newServiceStubs(
            WorkflowServiceStubsOptions.newBuilder()
                    .setTarget("localhost:7233")
                    .build()
    );
}

@Bean
public WorkflowClient workflowClient(
        WorkflowServiceStubs serviceStubs) {
    return WorkflowClient.newInstance(serviceStubs);
}

@Bean(initMethod = "start", destroyMethod = "shutdown")
public WorkerFactory workerFactory(
        WorkflowClient workflowClient,
        DroneMissionActivitiesImpl activities) {

    WorkerFactory factory =
            WorkerFactory.newInstance(workflowClient);

    Worker worker = factory.newWorker(TASK_QUEUE);

    worker.registerWorkflowImplementationTypes(
            DroneMissionWorkflowImpl.class);

    worker.registerActivitiesImplementations(activities);

    return factory;
}


}

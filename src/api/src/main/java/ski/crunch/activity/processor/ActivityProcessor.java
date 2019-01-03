package ski.crunch.activity.processor;

import ski.crunch.activity.model.processor.ActivityHolder;

public class ActivityProcessor {

    public ActivityHolder process(ActivityHolder holder) {

        PipelineManager<ActivityHolder> manager = new PipelineManager<>();

        Handler sortByTsHandler = new SortByTsHandler();
        Handler createHrvRecords = new CreateHrvRecordHandler();
        Handler mergeDuplicateRecordHandler = new MergeDuplicateRecordHandler();
        //Handler removeSpikesHandler = new RemoveSpikesHandler();
        Handler nullReplaceHandler = new NullReplaceHandler();
        Handler calcGradeHandler = new CalcGradeHandler();
        Handler calcMovingHandler = new CalcMovingHandler();
        Handler detectPauseHandler = new DetectPauseHandler();
        Handler closeSegmentsHandler = new CloseSegmentsHandler();
        Handler setEventIndexHandler = new SetEventIndexHandler();
        Handler detectLapHandler = new DetectLapHandler();

        // detect detect lap events
        // detect motion stops
        // summarize




        manager.addHandler(sortByTsHandler);
        manager.addHandler(createHrvRecords);
        manager.addHandler(sortByTsHandler);
        manager.addHandler(mergeDuplicateRecordHandler);
        // manager.addHandler(removeSpikesHandler);
        manager.addHandler(nullReplaceHandler);
        manager.addHandler(calcGradeHandler);
        manager.addHandler(calcMovingHandler);
        manager.addHandler(detectPauseHandler);
        manager.addHandler(closeSegmentsHandler);
        manager.addHandler(setEventIndexHandler);
        manager.addHandler(detectLapHandler);

        return manager.doPipeline(holder);
    }
}

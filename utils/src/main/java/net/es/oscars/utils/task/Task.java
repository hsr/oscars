package net.es.oscars.utils.task;



import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public abstract class Task {

    protected final UUID id;
    protected Outcome outcome;
    protected RunState runstate;
    protected Timeline timeline;
    protected String scope;
    private List<Task> prereqs = new ArrayList<Task>();



    public Task() {
        this.id = java.util.UUID.randomUUID();
        this.outcome = Outcome.UNDEFINED;
        this.runstate = RunState.UNSCHEDULED;
        this.timeline = new Timeline();
        this.timeline.createdAt = new Date().getTime();
    }

    public void onScheduled(long scheduledFor) throws TaskException {
        this.runstate = RunState.SCHEDULED;
        this.timeline.scheduledAt = new Date().getTime();
        this.timeline.scheduledFor = scheduledFor;
    }

    public void onRun() throws TaskException {
        this.runstate = RunState.RUNNING;
        this.timeline.started = new Date().getTime();
    }

    private void onFinish() throws TaskException {
        this.runstate = RunState.FINISHED;
        this.timeline.finished = new Date().getTime();
    }

    public void onSuccess() throws TaskException {
        this.onFinish();
        this.outcome = Outcome.OK;
    }

    public void onFail() throws TaskException {
        this.onFinish();
        this.outcome = Outcome.FAIL;
    }





    @Override
    public boolean equals(Object other) {
        boolean result = false;
        if (other instanceof Task) {
            Task that = (Task) other;
            result = (this.getId().equals(that.getId()));
        }

        return result;
    }
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public void setOutcome(Outcome outcome) {
        this.outcome = outcome;
    }

    public RunState getRunstate() {
        return runstate;
    }

    public void setRunstate(RunState runstate) {
        this.runstate = runstate;
    }

    public Timeline getTimeline() {
        return timeline;
    }

    public void setTimeline(Timeline timeline) {
        this.timeline = timeline;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public UUID getId() {
        return id;
    }


    public List<Task> getPrereqs() {
        return prereqs;
    }



}

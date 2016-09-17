package com.dabsquared.gitlabjenkins.trigger.handler;


import com.dabsquared.gitlabjenkins.cause.CauseData;
import com.dabsquared.gitlabjenkins.cause.GitLabWebHookCause;
import hudson.model.Action;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Queue;
import hudson.model.Queue.Item;
import hudson.model.Job;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.HttpResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Oscar Salvador Morillo Victoria
 */
public class JobCanceller {

    private final static Logger LOGGER = Logger.getLogger(JobCanceller.class.getName());

    private static <T extends GitLabWebHookCause> CauseData findCauseData(final Action[] actions) {
        for (final Action action : actions) {
            if (action instanceof CauseAction) {
                final CauseAction causeAction = (CauseAction) action;
                final GitLabWebHookCause webHookCause = causeAction.findCause(GitLabWebHookCause.class);
                if (webHookCause != null) {
                    return webHookCause.getData();
                }
            }
        }
        return null;
    }

    public static void cancelOutdatedScheduledJobs(final Job<?, ?> job, final Action[] actions) {

        LOGGER.log(Level.INFO, "Searching for outdated scheduled jobs");

        final CauseData triggerCauseData = JobCanceller.findCauseData(actions);
        if (triggerCauseData != null) {

            final Queue queue = Jenkins.getInstance().getQueue();
            final Item[] jobsInQueue = queue.getItems();
            for (final Item enqueuedJob : jobsInQueue) {

                if (enqueuedJob == null) continue;

                if (!StringUtils.equals(job.getName(), enqueuedJob.task.getName())) {
                    // leave other jobs untouched
                    continue;
                }

                final List<Cause> causes = enqueuedJob.getCauses();
                for (final Cause cause : causes) {
                    if (cause instanceof GitLabWebHookCause) {
                        final GitLabWebHookCause webHookCause = (GitLabWebHookCause) cause;
                        final CauseData causeData = webHookCause.getData();

                        if (triggerCauseData.getActionType() == causeData.getActionType()
                            && triggerCauseData.getSourceProjectId().equals(causeData.getSourceProjectId())
                            && triggerCauseData.getTargetProjectId().equals(causeData.getTargetProjectId())
                            && StringUtils.equals(triggerCauseData.getBranch(), causeData.getBranch())
                            && StringUtils.equals(triggerCauseData.getSourceBranch(), causeData.getSourceBranch())
                            && StringUtils.equals(triggerCauseData.getTargetBranch(), causeData.getTargetBranch())
                            && StringUtils.equals(triggerCauseData.getUserName(), causeData.getUserName())
                            && StringUtils.equals(triggerCauseData.getUserEmail(), causeData.getUserEmail())) {
                            LOGGER.log(Level.INFO, String.format(
                                "Scheduled build job with ID %d is outdated. Removing job from queue.", enqueuedJob.getId()));

                            try {
                                final HttpResponse httpResponse = queue.doCancelItem(enqueuedJob.getId());
                            } catch (IOException | ServletException e) {
                                LOGGER.log(Level.WARNING, String.format(
                                    "Failed to cancel outdated scheduled job %d.%s", enqueuedJob.getId(), e.getMessage()));
                            }
                        }
                    }
                }
            }
        }
    }
}


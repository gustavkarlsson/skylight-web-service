package se.gustavkarlsson.aurora_notifier.web_service.tasks;

import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.servlets.tasks.Task;
import se.gustavkarlsson.aurora_notifier.web_service.resources.Timestamped;
import se.gustavkarlsson.aurora_notifier.web_service.updater.KpIndexUpdater;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.PrintWriter;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class UpdateTask extends Task {
	private KpIndexUpdater updater;

	@Inject
	protected UpdateTask(KpIndexUpdater updater) {
		super("update");
		this.updater = checkNotNull(updater);
	}

	@Override
	public void execute(ImmutableMultimap<String, String> immutableMultimap, PrintWriter printWriter) throws Exception {
		Timestamped<Float> kpIndex = updater.update();
		printWriter.append(kpIndex.toString());
	}
}

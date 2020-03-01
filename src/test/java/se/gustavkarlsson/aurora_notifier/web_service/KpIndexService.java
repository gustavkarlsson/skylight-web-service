package se.gustavkarlsson.aurora_notifier.web_service;

import retrofit2.Call;
import retrofit2.http.GET;
import se.gustavkarlsson.aurora_notifier.web_service.resources.Timestamped;

public interface KpIndexService {

	@GET("kp-index")
	Call<Timestamped<Float>> get();
}

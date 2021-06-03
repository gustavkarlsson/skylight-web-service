package se.gustavkarlsson.aurora_notifier.web_service;

import retrofit2.Call;
import retrofit2.http.GET;

public interface KpIndexService {

	@GET("kp-index")
	Call<Timestamped<Float>> get();
}

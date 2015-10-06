package se.gustavkarlsson.aurora_notifier.web_service.providers;

public class DummyProvider implements Provider<Object> {

	@Override
	public String getValue() throws ProviderException {
		return null;
	}
}

package weatherApp.network;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import weatherApp.event.*;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

import static weatherApp.event.EventStream.eventStream;

public abstract class WeatherDataSource {
	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(WeatherDataSource.class);

	private static final int DEFAULT_POLL_INTERVAL = 60;
	private static final int INITIAL_DELAY = 3;
	private static final int TIMEOUT = 20;

	public Observable<? extends WeatherEvent> dataSourceStream() {
		return dataSourceStream(DEFAULT_POLL_INTERVAL);
	}

	public Observable<? extends WeatherEvent> dataSourceStream(Integer pollInterval) {
		/*
		 * This creates a stream of data events. Each event emitted corresponds
		 * to a piece of data fetched from a remote (i.e. Internet) data source.
		 * This class is capable of grabbing data in one of two ways. Firstly,
		 * it can poll the data source every POLL_INTERVAL seconds. Secondly, it
		 * can fetch data on request (e.g. when a user hits the refresh button
		 * which causes a RefreshRequestEvent to be triggered; the event is
		 * handled here). The code below essentially merges events that arrive
		 * via one of the two routes into a single stream of events.
		 */
		if (pollInterval == null) {
			pollInterval = DEFAULT_POLL_INTERVAL;
		}
		return fixedIntervalStream(pollInterval).compose(this::wrapRequest)
				.mergeWith(eventStream().eventsInIO().ofType(RefreshRequestEvent.class).compose(this::wrapRequest));
	}

	protected Observable<Long> fixedIntervalStream(int pollInterval) {
		return Observable.interval(INITIAL_DELAY, pollInterval, TimeUnit.SECONDS, Schedulers.io());
	}

	protected abstract Observable<? extends ValueEvent> makeRequest();

	protected HttpClientRequest<ByteBuf> prepareHttpGETRequest(String url) {
		/*
		 * As the name says, this creates an HTTP GET request (but does not send
		 * it, sending is done elsewhere).
		 */
		return HttpClientRequest.createGet(url);
	}

	protected Observable<String> unpackResponse(Observable<HttpClientResponse<ByteBuf>> responseObservable) {
		/*
		 * Extracts HTTP response's body to a plain Java string
		 */
		return responseObservable.flatMap(HttpClientResponse::getContent)
				.map(buffer -> buffer.toString(CharsetUtil.UTF_8));
	}

	private <T> Observable<WeatherEvent> wrapRequest(Observable<T> observable) {
		/*
		 * Issues an HTTP query but emits an appropriate even before the query
		 * is made and another event when the query is completed. This allows us
		 * to give visual feedback (spinning icon) to the user during the
		 * request.
		 */
		return observable.flatMap(ignore -> Observable.concat(Observable.just(new NetworkRequestIssuedEvent()), // emit
																												// NetworkRequestIssuedEvent
																												// before
				makeRequest().timeout(TIMEOUT, TimeUnit.SECONDS).doOnError(log::error) // if
																						// a
																						// request
																						// takes
																						// more
																						// than
																						// 30
																						// seconds
																						// abort
																						// it
						.cast(WeatherEvent.class).onErrorReturn(ErrorEvent::new), // and
																					// emit
																					// an
																					// error
																					// event
				Observable.just(new NetworkRequestFinishedEvent())) // emit
																	// NetworkRequestFinishedEvent
																	// after
		);
	}

	public static Integer getDefaultPollInterval() {
		return DEFAULT_POLL_INTERVAL;
	}
}

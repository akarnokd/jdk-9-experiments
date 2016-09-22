package hu.akarnokd.java9;

import java.net.URI;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.net.http.WebSocket.MessagePart;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class WebSocketTest {

	public static void main(String[] args) throws Exception {
		WebSocket.Builder wsb = WebSocket.newBuilder(new URI("http://www.example.com"), new Listener() {
			@Override
			public CompletionStage<?> onText(WebSocket arg0, CharSequence arg1, MessagePart arg2) {
				System.out.println(arg1);
				return CompletableFuture.completedStage(null);
			}
		});
		
		wsb.buildAsync().get(5, TimeUnit.SECONDS);
	}
}

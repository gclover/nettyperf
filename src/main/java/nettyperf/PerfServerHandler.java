package nettyperf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.ServerCookieEncoder;
import io.netty.util.CharsetUtil;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

public class PerfServerHandler extends ChannelInboundMessageHandlerAdapter<Object> {

	private HttpRequest request;
	private final StringBuilder buf = new StringBuilder();

	@Override
	public void messageReceived(ChannelHandlerContext context, Object msg) throws Exception {
		try {
			if (msg instanceof HttpRequest) {
				HttpRequest request = this.request = (HttpRequest)msg;
				//System.out.println("request");
			} else if (msg instanceof HttpContent) {
				HttpContent content = (HttpContent)msg;
				//System.out.println("content");
				buf.append("hello netty");
				writeResponse(context);
			}
			//this.request = (HttpRequest)msg;
		} catch (Exception e) {
			System.out.println("error");
		}
	}

	private void writeResponse(ChannelHandlerContext context) {
		boolean keepAlive = isKeepAlive(request);
		FullHttpResponse response = new DefaultFullHttpResponse(
				HTTP_1_1, OK, Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));

		response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");

		if (keepAlive) {
			response.headers().set(CONTENT_LENGTH, response.data().readableBytes());
			response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
		}

		String cookieString = request.headers().get(COOKIE);
		if (cookieString != null) {
			Set<Cookie> cookies = CookieDecoder.decode(cookieString);
			if (!cookies.isEmpty()) {
				for (Cookie cookie: cookies) 
					response.headers().add(SET_COOKIE, ServerCookieEncoder.encode(cookie));
			} else {
				response.headers().add(SET_COOKIE, ServerCookieEncoder.encode("key1", "value1"));
				response.headers().add(SET_COOKIE, ServerCookieEncoder.encode("key2", "value2"));
			}
		}

        context.nextOutboundMessageBuffer().add(response);

        if (!keepAlive) 
        	context.flush().addListener(ChannelFutureListener.CLOSE);
	}

	@Override
	public void endMessageReceived(ChannelHandlerContext context) throws Exception {
		context.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext context, Throwable cause) throws Exception {
		//cause.printStackTrace();
		context.close();
	}

}
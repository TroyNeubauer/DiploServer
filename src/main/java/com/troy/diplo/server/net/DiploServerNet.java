package com.troy.diplo.server.net;

import org.apache.logging.log4j.*;

import com.troy.diplo.game.DiploConstants;
import com.troy.diplo.server.DiploServer;
import com.troyberry.util.ThreadUtils;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.*;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.util.SelfSignedCertificate;

/**
 * Modification of {@link EchoServer} which utilizes Java object serialization.
 */
public final class DiploServerNet {

	private static final Logger logger = LogManager.getLogger(DiploServerNet.class);

	private volatile ChannelFuture future;
	private volatile EventLoopGroup bossGroup;
	private volatile EventLoopGroup workerGroup;
	private final Thread thread;
	private SslContext sslCtx;

	public DiploServerNet(DiploServer server) {
		this.thread = new Thread(() -> {

			SelfSignedCertificate ssc;
			if (DiploConstants.USE_SSL) {
				try {
					ssc = new SelfSignedCertificate();
					sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
				} catch (Exception e) {
					logger.fatal("Unable to setup server ssl!");
					logger.catching(e);
				}
			}

			this.bossGroup = new NioEventLoopGroup(1);
			this.workerGroup = new NioEventLoopGroup();
			try {
				ServerBootstrap b = new ServerBootstrap();
				b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ChannelPipeline p = ch.pipeline();
						if (DiploConstants.USE_SSL)
							p.addLast(sslCtx.newHandler(ch.alloc()));
						p.addLast(new ObjectEncoder(), new ObjectDecoder(ClassResolvers.cacheDisabled(null)), new DiploServerNetHandler(server));
					}
				});
				logger.info("Attempting to bind server socket");
				// Bind and start to accept incoming connections.
				this.future = b.bind(DiploConstants.PORT).sync();
				logger.info("Server ready to recieve connectitions");
			} catch (InterruptedException e) {
				logger.trace(Thread.currentThread().getName() + " Was interupted while waiting to start up");
			}
		}, "Server Network Setup-Thread");
		thread.start();
	}

	public void cleanUp() {
		if (future != null)
			future.channel().close();
		if (bossGroup != null)
			bossGroup.shutdownGracefully();
		if (workerGroup != null)
			workerGroup.shutdownGracefully();

		logger.info("Server socket closing");
	}

	public void join() {
		if (future != null)
			future.channel().close().syncUninterruptibly();
		if (bossGroup != null)
			bossGroup.shutdownGracefully().syncUninterruptibly();
		if (workerGroup != null)
			workerGroup.shutdownGracefully().syncUninterruptibly();

		thread.interrupt();
		ThreadUtils.join(thread);
	}
}

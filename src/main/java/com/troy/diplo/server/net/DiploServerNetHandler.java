package com.troy.diplo.server.net;

import org.apache.logging.log4j.*;

import com.troy.diplo.packet.*;
import com.troy.diplo.packet.RegisterReply.RegisterReplyEnum;
import com.troy.diplo.server.DiploServer;
import com.troy.diplo.server.database.DatabaseAccount;

import io.netty.channel.*;

/**
 * Handles both client-side and server-side handler depending on which constructor was called.
 */
public class DiploServerNetHandler extends ChannelInboundHandlerAdapter {
	
	private static final Logger logger = LogManager.getLogger(DiploServerNetHandler.class);
	
	private DiploServer server;
	
	public DiploServerNetHandler(DiploServer server) {
		this.server = server;
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		logger.info("new channel active! " + ctx);
		super.channelActive(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		if(msg instanceof LoginData) {
			LoginData data = (LoginData) msg;
			logger.info("new login attempt! " + msg);
			String username = new String(data.getUsername());
			boolean validCredentals = server.areCredentialsValid(username, data.getPassword());
			if(validCredentals) {
				DatabaseAccount account = server.getAccount(username);
				ctx.writeAndFlush(new LoginReply(true, account.getAccount().getProfile()));
			} else {
				ctx.writeAndFlush(new LoginReply(false, null));
			}
		} else if(msg instanceof RegisterData) {
			logger.info("new register attempt! " + msg);
			ctx.writeAndFlush(new RegisterReply(RegisterReplyEnum.REGISTER_FAIL_EMAIL_IN_USE));
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}

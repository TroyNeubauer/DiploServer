package com.troy.diplo.server.net;

import java.util.Map.Entry;

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
		if (msg instanceof LoginData) {
			handleLogin((LoginData) msg, ctx);
		} else if (msg instanceof RegisterData) {
			handleRegister((RegisterData) msg, ctx);
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

	private void handleRegister(RegisterData data, ChannelHandlerContext ctx) {
		String username = new String(data.getUsername()), email = new String(data.getEmail());
		if (server.containsUser(username)) {
			ctx.writeAndFlush(new RegisterReply(RegisterReplyEnum.REGISTER_FAIL_USERNAME_IN_USE));
		} else {
			for (Entry<String, DatabaseAccount> entry : server.getDatabase().getUsers().getUsers().entrySet()) {
				if (entry.getValue().getAccount().getEmail().equals(email)) {
					ctx.writeAndFlush(new RegisterReply(RegisterReplyEnum.REGISTER_FAIL_EMAIL_IN_USE));
					logger.info("email in use " + username + ", em " + email);
					return;
				}
			}
			logger.info("regitering user " + username + ", em " + email);
			server.registerUser(username, data.getPassword(), email);
			ctx.writeAndFlush(new RegisterReply(RegisterReplyEnum.REGISTER_SUCEED));
		}
	}

	private void handleLogin(LoginData data, ChannelHandlerContext ctx) {
		logger.info("new login attempt! " + data);
		String username = new String(data.getUsername());
		boolean validCredentals = server.areCredentialsValid(username, data.getPassword());
		if (validCredentals) {
			ctx.writeAndFlush(new LoginReply(true, server.getAccount(username).getAccount().getProfile()));
		} else {
			ctx.writeAndFlush(new LoginReply(false, null));
		}
	}
}

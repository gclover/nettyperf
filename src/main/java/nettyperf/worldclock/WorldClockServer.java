/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package nettyperf.worldclock;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Receives a list of continent/city pairs from a {@link WorldClockClient} to
 * get the local times of the specified cities.
 */
public class WorldClockServer {

    private final int port;

    public WorldClockServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        ServerBootstrap b = new ServerBootstrap();
        try {
            b.group(new NioEventLoopGroup(), new NioEventLoopGroup())
             .channel(NioServerSocketChannel.class)
             .childHandler(new WorldClockServerInitializer());

            b.bind(port).sync().channel().closeFuture().sync();
        } finally {
            b.shutdown();
        }
    }

    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8080;
        }
        new WorldClockServer(port).run();
    }
}

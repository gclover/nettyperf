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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import nettyperf.worldclock.WorldClockProtocol.Continent;
import nettyperf.worldclock.WorldClockProtocol.DayOfWeek;
import nettyperf.worldclock.WorldClockProtocol.LocalTime;
import nettyperf.worldclock.WorldClockProtocol.LocalTimes;
import nettyperf.worldclock.WorldClockProtocol.Location;
import nettyperf.worldclock.WorldClockProtocol.Locations;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Calendar.*;

public class WorldClockServerHandler extends ChannelInboundMessageHandlerAdapter<Locations> {

	private LocalTimes.Builder builder = null;

 	public WorldClockServerHandler() {

        long currentTime = System.currentTimeMillis();
        builder = LocalTimes.newBuilder();
	Locations locations = Locations.getDefaultInstance();
        for (Location l: locations.getLocationList()) {
            TimeZone tz = TimeZone.getTimeZone(
                    toString(l.getContinent()) + '/' + l.getCity());
            Calendar calendar = getInstance(tz);
            calendar.setTimeInMillis(currentTime);

            builder.addLocalTime(LocalTime.newBuilder().
                    setYear(calendar.get(YEAR)).
                    setMonth(calendar.get(MONTH) + 1).
                    setDayOfMonth(calendar.get(DAY_OF_MONTH)).
                    setDayOfWeek(DayOfWeek.valueOf(calendar.get(DAY_OF_WEEK))).
                    setHour(calendar.get(HOUR_OF_DAY)).
                    setMinute(calendar.get(MINUTE)).
                    setSecond(calendar.get(SECOND)).build());
        }

	}

    private static final Logger logger = Logger.getLogger(
            WorldClockServerHandler.class.getName());

    @Override
    public void messageReceived(ChannelHandlerContext ctx, Locations locations) throws Exception {
        ctx.write(builder.build());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.log(
                Level.WARNING,
                "Unexpected exception from downstream.", cause);
        ctx.close();
    }

    private static String toString(Continent c) {
        return c.name().charAt(0) + c.name().toLowerCase().substring(1);
    }
}

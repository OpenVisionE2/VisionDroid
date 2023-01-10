/* © 2010 Original creator
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid.helpers.enigma2;

import android.util.Log;

import androidx.annotation.NonNull;

import org.openvision.visiondroid.Profile;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

/**
 * @author sre
 * 
 */
public class DeviceDetector {
	@NonNull
	public static String LOG_TAG = DeviceDetector.class.getName();
	public static final String[] KNOWN_HOSTNAMES = { "pulse4k", "pulse4kmini", "viper4k", "vipercombo", "vipercombohdd", "viperslim", "vipert2c", "ip8", "pixel", "pixelpremium", "triplex", "azboxhd", "azboxme", "azboxminime", "beyonwizv2", "sogno8800hd", "uniboxhde", "alphatriplehd", "bre2zet2c", "mbtwinplus", "sf128", "sf138", "sf3038", "9910lx", "9911lx", "9920lx", "e4hd", "e4hdcombo", "e4hdhybrid", "e4hdultra", "mbmicro", "mbmicrov2", "odin2hybrid", "odinplus", "protek4k", "sf208", "sf228", "sf238", "singleboxlcd", "twinboxlcd", "twinboxlcdci5", "cube", "dual", "force1", "force1plus", "force2", "force2nano", "force2plus", "force2plushv", "force2se", "force3uhd", "force3uhdplus", "force4", "fusionhd", "fusionhdse", "galaxy4k", "iqonios100hd", "iqonios200hd", "iqonios300hd", "iqonios300hdv2", "lunix", "lunix34k", "lunix4k", "lunixco", "mediabox", "optimussos", "optimussos1", "optimussos1plus", "optimussos2", "optimussos2plus", "optimussos3plus", "purehd", "purehdse", "revo4k", "tm2t", "tm4ksuper", "tmnano", "tmnano2super", "tmnano2t", "tmnano3t", "tmnanom3", "tmnanose", "tmnanosecombo", "tmnanosem2", "tmnanoseplus", "tmsingle", "tmtwin", "tmtwin4k", "valalinux", "worldvisionf1", "worldvisionf1plus", "anadol4k", "anadol4kv2", "anadolprohd5", "arivacombo", "arivatwin", "axashis4kcombo", "axashis4kcomboplus", "axashisc4k", "axashistwin", "axashistwinplus", "dinobot4k", "dinobot4kmini", "dinobot4kplus", "dinobot4kse", "dinobot4ktwin", "dinobotu43", "dinobotu55", "iziboxelite4k", "iziboxone4kplus", "iziboxx3", "protek4kx1", "protek4kx2", "turing", "viper4kv20", "viper4kv40", "vipersingle", "vipertwin", "dm500hd", "dm500hdv2", "dm520", "dm7020hd", "dm7020hdv2", "dm7080", "dm800", "dm8000", "dm800se", "dm800sev2", "dm820", "dm900", "dm920", "dreamone", "dreamtwo", "ebox5000", "ebox5100", "ebox7358", "eboxlumi", "osmega", "osmini", "osmini4k", "osminiplus", "osmio4k", "osmio4kplus", "osnino", "osninoplus", "osninopro", "bre2ze", "enfinity", "evomini", "evominiplus", "marvel1", "x2plus", "formuler1", "formuler3", "formuler4", "formuler4turbo", "bre2ze4k", "hd11", "hd1100", "hd1200", "hd1265", "hd1500", "hd2400", "hd500c", "hd51", "hd530c", "hd60", "hd61", "hd66se", "vs1000", "vs1500", "gb800se", "gb800seplus", "gb800solo", "gb800ue", "gb800ueplus", "gbip4k", "gbipbox", "gbquad", "gbquad4k", "gbquadplus", "gbtrio4k", "gbtrio4kpro", "gbue4k", "gbultrase", "gbultraue", "gbultraueh", "gbx1", "gbx2", "gbx3", "gbx34k", "gbx3h", "reborn", "su980", "atemio5x00", "atemio6000", "atemio6100", "atemio6200", "atemionemesis", "beyonwizt2", "beyonwizt3", "beyonwizt4", "bwidowx", "evoslim", "mbhybrid", "mbmini", "mbminiplus", "mbtwin", "mbultra", "opticumtt", "sezam1000hd", "sezam5000hd", "sezammarvel", "ventonhdx", "xpeedlx", "xpeedlx3", "ixussone", "ixusszero", "alien5", "k1plus", "k1plusv2", "k1pro", "k2pro", "k2prov2", "k3pro", "multibox", "multiboxpro", "multiboxse", "x8hp", "sf4008", "sf8008", "sf8008m", "sfx6008", "sx88v2", "sx988", "axase3", "axodin", "maram9", "odroidc2", "9900lx", "og2ott4k", "raspberrypi", "raspberrypi0", "raspberrypi2", "raspberrypi3", "raspberrypi4", "adb_box", "adb_2850", "arivalink200", "atemio520", "atemio530", "atevio7500", "cuberevo", "cuberevo_2000hd", "cuberevo_250hd", "cuberevo_3000hd", "cuberevo_9500hd", "cuberevo_mini", "cuberevo_mini2", "forever_2424hd", "forever_3434hd", "forever_9898hd", "forever_nanosmart", "fortis_hdbox", "hc8100", "hl101", "hs7110", "hs7119", "hs7420", "hs7429", "hs7810a", "hs7819", "ipbox55", "ipbox99", "ipbox9900", "octagon1008", "opt9600", "opt9600mini", "opt9600prima", "pace7241", "qboxhd", "qboxhd_mini", "sagemcom88", "spark", "spark7162", "tf7700", "ufc960", "ufs910", "ufs912", "ufs913", "ufs922", "vip1_v1", "vip1_v2", "vip2", "vitamin_hd5000", "tiviaraplus", "tiviarmin", "enibox", "evoslimse", "evoslimt2c", "mago", "sf108", "sf98", "t2cable", "tyrant", "x1plus", "xcombo", "ustym4kottpremium", "ustym4kpro", "et7000mini", "xpeedc", "vuduo", "vuduo2", "vuduo4k", "vuduo4kse", "vusolo", "vusolo2", "vusolo4k", "vusolose", "vuultimo", "vuultimo4k", "vuuno", "vuuno4k", "vuuno4kse", "vuzero", "vuzero4k", "wetekhub", "wetekplay", "wetekplay2", "spycat", "spycatmini", "spycatminiplus", "xp1000", "beyonwizu4", "et10000", "et13000", "et1x000", "et4x00", "et5x00", "et6x00", "et7x00", "et8000", "et8500", "et9x00", "h10", "h11", "h3", "h4", "h5", "h6", "h7", "h8", "h9", "h9combo", "h9combose", "h9se", "i55", "i55plus", "i55plusse", "lc", "novacombo", "novaip", "novatwin", "sh1" };

	@NonNull
	public static ArrayList<Profile> getAvailableHosts() {
		ArrayList<Profile> profiles = new ArrayList<>();
		for (String hostname : KNOWN_HOSTNAMES) {
			try {
				InetAddress host = InetAddress.getByName(hostname);
				if (!host.isReachable(1500))
					continue;
				boolean simpleRemote = false;
				String ip = host.getHostAddress();

				Profile p = Profile.getDefault();
				p.setName(hostname);
				p.setHost(ip);
				p.setStreamHost(ip);
				p.setPort(80);
				p.setUser("root");
				p.setSimpleRemote(simpleRemote);
				addToList(profiles, p);
			} catch (IOException e) {
				Log.w(LOG_TAG, e.getMessage());
			}
		}

		JmDNS jmdns;
		try {
			jmdns = JmDNS.create();
			ServiceInfo si[] = jmdns.list("_http._tcp.local.");
			for (ServiceInfo s : si) {
				Log.i(LOG_TAG, Arrays.toString(s.getHostAddresses()));
				String address = s.getHostAddresses()[0];
				int port = s.getPort();
				boolean simpleRemote = false;
				Profile p = Profile.getDefault();
				p.setName(s.getName());
				p.setHost(address);
				p.setStreamHost(address);
				p.setPort(port);
				p.setUser("root");
				p.setSimpleRemote(simpleRemote);
				addToList(profiles, p);
			}
		} catch (IOException e) {
			Log.e(LOG_TAG, e.getMessage());
		}

		return profiles;
	}

	private static void addToList(@NonNull ArrayList<Profile> list, @NonNull Profile profile) {
		for (Profile p : list) {
			if (profile.getHost().equals(p.getHost())) {
				list.remove(p);
				break;
			}
		}
		list.add(profile);
	}
}

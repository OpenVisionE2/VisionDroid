/* © 2010 Original creator
 * 
 * Licensed under the Create-Commons Attribution-Noncommercial-Share Alike 3.0 Unported
 * http://creativecommons.org/licenses/by-nc-sa/3.0/
 */

package org.openvision.visiondroid;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;


/**
 * @author sre
 */
public class Profile implements Serializable {
	private static final long serialVersionUID = 8176949133234868302L;
	private int mId;
	@Nullable
	private String mName;
	private String mHost;
	private String mStreamHost;
	private String mEncoderPath;
	@Nullable
	private String mUser;
	@Nullable
	private String mPass;
	private String mEncoderUser;
	private String mEncoderPass;
	private boolean mLogin;
	private boolean mSsl;
	private boolean mStreamLogin;
	private boolean mFileLogin;
	private boolean mEncoderLogin;
	private boolean mEncoderStream;
	private boolean mFileSsl;
	private boolean mSimpleRemote;
	private int mPort;
	private int mStreamPort;
	private int mFilePort;
	private int mEncoderPort;
	private int mEncoderAudioBitrate;
	private int mEncoderVideoBitrate;

	private String mDefaultRef;
	private String mDefaultRefName;
	private String mDefaultRef2;
	private String mDefaultRef2Name;
	@Nullable
	private String mSessionId;

	@Nullable
	private String mCachedDeviceInfo;
    private String ssid;
    private boolean isDefaultProfileOnNoWifi;

	@NonNull
	public static Profile getDefault() {
		return new Profile(-1, "", "", "", 80, 8001, 80, false, "root", "openvision", false, false, false, false, false, "", "", "", "");
	}

	public Profile(int id, String name, String host, String streamHost, int port, int streamPort, int filePort, boolean login,
				   String user, String pass, boolean ssl, boolean streamLogin, boolean fileLogin, boolean fileSsl,
				   boolean simpleRemote, String defaultRef, String defaultRefName, String defaultRef2, String defaultRef2Name) {
		init(id, name, host, streamHost, port, streamPort, filePort, login, user, pass, ssl, streamLogin, fileLogin, fileSsl, simpleRemote, defaultRef, defaultRefName, defaultRef2, defaultRef2Name, false, "stream", 554, false, "", "", 2500, 128);
	}

	public Profile(int id, String name, String host, String streamHost, int port, int streamPort, int filePort, boolean login,
				   String user, String pass, boolean ssl, boolean streamLogin, boolean fileLogin, boolean fileSsl,
				   boolean simpleRemote, String defaultRef, String defaultRefName, String defaultRef2, String defaultRef2Name, boolean encoderStream,
				   String encoderPath, int encoderPort, boolean encoderLogin, String encoderUser, String encoderPass, int encoderVideoBitrate, int encoderAudioBitrate) {
		init(id, name, host, streamHost, port, streamPort, filePort, login, user, pass, ssl, streamLogin, fileLogin, fileSsl, simpleRemote, defaultRef, defaultRefName, defaultRef2, defaultRef2Name, encoderStream, encoderPath, encoderPort, encoderLogin, encoderUser, encoderPass, encoderVideoBitrate, encoderAudioBitrate);
	}

	private void init(int id, String name, String host, String streamHost, int port, int streamPort, int filePort, boolean login,
					  String user, String pass, boolean ssl, boolean streamLogin, boolean fileLogin, boolean fileSsl,
					  boolean simpleRemote, String defaultRef, String defaultRefName, String defaultRef2, String defaultRef2Name, boolean encoderStream,
					  String encoderPath, int encoderPort, boolean encoderLogin, String encoderUser, String encoderPass, int encoderVideoBitrate, int encoderAudioBitrate) {
		mId = id;
		mSessionId = null;
		mCachedDeviceInfo = null;

		setName(name);
		setHost(host);
		setStreamHost(streamHost);
		setPort(port);
		setStreamPort(streamPort);
		setFilePort(filePort);
		setLogin(login);
		setStreamLogin(streamLogin);
		setFileLogin(fileLogin);
		setFileSsl(fileSsl);
		setUser(user);
		setPass(pass);
		setSsl(ssl);
		setSimpleRemote(simpleRemote);
		setDefaultRefValues(defaultRef, defaultRefName);
		setDefaultRef2Values(defaultRef2, defaultRef2Name);
		//Encoder
		setEncoderStream(encoderStream);
		setEncoderPort(encoderPort);
		setEncoderPath(encoderPath);
		setEncoderAudioBitrate(encoderAudioBitrate);
		setEncoderVideoBitrate(encoderVideoBitrate);
		setEncoderLogin(encoderLogin);
		setEncoderUser(encoderUser);
		setEncoderPass(encoderPass);
	}

	public void setId(int id) {
		mId = id;
	}

	public void setPort(int port) {
		mPort = port;
	}

	public void setPort(@NonNull String port, boolean ssl) {
		mSsl = ssl;
		setPort(port);
	}

	public void setStreamPort(int streamPort) {
		mStreamPort = streamPort;
	}

	public void setFilePort(int filePort) {
		mFilePort = filePort;
	}

	@Nullable
	public String getName() {
		return mName;
	}

	public void setName(@Nullable String name) {
		if (name == null) {
			name = "";
		}
		mName = name;
	}

	public String getHost() {
		return mHost;
	}

	public void setHost(@Nullable String host) {
		if (host == null) {
			host = "";
		}
		mHost = host.replace("http://", "").replace("https://", "");
	}

	public String getStreamHost() {
		if ("".equals(mStreamHost) || mStreamHost == null) {
			return mHost;
		} else {
			return mStreamHost;
		}
	}

	public void setStreamHost(@Nullable String streamHost) {
		if (streamHost == null)
			streamHost = "";
		mStreamHost = streamHost.replace("http://", "").replace("https://", "");
	}

	public String getStreamHostValue() {
		return mStreamHost;
	}

	public void setEncoderPath(String encoderPath) {
		mEncoderPath = encoderPath;
	}

	public String getEncoderPath() {
		return mEncoderPath;
	}

	@Nullable
	public String getUser() {
		return mUser;
	}

	public void setUser(@Nullable String user) {
		if (user == null)
			user = "";
		mUser = user;
	}

	@Nullable
	public String getPass() {
		return mPass;
	}

	public void setPass(@Nullable String pass) {
		if (pass == null)
			pass = "";
		mPass = pass;
	}

	public boolean isLogin() {
		return mLogin;
	}

	public void setLogin(boolean login) {
		mLogin = login;
	}

	public String getEncoderUser() {
		return mEncoderUser;
	}

	public void setEncoderUser(String user) {
		mEncoderUser = user;
	}

	public String getEncoderPass() {
		return mEncoderPass;
	}

	public void setEncoderPass(String pass) {
		mEncoderPass = pass;
	}

	public boolean isEncoderLogin() {
		return mEncoderLogin;
	}

	public void setEncoderLogin(boolean isLogin) {
		mEncoderLogin = isLogin;
	}

	public boolean isSsl() {
		return mSsl;
	}

	public void setSsl(boolean ssl) {
		mSsl = ssl;
	}

	public boolean isFileLogin() {
		return mFileLogin;
	}

	public void setFileLogin(boolean login) {
		mFileLogin = login;
	}

	public boolean isFileSsl() {
		return mFileSsl;
	}

	public void setFileSsl(boolean ssl) {
		mFileSsl = ssl;
	}

	public boolean isSimpleRemote() {
		return mSimpleRemote;
	}

	public void setSimpleRemote(boolean simpleRemote) {
		mSimpleRemote = simpleRemote;
	}

	public int getId() {
		return mId;
	}

	public int getPort() {
		return mPort;
	}

	public void setPort(@NonNull String port) {
		try {
			mPort = Integer.valueOf(port);
		} catch (NumberFormatException e) {
			Log.w(VisionDroid.LOG_TAG, e.toString());
			if (mSsl) {
				mPort = 443;
			} else {
				mPort = 80;
			}
		}
	}

	@NonNull
	public String getPortString() {
		return String.valueOf(mPort);
	}

	public int getStreamPort() {
		return mStreamPort;
	}

	public void setStreamPort(@NonNull String streamPort) {
		try {
			mStreamPort = Integer.valueOf(streamPort);
		} catch (NumberFormatException e) {
			Log.w(VisionDroid.LOG_TAG, e.toString());
		}
	}


	public boolean isEncoderStream() {
		return mEncoderStream;
	}

	public void setEncoderStream(boolean encoderStream) {
		mEncoderStream = encoderStream;
	}

	@NonNull
	public String getStreamPortString() {
		return String.valueOf(mStreamPort);
	}

	public int getEncoderPort() {
		return mEncoderPort;
	}
	@NonNull
	public String getEncoderPortString() {
		return String.valueOf(mEncoderPort);
	}

	public void setEncoderPort(int port) {
		mEncoderPort = port;
	}

	public void setEncoderPort(@NonNull String port) {
		try {
			mEncoderPort = Integer.valueOf(port);
		} catch (NumberFormatException e) {
			Log.w(VisionDroid.LOG_TAG, e.toString());
		}
	}

	public int getEncoderVideoBitrate() {
		return mEncoderVideoBitrate;
	}

	@NonNull
	public String getEncoderVideoBitrateString() {
		return String.valueOf(mEncoderVideoBitrate);
	}

	public void setEncoderVideoBitrate(int bitrate) {
		mEncoderVideoBitrate = bitrate;
	}

	public void setEncoderVideoBitrate(@NonNull String bitrate) {
		try {
			mEncoderVideoBitrate = Integer.valueOf(bitrate);
		} catch (NumberFormatException e) {
			Log.w(VisionDroid.LOG_TAG, e.toString());
		}
	}

	public int getEncoderAudioBitrate() {
		return mEncoderAudioBitrate;
	}

	@NonNull
	public String getEncoderAudioBitrateString() {
		return String.valueOf(mEncoderAudioBitrate);
	}

	public void setEncoderAudioBitrate(int bitrate) {
		mEncoderAudioBitrate = bitrate;
	}

	public void setEncoderAudioBitrate(@NonNull String bitrate) {
		try {
			mEncoderAudioBitrate = Integer.valueOf(bitrate);
		} catch (NumberFormatException e) {
			Log.w(VisionDroid.LOG_TAG, e.toString());
		}
	}

	public int getFilePort() {
		return mFilePort;
	}

	public void setFilePort(@NonNull String filePort) {
		try {
			mFilePort = Integer.valueOf(filePort);
		} catch (NumberFormatException e) {
			Log.w(VisionDroid.LOG_TAG, e.toString());
			mFilePort = 80;
		}
	}

	@NonNull
	public String getFilePortString() {
		return String.valueOf(mFilePort);
	}

	public void setDefaultRefValues(String ref, String name) {
		setDefaultRef(ref);
		setDefaultRefName(name);
	}

	public void setDefaultRef2Values(String ref, String name) {
		setDefaultRef2(ref);
		setDefaultRef2Name(name);
	}

	public String getDefaultRef() {
		return mDefaultRef;
	}

	public void setDefaultRef(String defaultRef) {
		mDefaultRef = defaultRef;
	}

	public String getDefaultRefName() {
		return mDefaultRefName;
	}

	public void setDefaultRefName(String defaultRefName) {
		mDefaultRefName = defaultRefName;
	}

	public String getDefaultRef2() {
		return mDefaultRef2;
	}

	public void setDefaultRef2(String defaultRef2) {
		mDefaultRef2 = defaultRef2;
	}

	public String getDefaultRef2Name() {
		return mDefaultRef2Name;
	}

	public void setDefaultRef2Name(String defaultRef2Name) {
		mDefaultRef2Name = defaultRef2Name;
	}

	public boolean isStreamLogin() {
		return mStreamLogin;
	}

	public void setStreamLogin(boolean streamLogin) {
		mStreamLogin = streamLogin;
	}

	public void setSessionId(String sessionId) {
		mSessionId = sessionId;
	}

	@Nullable
	public String getSessionId() {
		return mSessionId;
	}

	public void setCachedDeviceInfo(String deviceInfo) {
		mCachedDeviceInfo = deviceInfo;
	}

	@Nullable
	public String getCachedDeviceInfo() {
		return mCachedDeviceInfo;
	}

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public boolean isDefaultProfileOnNoWifi() {
        return isDefaultProfileOnNoWifi;
    }

    public void setDefaultProfileOnNoWifi(boolean defaultProfileOnNoWifi) {
        isDefaultProfileOnNoWifi = defaultProfileOnNoWifi;
    }

    public boolean equals(@NonNull Profile p) {
		return getHost().equals(p.getHost())
				&& getStreamHost().equals(p.getStreamHost())
				&& getUser().equals(p.getUser())
				&& getPass().equals(p.getPass())
				&& isLogin() == p.isLogin()
				&& isSsl() == p.isSsl()
				&& isSimpleRemote() == p.isSimpleRemote()
				&& getId() == p.getId()
				&& getPort() == p.getPort()
				&& getStreamPort() == p.getStreamPort()
				&& getFilePort() == p.getFilePort()
				&& isStreamLogin() == p.isStreamLogin()
				&& isFileSsl() == p.isFileSsl()
				&& isFileLogin() == p.isFileLogin()
				&& isEncoderStream() == p.isEncoderStream()
				&& getEncoderPort() == p.getEncoderPort()
				&& getEncoderPath().equals(p.getEncoderPath())
				&& isEncoderLogin() == p.isEncoderLogin()
				&& getEncoderUser().equals(p.getEncoderUser())
				&& getEncoderPass().equals(p.getEncoderPass())
				&& getEncoderVideoBitrate() == p.getEncoderVideoBitrate()
				&& getEncoderAudioBitrate() == p.getEncoderAudioBitrate();
	}
}

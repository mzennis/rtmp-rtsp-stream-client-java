package com.pedro.rtplibrary.rtmp;

import android.content.Context;
import android.media.MediaCodec;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import android.view.SurfaceView;
import android.view.TextureView;

import com.pedro.rtmp.flv.video.ProfileIop;
import com.pedro.rtmp.rtmp.RtmpClient;
import com.pedro.rtmp.utils.ConnectCheckerRtmp;
import com.pedro.rtplibrary.base.Camera1Base;
import com.pedro.rtplibrary.view.LightOpenGlView;
import com.pedro.rtplibrary.view.OpenGlView;
import java.nio.ByteBuffer;

/**
 * More documentation see:
 * {@link com.pedro.rtplibrary.base.Camera1Base}
 *
 * Created by pedro on 25/01/17.
 */

public class RtmpCamera1 extends Camera1Base {

  private final RtmpClient rtmpClient;

  public RtmpCamera1(SurfaceView surfaceView, ConnectCheckerRtmp connectChecker) {
    super(surfaceView);
    rtmpClient = new RtmpClient(connectChecker);
  }

  public RtmpCamera1(TextureView textureView, ConnectCheckerRtmp connectChecker) {
    super(textureView);
    rtmpClient = new RtmpClient(connectChecker);
  }

  @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
  public RtmpCamera1(OpenGlView openGlView, ConnectCheckerRtmp connectChecker) {
    super(openGlView);
    rtmpClient = new RtmpClient(connectChecker);
  }

  @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
  public RtmpCamera1(LightOpenGlView lightOpenGlView, ConnectCheckerRtmp connectChecker) {
    super(lightOpenGlView);
    rtmpClient = new RtmpClient(connectChecker);
  }

  @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
  public RtmpCamera1(Context context, ConnectCheckerRtmp connectChecker) {
    super(context);
    rtmpClient = new RtmpClient(connectChecker);
  }

  /**
   * H264 profile.
   *
   * @param profileIop Could be ProfileIop.BASELINE or ProfileIop.CONSTRAINED
   */
  public void setProfileIop(ProfileIop profileIop) {
    rtmpClient.setProfileIop(profileIop);
  }

  @Override
  public void resizeCache(int newSize) throws RuntimeException {
    rtmpClient.resizeCache(newSize);
  }

  @Override
  public int getCacheSize() {
    return rtmpClient.getCacheSize();
  }

  @Override
  public long getSentAudioFrames() {
    return rtmpClient.getSentAudioFrames();
  }

  @Override
  public long getSentVideoFrames() {
    return rtmpClient.getSentVideoFrames();
  }

  @Override
  public long getDroppedAudioFrames() {
    return rtmpClient.getDroppedAudioFrames();
  }

  @Override
  public long getDroppedVideoFrames() {
    return rtmpClient.getDroppedVideoFrames();
  }

  @Override
  public void resetSentAudioFrames() {
    rtmpClient.resetSentAudioFrames();
  }

  @Override
  public void resetSentVideoFrames() {
    rtmpClient.resetSentVideoFrames();
  }

  @Override
  public void resetDroppedAudioFrames() {
    rtmpClient.resetDroppedAudioFrames();
  }

  @Override
  public void resetDroppedVideoFrames() {
    rtmpClient.resetDroppedVideoFrames();
  }

  @Override
  public void setAuthorization(String user, String password) {
    rtmpClient.setAuthorization(user, password);
  }

  /**
   * Some Livestream hosts use Akamai auth that requires RTMP packets to be sent with increasing
   * timestamp order regardless of packet type.
   * Necessary with Servers like Dacast.
   * More info here:
   * https://learn.akamai.com/en-us/webhelp/media-services-live/media-services-live-encoder-compatibility-testing-and-qualification-guide-v4.0/GUID-F941C88B-9128-4BF4-A81B-C2E5CFD35BBF.html
   */
  public void forceAkamaiTs(boolean enabled) {
    rtmpClient.forceAkamaiTs(enabled);
  }

  @Override
  protected void prepareAudioRtp(boolean isStereo, int sampleRate) {
    rtmpClient.setAudioInfo(sampleRate, isStereo);
  }

  @Override
  protected void startStreamRtp(String url) {
    if (videoEncoder.getRotation() == 90 || videoEncoder.getRotation() == 270) {
      rtmpClient.setVideoResolution(videoEncoder.getHeight(), videoEncoder.getWidth());
    } else {
      rtmpClient.setVideoResolution(videoEncoder.getWidth(), videoEncoder.getHeight());
    }
    rtmpClient.connect(url);
  }

  @Override
  protected void stopStreamRtp() {
    rtmpClient.disconnect();
  }

  @Override
  public void setReTries(int reTries) {
    rtmpClient.setReTries(reTries);
  }

  @Override
  protected boolean shouldRetry(String reason) {
    return rtmpClient.shouldRetry(reason);
  }

  @Override
  public void reConnect(long delay, @Nullable String backupUrl) {
    rtmpClient.reConnect(delay, backupUrl);
  }

  @Override
  public boolean hasCongestion() {
    return rtmpClient.hasCongestion();
  }

  @Override
  protected void getAacDataRtp(ByteBuffer aacBuffer, MediaCodec.BufferInfo info) {
    rtmpClient.sendAudio(aacBuffer, info);
  }

  @Override
  protected void onSpsPpsVpsRtp(ByteBuffer sps, ByteBuffer pps, ByteBuffer vps) {
    rtmpClient.setSPSandPPS(sps, pps, vps);
  }

  @Override
  protected void getH264DataRtp(ByteBuffer h264Buffer, MediaCodec.BufferInfo info) {
    rtmpClient.sendVideo(h264Buffer, info);
  }

  @Override
  public void setLogs(boolean enable) {
    rtmpClient.setLogs(enable);
  }
}

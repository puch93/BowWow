/*
 * Copyright (C) 2012 Jacquet Wong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * musicg api in Google Code: http://code.google.com/p/musicg/
 * Android Application in Google Play: https://play.google.com/store/apps/details?id=com.whistleapp
 *
 */

package kr.core.bowwow.detectApi;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Environment;
import android.util.Log;

import com.musicg.api.WhistleApi;
import com.musicg.wave.WaveHeader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import kr.core.bowwow.app;
import kr.core.bowwow.utils.MyUtil;
import kr.core.bowwow.utils.StringUtil;

public class DetectorThread extends Thread {

    private RecorderThread recorder;
    private WaveHeader waveHeader;
    //	private WhistleApi whistleApi;
    private BarkApi barkApi;
    private volatile Thread _thread;

    private LinkedList<Boolean> whistleResultList = new LinkedList<Boolean>();
    private int numWhistles;
    private int whistleCheckLength = 3;
    private int whistlePassScore = 3;
//	private int whistleCheckLength = 2;
//	private int whistlePassScore = 2;

    private OnSignalsDetectedListener onSignalsDetectedListener;

    private long detectTime = 0;

    BufferedInputStream bis;
    BufferedOutputStream bos;

    File waveFile;
    File tempFile;

    public DetectorThread(RecorderThread recorder) {
        this.recorder = recorder;
        AudioRecord audioRecord = recorder.getAudioRecord();

        int bitsPerSample = 0;
        if (audioRecord.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT) {
            bitsPerSample = 16;
        } else if (audioRecord.getAudioFormat() == AudioFormat.ENCODING_PCM_8BIT) {
            bitsPerSample = 8;
        }

        int channel = 0;
        // whistle detection only supports mono channel
//		if (audioRecord.getChannelConfiguration() == AudioFormat.CHANNEL_CONFIGURATION_MONO){
        if (audioRecord.getChannelConfiguration() == AudioFormat.CHANNEL_IN_MONO) {
            channel = 1;
        }

        waveHeader = new WaveHeader();
        waveHeader.setChannels(channel);
//		waveHeader.setChannels(AudioFormat.CHANNEL_IN_MONO);
        waveHeader.setBitsPerSample(bitsPerSample);
        waveHeader.setSampleRate(audioRecord.getSampleRate());
        Log.d(MyUtil.TAG, "waveHeader: " + waveHeader.toString());
//		whistleApi = new WhistleApi(waveHeader);
        barkApi = new BarkApi(waveHeader);
    }

    private void initBuffer() {
        numWhistles = 0;
        whistleResultList.clear();

        // init the first frames
        for (int i = 0; i < whistleCheckLength; i++) {
            whistleResultList.add(false);
        }
        // end init the first frames
    }

    public void start() {
        _thread = new Thread(this);
        _thread.start();
    }

    public void stopDetection() {
        _thread = null;
    }

    public void run() {
        try {
            byte[] buffer;
            initBuffer();

            Thread thisThread = Thread.currentThread();
            while (_thread == thisThread) {
                // detect sound
                buffer = recorder.getFrameBytes();
//				Log.d(MyUtil.TAG, "DetectorThread");
                // audio analyst
                if (buffer != null) {
                    // sound detected
                    // whistle detection

                    // 짖었는지 여부
                    boolean isBark = barkApi.isBark(buffer);

                    if (!app.isTrans) {
                        if (whistleResultList.getFirst()) {
                            numWhistles--;
                        }

                        whistleResultList.removeFirst();
                        whistleResultList.add(isBark);

                        if (isBark) {
                            numWhistles++;
                            Log.i(StringUtil.TAG_BARK, "numWhistles: " + numWhistles);
                        }
                    }

                    if (numWhistles >= whistlePassScore) {
                        // clear buffer
                        initBuffer();
                        if (detectTime == 0) {
                            detectTime = System.currentTimeMillis();
                        }

                        File dir = new File(Environment.getExternalStorageDirectory() + "/bowwow/");
                        dir.mkdirs();

                        if (waveFile == null) {
                            waveFile = new File(Environment.getExternalStorageDirectory() + "/bowwow/upFile.wav");
                        }
                        if (tempFile == null) {
                            tempFile = new File(Environment.getExternalStorageDirectory() + "/bowwow/test_temp.bak");
                        }
                    }


                    if (detectTime > 0) {
                        Log.i(StringUtil.TAG_BARK, "detectTime > 0");
                        try {
                            if (bos == null) {
                                bos = new BufferedOutputStream(new FileOutputStream(tempFile));
                            }
                            bos.write(buffer);
                            bos.flush();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    long time = System.currentTimeMillis();
                    if (detectTime != 0) {
                        Log.i(StringUtil.TAG_BARK, "time - detectTime: " + (time - detectTime));
                        if (time - detectTime >= 2000) {
                            Log.i(StringUtil.TAG_BARK, "time - detectTime >= 2000");
                            bos.close();

                            int read = 0;
                            byte[] bu = new byte[buffer.length];

                            bis = new BufferedInputStream(new FileInputStream(tempFile));
                            bos = new BufferedOutputStream(new FileOutputStream(waveFile));

                            bos.write(getFileHeader((int) tempFile.length()));
                            while ((read = bis.read(bu)) != -1) {
                                bos.write(bu);
                                bos.flush();
                            }
                            bis.close();
                            bos.close();

                            onBarkDetected(waveFile.getAbsolutePath());

                            detectTime = 0;
                            tempFile = null;
                            waveFile = null;
                            bos = null;
                        }
                    }
                    // end whistle detection
                } else {
                    // no sound detected
                    if (whistleResultList.getFirst()) {
                        numWhistles--;
                    }
                    whistleResultList.removeFirst();
                    whistleResultList.add(false);
                }
                // end audio analyst
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onBarkDetected(String filePath) {
        if (onSignalsDetectedListener != null) {
            onSignalsDetectedListener.onBarkDetected(filePath);
        }
    }
//	private void onBarkDetected(){
//		if (onSignalsDetectedListener != null){
//			onSignalsDetectedListener.onBarkDetected();
//		}
//	}

    public void setOnSignalsDetectedListener(OnSignalsDetectedListener listener) {
        onSignalsDetectedListener = listener;
    }


    private byte[] getFileHeader(int mAudioLen) {
        byte[] header = new byte[0x2c];
        int totalDataLen = mAudioLen + 40;
        long byteRate = 16 * 0xac44 * 1 / 8;
        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = (byte) 1;  // format = 1 (PCM방식)
        header[21] = 0;
        header[22] = 1;
        header[23] = 0;
        header[24] = (byte) (0xac44 & 0xff);
        header[25] = (byte) ((0xac44 >> 8) & 0xff);
        header[26] = (byte) ((0xac44 >> 16) & 0xff);
        header[27] = (byte) ((0xac44 >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) 16 * 1 / 8;  // block align
        header[33] = 0;
        header[34] = 16;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (mAudioLen & 0xff);
        header[41] = (byte) ((mAudioLen >> 8) & 0xff);
        header[42] = (byte) ((mAudioLen >> 16) & 0xff);
        header[43] = (byte) ((mAudioLen >> 24) & 0xff);
        return header;
    }
}
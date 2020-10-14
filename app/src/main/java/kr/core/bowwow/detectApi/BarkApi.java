package kr.core.bowwow.detectApi;

import com.musicg.api.DetectionApi;
import com.musicg.wave.WaveHeader;

import kr.core.bowwow.utils.DetectionApiTest;

public class BarkApi extends DetectionApiTest {

    public BarkApi(WaveHeader waveHeader) {
        super(waveHeader);
    }

    protected void init() {

        /* default */
//        this.minFrequency = 1000.0f;
//        this.maxFrequency = 3200.0f;
//
//        this.minIntensity = 5000.0f;
//        this.maxIntensity = 35000.0f;
//
//        this.minStandardDeviation = 0.0f;
//        this.maxStandardDeviation = 0.1f;
//        this.highPass = 100;
//        this.lowPass = 10000;
//        this.minNumZeroCross = 55;
//        this.maxNumZeroCross = 100;
//        this.numRobust = 4;

        /* 수정 1 */
        this.minFrequency = 0.0f;
        this.maxFrequency = Double.MAX_VALUE;

        this.minIntensity = 3000.0f; //
        this.maxIntensity = 100000.0f;

        this.minStandardDeviation = 0.0f;
        this.maxStandardDeviation = 1.0f;
        this.highPass = 5;
        this.lowPass = 10000;
        this.minNumZeroCross = 0;
        this.maxNumZeroCross = 100;
        this.numRobust = 1;
    }

    public boolean isBark(byte[] audioBytes) {
        return this.isSpecificSound(audioBytes);
    }
}

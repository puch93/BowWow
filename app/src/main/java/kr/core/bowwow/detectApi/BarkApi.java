package kr.core.bowwow.detectApi;

import com.musicg.api.DetectionApi;
import com.musicg.wave.WaveHeader;

public class BarkApi extends DetectionApi {

    public BarkApi(WaveHeader waveHeader) {
        super(waveHeader);
    }

    protected void init() {

        this.minFrequency = 1000.0f;
        this.maxFrequency = 3200.0f;
        this.minIntensity = 5000.0f;
        this.maxIntensity = 35000.0f;
        this.minStandardDeviation = 0.0f;
        this.maxStandardDeviation = 0.1f;
        this.highPass = 100;
        this.lowPass = 10000;
        this.minNumZeroCross = 55;
        this.maxNumZeroCross = 100;
        this.numRobust = 4;

    }

    public boolean isBark(byte[] audioBytes) {
        return this.isSpecificSound(audioBytes);
    }
}

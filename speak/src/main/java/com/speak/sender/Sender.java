package com.speak.sender;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.speak.utils.BitManipulationHelper;
import com.speak.utils.Configuration;

import java.util.ArrayList;

public class Sender {

    ToneGeneratorThread toneGeneratorThread;

    ArrayList<Byte> getEncodedBits(String input, Configuration configuration) {

        BitManipulationHelper bitManipulationHelper = new BitManipulationHelper(configuration);
        ArrayList<String> inputBits = bitManipulationHelper.convertAsciiToBinary(input);
        ArrayList<String> pseudoRandomSequence = bitManipulationHelper.generatePseudoRandomSequence();
        ArrayList<Byte> interpolatedDataBits = bitManipulationHelper.interpolateBits(inputBits,configuration.getSamplesPerDataBit());
        ArrayList<Byte> interpolatedCodeBits = bitManipulationHelper.interpolateBits(pseudoRandomSequence,configuration.getSamplesPerCodeBit());
        return bitManipulationHelper.encodeBits(interpolatedCodeBits, interpolatedDataBits,pseudoRandomSequence);
    }

    public void sendData(final String input, final Configuration configuration, final SenderCallBack senderCallBack) {
        ArrayList<Byte> encodedBits = getEncodedBits(input, configuration);
        Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                senderCallBack.onSendComplete();
                Log.d("test", "handler message executed");
                return false;
            }
        });
        stopExistingThread();
        toneGeneratorThread = new ToneGeneratorThread(encodedBits, configuration, handler);
        toneGeneratorThread.start();
    }

    public void sendData(final String input, final Configuration configuration) {
        ArrayList<Byte> encodedBits = getEncodedBits(input, configuration);
        stopExistingThread();
        toneGeneratorThread = new ToneGeneratorThread(encodedBits, configuration);
        toneGeneratorThread.start();
    }

    public void stopExistingThread() {
        if (toneGeneratorThread != null && !toneGeneratorThread.isInterrupted()) {
            toneGeneratorThread.abort();
        }
    }

    public interface SenderCallBack {
        void onSendComplete();
    }

}

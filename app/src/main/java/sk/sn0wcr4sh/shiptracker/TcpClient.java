package sk.sn0wcr4sh.shiptracker;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by sn0wcr4sh on 9/18/2017.
 *
 */

public class TcpClient {

    interface ActionListener {
        void onConnectionFailed(Exception e);
        void onConnected();
        void onDataReceived(String data);
        void onDisconnected(Exception e);
    }

    interface ConnectListener {
        void onConnected(Socket socket);
        void onConnectionFailed(Exception e);
    }

    interface ReadListener {
        void onDataRead(String data);
        void onDisconnected(Exception e);
    }

    private final int CONNECT_TIMEOUT = 5000;
    private final int READING_DELAY = 1000;

    private ActionListener mListener;

    private Socket mSocket;
    private Thread mReadingThread;

    public TcpClient(ActionListener listener) {
        mListener = listener;
    }

    public void connect(String hostAddress, int port) {
        Thread connectThread = new Thread(
                new ConnectRunnable(
                        hostAddress,
                        port,
                        new ConnectListener() {
                            @Override
                            public void onConnected(Socket socket) {
                                mListener.onConnected();
                                mSocket = socket;
                                mReadingThread = new Thread(new ReadingRunnable(
                                        socket,
                                        new ReadListener() {
                                            @Override
                                            public void onDataRead(String data) {
                                                mListener.onDataReceived(data);
                                            }

                                            @Override
                                            public void onDisconnected(Exception e) {
                                                mListener.onDisconnected(e);
                                            }
                                        }
                                ));
                                mReadingThread.start();
                            }

                            @Override
                            public void onConnectionFailed(Exception e) {
                                mListener.onConnectionFailed(e);
                            }
                        }));
        connectThread.start();
    }

    public void stop() {
        if (mReadingThread != null) {
            mReadingThread.interrupt();
        }

        try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ConnectRunnable implements Runnable {

        private String mHostAddress;
        private int mPort;
        private ConnectListener mListener;

        ConnectRunnable(String hostAddress, int port, ConnectListener listener) {
            mHostAddress = hostAddress;
            mPort = port;
            mListener = listener;
        }

        @Override
        public void run() {
            try {
                Log.d(Constant.TAG, "Connecting to " + mHostAddress + " : " + mPort);

                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(mHostAddress, mPort), CONNECT_TIMEOUT);

                mListener.onConnected(socket);
            }
            catch (Exception e) {
                e.printStackTrace();
                mListener.onConnectionFailed(e);
            }
        }
    }

    private class ReadingRunnable implements Runnable {

        private Socket mSocket;
        private ReadListener mListener;

        private BufferedReader mReader;
        boolean mDataReceived;

        ReadingRunnable(Socket socket, ReadListener listener) {
            mSocket = socket;
            mListener = listener;
        }

        @Override
        public void run() {

            final int BUFFER_SIZE = 1024;
            char[] buffer = new char[BUFFER_SIZE];

            try {
                mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
                mListener.onDisconnected(e);
                return;
            }

            while (!Thread.currentThread().isInterrupted() && isConnected()) {
                try {
                    mDataReceived = false;

                    int read = mReader.read(buffer, 0, BUFFER_SIZE);
                    if (read > 0) {
                        mListener.onDataRead(new String(buffer, 0, read));
                    }

                    if (!mDataReceived)
                        Thread.sleep(READING_DELAY);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    mListener.onDisconnected(e);
                    break;
                }
            }
        }

        private boolean isConnected() {
            return mSocket != null && mSocket.isConnected() && !mSocket.isClosed();
        }
    }
}

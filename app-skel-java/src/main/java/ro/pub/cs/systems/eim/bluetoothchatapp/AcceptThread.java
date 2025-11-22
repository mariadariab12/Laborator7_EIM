package ro.pub.cs.systems.eim.bluetoothchatapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.UUID;

public class AcceptThread extends Thread {
    private BluetoothServerSocket serverSocket;
    private final MainActivity mainActivity;

    private ListView chatListView;
    private EditText messageEditText;
    private Button sendButton;
    private Button listDevicesButton;
    private static final String TAG = "AcceptThread";

    //Serverul care asteapta conexiuni
    public AcceptThread(MainActivity activity, BluetoothAdapter adapter, UUID uuid) {
        this.mainActivity = activity;

        BluetoothServerSocket tempSocket = null;
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("AcceptThread", "BLUETOOTH_CONNECT permission not granted.");
                    return;
                }
            }
            tempSocket = adapter.listenUsingRfcommWithServiceRecord("BluetoothChatApp", uuid);
        } catch (IOException e) {
            Log.e("AcceptThread", "Error creating server socket: " + e.getMessage());
        }
        this.serverSocket = tempSocket;
    }


    @Override
    public void run() {
        if (serverSocket == null) {
            Log.d(TAG, "ServerSocket is null, cannot accept connections.");
            return;
        }

        BluetoothSocket socket;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // Așteaptă o conexiune
                socket = serverSocket.accept();
                if (socket != null) {
                    Log.d(TAG, "Connection accepted. Managing socket...");

                    // Predă conexiunea aplicației
                    mainActivity.manageConnectedSocket(socket);
                    closeServerSocket();
                    break;
                }
            } catch (IOException e) {
                Log.e(TAG, "Error while accepting connection: " + e.getMessage());
                break;
            }
        }
        Log.d(TAG, "AcceptThread exiting.");
    }




    public void cancel() {
        Log.d(TAG, "Canceling AcceptThread...");
        closeServerSocket();
    }

    private void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
                Log.d(TAG, "ServerSocket closed successfully.");
            }
        } catch (IOException e) {
            Log.e(TAG, "Error closing ServerSocket: " + e.getMessage());
        }
    }


}
